package com.example.gestion_salle_de_jeux.ui.gameroom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.GameSession
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.PaymentStatus
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.SessionStatus

class GameRoomViewModel : ViewModel() {

    // TODO: Injecter un Repository pour obtenir les vraies sessions

    private val _gameSessions = MutableLiveData<List<GameSession>>()
    val gameSessions: LiveData<List<GameSession>> = _gameSessions

    init {
        loadGameSessions()
    }

    private fun loadGameSessions() {
        _gameSessions.value = createDummySessions()
    }

    private fun createDummySessions(): List<GameSession> {
        return listOf(
            GameSession(
                id = "1",
                postName = "Poste 1",
                consoleName = "PS5",
                gameName = "PES",
                players = "Davide - Laura",
                matchDetails = "4 * 500 Ar = 2000 Ar",
                timeRemaining = "...",
                paymentStatus = PaymentStatus.PAID,
                sessionStatus = SessionStatus.ONLINE,
                isPaused = true
            ),
            GameSession(
                id = "2",
                postName = "Poste 2",
                consoleName = "PS3",
                gameName = "COD",
                players = "Mainty - Nekena",
                matchDetails = "10 : 40 min → 401 Ar",
                timeRemaining = "...",
                paymentStatus = PaymentStatus.UNPAID,
                sessionStatus = SessionStatus.WARNING,
                isPaused = false
            ),
            GameSession(
                id = "3",
                postName = "Poste 3",
                consoleName = "PS2",
                gameName = "COD",
                players = "Mainty - Nekena",
                matchDetails = "10 : 40 min → 401 Ar",
                timeRemaining = "...",
                paymentStatus = PaymentStatus.UNPAID,
                sessionStatus = SessionStatus.ERROR,
                isPaused = true
            )
        )
    }

    // TODO: Ajouter les fonctions appelées par l'adapter
    fun onPlayPauseClicked(session: GameSession) {
        // Logique pour mettre en pause / reprendre
    }

    fun onStopClicked(session: GameSession) {
        // Logique pour arrêter
    }

    fun onAddTimeClicked(session: GameSession) {
        // Logique pour ajouter du temps
    }

    fun onPaymentClicked(session: GameSession) {
        // Logique pour marquer comme payé
    }
}