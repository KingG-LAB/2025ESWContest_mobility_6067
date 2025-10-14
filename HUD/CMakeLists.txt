cmake_minimum_required(VERSION 3.16)

project(HUD_EX VERSION 0.1 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)
set(CMAKE_AUTOUIC ON)
# ✅ Qt6 모듈 찾기 (MQTT 포함)
find_package(Qt6 REQUIRED COMPONENTS Core Gui Qml Quick Mqtt)

# ✅ 실행 파일 생성
qt_add_executable(appHUD
    main.cpp
    mqttmanager.cpp
    mqttmanager.h
)

# ✅ QML 모듈 + 리소스 등록
qt_add_qml_module(appHUD
    URI HUD_EX
    VERSION 1.0
    QML_FILES
        Main.qml
    RESOURCES
        HUD_EX.qrc
)

# ✅ 링크할 라이브러리
target_link_libraries(appHUD
    PRIVATE Qt6::Core Qt6::Gui Qt6::Qml Qt6::Quick Qt6::Mqtt
)

# ✅ 설치 규칙
include(GNUInstallDirs)
install(TARGETS appHUD
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
)
