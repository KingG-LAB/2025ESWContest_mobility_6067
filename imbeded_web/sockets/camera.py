from extensions import socketio

# @socketio.on("image_broadcast_cam1", namespace="/")
# def handle_cam1_image(data):
#     print(f"📷 카메라 프레임 수신, 길이={len(data.get('image', ''))}")
#     socketio.emit("image_broadcast_cam1", data["image"], namespace="/")
@socketio.on("image_broadcast_cam1", namespace="/")
def handle_cam1_image(data):
    print(f"📷 카메라 프레임 수신, 길이={len(data)}")
    socketio.emit("image_broadcast_cam1", data, namespace="/")
