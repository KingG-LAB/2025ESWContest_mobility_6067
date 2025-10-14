package com.example.emergencypriority

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.emergencypriority.model.Location
import com.example.emergencypriority.model.RouteState
import com.example.emergencypriority.model.Screen
import com.example.emergencypriority.ui.screens.*
import com.example.emergencypriority.socket.SocketManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                App(this)
            }
        }
    }
}

@Composable
fun App(context: ComponentActivity) {
    val nav = rememberNavController()

    // ✅ Location 객체로 즐겨찾기 초기화
    val favorites = remember {
        mutableStateListOf(
            Location("중앙대학교병원", "37.506699", "126.960601")
        )
    }
    var route by remember { mutableStateOf(RouteState()) }

    var ip by remember { mutableStateOf("10.0.2.2") }
    var carNumber by remember { mutableStateOf("119다 119") }

    NavHost(navController = nav, startDestination = Screen.Favorite.route) {

        // ---------- 홈 화면 ----------
        composable(Screen.Favorite.route) {
            HomeScreen(
                favorites = favorites,
                onPick = { loc ->                // ✅ Location 단위로 받음
                    route = route.copy(location = loc)
                    nav.navigate(Screen.Route.route)
                },
                onAddFavorite = { loc ->          // ✅ Location 단위로 추가
                    if (loc.name.isNotBlank() && favorites.none { it.name == loc.name }) {
                        favorites.add(loc)
                    }
                },
                onRemoveFavorite = { name ->
                    favorites.removeAll { it.name == name }
                },
                onSearch = { loc ->               // ✅ Location 단위로 검색 처리
                    route = route.copy(location = loc)
                    nav.navigate(Screen.Route.route)
                },
                onManageFavorites = { nav.navigate(Screen.ManageFavorites.route) },
                onSettings = { nav.navigate(Screen.Settings.route) },
                onBackHome = {
                    nav.navigate(Screen.Favorite.route) {
                        popUpTo(Screen.Favorite.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ---------- 목적지 전송 화면 ----------
        composable(Screen.Route.route) {
            RouteScreen(
                route = route,
                isAddVisible = route.location?.let { loc ->
                    favorites.none { it.name == loc.name }
                } ?: false,
                onSendRequest = { loc, callback ->   // ✅ Location 단위로 전송
                    val socket = SocketManager(
                        serverIp = ip,
                        serverPort = 6000
                    )

                    val payload = """
                        {
                            "car": "$carNumber",
                            "dest": "${loc.name}",
                            "lat": "${loc.lat}",
                            "lng": "${loc.lng}"
                        }
                    """.trimIndent()

                    socket.sendMessage(
                        message = payload,
                        onSent = { success, response ->
                            if (success) {
                                println("🚑 소켓 전송 성공 -> 서버=$ip, 차량=$carNumber, 목적지=${loc.name} (${loc.lat},${loc.lng})")
                                callback(true, response)
                            } else {
                                println("❌ 소켓 전송 실패 -> 서버=$ip")
                                callback(false, response)
                            }
                        },
                        onError = { e ->
                            e.printStackTrace()
                            callback(false, e.message ?: "서버 응답 없음")
                        }
                    )
                },
                onAddFavorite = { loc ->
                    if (loc.name.isNotBlank() && favorites.none { it.name == loc.name }) {
                        favorites.add(loc)
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        // ---------- 즐겨찾기 관리 ----------
        composable(Screen.ManageFavorites.route) {
            ManageFavoritesScreen(
                favorites = favorites,
                onAddFavorite = { loc ->
                    if (loc.name.isNotBlank() && favorites.none { it.name == loc.name }) {
                        favorites.add(loc)
                    }
                },
                onRemoveFavorite = { name -> favorites.removeAll { it.name == name } },
                onBack = { nav.popBackStack() }
            )
        }

        // ---------- 설정 화면 ----------
        composable(Screen.Settings.route) {
            SettingsScreen(
                ip = ip,
                carNumber = carNumber,
                onIpChange = { newIp -> ip = newIp },
                onCarNumberChange = { newCar -> carNumber = newCar },
                onBack = { nav.popBackStack() }
            )
        }
    }
}
