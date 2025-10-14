#include "mqttmanager.h"
#include <QDebug>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonValue>
#include <QPointer>
#include <QElapsedTimer>

MqttManager::MqttManager(QObject *parent) : QObject(parent) {
    m_client = new QMqttClient(this);
    m_lastUpdate.start();   // ✅ 타이머 시작

    // 안전한 기본 설정
    m_client->setHostname(QStringLiteral("10.210.97.35"));
    m_client->setPort(1883);

    // 에러 로깅
    connect(m_client, &QMqttClient::errorChanged, this,
            [this](QMqttClient::ClientError error) {
        qWarning() << "[MQTT Error]" << error
                   << "host=" << m_client->hostname()
                   << "port=" << m_client->port();
    });

    // 상태 변경
    connect(m_client, &QMqttClient::stateChanged, this,
            [this](QMqttClient::ClientState state) {
        qDebug() << "[MQTT State]" << state;
        if (!m_client) return;

        if (state == QMqttClient::Connected) {
            // 이미 구독돼 있으면 또 구독하지 않도록 주의
            static bool subscribed = false;
            if (!subscribed) {
                auto sub = m_client->subscribe(QMqttTopicFilter(QStringLiteral("car/hud")));
                if (!sub) {
                    qWarning() << "[MQTT] Subscribe failed!";
                } else {
                    subscribed = true;
                    qDebug() << "[MQTT] Subscribed to car/hud";
                }
            }
        }
    });

    // 메시지 수신 (QueuedConnection으로 안전하게)
    connect(m_client, &QMqttClient::messageReceived,
            this, &MqttManager::onMessageReceived,
            Qt::QueuedConnection);
}

MqttManager::~MqttManager() {
    if (m_client) {
        // 안전한 정리: 먼저 구독 해제 시도(브로커가 죽어도 무시)
        m_client->unsubscribe(QStringLiteral("car/hud"));
        m_client->disconnectFromHost();
        // QObject parent가 delete 처리
    }
}

void MqttManager::connectToBroker() {
    if (!m_client) {
        qWarning() << "[MQTT] Client is null";
        return;
    }
    if (m_client->state() == QMqttClient::Connected ||
        m_client->state() == QMqttClient::Connecting) {
        qDebug() << "[MQTT] Already connected/connecting";
        return;
    }
    qDebug() << "[MQTT] Connecting to broker..."
             << "host=" << m_client->hostname()
             << "port=" << m_client->port();
    m_client->connectToHost();
}

void MqttManager::onMessageReceived(const QByteArray &message,
                                    const QMqttTopicName &topic) {
    // 토픽 필터(오동작 방지)
    if (topic.name() != QLatin1String("car/hud")) {
        qWarning() << "[MQTT] Ignored topic:" << topic.name();
        return;
    }

    if (m_lastUpdate.isValid() && m_lastUpdate.elapsed() < 500) {
    return;
    }
    m_lastUpdate.restart();

    // 크기 과도한 페이로드 방지(1MB 상한)
    if (message.size() > 1024 * 1024) {
        qWarning() << "[MQTT] Oversized payload:" << message.size();
        return;
    }

    QJsonParseError parseError;
    const QJsonDocument doc = QJsonDocument::fromJson(message, &parseError);
    if (parseError.error != QJsonParseError::NoError || !doc.isObject()) {
        qWarning() << "[MQTT Parse Error]" << parseError.errorString();
        return;
    }

    const QJsonObject obj = doc.object();

    // lanes
    if (obj.contains("lanes")) {
        const QJsonValue v = obj.value("lanes");
        if (v.isDouble()) {
            int nl = clampInt(v.toInt(3), kMinLanes, kMaxLanes);
            if (m_totalLanes != nl) {
                m_totalLanes = nl;
                emit totalLanesChanged();
                // totalLanes 바뀌면 current/ambulanceLane도 범위 재조정
                int clampedCL = clampInt(m_currentLane, 1, m_totalLanes);
                if (clampedCL != m_currentLane) {
                    m_currentLane = clampedCL;
                    emit currentLaneChanged();
                }
                int clampedAL = clampInt(m_ambulanceLane, 1, m_totalLanes);
                if (clampedAL != m_ambulanceLane) {
                    m_ambulanceLane = clampedAL;
                    emit ambulanceLaneChanged();
                }
            }
        }
    }

    // currentLane
    if (obj.contains("currentLane")) {
        const QJsonValue v = obj.value("currentLane");
        if (v.isDouble()) {
            int nl = clampInt(v.toInt(m_currentLane), 1, m_totalLanes);
            if (m_currentLane != nl) {
                m_currentLane = nl;
                emit currentLaneChanged();
            }
        }
    }

    // avoidDir (0/1/2 외 값 방지)
    if (obj.contains("avoidDir")) {
        const QJsonValue v = obj.value("avoidDir");
        if (v.isDouble()) {
            int na = clampInt(v.toInt(m_avoidDir), 0, 2);
            if (m_avoidDir != na) {
                m_avoidDir = na;
                emit avoidDirChanged();
            }
        }
    }

    // ambulanceLane
    if (obj.contains("ambulanceLane")) {
        const QJsonValue v = obj.value("ambulanceLane");
        if (v.isDouble()) {
            int na = clampInt(v.toInt(m_ambulanceLane), 1, m_totalLanes);
            if (m_ambulanceLane != na) {
                m_ambulanceLane = na;
                emit ambulanceLaneChanged();
            }
        }
    }

    // state (문자열만 허용)
    if (obj.contains("state")) {
        const QJsonValue v = obj.value("state");
        if (v.isString()) {
            const QString ns = v.toString();
            // 허용 목록(선택): idle / samePath / nearby
            if (ns == QLatin1String("idle") ||
                ns == QLatin1String("samePath") ||
                ns == QLatin1String("nearby")) {
                if (m_state != ns) {
                    m_state = ns;
                    emit stateChanged();
                }
            } else {
                qWarning() << "[MQTT] Unknown state:" << ns;
            }
        }
    }

    // ETA (string or number)
    if (obj.contains("eta")) {
        const QJsonValue v = obj.value("eta");
        QString newEta;
        if (v.isString()) {
            newEta = v.toString();
        } else if (v.isDouble()) {
            int sec = qMax(0, v.toInt(0));
            newEta = QStringLiteral("%1m %2s").arg(sec/60).arg(sec%60);
        }

        if (m_eta != newEta) {
            m_eta = newEta;
            emit etaChanged();
        }
    }
}
