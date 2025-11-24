# -*- coding: utf-8 -*-
import os
import json
from datetime import datetime
from flask import request, jsonify
import paho.mqtt.client as paho
from flask import Flask, redirect, url_for
from flask_sqlalchemy import SQLAlchemy
from flask_socketio import SocketIO
from extensions import db
from config import Config
from routes import auth, dashboard, video
from models.ambulance_log import AmbulanceLog
from utils.car_utils import normalize_car_no
from utils.crossroad_utils import load_crossroad_csv, compute_crossroad_directions,haversine
import logging
logging.getLogger("werkzeug").setLevel(logging.ERROR)

#"C:\Program Files\mosquitto\mosquitto.exe" -c "C:\Program Files\mosquitto\mosquitto.conf" -v

#---
#교차로 정보 읽기
#--
crossroad_df = load_crossroad_csv("static\crossroad_map\CrossroadMap.csv")
expected_crossroads = []   # 주행 예상 교차로 + 방향 저장


# ---------------------------------------------------

# 확장 객체 (extensions.py 역할)
# ---------------------------------------------------

socketio = SocketIO(cors_allowed_origins="*", async_mode="threading")

# ---------------------------------------------------
# Flask 애플리케이션 설정
# ---------------------------------------------------
app = Flask(__name__)
app.config.from_object(Config)

# 확장 초기화
db.init_app(app)
socketio.init_app(app)

# 블루프린트 등록
app.register_blueprint(auth.bp)
app.register_blueprint(dashboard.bp)
app.register_blueprint(video.bp)

@app.route("/control_crossroad", methods=["POST"])
def control_crossroad():
    """
    웹 버튼 클릭 시 교차로 제어 명령 MQTT로 전송 (QoS=2)
    """
    try:
        payload = {
            "event": "change",
            "crossroad_id": 1,
            "timestamp": datetime.now().isoformat()
        }

        topic = "crossroad/1/control"

        mqtt_client.publish(topic, json.dumps(payload), qos=2, retain=False)
        print(f"🚦 [MQTT 전송] {topic}: {payload}")

        return jsonify({"success": True, "topic": topic, "payload": payload})

    except Exception as e:
        print("❌ MQTT 발행 실패:", e)
        return jsonify({"success": False, "error": str(e)}), 500

@app.route("/")
def index():
    return redirect(url_for("auth.login"))

# ---------------------------------------------------
# Socket.IO 이벤트 핸들러 (카메라 프레임)
# ---------------------------------------------------
@socketio.on("image_broadcast_cam1", namespace="/")
def handle_cam1_image(data):
    # print(f"📷 카메라 프레임 수신, 길이={len(data)}")
    socketio.emit("image_broadcast_cam1", data, namespace="/")

# ---------------------------------------------------
# MQTT + SocketIO 통합 로직
# ---------------------------------------------------
mqtt_client = paho.Client(client_id="flask-subscriber", clean_session=True)

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("✅ MQTT 브로커 연결 성공")
        client.subscribe("ambulance/web/#")
        client.subscribe("normalcar/web/#")
    else:
        print(f"❌ MQTT 연결 실패 (rc={rc})")

