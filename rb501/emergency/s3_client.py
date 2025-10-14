import boto3
# ✅ AWS S3 클라이언트 (항상 키 포함)
s3 = boto3.client("s3",
    aws_access_key_id="",
    aws_secret_access_key="",
    region_name=""
)
bucket_name = ""
