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
    val rawTotalPrice: Double = 0.0,

    val hasSounded: Boolean = false,

    // --- NOUVEAUX CHAMPS POUR GESTION COUPURE ---
    val isPowerCutMode: Boolean = false,
    val powerCutInfo: String = "",     // Le texte à afficher (ex: "À rendre : 200 Ar")
    val partialAmountToPay: Double = 0.0, // Le montant à verser en finance si on clique sur payer
    val partialAmountToRefund: Double = 0.0 // Le montant à rendre (informatif)
)

enum class PaymentStatus { PAID, UNPAID }
enum class SessionStatus { ONLINE, WARNING, ERROR }