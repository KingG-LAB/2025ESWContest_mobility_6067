package com.example.emergencypriority.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * 설정 화면 (SettingsScreen)
 *
 * - 현재 저장된 IP 주소와 차량 번호 표시
 * - 새로운 값을 입력 후 엔터(완료) 누르면 갱신됨
 * - 갱신 시 "변경 완료" 다이얼로그 표시
 * - "돌아가기" 버튼 제공
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    ip: String,
    carNumber: String,
    onIpChange: (String) -> Unit,
    onCarNumberChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var ipInput by remember { mutableStateOf("") }          // IP 입력 상태
    var carInput by remember { mutableStateOf("") }         // 차량번호 입력 상태
    var showDialog by remember { mutableStateOf<String?>(null) } // 변경 완료 메시지

    Scaffold(
        topBar = { TopAppBar(title = { Text("설정") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 현재 저장된 IP
            Text("현재 IP 주소: $ip", style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = ipInput,
                onValueChange = { ipInput = it },
                placeholder = { Text("새 IP 주소 입력") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (ipInput.isNotBlank()) {
                        onIpChange(ipInput)
                        showDialog = "IP 주소가 변경되었습니다."
                        ipInput = ""
                    }
                }),
                modifier = Modifier.fillMaxWidth()
            )

            // 현재 차량 번호
            Text("현재 차량 번호: $carNumber", style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = carInput,
                onValueChange = { carInput = it },
                placeholder = { Text("새 차량 번호 입력") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (carInput.isNotBlank()) {
                        onCarNumberChange(carInput)
                        showDialog = "차량 번호가 변경되었습니다."
                        carInput = ""
                    }
                }),
                modifier = Modifier.fillMaxWidth()
            )

            // 돌아가기 버튼
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("← 돌아가기")
            }
        }

        // ✅ 변경 완료 다이얼로그
        showDialog?.let { message ->
            AlertDialog(
                onDismissRequest = { showDialog = null },
                title = { Text("변경 완료") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { showDialog = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
