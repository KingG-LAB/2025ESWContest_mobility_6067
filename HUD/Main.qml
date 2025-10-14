import QtQuick
import QtQuick.Controls
import QtQuick.Window

ApplicationWindow {
    id: window
    width: 800
    height: 480
    visible: true
    visibility: "FullScreen"   // ✅ 전체화면 실행
    color: "black"
    title: qsTr("HUD with Dynamic Lanes & Animated Arrow + Center Red Lane Effect")

    // ✅ 좌우 반전 적용
    scale: -1

    // ===== 공통 유틸 =====
    function isNum(x)        { return typeof x === "number" && isFinite(x) }
    function toInt(x, d)     { return isNum(x) ? Math.floor(x) : d }
    function clamp(x,a,b)    { return (x < a) ? a : (x > b) ? b : x }

    // MQTT object가 아직 바인드 안되었을 수 있으므로 safe getters
    function tl() {            // totalLanes
        let t = mqtt ? toInt(mqtt.totalLanes, 3) : 3
        return clamp(t, 1, 20)
    }
    function cl() {            // currentLane
        let c = mqtt ? toInt(mqtt.currentLane, 1) : 1
        return clamp(c, 1, tl())
    }
    function al() {            // ambulanceLane
        let a = mqtt ? toInt(mqtt.ambulanceLane, 1) : 1
        return clamp(a, 1, tl())
    }
    function ad() {            // avoidDir (0:none,1:right,2:left)
        let v = mqtt ? toInt(mqtt.avoidDir, 0) : 0
        return clamp(v, 0, 2)
    }
    function st() {            // state
        return (mqtt && typeof mqtt.state === "string") ? mqtt.state : "idle"
    }

    // 전역 보정값
    property int outerMargin: 150
    property int laneLineWidth: 3
    property int carIconHeight: 160

    // ✅ ETA는 문자열 버퍼만 UI가 참조
    property string etaText: ""

    // ===== repaint 디바운스 (신호 폭주 방지) =====
    Timer {
        id: repaintDebounce
        interval: 16      // ≈ 60fps 한 프레임
        repeat: false
        onTriggered: {
            if (redLaneEffect.visible) redLaneEffect.requestPaint()
            if (laneChangeArrow.visible) laneChangeArrow.requestPaint()
        }
    }

    Item {
        id: roadArea
        anchors.fill: parent
        anchors.leftMargin: outerMargin
        anchors.rightMargin: outerMargin

        // 0/NaN/비정상 값 방지
        property real laneSpacing: Math.max(1, width / tl())

        // ===== 차선 라인 =====
        Repeater {
            id: laneRepeater
            model: tl() + 1
            Item {
                id: lineSlot
                property int lw: Math.max(1, laneLineWidth)
                width: lw
                height: parent ? parent.height : 0
                y: 0

                // 마지막 라인은 우측 경계로 보정
                x: (index === tl()) ? Math.max(0, roadArea.width - lw)
                                    : Math.round(index * roadArea.laneSpacing)

                Rectangle {
                    anchors.fill: parent
                    color: "white"
                    visible: (index === 0 || index === tl())
                }

                Repeater {
                    // 중간 점선
                    model: (index > 0 && index < tl()) ? 10 : 0
                    Rectangle {
                        width: lineSlot.lw
                        height: 30
                        color: "white"
                        opacity: 0.9
                        x: -(lineSlot.lw / 2)
                        y: index * 60
                    }
                }
            }
        }

        // ===== 빨간 점선(응급차 레인) =====
        Canvas {
            id: redLaneEffect
            anchors.fill: parent
            z: 5
            property real progress: 0.0
            visible: (st() === "samePath")

            // 안정화 옵션
            renderTarget: Canvas.Image
            renderStrategy: Canvas.Threaded
            antialiasing: false
            smooth: false

            onPaint: {
                const ctx = getContext && getContext("2d")
                if (!ctx) return
                if (width <= 0 || height <= 0) return
                if (!isFinite(roadArea.laneSpacing) || roadArea.laneSpacing <= 0) return

                try { ctx.clearRect(0, 0, width, height) } catch (e) { return }

                if (st() !== "samePath") return

                const t = tl()
                const a = al()
                if (!isFinite(t) || t <= 0 || !isFinite(a)) return

                const laneX = (a - 0.5) * roadArea.laneSpacing
                const fromY = height + 100
                const toY   = height - 580
                if (!isFinite(laneX) || !isFinite(fromY) || !isFinite(toY)) return

                ctx.lineWidth = 15
                ctx.strokeStyle = "#FF0000"
                ctx.lineCap = "butt"

                const segmentLength = 50
                const gap = 60
                const offset = (progress % 1) * (segmentLength + gap)
                const arrowSize = 30

                for (let y = fromY - offset; y > toY; y -= (segmentLength + gap)) {
                    const y1 = Math.max(y - segmentLength, toY)

                    ctx.beginPath()
                    ctx.moveTo(laneX, y)
                    ctx.lineTo(laneX, y1)
                    ctx.stroke()

                    ctx.beginPath()
                    ctx.fillStyle = "#FF0000"
                    ctx.moveTo(laneX, y1 - arrowSize)
                    ctx.lineTo(laneX - arrowSize / 2, y1)
                    ctx.lineTo(laneX + arrowSize / 2, y1)
                    ctx.closePath()
                    ctx.fill()
                }
            }

            Timer {
                interval: 50
                running: redLaneEffect.visible
                repeat: true
                onTriggered: {
                    redLaneEffect.progress = (redLaneEffect.progress + 0.05) % 1.0
                    redLaneEffect.requestPaint()
                }
            }
            onVisibleChanged: if (!visible) { progress = 0.0 }
        }

        // ===== 차량 아이콘 =====
        Image {
            id: car
            source: "file:///home/rbhud/HUD/assets/Car.svg"
            z: 20
            width: 100
            height: carIconHeight
            fillMode: Image.PreserveAspectFit
            anchors.bottom: parent.bottom
            anchors.bottomMargin: 20

            x: clamp(((cl() - 0.5) * roadArea.laneSpacing) - width / 2, -width, roadArea.width)

            Behavior on x {
                NumberAnimation { duration: 300; easing.type: Easing.InOutQuad }
            }
            onStatusChanged: if (status === Image.Error) console.warn("Car.svg load failed:", source)
        }

        // ===== 회피 화살표 =====
        Canvas {
            id: laneChangeArrow
            anchors.fill: parent
            z: 10
            property real progress: 0.0
            visible: (st() === "samePath")

            // 안정화 옵션
            renderTarget: Canvas.Image
            renderStrategy: Canvas.Threaded
            antialiasing: false
            smooth: false

            onPaint: {
                const ctx = getContext && getContext("2d")
                if (!ctx) return
                if (width <= 0 || height <= 0) return
                if (!isFinite(roadArea.laneSpacing) || roadArea.laneSpacing <= 0) return

                try { ctx.clearRect(0, 0, width, height) } catch (e) { return }

                if (st() !== "samePath") return

                const t  = tl()
                const c0 = cl()
                const av = ad()    // ← 이름 충돌 방지
                if (!isFinite(t) || t <= 0) return

                let targetLane = c0
                if (av === 1) targetLane++
                else if (av === 2) targetLane--
                targetLane = clamp(targetLane, 1, t)

                const startX = (c0 - 0.5) * roadArea.laneSpacing
                const startY = height - (carIconHeight + 20)
                const endX   = (targetLane - 0.5) * roadArea.laneSpacing
                if (!isFinite(startX) || !isFinite(startY) || !isFinite(endX)) return

                const up1    = 90
                const diagDy = 90
                const down3  = 80

                const P0 = {x:startX, y:startY}
                const P1 = {x:startX, y:startY - up1}
                const P2 = {x:endX,  y:P1.y - diagDy}
                const P3 = {x:endX,  y:P2.y - down3}

                function dist(a,b){ const dx=a.x-b.x, dy=a.y-b.y; return Math.sqrt(dx*dx+dy*dy) }
                const L1 = dist(P0,P1), L2 = dist(P1,P2), L3 = dist(P2,P3)
                const Ltot = Math.max(1, L1 + L2 + L3)
                let drawLen = clamp(progress, 0, 1) * Ltot
                if (!isFinite(drawLen) || Ltot <= 0) return

                ctx.beginPath()
                ctx.lineWidth = 22
                ctx.strokeStyle = "#39FF14"
                ctx.lineCap = "round"
                ctx.lineJoin = "round"
                ctx.moveTo(P0.x, P0.y)

                function lineToClamped(from, to, remain) {
                    const seg = dist(from,to)
                    if (remain >= seg) { ctx.lineTo(to.x,to.y); return remain - seg }
                    if (remain > 0) {
                        const tt = remain / seg
                        ctx.lineTo(from.x + (to.x-from.x)*tt, from.y + (to.y-from.y)*tt)
                        return 0
                    }
                    return 0
                }

                let r = drawLen
                r = lineToClamped(P0,P1,r)
                r = lineToClamped(P1,P2,r)
                r = lineToClamped(P2,P3,r)
                ctx.stroke()

                // 진행이 끝에 충분히 근접하면 화살표 머리 표시
                if (progress >= 0.999) {
                    const arrow = 35
                    ctx.beginPath()
                    ctx.fillStyle = "#39FF14"
                    ctx.moveTo(P3.x, P3.y - arrow)
                    ctx.lineTo(P3.x - arrow/2, P3.y + 4)
                    ctx.lineTo(P3.x + arrow/2, P3.y + 4)
                    ctx.closePath()
                    ctx.fill()
                }
            }

            Timer {
                id: arrowTimer
                interval: 30
                running: false
                repeat: true
                onTriggered: {
                    // 1.0까지 채운 뒤 멈추고(머리 유지), 다음 이벤트에서 다시 startAnimation()
                    if (laneChangeArrow.progress >= 1.0) {
                        laneChangeArrow.progress = 1.0
                        if (arrowTimer.running) arrowTimer.stop()
                        laneChangeArrow.requestPaint()
                    } else {
                        laneChangeArrow.progress = Math.min(1.0, laneChangeArrow.progress + 0.02)
                        laneChangeArrow.requestPaint()
                    }
                }
            }

            function startAnimation() {
                // 새로 시작할 때만 리셋하고 타이머 시작
                progress = 0.0
                if (!arrowTimer.running) arrowTimer.start()
                // 즉시 한 프레임 요청 (시작 지점 표시)
                requestPaint()
            }

            onVisibleChanged: if (!visible && arrowTimer.running) { arrowTimer.stop(); progress = 0.0 }
        }
    }

    // ===== HUD Overlay =====
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
                visible: (st() === "samePath")
                Text {
                    id: etaLabel
                    text: "ETA"
                    font.pixelSize: 70
                    color: "white"
                    font.bold: true
                    scale: -1   // 두 번 반전 → 다시 정상 표시
                }
                Text {
                    text: window.etaText
                    font.pixelSize: 70
                    font.family: "Noto Sans"
                    color: "lightgreen"
                    scale: -1   // ETA 값도 정상 표시
                }
            }

            Image {
                source: "file:///home/rbhud/HUD/assets/siren.png"
                width: 150
                height: 150
                anchors.verticalCenter: etaLabel.verticalCenter
                visible: (st() === "samePath" || st() === "nearby")
                onStatusChanged: if (status === Image.Error) console.warn("siren.png load failed:", source)
                SequentialAnimation on opacity {
                    loops: Animation.Infinite
                    NumberAnimation { from: 1.0; to: 0.2; duration: 500 }
                    NumberAnimation { from: 0.2; to: 1.0; duration: 500 }
                }
            }
        }
    }

    // ===== MQTT 이벤트 바인딩 (널 타겟 안전) =====
    Connections {
        target: (typeof mqtt !== "undefined") ? mqtt : null

        onEtaChanged: {
            if (!mqtt) { window.etaText = ""; return }
            if (typeof mqtt.eta === "string") {
                window.etaText = mqtt.eta
            } else if (isNum(mqtt.eta)) {
                const sec = Math.max(0, Math.floor(mqtt.eta))
                const m = Math.floor(sec / 60)
                const s = sec % 60
                window.etaText = m + "m " + s + "s"
            } else {
                window.etaText = ""
            }
        }

        onAvoidDirChanged: {
            if (st() === "samePath") {
                laneChangeArrow.startAnimation()
                redLaneEffect.progress = 0.0
                repaintDebounce.restart()
            }
        }
        onCurrentLaneChanged: {
            if (st() === "samePath") {
                laneChangeArrow.startAnimation()
                redLaneEffect.progress = 0.0
                repaintDebounce.restart()
            }
        }
        onStateChanged: {
            if (st() === "samePath") {
                laneChangeArrow.startAnimation()
                redLaneEffect.progress = 0.0
                repaintDebounce.restart()
            } else {
                window.etaText = ""
                // 상태 벗어나면 안전 정지
                if (laneChangeArrow.arrowTimer && laneChangeArrow.arrowTimer.running)
                    laneChangeArrow.arrowTimer.stop()
                laneChangeArrow.progress = 0.0
            }
        }
    }
}
