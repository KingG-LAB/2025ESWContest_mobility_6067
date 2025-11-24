package com.example.emergencypriority.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencypriority.model.RouteState
import com.example.emergencypriority.model.Location
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.json.JSONObject

@Composable
fun RouteScreen(
    route: RouteState,
    isAddVisible: Boolean,
    onSendRequest: (Location, (Boolean, String) -> Unit) -> Unit,
    onAddFavorite: (Location) -> Unit,
    onBack: () -> Unit
) {
    var showSending by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showApiError by remember { mutableStateOf(false) }
    var showSocketError by remember { mutableStateOf(false) }
    var responseMessage by remember { mutableStateOf("") }
    var showAdded by remember { mutableStateOf(false) }
    var attempts by remember { mutableStateOf(0) }

    // ✅ 응답에서 파싱된 거리 / 시간 상태
    var distance by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
 
    val coroutineScope = rememberCoroutineScope()
    val location = route.location

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ---------- 경로 정보 ----------
        Text(
            text = location?.name ?: "목적지 없음",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))

        // ✅ 거리/소요시간 응답 있으면 표시
        if (distance.isNotEmpty() && duration.isNotEmpty()) {
            Text("거리: $distance", fontSize = 20.sp)
            Text("소요시간: $duration", fontSize = 20.sp)
        }

        Spacer(Modifier.height(32.dp))

        // ---------- 요청 보내기 버튼 ----------
        Button(
            onClick = {
                if (location == null) return@Button

                showSending = true
                showSuccess = false
                showApiError = false
                showSocketError = false
                attempts = 0

                fun trySend() {
                    attempts++
                    onSendRequest(location) { success, response ->
                        if (success) {
                            try {
                                val json = JSONObject(response)
                                if (json.optString("status") == "success") {
                                    distance = json.optString("distance")
                                    duration = json.optString("duration")
                                    responseMessage = "전송 성공 ✅"
                                    showSuccess = true
                                } else {
                                    // 🔥 API 실패 처리
                                    val errorRaw = json.optString("error", "알 수 없는 오류")
                                    var errorMsg = errorRaw
                                    try {
                                        val errorJson = JSONObject(errorRaw)
                                        errorMsg = errorJson.optString("msg", errorRaw)
                                    } catch (_: Exception) {
                                        // 그냥 문자열로 출력
                                    }
                                    responseMessage = "API 요청 실패 ❌\n$errorMsg"
                                    showApiError = true
                                }
                            } catch (e: Exception) {
                                responseMessage = "응답 파싱 실패 ❌\n${e.message}"
                                showApiError = true
                            }
                            showSending = false
                        } else {
                            // ✅ 소켓 자체 실패 → 재시도 3회
                            if (attempts < 3) {
                                coroutineScope.launch {
                                    delay(2000)
                                    trySend()
                                }
                            } else {
                                responseMessage = "서버 연결 실패 ❌ (3회 재시도 실패)"
                                showSocketError = true
                                showSending = false
                            }
                        }
                    }
                }
                trySend()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
        ) {
            Text("🚨 요청 보내기", color = Color.White)
        }

        // ---------- 즐겨찾기 추가 버튼 ----------
        if (isAddVisible && location != null) {
            Button(
                onClick = {
                    onAddFavorite(location)
                    showAdded = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4))
            ) {
                Text("⭐ 즐겨찾기에 추가", color = Color.White)
            }
        }

        // ---------- 돌아가기 버튼 ----------
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("← 돌아가기", color = Color.White)
        }

        // ---------- 다이얼로그 ----------
        if (showSending) {
            AlertDialog(
                onDismissRequest = { showSending = false },
                title = { Text("📡 요청 보내는 중...") },
                text = { Text("시도 횟수: $attempts / 3") },
                confirmButton = {
                    TextButton(onClick = { showSending = false }) {
                        Text("취소")
                    }
                }
            )
        }

        if (showSuccess) {
            AlertDialog(
                onDismissRequest = { showSuccess = false },
                title = { Text("✅ 전송 완료") },
                text = { Text(responseMessage) },
                confirmButton = {
                    TextButton(onClick = { showSuccess = false }) {
                        Text("확인")
                    }
                }
            )
        }

        if (showApiError) {
            AlertDialog(
                onDismissRequest = { showApiError = false },
                title = { Text("❌ API 오류") },
                text = { Text(responseMessage) },
                confirmButton = {
                    TextButton(onClick = { showApiError = false }) {
                        Text("확인")
                    }
                }
            )
        }

        if (showSocketError) {
            AlertDialog(
                onDismissRequest = { showSocketError = false },
                title = { Text("📡 서버 연결 실패") },
                text = { Text(responseMessage) },
                confirmButton = {
                    TextButton(onClick = { showSocketError = false }) {
                        Text("확인")
                    }
                }
            )
        }

        if (showAdded) {
            AlertDialog(
                onDismissRequest = { showAdded = false },
                title = { Text("⭐ 즐겨찾기 추가") },
                text = { Text("${location?.name ?: "알 수 없는 위치"} 추가 완료") },
                confirmButton = {
                    TextButton(onClick = { showAdded = false }) {
                        Text("확인")
                    }
                }
            )
        }
    }
}
