package com.example.emergencypriority.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencypriority.model.Location   // ✅ Location import
import com.example.emergencypriority.viewmodel.TmapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFavoritesScreen(
    favorites: List<Location>,                // ✅ String → Location
    onAddFavorite: (Location) -> Unit,        // ✅ Location 단위로 추가
    onRemoveFavorite: (String) -> Unit,       // 이름으로 삭제
    onBack: () -> Unit,
    viewModel: TmapViewModel = viewModel()
) {
    var input by remember { mutableStateOf("") }
    val results by viewModel.results.collectAsState(initial = emptyList())
    val errorMessage by viewModel.errorMessage.collectAsState()   // ✅ 에러 상태 구독

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(64.dp))
        // 🔍 입력창
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            placeholder = { Text("즐겨찾기 추가") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                if (input.isNotBlank()) {
                    viewModel.search(input) // ✅ API 검색 실행
                }
            }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 🔍 검색 결과 리스트
        if (results.isNotEmpty()) {
            Text("검색 결과:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp) // ✅ 검색 결과 최대 높이 제한
            ) {
                items(results.take(6)) { poi ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(poi.name, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f)) // 🔥 버튼 오른쪽 끝으로 밀기
                            Button(onClick = {
                                // ✅ Poi → Location 변환해서 추가
                                onAddFavorite(Location(poi.name, poi.noorLat, poi.noorLon))
                                input = ""              // 입력창 비우기
                                viewModel.clearResults() // 검색결과 지우기
                            }) {
                                Text("추가")
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ⭐ 즐겨찾기 리스트
        Text("⭐ 즐겨찾기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // ✅ 화면 꽉 차면 스크롤
        ) {
            items(favorites) { place ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(place.name, fontWeight = FontWeight.Bold)   // ✅ 이름만 출력
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { onRemoveFavorite(place.name) }) {
                            Text("삭제")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))   // 🔼 기존 16dp → 더 크게 (예: 48dp)

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp) // 🔼 버튼 자체를 살짝 위로 띄우기
        ) {
            Text("← 돌아가기")
        }

    }

    // ✅ 에러 다이얼로그 추가
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("확인")
                }
            },
            title = { Text("❌ 오류") },
            text = { Text(errorMessage ?: "") }
        )
    }
}
