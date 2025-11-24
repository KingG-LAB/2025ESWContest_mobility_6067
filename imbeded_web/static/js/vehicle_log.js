// ✅ vehicle_log.js에서만 socket 연결 생성
const socket = io("http://localhost:5001", {
  transports: ["websocket"]
});

socket.on("connect", () => {
  console.log("✅ vehicle_log.js 소켓 연결됨");
});

socket.on("disconnect", () => {
  console.warn("❌ vehicle_log.js 소켓 끊김");
});

// 🚘 공통: 로그 행 추가 또는 업데이트 함수
function updateOrPrependLogRow(log) {
  const table = document.getElementById("vehicle-log-body");
  if (!table) return; // 안전 처리

  const carNo = log.vehicle_id || log.car;
  const startTime = log.departure_time || log.start_time;

  // 🚩 기존 행 탐색
  const rows = table.getElementsByTagName("tr");
  for (let row of rows) {
    const rowCar = row.cells[0].innerText;
    const rowStart = row.cells[1].innerText;

    if (rowCar === carNo && rowStart === startTime) {
      // ✅ ETA → 도착시간 갱신
      row.cells[2].innerText = log.arrival_time || log.estimated_arrival_time || log.eta_time || "-";
      console.log(`🔄 ${carNo} 로그 갱신됨 (도착시간: ${log.arrival_time})`);
      return;
    }
  }

  // 🚩 기존 행이 없으면 새로 추가
  if (table.rows.length >= 10) {
    table.deleteRow(table.rows.length - 1);
  }
  const row = document.createElement("tr");
  row.innerHTML = `
    <td>${carNo || "-"}</td>
    <td>${startTime || "-"}</td>
    <td>${log.estimated_arrival_time || log.eta_time || log.arrival_time || "-"}</td>
    <td>${log.start_location || log.origin || "-"}</td>
    <td>${log.destination || log.dest || "-"}</td>
  `;
  if (table.firstChild) {
    table.insertBefore(row, table.firstChild);
  } else {
    table.appendChild(row);
  }
}

// ✅ 출발 알림 이벤트
socket.on("ambulance_start", function (data) {
  console.log("📥 ambulance_start:", data);
  updateOrPrependLogRow(data);
});

// ✅ 도착 알림 이벤트
socket.on("ambulance_arrival", function (data) {
  console.log("📥 ambulance_arrival:", data);
  updateOrPrependLogRow(data);
});
