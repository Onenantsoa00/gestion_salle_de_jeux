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

    // Données internes
    val rawStartTime: Long = 0,
    val rawDurationMinutes: Long = 0,
    val rawPauseStartTime: Long = 0,
    val rawTotalPauseDuration: Long = 0,

    // NOUVEAU : État de l'alarme
    val hasSounded: Boolean = false
)

enum class PaymentStatus { PAID, UNPAID }
enum class SessionStatus { ONLINE, WARNING, ERROR }