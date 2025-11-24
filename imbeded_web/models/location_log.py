from extensions import db
from datetime import datetime

class EmergenceLocationLog(db.Model):
    __tablename__ = 'emergence_location_logs'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    vehicle_id = db.Column(db.String, nullable=False)
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)
