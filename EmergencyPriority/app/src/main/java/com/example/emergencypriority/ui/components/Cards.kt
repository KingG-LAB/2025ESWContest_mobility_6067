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
 * 병원/목적지 등을 카드 형태로 보여주는 UI 컴포넌트
 *
 * - 즐겨찾기 목록, 직접 입력 버튼, 병원 선택 카드 등에서 사용 가능
 * - 기본 버튼보다 넓은 공간을 차지하며, 카드 형태로 배치
 * - 클릭 이벤트(onClick)를 받아 목적지를 선택하거나 추가하는데 활용
 *
 * @param text 카드 안에 표시될 문자열 (병원명 등)
 * @param bg 카드의 배경 색상
 * @param hover 클릭되었을 때/호버 상태의 색상 (현재는 미사용, 확장 가능)
 * @param minWidth 최소 너비 (기본 180dp)
 * @param maxWidth 최대 너비 (기본 360dp)
 * @param minHeight 최소 높이 (기본 84dp)
 * @param onClick 카드 클릭 시 실행할 동작
 */
@Composable
fun HospitalCard(
    text: String,
    bg: Color,
    hover: Color,
    minWidth: Dp = 180.dp,
    maxWidth: Dp = 360.dp,
    minHeight: Dp = 84.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            // 최소/최대 너비 제한
            .widthIn(min = minWidth, max = maxWidth)
            // 최소 높이 제한
            .heightIn(min = minHeight)
            // 모서리를 둥글게 (20dp 곡률)
            .clip(RoundedCornerShape(20.dp))
            // 카드 배경색 지정
            .background(bg)
            // 클릭 이벤트 처리
            .clickable { onClick() }
            // 내부 여백 (좌우 20dp, 위아래 18dp)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        // 텍스트는 카드의 왼쪽 정렬 (시작 위치)
        contentAlignment = Alignment.CenterStart
    ) {
        // 카드 내부의 텍스트
        Text(
            text = text,                 // 카드에 표시할 텍스트
            color = Color.White,         // 글씨 색상 (흰색)
            fontSize = 24.sp,            // 글씨 크기
            fontWeight = FontWeight.SemiBold // 글씨 굵기: SemiBold
        )
    }
}
