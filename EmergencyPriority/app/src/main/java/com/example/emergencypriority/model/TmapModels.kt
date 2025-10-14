// TmapModels.kt
package com.example.emergencypriority.model

data class Poi(
    val name: String,
    val noorLat: String,
    val noorLon: String
)

data class PoiResponse(val searchPoiInfo: SearchPoiInfo)
data class SearchPoiInfo(val pois: Pois)
data class Pois(val poi: List<Poi>)
