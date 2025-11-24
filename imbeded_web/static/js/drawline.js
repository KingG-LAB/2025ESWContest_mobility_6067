var DrawLine = DrawLine || {};

DrawLine.map = null;
DrawLine.vehicleMarker = null;
DrawLine.routeLines = []; // 🛣️ 라인 저장 배열
DrawLine.carMarker = null;  // 🚘 일반 차량 마커 저장

// 🔑 Tmap API Key
const TMAP_APP_KEY = "";

$(function () {
    DrawLine.initMap();

    // 👉 지도 초기화 후 약간 딜레이를 두고 소켓 연결
    setTimeout(() => {
        // ✅ 전역 socket 대신, 이 모듈에서만 socket 생성
        const socket = io("http://localhost:5001", {
            transports: ["websocket"]
        });

        socket.on("connect", () => {
            console.log("✅ drawline.js 소켓 연결됨");
        });

        socket.on("disconnect", () => {
            console.warn("❌ drawline.js 소켓 끊김");
        });
        
        // 🚘 일반 차량 현재 위치 이벤트
        socket.on("normalcar_current", function (data) {
            console.log("🚘 일반 차량 위치 수신:", data);
            if (data.current) {
                DrawLine.updateCarMarker(data.current.lat, data.current.lng);
            }
        });
        

        // 🚑 경로 이벤트
        socket.on("ambulance_route", function (data) {
            console.log("🚑 경로 데이터 수신:", data);
            if (data.route_points && data.route_points.length > 0) {
                var linePoints = data.route_points.map(
                    p => new Tmapv2.LatLng(p.lat, p.lng)
                );
                DrawLine.drawLine(linePoints, "#0000FF");
                DrawLine.setMapBound(linePoints);
            }
        });

        // 🚘 현재 위치 이벤트
        socket.on("ambulance_current", function (data) {
            console.log("🚘 현재 위치 수신:", data);
            if (data.current) {
                DrawLine.updateVehicleMarker(data.current.lat, data.current.lng);
            }
        });

        // 🏁 도착 알림 이벤트
        socket.on("ambulance_arrival", function (data) {
            console.log("🏁 도착 알림 수신:", data);

            // 🚗 차량 마커 제거
            if (DrawLine.vehicleMarker) {
                DrawLine.vehicleMarker.setMap(null);
                DrawLine.vehicleMarker = null;
            }

            // 🛣️ 라인 제거
            if (DrawLine.routeLines && DrawLine.routeLines.length > 0) {
                DrawLine.routeLines.forEach(line => line.setMap(null));
                DrawLine.routeLines = [];
            }

            console.log(`🏁 ${data.dest} 도착 완료 🚑`);
        });
    }, 500); // 지도 초기화 후 0.5초 뒤 소켓 연결
});

// -------------------------------
// ✅ 지도 초기화
// -------------------------------
DrawLine.initMap = function () {
    DrawLine.map = new Tmapv2.Map("map_div", {
        width: "100%",
        height: "500px",
        zoomControl: true,
        scrollwheel: true,
    });
};

// -------------------------------
// ✅ 차량 마커 업데이트
// -------------------------------
DrawLine.updateVehicleMarker = function (lat, lng) {
    if (!DrawLine.map) {
        console.warn("⚠️ 지도 객체가 아직 초기화되지 않았습니다.");
        return;
    }

    if (!DrawLine.vehicleMarker) {
        // 🚑 최초 생성
        DrawLine.vehicleMarker = new Tmapv2.Marker({
            position: new Tmapv2.LatLng(lat, lng),
            icon: "/static/images/ambulance.png",
            iconSize: new Tmapv2.Size(30, 30),
            map: DrawLine.map,
        });
    } else {
        if (!DrawLine.vehicleMarker.getMap()) {
            DrawLine.vehicleMarker.setMap(DrawLine.map);
        }
        if (lat && lng) {
            try {
                DrawLine.vehicleMarker.setPosition(new Tmapv2.LatLng(lat, lng));
            } catch (e) {
                console.warn("⚠️ 마커 업데이트 실패:", e);
            }
        }
    }
};



// ✅ 일반 차량 마커 업데이트
DrawLine.updateCarMarker = function (lat, lng) {
    if (!DrawLine.map) {
        console.warn("⚠️ 지도 객체가 아직 초기화되지 않았습니다.");
        return;
    }

    if (!DrawLine.carMarker) {
        // 🚘 최초 생성
        DrawLine.carMarker = new Tmapv2.Marker({
            position: new Tmapv2.LatLng(lat, lng),
            icon: "/static/images/car.png",   // 일반 차량 아이콘
            iconSize: new Tmapv2.Size(40, 40),
            map: DrawLine.map,
        });
    } else {
        if (!DrawLine.carMarker.getMap()) {
            DrawLine.carMarker.setMap(DrawLine.map);
        }
        if (lat && lng) {
            try {
                DrawLine.carMarker.setPosition(new Tmapv2.LatLng(lat, lng));
            } catch (e) {
                console.warn("⚠️ 일반 차량 마커 업데이트 실패:", e);
            }
        }
    }
};


// -------------------------------
// ✅ 라인 그리기 (Polyline 저장)  
// -------------------------------
DrawLine.drawLine = function (pointList, lineColor) {
    if (!pointList || pointList.length < 2) return;
    var polyline = new Tmapv2.Polyline({
        path: pointList,
        strokeColor: lineColor,
        strokeWeight: 6,
        map: DrawLine.map,
    });
    DrawLine.routeLines.push(polyline); // 🛣️ 라인 저장
};

// -------------------------------
// ✅ 지도 화면 맞춤
// -------------------------------
DrawLine.setMapBound = function (pointList) {
    if (!pointList || pointList.length < 2) return;
    var bounds = new Tmapv2.LatLngBounds();
    pointList.forEach(p => bounds.extend(p));
    DrawLine.map.panToBounds(bounds);
};

// --------------------------------------
// ✅ (옵션) 도로 매칭 함수 – GPS 좌표 보정용
// --------------------------------------
DrawLine.matchToRoad = function (lat, lng, callback) {
    var coordStr = lng + "," + lat;
    $.ajax({
        type: "POST",
        url: "https://apis.openapi.sk.com/tmap/road/matchToRoads500?version=1",
        headers: { appKey: TMAP_APP_KEY },
        contentType: "application/x-www-form-urlencoded",
        data: {
            responseType: "1",
            coords: coordStr,
        },
        success: function (response) {
            if (response.resultData && response.resultData.matchedPoints.length > 0) {
                var loc = response.resultData.matchedPoints[0].matchedLocation;
                if (loc) callback(loc.latitude, loc.longitude);
            } else {
                callback(lat, lng);
            }
        },
        error: function (xhr, status, error) {
            console.error("❌ 도로 매칭 실패:", status, error);
            callback(lat, lng);
        },
    });
};
