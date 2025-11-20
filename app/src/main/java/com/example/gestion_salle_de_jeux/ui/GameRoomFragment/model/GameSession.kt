package com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model

data class GameSession(
    val id: String,
    val postName: String,
    val consoleName: String,
    val gameName: String,
    val players: String,
    val matchDetails: String,
    val timeRemaining: String,
    val paymentStatus: PaymentStatus,
    val sessionStatus: SessionStatus,
    val isPaused: Boolean,

    // Nouveaux champs pour le calcul (non affichés directement)
    val rawStartTime: Long = 0,
    val rawDurationMinutes: Long = 0
)

enum class PaymentStatus { PAID, UNPAID }
enum class SessionStatus { ONLINE, WARNING, ERROR }