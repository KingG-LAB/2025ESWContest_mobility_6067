package com.example.emergencypriority.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 입력을 받을 수 있는 다이얼로그 컴포넌트
 *
 * - 즐겨찾기 추가, 도착지 입력 등에 사용
 * - TextField와 확인/취소 버튼을 포함
 *
 * @param title 다이얼로그 제목
 * @param placeholder 입력창 힌트 텍스트
 * @param icon 제목 왼쪽에 붙일 아이콘 텍스트 (예: "⭐" or "🔎")
 * @param onDismiss 다이얼로그 닫기 시 실행되는 동작
 * @param onConfirm 입력값을 전달하는 콜백
 */
@Composable
fun NiceInputDialog(
    title: String,
    placeholder: String,
    icon: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // 입력된 텍스트를 상태로 관리
    var text by remember { mutableStateOf("") }
    // 포커스를 제어하기 위한 객체
    val focus = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss, // 바깥 클릭/취소 시 닫기
        title = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                // 제목 앞에 아이콘 표시
                Text(icon, fontSize = 26.sp)
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            // 입력 필드
            OutlinedTextField(
                value = text,
                onValueChange = { text = it }, // 값이 변경될 때마다 상태 업데이트
                placeholder = { Text(placeholder) }, // 힌트 텍스트
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done // 엔터를 "완료"로 표시
                ),
                keyboardActions = KeyboardActions(onDone = {
                    // 완료 버튼 누르면 포커스 해제 후 확인 콜백 호출
                    focus.clearFocus()
                    if (text.isNotBlank()) onConfirm(text)
                }),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            // "확인" 버튼
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text) }) {
                Text("확인")
            }
        },
        dismissButton = {
            // "취소" 버튼
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

/**
 * 단순 알림 다이얼로그 (OK 버튼만 있음)
 *
 * - 요청 전송 완료, 즐겨찾기 추가 완료 등 알림용
 *
 * @param message 표시할 메시지
 * @param onDismiss 닫기 콜백
 */
@Composable
fun SuccessDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50) // 초록색
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("확인", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("완료", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(message, fontSize = 16.sp)
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White // 연한 초록 배경
    )
}
@Composable
fun InfoDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, // 외부 클릭/뒤로 가기 시 닫기
        title = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                // 왼쪽에 강조용 박스와 느낌표
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF4ECDC4)),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(12.dp))
                Text(message, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            // OK 버튼 (가운데 정렬)
            Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                    Text("OK", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    )
}