def on_message_factory(app):
    def on_message(client, userdata, msg):
        print("📥 MQTT 메시지 도착:", msg.topic, msg.payload[:100])
        try:
            data = json.loads(msg.payload.decode())
            print(f"📡 [MQTT 수신] {msg.topic} → {list(data.keys())}")

            # 출발 이벤트
            if msg.topic == "ambulance/web/start":
                with app.app_context():
                    car_no = data.get("car")
                    start_time = datetime.strptime(data["start_time"], "%Y-%m-%d %H:%M:%S")

                    normalized_car_no = normalize_car_no(car_no)
                    timestamp = start_time.strftime("%Y%m%d_%H%M%S")
                    file_name = f"{normalized_car_no}_{timestamp}.mp4"

                    log = AmbulanceLog(car_no=car_no, start_time=start_time, video_url=file_name)
                    db.session.merge(log)
                    db.session.commit()

                socketio.emit("ambulance_start", data, namespace="/")
                print(f"✅ DB INSERT: {car_no}, 출발={start_time}, 파일명={file_name}")

            # 도착 이벤트
            elif msg.topic == "ambulance/web/arrival":
                with app.app_context():
                    car_no = data.get("car")
                    start_time = datetime.strptime(data["start_time"], "%Y-%m-%d %H:%M:%S")

                    log = db.session.get(AmbulanceLog, (car_no, start_time))  # 
                    if log:
                        log.arrival_time = datetime.strptime(data["arrival_time"], "%Y-%m-%d %H:%M:%S")
                        db.session.commit()

                socketio.emit("ambulance_arrival", data, namespace="/")
                print(f"✅ DB UPDATE(도착): {car_no}, 도착={log.arrival_time}")

            # 경로 이벤트
            elif msg.topic == "ambulance/web/route":
                try:
                    data["route_points"] = [
                        {"lat": float(p[0]), "lng": float(p[1])} if isinstance(p, (list, tuple)) else
                        {"lat": float(p["lat"]), "lng": float(p["lng"])}
                        for p in data["route_points"]
                    ]
                except Exception as e:
                    print("⚠️ 좌표 변환 실패:", e)
                
                global expected_crossroads
                expected_crossroads = compute_crossroad_directions(data["route_points"], crossroad_df, radius=50)
                print("🚦 예상 교차로 및 접근 방향:")
                for c in expected_crossroads:
                    c["status"] = "pending"
                    print(f"  - {c['name']}: {c['explain']} (진입={c['in_dir']} → 이탈={c['out_dir']}, turn={c['turn']})")


                print("🚑 emit 직전 좌표 샘플:", data["route_points"][:2])
                socketio.emit("ambulance_route", data, namespace="/")

            # 현재 위치 이벤트
            elif msg.topic == "ambulance/web/current":
                print("🚑 수신 데이터:", data)

                current = data.get("current", {})
                lat = current.get("lat")
                lon = current.get("lng")

                if lat is not None and lon is not None:
                    lat, lon = float(lat), float(lon)

                    # ✅ 예상 교차로와 거리 비교
                    for c in expected_crossroads:
                        d = haversine(lat, lon, c["lat"], c["lon"])

                        if c["status"] == "pending" and d <= 300:
                            print(f"⚠️ 교차로 접근 알림: {c['name']} "
                            f"(진입={c['in_dir']} → 이탈={c['out_dir']}, turn={c['turn']}, 거리={d:.1f}m)")
                            c["status"] = "approaching"

                            mqtt_client.publish(
                                f"ambulance/web/crossroad/{c['id']}",
                                json.dumps({
                                    "event": "approach",
                                    "crossroad_id": c["id"],
                                    "crossroad_name": c["name"],
                                    "turn": c.get("turn"),             # 직진/좌회전/우회전/유턴
                                    "in_dir": c.get("in_dir"),         # 진입 방향
                                    "out_dir": c.get("out_dir"),       # 이탈 방향
                                    "explain": c.get("explain"),       # 사람이 읽기 쉬운 설명
                                    "distance": round(d, 1),
                                    "timestamp": datetime.now().isoformat()
                                }),
                                qos=2,     # ✅ QoS 2 보장
                                retain=False
                            )

                        elif c["status"] == "approaching" and d <= 50:
                            print(f"🚦 교차로 도착: {c['name']} (거리={d:.1f}m)")
                            c["status"] = "arrived"
                     

                        elif c["status"] == "arrived" and d > 50:
                            print(f"✅ 교차로 통과 완료: {c['name']}")
                            c["status"] = "passed"
                            mqtt_client.publish(f"ambulance/web/crossroad/{c['id']}", json.dumps({
                                "event": "passed",
                                "crossroad_id": c["id"],
                                "distance": round(d, 1),
                                "timestamp": datetime.now().isoformat()
                            }),
                            qos=2,     # ✅ QoS 2 보장
                            retain=False
                        )

                else:
                    print("⚠️ current 좌표 없음:", data)

                socketio.emit("ambulance_current", data, namespace="/")

            
            elif msg.topic == "normalcar/web/current":
                socketio.emit("normalcar_current", data, namespace="/")
                print("🚗 일반 차량 현재 위치 전송:", data)


        except Exception as e:
            print(f"❌ MQTT 처리 오류: {e}")

    return on_message

mqtt_client.on_connect = on_connect
mqtt_client.on_message = on_message_factory(app)

# ---------------------------------------------------
# 실행 엔트리포인트
# ---------------------------------------------------
if __name__ == "__main__":
    with app.app_context():
        db.session.expire_all()
        db.create_all()
    print("👉 DB 사용 경로:", os.path.abspath("test.db"))

    # MQTT 연결 시작
    try:
        mqtt_client.connect(app.config["MQTT_BROKER"], app.config["MQTT_PORT"], 60)
        print("✅ MQTT 연결 시도 완료")
    except Exception as e:
        print(f"⚠️ 초기 MQTT 브로커 연결 실패: {e}")
    finally:
        # loop_start는 반드시 실행 → 브로커가 나중에 켜져도 자동 재연결됨
        mqtt_client.loop_start()


    socketio.run(app, host="0.0.0.0", port=5001, use_reloader=False, debug=False)
