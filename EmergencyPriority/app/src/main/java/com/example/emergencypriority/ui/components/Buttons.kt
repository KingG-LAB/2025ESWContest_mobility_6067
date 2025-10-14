package com.example.emergencypriority.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

/**
 * 커스텀 큰 버튼 UI 컴포넌트
 *
 * - Material3의 기본 Button을 쓰지 않고
 *   Box와 Modifier를 조합해서 직접 디자인한 버튼.
 * - 너비, 높이, 배경색(bg), hover 색상(예비값), 텍스트를
 *   외부에서 자유롭게 지정 가능.
 * - 주로 메인 액션(예: 요청 보내기, 즐겨찾기 추가 등)에 사용.
 *
 * @param text 버튼 안에 표시할 문자열
 * @param bg 버튼의 배경 색상
 * @param hover 사용자가 눌렀을 때 보여줄 예정인 색상 (현재는 미사용, 확장 가능)
 * @param width 버튼의 너비 (기본값: 300dp)
 * @param height 버튼의 높이 (기본값: 72dp)
 * @param onClick 버튼이 클릭되었을 때 실행할 동작(콜백 함수)
 */
@Composable
fun PrimaryBigButton(
    text: String,
    bg: Color,
    hover: Color,
    width: Dp = 300.dp,
    height: Dp = 72.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            // 버튼의 너비와 높이를 설정
            .width(width)
            .height(height)
            // 모서리를 둥글게 잘라내기 (22dp 곡률)
            .clip(RoundedCornerShape(22.dp))
            // 배경색을 지정 (bg 파라미터로 전달된 색상)
            .background(bg)
            // 클릭 이벤트 연결 (onClick 실행)
            .clickable { onClick() }
            // 버튼 내부 여백 (좌우 16dp)
            .padding(horizontal = 16.dp),
        // Box 안의 콘텐츠(텍스트)를 중앙에 배치
        contentAlignment = Alignment.Center
    ) {
        // 버튼 안에 표시될 텍스트
        Text(
            text = text,            // 외부에서 전달받은 문자열
            color = Color.White,    // 텍스트 색상: 흰색
            fontSize = 22.sp,       // 글씨 크기: 22sp
            fontWeight = FontWeight.Bold // 글씨 굵기: 굵게
        )
    }
}
