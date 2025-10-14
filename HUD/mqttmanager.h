#ifndef MQTTMANAGER_H
#define MQTTMANAGER_H

#include <QObject>
#include <QtMqtt/QMqttClient>
#include <QtMqtt/QMqttTopicFilter>
#include <QtMqtt/QMqttSubscription>
#include <QtMqtt/QMqttTopicName>
#include <QElapsedTimer>

class MqttManager : public QObject {
    Q_OBJECT
    Q_DISABLE_COPY(MqttManager)

    Q_PROPERTY(QString eta READ eta NOTIFY etaChanged)
    Q_PROPERTY(int totalLanes READ totalLanes NOTIFY totalLanesChanged)
    Q_PROPERTY(int currentLane READ currentLane NOTIFY currentLaneChanged)
    Q_PROPERTY(int avoidDir READ avoidDir NOTIFY avoidDirChanged)
    Q_PROPERTY(int ambulanceLane READ ambulanceLane NOTIFY ambulanceLaneChanged)
    Q_PROPERTY(QString state READ state NOTIFY stateChanged)

public:
    explicit MqttManager(QObject *parent = nullptr);
    ~MqttManager() override;

    QString eta() const { return m_eta; }
    int totalLanes() const { return m_totalLanes; }
    int currentLane() const { return m_currentLane; }
    int avoidDir() const { return m_avoidDir; }
    int ambulanceLane() const { return m_ambulanceLane; }
    QString state() const { return m_state; }

    Q_INVOKABLE void connectToBroker();

signals:
    void etaChanged();
    void totalLanesChanged();
    void currentLaneChanged();
    void avoidDirChanged();
    void ambulanceLaneChanged();
    void stateChanged();

private slots:
    void onMessageReceived(const QByteArray &message,
                           const QMqttTopicName &topic);

private:
    // 내부 유틸: 안전 클램프/형변환
    static int clampInt(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }

    QMqttClient *m_client = nullptr;

    QString m_eta;
    int m_totalLanes = 3;
    int m_currentLane = 2;
    int m_avoidDir = 0;
    int m_ambulanceLane = 2;
    QString m_state = QStringLiteral("idle");

    // 상한선들(비정상 payload 방지)
    static constexpr int kMaxLanes = 20;
    static constexpr int kMinLanes = 1;

private:
    QElapsedTimer m_lastUpdate;   // ✅ 마지막 업데이트 시각 기록
};

#endif // MQTTMANAGER_H
