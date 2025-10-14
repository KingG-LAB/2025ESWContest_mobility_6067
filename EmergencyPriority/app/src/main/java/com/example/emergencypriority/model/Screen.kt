package com.example.emergencypriority.model

sealed class Screen(val route: String) {
    data object Favorite : Screen("favorite")
    data object Route : Screen("route")
    data object ManageFavorites : Screen("manageFavorites")
    data object Settings : Screen("settings")
}
