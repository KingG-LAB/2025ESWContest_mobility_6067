package com.example.emergencypriority.model

// 병원, 소방서, 경찰서, 즐겨찾기 등 공통 목적지 데이터 구조
data class Location(
    val name: String,   // 장소 이름
    val lat: String,    // 위도
    val lng: String     // 경도
)
