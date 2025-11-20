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

        // Affichage dynamique du poste (si on a plusieurs PS4, on ne sait pas laquelle c'est exactement
        // sans une table physique distincte, mais on affiche le nom générique)
        return GameSession(
            id = jeu.id.toString(),
            postName = "Poste", // Ou materiel?.id pour l'ID unique de la session
            consoleName = materiel?.nom ?: "Inconnu",
            gameName = jeu.titre,
            players = playeur?.nom ?: "Inconnu",
            matchDetails = "${jeu.nombre_tranches} tranches • ${jeu.montant_total.toInt()} Ar",
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

    // --- ACTIONS UTILISATEUR ---

    fun onStopClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                // 1. Terminer la session
                jeuxDao.updateJeux(jeu.copy(est_termine = true))

                // 2. Libérer UNE unité de matériel (Décrémenter)
                val materiel = materielDao.getMaterielById(jeu.id_materiel)
                if (materiel != null) {
                    // On s'assure de ne pas descendre en dessous de 0
                    val nouvelUsage = if (materiel.quantite_utilise > 0) materiel.quantite_utilise - 1 else 0

                    // Remettre id_reserve à 0 n'est plus pertinent ici si on gère par quantité,
                    // mais on peut le laisser à 0 pour dire "pas totalement plein" si on veut.
                    // Le plus important est quantite_utilise.
                    materielDao.update(materiel.copy(quantite_utilise = nouvelUsage))
                }
            }
        }
    }

    fun onPaymentClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                val newStatus = !jeu.est_paye
                jeuxDao.updateJeux(jeu.copy(est_paye = newStatus))
            }
        }
    }

    fun onPlayPauseClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                val newPauseState = !jeu.est_en_pause
                jeuxDao.updateJeux(jeu.copy(est_en_pause = newPauseState))
            }
        }
    }

    fun onAddTimeClicked(session: GameSession) {
        // À implémenter plus tard
    }

    class Factory(private val jeuxDao: JeuxDao, private val materielDao: MaterielDao, private val playeurDao: PlayeurDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameRoomViewModel::class.java)) return GameRoomViewModel(jeuxDao, materielDao, playeurDao) as T
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}