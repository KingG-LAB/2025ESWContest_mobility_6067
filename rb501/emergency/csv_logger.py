import csv, os, boto3
from datetime import datetime
from s3_client import s3, bucket_name
# 🔤 차량번호 한글 → 영문 변환 매핑
kor_map = {
    "가": "ga", "나": "na", "다": "da", "라": "ra", "마": "ma",
    "바": "ba", "사": "sa", "아": "a",  "자": "ja", "차": "cha",
    "카": "ka", "타": "ta", "파": "pa", "하": "ha"
}

def normalize_car_id(car_id: str) -> str:
    safe = ""
    for ch in car_id:
        if ch in kor_map:
            safe += kor_map[ch]
        elif ch.isalnum():
            safe += ch
        else:
            safe += "_"
    return safe


csv_file = None
csv_writer = None
csv_file_path = None

def start_csv_logging(car, start_time):
    global csv_file, csv_writer, csv_file_path

    if not os.path.exists("logs"):
        os.makedirs("logs")  # Create the 'logs' directory if it doesn't exist

    safe_car_id = normalize_car_id(car)

    filename = f"{safe_car_id}_{start_time.strftime('%Y%m%d_%H%M%S')}.csv"
    csv_file_path = os.path.join("logs", filename)

    csv_file = open(csv_file_path, mode="w", newline="", encoding="utf-8-sig")
    csv_writer = csv.writer(csv_file)

    # ✅ 위도/경도 분리 대신 current 배열로 저장
    csv_writer.writerow(["timestamp", "car_id", "current", "total_lanes", "car_lane", "same_road_and_dir"])
    print(f"📝 CSV 로깅 시작: {csv_file_path}")
    return csv_writer

def stop_csv_logging():
    global csv_file, csv_file_path
    if csv_file:
        csv_file.close()
        try:
            s3_key = f"logs/{os.path.basename(csv_file_path)}"
            s3.upload_file(csv_file_path, bucket_name, s3_key, ExtraArgs={'ContentType': 'text/csv'})
            print(f"✅ CSV 업로드 완료 → https://{bucket_name}.s3.us-east-1.amazonaws.com/{s3_key}")
        except Exception as e:
            print(f"❌ CSV 업로드 실패: {e}")
        csv_file, csv_file_path = None, None

def log_feedback(timestamp, car_id, lat, lng, total_lanes, car_lane, same_road):
    global csv_writer
    if csv_writer:
        # lat/lng 따로 쓰지 않고 current 배열로 저장
        current = [lat, lng]
        csv_writer.writerow([timestamp, car_id, current, total_lanes, car_lane, same_road])
