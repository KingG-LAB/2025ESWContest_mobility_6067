# config.py
import os

basedir = os.path.abspath(os.path.dirname(__file__))

class Config:
    # MQTT 설정
    MQTT_BROKER = "10.210.97.35"   # ✅ 여기에 브로커 IP만 바꾸면 됨(변경 필수!!!!!)
    MQTT_PORT = 1883

    # 소켓 서버 주소
    CAR_SERVER_URL = "http://127.0.0.1:5000" #얘는 내부 통신이여서 바꿀 필요 없음

    WEB_SERVER_URL = "http://192.168.137.184:5001" #웹 띄운 노트북 ip로 변경(변경 필수!!!!)

    # 🔑 Kakao Mobility REST API Key
    REST_API_KEY = "f345f684051191769c60a3d5f15d3774"
    API_URL = "https://apis-navi.kakaomobility.com/v1/directions"