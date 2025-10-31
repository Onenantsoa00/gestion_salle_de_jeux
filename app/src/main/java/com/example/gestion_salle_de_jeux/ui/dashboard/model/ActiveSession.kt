package com.example.gestion_salle_de_jeux.ui.dashboard.model

data class ActiveSession(
    val id: String,
    val postName: String, // ex: "Post 1"
    val consoleName: String, // ex: "PS5"
    val players: String, // ex: "Laura - Davida"
    val duration: String, // ex: "40 : 50 min"
    val paymentInfo: String, // ex: "2 000 Ariary - Payé"
    val status: SessionStatus,
    val isPaused: Boolean
)

enum class SessionStatus {
    ONLINE, // Vert
    BUSY,   // Jaune
    OFFLINE // Rouge
}