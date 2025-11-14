package com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model

data class GameSession(
    val id: String,
    val postName: String, // ex: "Poste 1"
    val consoleName: String, // ex: "PS5"
    val gameName: String, // ex: "PES"
    val players: String, // ex: "Davide - Laura"
    val matchDetails: String, // ex: "4 * 500 Ar = 2000 Ar"
    val timeRemaining: String, // ex: "10 : 40 min"
    val paymentStatus: PaymentStatus,
    val sessionStatus: SessionStatus,
    val isPaused: Boolean
)

enum class PaymentStatus {
    PAID,
    UNPAID
}

enum class SessionStatus {
    ONLINE, // Vert
    WARNING, // Jaune
    ERROR // Triangle d'avertissement
}