# server.py
# -*- coding: utf-8 -*-
import socket, sys, json, time, threading
import paho.mqtt.client as mqtt
from datetime import datetime, timedelta
# from camera_handler import CameraHandler
from mqtt_publisher import MqttPublisher   # ✅ 통일된 Publisher 사용
from kakao_client import KakaoClient
import csv_logger 
from csv_logger import log_feedback
from camera_handler import start_camera_relay, camera_handler, sio_car
from config import Config
from lcd_display import LcdDisplay

is_driving = False  # 전역 플래그
HOST = "0.0.0.0"
PORT = 6000 #소켓 포트임 안드로이드 앱이랑


kakao = KakaoClient(api_key=Config.REST_API_KEY)
publisher = MqttPublisher(broker=Config.MQTT_BROKER, port=Config.MQTT_PORT)
lcd = LcdDisplay(vehicle_name="119da 119",vehicle_ip="192.168.137.238")

# -------------------------------
# MQTT Subscriber (구급차 → feedback 수신)
# -------------------------------
def on_connect(client, userdata, flags, rc):
    print("✅ 구급차 MQTT 연결 완료")
    client.subscribe("ambulance/feedback")  # 🚗 차량 피드백 토픽 구독

def on_message(client, userdata, msg):
    global is_driving
    raw = msg.payload.decode()
    print(f"📩 MQTT 메시지 도착: {raw}")
    print(f"flag : {is_driving}")


    if not is_driving:
        return  # 주행 중이 아닐 때는 무시
    try:
        payload = json.loads(msg.payload.decode())
        print(f"📥 피드백 수신 → {payload}")

        car_id = payload.get("car")
        current = payload.get("current", {})  # dict
        lat = current.get("lat")
        lng = current.get("lng")
        total_lanes = payload.get("total_lanes")
        car_lane = payload.get("car_lane")
        same_road = payload.get("same_road_and_dir")
        timestamp = payload.get("timestamp")

        print(f"🚗 차량 {car_id} @ {current}") 
        print(f"   ⛖ 도로 전체 차선: {total_lanes}, 차량 위치 차선: {car_lane}")
        print(f"   🔄 동일 도로 여부: {same_road}, 시각: {timestamp}")

        log_feedback(timestamp, car_id, lat, lng, total_lanes, car_lane, same_road)

    except Exception as e:
        print("❌ 피드백 처리 오류:", e)

def start_feedback_listener():
    client = mqtt.Client(client_id="ambulance-subscriber")
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect(Config.MQTT_BROKER, Config.MQTT_PORT, 60)
    client.loop_start()
    return client

# -------------------------------
# 주행 시뮬레이션 (별도 쓰레드 실행)
# -------------------------------
def simulate_drive(car, dest, kakao_json, start_time):
    global is_driving
    start_camera_relay(car, start_time)  # ✅ 라즈 연결 + 녹화 시작
    lcd.update_status(state="start")

    full_points = kakao.extract_all_points(kakao_json)
    web_points  = kakao.extract_web_points(kakao_json)

    if not full_points:
        print("❌ 추출된 경로 좌표 없음")
        return

    # ✅ 웹(경량 데이터)
    publisher.send_route(car, dest, web_points, web_points[0])
    
    is_driving = True

    # 주행 중 전송
    for i, coord in enumerate(full_points, 1):
        publisher.send_current(car, dest, coord, web=True)
        publisher.send_current(car, dest, coord, route_info=kakao_json, web=False)
        print(f"📡 웹/차량 발행 {i}/{len(full_points)}")
        # time.sleep(0.2)
        time.sleep(1.0)

    # 도착 알림
    publisher.send_arrival(car, dest, start_time.strftime("%Y-%m-%d %H:%M:%S"))
    print(f"🏁 도착 알림 발행 → {dest}")
    camera_handler.stop_and_upload()   # ✅ 전역 인스턴스 종료
    sio_car.disconnect()
    csv_logger.stop_csv_logging()
    lcd.update_status(state="finished")
    is_driving = False


# -------------------------------
# 메인 서버 루프
# -------------------------------
def main():
    SADANG_LNG, SADANG_LAT = "126.9816", "37.4765"
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((HOST, PORT))
    server.listen()
    server.settimeout(1)  # ✅ 1초마다 accept 깨어남
    print("🚑 서버 대기중... (Ctrl+C 로 종료)")

    try:
        while True:
            try:
                conn, addr = server.accept()
            except socket.timeout:
                continue  # 1초마다 루프 재진입, Ctrl+C 즉시 반응 가능

            print("📡 연결됨:", addr)
            data = conn.recv(1024).decode().strip()
            if not data:
                continue

            try:
                msg = json.loads(data)
                car  = msg.get("car", "119다119")
                dest = msg.get("dest", "중앙대학교 병원")
                dest_lat, dest_lng = float(msg["lat"]), float(msg["lng"])

                result = kakao.request_route(SADANG_LNG, SADANG_LAT, dest_lng, dest_lat)
                if result["success"]:
                    now = datetime.now()
                    sections = result["raw"]["routes"][0]["sections"]
                    total_duration = sum(int(s.get("duration",0)) for s in sections)
                    eta = now + timedelta(seconds=total_duration)

                    response_msg = {
                        "status":"success",
                        "distance":result["distance"],
                        "duration":result["duration"],
                        "start_time":now.strftime("%Y-%m-%d %H:%M:%S"),
                        "eta_time":eta.strftime("%Y-%m-%d %H:%M:%S")
                    }

                    conn.sendall((json.dumps(response_msg, ensure_ascii=False)+"\n").encode())
                    publisher.send_start(car, "사당역", dest, now)
                    print("시작 mqtt발송")
                    csv_logger.start_csv_logging(car, now)
                    
                    # ✅ simulate_drive를 별도 쓰레드에서 실행
                    threading.Thread(target=simulate_drive, args=(car, dest, result["raw"], now), daemon=True).start()

                else:
                    conn.sendall((json.dumps({"status":"fail","error":result["error"]}, ensure_ascii=False)+"\n").encode())

            except Exception as e:
                print("❌ 처리 오류:", e)
                conn.sendall((json.dumps({"status":"fail","error":str(e)}, ensure_ascii=False)+"\n").encode())

    except KeyboardInterrupt:
        print("\n🛑 서버 종료 중...")
        server.close()
        lcd.stop()
        sys.exit(0)

if __name__ == "__main__":
    start_feedback_listener()
    lcd.start()
    main()
