package com.example.gestion_salle_de_jeux.ui.gameroom

import androidx.lifecycle.*
import com.example.gestion_salle_de_jeux.data.dao.JeuxDao
import com.example.gestion_salle_de_jeux.data.dao.MaterielDao
import com.example.gestion_salle_de_jeux.data.dao.PlayeurDao
import com.example.gestion_salle_de_jeux.data.entity.Jeux
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.GameSession
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.PaymentStatus
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class GameRoomViewModel(
    private val jeuxDao: JeuxDao,
    private val materielDao: MaterielDao,
    private val playeurDao: PlayeurDao
) : ViewModel() {

    private val _dbSessions = jeuxDao.getActiveSessions()
    private val _uiGameSessions = MediatorLiveData<List<GameSession>>()
    val gameSessions: LiveData<List<GameSession>> = _uiGameSessions

    init {
        startTimerLoop()

        viewModelScope.launch {
            _dbSessions.collect { sessionsBrutes ->
                val uiSessions = sessionsBrutes.map { jeu ->
                    mapEntityToUi(jeu)
                }
                _uiGameSessions.postValue(uiSessions)
            }
        }
    }

    private fun startTimerLoop() {
        viewModelScope.launch {
            while (true) {
                _uiGameSessions.value?.let { currentList ->
                    val updatedList = currentList.map { session ->
                        updateTimeForSession(session)
                    }
                    _uiGameSessions.postValue(updatedList)
                }
                delay(1000)
            }
        }
    }

    private suspend fun mapEntityToUi(jeu: Jeux): GameSession {
        val materiel = materielDao.getMaterielById(jeu.id_materiel)
        val playeur = playeurDao.getPlayeurById(jeu.id_playeur)

        return GameSession(
            id = jeu.id.toString(),
            postName = "Poste ${materiel?.id}",
            consoleName = materiel?.nom ?: "Inconnu",
            gameName = jeu.titre,
            players = playeur?.nom ?: "Inconnu",
            matchDetails = "${jeu.nombre_tranches} match(s) • Total: ${jeu.montant_total.toInt()} Ar",
            timeRemaining = "",
            paymentStatus = if (jeu.est_paye) PaymentStatus.PAID else PaymentStatus.UNPAID,
            sessionStatus = SessionStatus.ONLINE,
            isPaused = jeu.est_en_pause,
            rawStartTime = jeu.timestamp_debut,
            rawDurationMinutes = jeu.duree_totale_prevue
        )
    }

    private fun updateTimeForSession(session: GameSession): GameSession {
        if (session.isPaused) return session.copy(timeRemaining = "PAUSE")

        val now = System.currentTimeMillis()
        val endTime = session.rawStartTime + (session.rawDurationMinutes * 60 * 1000)
        val diff = endTime - now

        val status: SessionStatus
        val timeText: String

        if (diff > 0) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
            timeText = String.format("%02d:%02d", minutes, seconds)
            status = if (minutes < 2) SessionStatus.WARNING else SessionStatus.ONLINE
        } else {
            timeText = "TERMINÉ"
            status = SessionStatus.ERROR
        }

        return session.copy(timeRemaining = timeText, sessionStatus = status)
    }

    // --- MÉTHODES PUBLIQUES APPELÉES PAR LE FRAGMENT (Correctifs) ---

    fun onStopClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                // Marquer comme terminé
                jeuxDao.updateJeux(jeu.copy(est_termine = true))

                // Libérer le matériel
                val materiel = materielDao.getMaterielById(jeu.id_materiel)
                if (materiel != null) {
                    materielDao.update(materiel.copy(id_reserve = 0, quantite_utilise = 0))
                }
            }
        }
    }

    fun onPaymentClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                // Inverser le statut payé/non payé ou juste mettre à payé
                val newStatus = !jeu.est_paye
                jeuxDao.updateJeux(jeu.copy(est_paye = newStatus))
                // TODO: Ajouter une ligne dans Finance ici si newStatus est true
            }
        }
    }

    fun onPlayPauseClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                val newPauseState = !jeu.est_en_pause
                // Logique simplifiée : on met juste en pause le flag.
                // Pour un timer précis, il faudrait ajuster le timestamp_debut lors de la reprise
                // Mais pour l'instant, on gère juste l'état visuel.
                jeuxDao.updateJeux(jeu.copy(est_en_pause = newPauseState))
            }
        }
    }

    fun onAddTimeClicked(session: GameSession) {
        // Logique future pour ajouter du temps
        // Pour l'instant, on ne fait rien ou on affiche un Toast dans le fragment
    }

    class Factory(private val jeuxDao: JeuxDao, private val materielDao: MaterielDao, private val playeurDao: PlayeurDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameRoomViewModel::class.java)) return GameRoomViewModel(jeuxDao, materielDao, playeurDao) as T
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}