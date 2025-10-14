#include "mqttmanager.h"
#include <QDebug>
#include <QJsonDocument>
#include <QJsonObject>

// 🚗 생성자
MqttManager::MqttManager(QObject *parent) : QObject(parent) {
    // MQTT 클라이언트 객체 생성 (this에 소속 → 자동 메모리 관리됨)
    m_client = new QMqttClient(this);

    // MQTT 브로커 주소와 포트 설정
    m_client->setHostname("10.210.98.208");
    m_client->setPort(1883);


    // 🔔 에러 발생 시 로그 출력
    connect(m_client, &QMqttClient::errorChanged, this,
    [this](QMqttClient::ClientError error) {
        qWarning() << "[MQTT Error]" << error
                   << "host=" << m_client->hostname()
                   << "port=" << m_client->port();
    });


    // 🔔 상태(state) 변경 시 처리 (Disconnected, Connecting, Connected 등)
    connect(m_client, &QMqttClient::stateChanged, this,
        [this](QMqttClient::ClientState state) {
            qDebug() << "[MQTT State]" << state;

            // 브로커 연결 완료되면 car/hud 토픽 구독
            if (state == QMqttClient::Connected) {
                auto sub = m_client->subscribe(QMqttTopicFilter("car/hud"));
                if (!sub)
                    qWarning() << "[MQTT] Subscribe failed!";
                else
                    qDebug() << "[MQTT] Subscribed to car/hud";
            }
        });

    // 🔔 MQTT 메시지 수신 시 onMessageReceived 슬롯 실행
    // (Qt6은 두 번째 파라미터가 QMqttTopicName)
    connect(m_client, &QMqttClient::messageReceived,
            this, &MqttManager::onMessageReceived);
}
//  * @brief 소멸자
//  * 앱 종료 시 브로커와 연결 해제
//  */
MqttManager::~MqttManager() {
    if (m_client) {
        m_client->disconnectFromHost();
    }
}

// 🚗 브로커 연결 시도
void MqttManager::connectToBroker() {
    qDebug() << "[MQTT] Connecting to broker..."
             << "host=" << m_client->hostname()
             << "port=" << m_client->port();
    m_client->connectToHost();
}

// 🚗 MQTT 메시지 수신 처리
// (JSON 형태의 payload를 파싱해서 멤버 변수 업데이트 후 시그널 발생)
void MqttManager::onMessageReceived(const QByteArray &message,
                                    const QMqttTopicName &topic) {
    qDebug() << "[MQTT Received]" << topic.name() << message;

    // JSON 파싱
    QJsonParseError parseError;
    QJsonDocument doc = QJsonDocument::fromJson(message, &parseError);

    if (parseError.error != QJsonParseError::NoError || !doc.isObject()) {
        qWarning() << "[MQTT Parse Error]" << parseError.errorString();
        return;
    }

    QJsonObject obj = doc.object();

    // ETA (문자열 or 숫자 모두 대응)
    if (obj.contains("eta")) {
        QJsonValue v = obj.value("eta");
        QString newEta;
        if (v.isString()) {
            newEta = v.toString();
        } else if (v.isDouble()) {
            int sec = int(v.toDouble());
            newEta = QString("%1m %2s").arg(sec/60).arg(sec%60);
        }
        if (m_eta != newEta) {
            m_eta = newEta;
            emit etaChanged();
        }
    }

    // 총 차선 개수 업데이트
    if (obj.contains("lanes")) {   // 🔹 Python에서 보내는 그대로 유지
        int newLanes = obj["lanes"].toInt();
        if (m_totalLanes != newLanes) {
            m_totalLanes = newLanes;
            emit totalLanesChanged();
        }
    }

    // 현재 차선 위치 업데이트
    if (obj.contains("currentLane")) {
        int newLane = obj["currentLane"].toInt();
        if (m_currentLane != newLane) {
            m_currentLane = newLane;
            emit currentLaneChanged();
        }
    }

    // 회피 방향(왼/오른쪽) 업데이트
    if (obj.contains("avoidDir")) {
        int newAvoid = obj["avoidDir"].toInt();
        if (m_avoidDir != newAvoid) {
            m_avoidDir = newAvoid;
            emit avoidDirChanged();
        }
    }

    // 구급차 위치 차선 업데이트
    if (obj.contains("ambulanceLane")) {
        int newAmb = obj["ambulanceLane"].toInt();
        if (m_ambulanceLane != newAmb) {
            m_ambulanceLane = newAmb;
            emit ambulanceLaneChanged();
        }
    }

    // 현재 상태(state: idle, samePath, nearby 등) 업데이트
    if (obj.contains("state")) {
        QString newState = obj["state"].toString();
        if (m_state != newState) {
            m_state = newState;
            emit stateChanged();
        }
    }
}
