package com.example.gestion_salle_de_jeux.ui.history.model

data class HistoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val iconType: HistoryIconType
)

enum class HistoryIconType {
    POWER_OUTAGE,
    GAME_ROOM,
    FINANCE,
    MATERIAL_EXPENSE
}