package com.example.emergencypriority.network

import com.example.emergencypriority.model.PoiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TmapService {
    @GET("tmap/pois")
    suspend fun searchPoi(
        @Header("appKey") appKey: String = "73xHlMiaGI39dgyBwYeO55jUPwFiKn4027JN3ntC",
        @Query("version") version: Int = 1,
        @Query("format") format: String = "json",
        @Query("searchKeyword") keyword: String,
        @Query("resCoordType") resCoordType: String = "WGS84GEO",
        @Query("reqCoordType") reqCoordType: String = "WGS84GEO",
        @Query("count") count: Int = 10
    ): PoiResponse
}
