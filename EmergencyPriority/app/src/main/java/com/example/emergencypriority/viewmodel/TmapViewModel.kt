package com.example.emergencypriority.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencypriority.network.TmapService
import com.example.emergencypriority.model.Poi     // ✅ model에서 import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class TmapViewModel : ViewModel() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://apis.openapi.sk.com/") // Tmap API base url
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(TmapService::class.java)

    private val _results = MutableStateFlow<List<Poi>>(emptyList())
    val results: StateFlow<List<Poi>> = _results

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearResults() {
        _results.value = emptyList()
    }

    // ✅ 에러 메시지 초기화 함수 추가
    fun clearError() {
        _errorMessage.value = null
    }

    fun search(keyword: String) {
        viewModelScope.launch {
            try {
                val response = service.searchPoi(keyword = keyword)
                _results.value = response.searchPoiInfo.pois.poi
                _errorMessage.value = null // 성공 시 에러 초기화
            } catch (e: Exception) {
                e.printStackTrace()
                _results.value = emptyList()
                _errorMessage.value = "API 요청 실패: ${e.message ?: "알 수 없는 오류"}"
            }
        }
    }
}
