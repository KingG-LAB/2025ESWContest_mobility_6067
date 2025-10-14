#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <QUrl>
#include <QTimer>
#include "mqttmanager.h"   // ?? MQTT 매니저 클래스 포함
#include <QLoggingCategory>

int main(int argc, char *argv[])
{
    // ? MQTT 로그만 활성화
    QLoggingCategory::setFilterRules(
        "qt.mqtt.debug=true\n"
        "qt.mqtt.connection.verbose=true\n"
        "qt.scenegraph.general=true\n"
        "qt.scenegraph.renderloop=true\n"
        "qt.quick.painting=true\n"
    );
    // Qt GUI 애플리케이션 실행 환경 준비
    QGuiApplication app(argc, argv);

    // QML 엔진 생성 (QML 파일 로딩 담당)   
    QQmlApplicationEngine engine;

    // 시작 로그
    qDebug() << "?? HUD Application starting...";

    // ?? MQTT 매니저 객체 생성
    MqttManager mqtt;

    // 이벤트 루프 시작 직후 브로커 연결 시도
    QTimer::singleShot(0, &mqtt, &MqttManager::connectToBroker);

    // QML 쪽에서 "mqtt"라는 이름으로 C++ 객체 접근 가능하게 등록
    // → QML에서 mqtt.eta, mqtt.state 등으로 바로 접근 가능
    engine.rootContext()->setContextProperty("mqtt", &mqtt);

    // QML 파일 로드 (절대 경로 지정)
    //    ?? 현재는 /home/rbhud/HUD/Main.qml 사용
    const QUrl url = QUrl::fromLocalFile("/home/rbhud/HUD/Main.qml");
    // const QUrl url("qrc:/HUD_EX/Main.qml");  // 리소스 파일로 사용할 수도 있음

    // QML 객체 생성 실패 시 앱 종료 처리
    QObject::connect(
        &engine, &QQmlApplicationEngine::objectCreated,
        &app, [url](QObject *obj, const QUrl &objUrl) {
            if (!obj && url == objUrl)
                QCoreApplication::exit(-1);
        }, Qt::QueuedConnection);

    // QML 로드 실행
    engine.load(url);

    // 이벤트 루프 시작 (앱 실행)
    return app.exec();
}
