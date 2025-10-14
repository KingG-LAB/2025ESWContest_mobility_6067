#ifndef MQTTMANAGER_H
#define MQTTMANAGER_H

#include <QObject>
#include <QtMqtt/QMqttClient>
#include <QtMqtt/QMqttTopicFilter>
#include <QtMqtt/QMqttSubscription>
#include <QtMqtt/QMqttTopicName>

// 🚗 HUD 애플리케이션에서 MQTT 메시지를 받아
// QML과 데이터를 공유하기 위한 매니저 클래스
class MqttManager : public QObject {
    Q_OBJECT

    // 🔹 QML에서 접근 가능한 속성(Property) 정의
    //    - READ: 게터 함수
    //    - NOTIFY: 값 변경 시 QML에 알림
    Q_PROPERTY(QString eta READ eta NOTIFY etaChanged)                // ETA (도착 예상 시간)
    Q_PROPERTY(int totalLanes READ totalLanes NOTIFY totalLanesChanged) // 전체 차선 수
    Q_PROPERTY(int currentLane READ currentLane NOTIFY currentLaneChanged) // 현재 차량 차선
    Q_PROPERTY(int avoidDir READ avoidDir NOTIFY avoidDirChanged)     // 회피 방향 (0:없음, 1:오른쪽, 2:왼쪽)
    Q_PROPERTY(int ambulanceLane READ ambulanceLane NOTIFY ambulanceLaneChanged) // 구급차가 달리는 차선
    Q_PROPERTY(QString state READ state NOTIFY stateChanged)          // 현재 상태 (idle / samePath / nearby 등)

public:
    explicit MqttManager(QObject *parent = nullptr);
    ~MqttManager();  // ✅ 소멸자 추가

    // 🔹 QML에서 읽을 수 있는 getter 함수들
    QString eta() const { return m_eta; }
    int totalLanes() const { return m_totalLanes; }
    int currentLane() const { return m_currentLane; }
    int avoidDir() const { return m_avoidDir; }
    int ambulanceLane() const { return m_ambulanceLane; }
    QString state() const { return m_state; }

    // MQTT 브로커에 연결 시도
    void connectToBroker();

signals:
    // 🔔 값이 변경될 때 QML에 알림을 주는 시그널들
    void etaChanged();
    void totalLanesChanged();
    void currentLaneChanged();
    void avoidDirChanged();
    void ambulanceLaneChanged();
    void stateChanged();

private slots:
    // 🔔 MQTT 메시지 수신 시 호출되는 슬롯
    //     - payload(message)를 JSON 파싱
    //     - QML 바인딩된 속성 업데이트
    void onMessageReceived(const QByteArray &message,
                           const QMqttTopicName &topic);   // ✅ Qt6 시그니처

private:
    // 내부 멤버 변수들
    QMqttClient *m_client = nullptr;  // ✅ 반드시 nullptr 초기화
    QString m_eta;           // ETA 문자열
    int m_totalLanes = 3;    // 전체 차선 개수 (기본값 3)
    int m_currentLane = 2;   // 현재 차량 차선 (기본값 2, 중앙)
    int m_avoidDir = 0;      // 회피 방향 (0=없음, 1=우측, 2=좌측)
    int m_ambulanceLane = 2; // 구급차 차선 위치
    QString m_state = "idle";// 상태 문자열 (기본값 idle)
};

#endif // MQTTMANAGER_H
