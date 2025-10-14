package com.example.emergencypriority.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencypriority.model.Location   // ✅ Location import
import com.example.emergencypriority.viewmodel.TmapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    favorites: List<Location>,
    onPick: (Location) -> Unit,         // ✅ Location
    onAddFavorite: (Location) -> Unit,  // ✅ Location
    onRemoveFavorite: (String) -> Unit, // 삭제는 이름으로
    onSearch: (Location) -> Unit,       // ✅ Location
    onManageFavorites: () -> Unit,
    onSettings: () -> Unit,
    onBackHome: () -> Unit,
    viewModel: TmapViewModel = viewModel()
) {
    var search by remember { mutableStateOf("") }
    val results by viewModel.results.collectAsState(initial = emptyList())

    // ✅ HomeScreen 다시 표시될 때마다 검색결과 초기화
    LaunchedEffect(Unit) {
        search = ""
        viewModel.clearResults()
    }

    val isSearching = results.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "긴급 차량 우선 통행 시스템",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { onBackHome() },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onManageFavorites() },
                    icon = { Icon(Icons.Default.Favorite, null) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onSettings() },
                    icon = { Icon(Icons.Default.Settings, null) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 🔍 목적지 검색창
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("목적지 검색") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search   // ✅ Enter 대신 검색 버튼 보이게
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        println("✅ IME Search 눌림, 입력값=$search")  // 로그 찍기
                        viewModel.search(search)
                    },
                    onDone = {
                        println("✅ IME Done 눌림, 입력값=$search")    // 로그 찍기
                        viewModel.search(search)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // -------------------
                // 🔍 검색 결과 모드
                // -------------------
                if (isSearching) {
                    item {
                        Text(
                            "검색 결과",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(results.take(6)) { poi ->
                        val loc = Location(poi.name, poi.noorLat, poi.noorLon) // ✅ Location 변환
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
                                Spacer(modifier = Modifier.weight(1f))
                                Button(onClick = { onPick(loc) }) {   // ✅ Location 전달
                                    Text("도착")
                                }
                            }
                        }
                    }

                    // 취소 버튼
                    item {
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                search = ""
                                viewModel.clearResults()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray
                            )
                        ) {
                            Text("취소")
                        }
                    }
                }
                // -------------------
                // ⭐ 즐겨찾기 모드
                // -------------------
                else {
                    if (favorites.isNotEmpty()) {
                        item {
                            Text(
                                "⭐ 즐겨찾기",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(8.dp))
                        }
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
                                    Text(place.name, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(onClick = { onPick(place) }) {   // ✅ Location 전달
                                        Text("도착")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ✅ 여기 추가
            val errorMessage by viewModel.errorMessage.collectAsState()
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
    }
}
