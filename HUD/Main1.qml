import QtQuick
import QtQuick.Controls
import QtQuick.Window

ApplicationWindow {
    id: window
    width: 800
    height: 480
    visible: true
    color: "black"
    title: qsTr("HUD with Dynamic Lanes & Animated Arrow + Center Red Lane Effect")

    // 전역 보정값
    property int outerMargin: 150 // 창 양쪽 여백 (요청대로 10px)
    property int laneLineWidth: 3
    property int carIconHeight: 160

    // ✅ MQTT에서 넘어오는 ETA를 직접 쓰지 않고, 안전 버퍼에 저장해서 UI가 참조
    property string etaText: ""

    // ------------------------
    // roadArea: 실제 차선이 그려지는 영역 (양쪽 10px 여백)
    // 모든 차선 좌표는 이 영역 기준(width/lLanes)
    // ------------------------
    Item {
        id: roadArea
        anchors.top: parent.top
        anchors.bottom: parent.bottom
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.leftMargin: outerMargin
        anchors.rightMargin: outerMargin

        // 도로 기준 간격: roadArea.width / lLanes
        // (totalLanes는 mqtt.totalLanes를 직접 참조)
        property real laneSpacing: (mqtt && mqtt.totalLanes > 0)
                                   ? width / mqtt.totalLanes
                                   : width / 3


        // -------------------------------
        // 차선 라인 (왼쪽 실선, 중간 점선들, 오른쪽 실선)
        // Repeater 모델: totalLanes + 1 (라인 수 = 차선수 + 1)
        // -------------------------------
        Repeater {
            id: laneRepeater
            model: (mqtt && mqtt.totalLanes) ? mqtt.totalLanes + 1 : 4
            Item {
                id: lineSlot
                property int lw: laneLineWidth
                width: lw
                height: parent.height
                y: 0

                // x 계산: 마지막(오른쪽 경계)은 roadArea.width - lineWidth 해서 화면 밖으로 나가지 않게 보정
                x: (mqtt && mqtt.totalLanes > 0 && index === mqtt.totalLanes)
                      ? (roadArea.width - lw)
                      : Math.round(index * roadArea.laneSpacing)


                // 실선 (왼쪽 또는 오른쪽)
                Rectangle {
                    anchors.fill: parent
                    color: "white"
                    visible: (index === 0 || index === mqtt.totalLanes)
                }

                // 중간은 점선(시각화용)
                Repeater {
                    model: (mqtt && mqtt.totalLanes > 0 && index > 0 && index < mqtt.totalLanes) ? 10 : 0
                    Rectangle {
                        width: lineSlot.lw
                        height: 30
                        color: "white"
                        opacity: 0.9
                        x: -(lineSlot.lw / 2) // 중앙 정렬 보정
                        y: index * 60 + (index * 0) // 간격 고정 (원래 코드 스타일 유지)
                    }
                }
            }
        }

        // -------------------------------
        // 빨간 점선 (구급차 비상차로 안내선) - roadArea 기준
        // 표시: mqtt.state === "samePath"
        // -------------------------------
        Canvas {
            id: redLaneEffect
            anchors.fill: parent
            z: 5
            property real progress: 0.0
            visible: mqtt && mqtt.state === "samePath"

            onPaint: {
                var ctx = getContext("2d")
                ctx.clearRect(0, 0, width, height)
                
                if (!mqtt || mqtt.state !== "samePath")
                    return
                if (!mqtt || mqtt.totalLanes <= 0 || !mqtt.ambulanceLane)
                    return
                
                if (!mqtt || typeof mqtt.ambulanceLane !== "number" || mqtt.ambulanceLane <= 0)
                    return
                // 구급차 차선 기준
                var t = Number(mqtt.totalLanes)
                var a = Number(mqtt.ambulanceLane)
                // totalLanes / ambulanceLane 숫자 가드

                a = Math.max(1, Math.min(t, Math.round(a)))
                var laneX = (a - 0.5) * roadArea.laneSpacing
                var fromY = height + 100
                var toY = height - 580

                // 스타일
                ctx.lineWidth = 15
                ctx.strokeStyle = "#FF0000"
                ctx.lineCap = "butt"

                // 세그먼트 간격
                var segmentLength = 50
                var gap = 60
                var offset = progress * (segmentLength + gap)

                var arrowSize = 30 // 화살표 머리 크기

                for (var y = fromY - offset; y > toY; y -= (segmentLength + gap)) {
                    var y1 = Math.max(y - segmentLength, toY)

                    // 🔴 세로 막대 (segment)
                    ctx.beginPath()
                    ctx.moveTo(laneX, y)
                    ctx.lineTo(laneX, y1)
                    ctx.stroke()

                    // 🔴 세그먼트 끝에 화살표 머리 붙이기
                    ctx.beginPath()
                    ctx.fillStyle = "#FF0000"
                    ctx.moveTo(laneX, y1 - arrowSize) // 위 꼭짓점
                    ctx.lineTo(laneX - arrowSize / 2, y1) // 왼쪽 아래
                    ctx.lineTo(laneX + arrowSize / 2, y1) // 오른쪽 아래
                    ctx.closePath()
                    ctx.fill()
                }
            }

            Timer {
                interval: 50
                running: redLaneEffect.visible
                repeat: true
                onTriggered: {
                    redLaneEffect.progress += 0.05
                    if (redLaneEffect.progress > 1.0)
                        redLaneEffect.progress = 0.0
                    redLaneEffect.requestPaint()
                }
            }
        }

        // -------------------------------
        // 차량 (아이콘) — roadArea 기준으로 위치
        // -------------------------------
        Image {
            id: car
            //source: "file:///C:/Users/seokhwan/Desktop/HUD/assets/Car.svg"
            source : "file:///home/rbhud/HUD/assets/Car.svg"
	    z: 20
            width: 100
            height: carIconHeight
            fillMode: Image.PreserveAspectFit
            anchors.bottom: parent.bottom
            anchors.bottomMargin: 20

            // mqtt.currentLane은 1-indexed (예: 1,2,3). 중앙에 차량을 놓기 위해 -0.5 보정.
            x: ((mqtt
                 && mqtt.currentLane) ? ((mqtt.currentLane - 0.5)
                                         * roadArea.laneSpacing) : (1.5 * roadArea.laneSpacing))
               - width / 2

            Behavior on x {
                NumberAnimation {
                    duration: 300
                    easing.type: Easing.InOutQuad
                }
            }
        }

        // -------------------------------
        // 회피 화살표 (녹색) — 단순 직선/꺾기 방식 (첫 번째 코드 스타일)
        // -------------------------------
        Canvas {
            id: laneChangeArrow
            anchors.fill: parent
            z: 10
            property real progress: 0.0
            visible: mqtt && mqtt.state === "samePath"

            onPaint: {
                var ctx = getContext("2d")
                ctx.clearRect(0, 0, width, height)

                if (!mqtt || mqtt.state !== "samePath") return
                if (!mqtt || mqtt.totalLanes <= 0 || !mqtt.currentLane) return

                var t = Number(mqtt.totalLanes)
                var c = Number(mqtt.currentLane)
                var ad = Number(mqtt.avoidDir)
                if (!isFinite(t) || t <= 0) return
                if (!isFinite(c) || c <= 0) return

                c = Math.max(1, Math.min(t, Math.round(c)))

                var targetLane = c
                if (ad === 1) targetLane = c + 1
                else if (ad === 2) targetLane = c - 1
                targetLane = Math.max(1, Math.min(t, Math.round(targetLane)))

                // ── 기본 좌표 계산 ─────────────────────────────────────────
                var startX = (mqtt.currentLane - 0.5) * roadArea.laneSpacing
                var startY = height - (carIconHeight + 20)

                // 목표 차선(좌/우 1칸)
                //var targetLane = mqtt.currentLane
                if (mqtt.avoidDir === 1) targetLane += 1   // 오른쪽
                else if (mqtt.avoidDir === 2) targetLane -= 1   // 왼쪽
                targetLane = Math.max(1, Math.min(mqtt.totalLanes, targetLane))

                var endX = (targetLane - 0.5) * roadArea.laneSpacing

                // 모양(세 구간) 파라미터
                var up1     = 90   // 1단계: 위로 올라가는 길이
                var diagDy  = 90   // 2단계: 대각선에서 내려가는 세로량(+면 아래로)
                var down3   = 80   // 3단계: 마지막 수직(아래) 길이
                var endY    = (startY - up1) + diagDy + down3  // 최종 끝 Y

                // 각 구간 끝점 (P0->P1->P2->P3)
                var P0 = {x:startX, y:startY}
                var P1 = {x:startX, y:startY - up1}                  // 수직 ↑
                var P2 = {x:endX,  y:P1.y - diagDy}                  // 대각선 ↘ (x는 endX까지)
                var P3 = {x:endX,  y:P2.y - down3}                   // 수직 ↓ (최종)

                // 구간 길이(픽셀)과 전체 길이
                function dist(a,b){ var dx=a.x-b.x, dy=a.y-b.y; return Math.sqrt(dx*dx+dy*dy) }
                var L1 = dist(P0, P1)
                var L2 = dist(P1, P2)
                var L3 = dist(P2, P3)
                var Ltot = L1 + L2 + L3

                // progress(0~1)을 픽셀 길이로 변환
                var drawLen = Ltot * Math.min(Math.max(progress, 0.0), 1.0)

                // ── 선 스타일 ────────────────────────────────────────────
                ctx.beginPath()
                ctx.lineWidth = 22
                ctx.strokeStyle = "#39FF14"
                ctx.lineCap = "round"
                ctx.lineJoin = "round"
                ctx.moveTo(P0.x, P0.y)

                // 한 폴리라인을 "길이" 기준으로 잘라 그리기
                function lineToClamped(from, to, remain) {
                    // from->to 구간 길이가 remain보다 짧으면 통째로 그림
                    var seg = dist(from, to)
                    if (remain >= seg) {
                        ctx.lineTo(to.x, to.y)
                        return remain - seg
                    } else if (remain > 0) {
                        // 중간까지만 보간해서 그림
                        var t = remain / seg
                        var x = from.x + (to.x - from.x) * t
                        var y = from.y + (to.y - from.y) * t
                        ctx.lineTo(x, y)
                        return 0
                    }
                    // remain==0 이면 아무 것도 안 그림(현재 위치 유지)
                    return 0
                }

                // 순서대로: ↑, ↘, ↓
                var remain = drawLen
                remain = lineToClamped(P0, P1, remain)
                remain = lineToClamped(P1, P2, remain)
                remain = lineToClamped(P2, P3, remain)

                ctx.stroke()

                // ── 화살표 머리(완성 시) ──────────────────────────────────
                if (progress >= 1.0) {
                    // 끝 방향은 수직 ↓ 이라 삼각형을 아래 방향으로
                    var arrow = 35
                    ctx.beginPath()
                    ctx.fillStyle = "#39FF14"
                    ctx.moveTo(P3.x, P3.y - arrow)             // 아래 꼭짓점
                    ctx.lineTo(P3.x - arrow/2, P3.y + 4)       // 좌측 위
                    ctx.lineTo(P3.x + arrow/2, P3.y + 4)       // 우측 위
                    ctx.closePath()
                    ctx.fill()
                }
            }


            Timer {
                id: arrowTimer
                interval: 30; running: false; repeat: true
                onTriggered: {
                    if (laneChangeArrow.progress < 1.0) {
                        laneChangeArrow.progress += 0.02
                        laneChangeArrow.requestPaint()
                    } else {
                        laneChangeArrow.progress = 0.0   // 다시 처음부터 시작
                        laneChangeArrow.requestPaint()
                    }
                }
            }

            function startAnimation() {
                progress = 0.0
                arrowTimer.start()
            }
        }

    }

    // -------------------------------
    // HUD Overlay (ETA + 사이렌)
    // -------------------------------
    Item {
        id: hudOverlay
        anchors.fill: parent
        z: 9999

        Row {
            anchors.top: parent.top
            anchors.left: parent.left
            anchors.topMargin: 20
            anchors.leftMargin: 20
            spacing: 8

            Column {
                spacing: 6
                visible: mqtt && mqtt.state === "samePath"
                Text {
                    id: etaLabel
                    text: "ETA"
                    font.pixelSize: 70
                    color: "white"
                    font.bold: true
                }
                // ✅ 이제 mqtt.eta 직접참조 대신 etaText만 사용
                Text {
                    text: window.etaText
                    font.pixelSize: 70
                    font.family: "Noto Sans"    // 또는 "DejaVu Sans"
                    color: "lightgreen"
                }
            }

            Image {
                //source: "file:///C:/Users/seokhwan/Desktop/HUD/assets/siren.png"
                source: "file:///home/rbhud/HUD/assets/siren.png"
                width: 150
                height: 150
                anchors.verticalCenter: etaLabel.verticalCenter
                visible: mqtt && (mqtt.state === "samePath"
                                  || mqtt.state === "nearby")
                SequentialAnimation on opacity {
                    loops: Animation.Infinite
                    NumberAnimation {
                        from: 1.0
                        to: 0.2
                        duration: 500
                    }
                    NumberAnimation {
                        from: 0.2
                        to: 1.0
                        duration: 500
                    }
                }
            }
        }
    }

    // -------------------------------
    // 자동으로 avoidDir 변경 시 laneChangeArrow.startAnimation() 호출
    // (mqtt 값이 바뀌면 차선 애니 실행)
    // -------------------------------
    // ✅ MQTT 이벤트를 받아서 etaText를 안전하게 갱신
    Connections {
        target: mqtt

        // ETA가 바뀔 때만 문자열로 안전 복사
        onEtaChanged: {
            if (mqtt && typeof mqtt.eta === "string") {
                window.etaText = mqtt.eta
            } else if (mqtt && (typeof mqtt.eta === "number")) {
                // 숫자로 올 경우 포맷팅(분:초) – 선택 사항
                var sec = Math.max(0, Math.floor(mqtt.eta))
                var m = Math.floor(sec / 60)
                var s = sec % 60
                window.etaText = m + "m " + s + "s"
            } else {
                window.etaText = ""
            }
        }

        onAvoidDirChanged: if (mqtt.state === "samePath") {
            laneChangeArrow.startAnimation()
            redLaneEffect.progress = 0.0
        }
        onCurrentLaneChanged: if (mqtt.state === "samePath") {
            laneChangeArrow.startAnimation()
            redLaneEffect.progress = 0.0
        }
        onStateChanged: {
            if (mqtt.state === "samePath") {
                laneChangeArrow.startAnimation()
                redLaneEffect.progress = 0.0
            } else {
                // ✅ samePath가 아니면 ETA 숨김
                window.etaText = ""
            }
        }
    }
}
