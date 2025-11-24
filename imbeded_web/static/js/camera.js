// ✅ 이 모듈 안에서만 socket 연결 생성
const camSocket = io("http://localhost:5001", {
  transports: ["websocket"]
});

camSocket.on("connect", () => {
  console.log("✅ camera.js 소켓 연결됨");
});

camSocket.on("disconnect", () => {
  console.warn("❌ camera.js 소켓 끊김");
  showNoSignal();
});

// -------------------------------
// ✅ 카메라 No Signal 처리
// -------------------------------
let cam1Timeout;
const CAM1_TIMEOUT_MS = 5000;

function showNoSignal() {
  document.getElementById("cam1").src = "/static/images/no_signal.png";
}
showNoSignal();

// -------------------------------
// ✅ 카메라 프레임 이벤트 처리
// -------------------------------
camSocket.on("image_broadcast_cam1", function (base64image) {
  document.getElementById("cam1").src = "data:image/jpeg;base64," + base64image;
  clearTimeout(cam1Timeout);
  cam1Timeout = setTimeout(showNoSignal, CAM1_TIMEOUT_MS);
});
