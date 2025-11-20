package com.example.gestion_salle_de_jeux.ui.gameroom

import androidx.lifecycle.*
import com.example.gestion_salle_de_jeux.data.dao.JeuxDao
import com.example.gestion_salle_de_jeux.data.dao.MaterielDao
import com.example.gestion_salle_de_jeux.data.dao.PlayeurDao
import com.example.gestion_salle_de_jeux.data.entity.Finance
import com.example.gestion_salle_de_jeux.data.entity.Jeux
import com.example.gestion_salle_de_jeux.data.repository.FinanceRepository
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.GameSession
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.PaymentStatus
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

class GameRoomViewModel(
    private val jeuxDao: JeuxDao,
    private val materielDao: MaterielDao,
    private val playeurDao: PlayeurDao,
    private val financeRepository: FinanceRepository
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
                delay(1000) // Rafraîchissement chaque seconde
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
            matchDetails = "${jeu.nombre_tranches} tranches • ${jeu.montant_total.toInt()} Ar",
            timeRemaining = "", // Sera calculé
            paymentStatus = if (jeu.est_paye) PaymentStatus.PAID else PaymentStatus.UNPAID,
            sessionStatus = SessionStatus.ONLINE,

            // Gestion Pause
            isPaused = jeu.est_en_pause,
            rawStartTime = jeu.timestamp_debut,
            rawDurationMinutes = jeu.duree_totale_prevue,
            rawPauseStartTime = jeu.timestamp_pause_debut,
            rawTotalPauseDuration = jeu.duree_cumulee_pause
        )
    }

    private fun updateTimeForSession(session: GameSession): GameSession {
        // 1. Calcul de l'heure de fin théorique (Début + Durée + Pauses passées)
        val originalEndTime = session.rawStartTime + (session.rawDurationMinutes * 60 * 1000)
        val effectiveEndTime = originalEndTime + session.rawTotalPauseDuration

        // 2. Point de référence pour le calcul
        // Si EN PAUSE : On fige le temps à l'heure où la pause a commencé
        // Si EN JEU : On utilise l'heure actuelle
        val referenceTime = if (session.isPaused) session.rawPauseStartTime else System.currentTimeMillis()

        val diff = effectiveEndTime - referenceTime
        val status: SessionStatus
        val timeText: String

        if (diff > 0) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
            timeText = String.format("%02d:%02d", minutes, seconds)

            // Warning si moins de 2 min ET que ce n'est pas en pause
            status = if (minutes < 2 && !session.isPaused) SessionStatus.WARNING else SessionStatus.ONLINE
        } else {
            timeText = "TERMINÉ"
            status = SessionStatus.ERROR
        }

        return session.copy(timeRemaining = timeText, sessionStatus = status)
    }

    // --- ACTIONS UTILISATEUR ---

    fun onPlayPauseClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                if (jeu.est_en_pause) {
                    // REPRENDRE LE JEU (Play)
                    // On calcule combien de temps on a passé en pause et on l'ajoute au cumul
                    val pauseDuration = System.currentTimeMillis() - jeu.timestamp_pause_debut
                    val newTotalPause = jeu.duree_cumulee_pause + pauseDuration

                    jeuxDao.updateJeux(jeu.copy(
                        est_en_pause = false,
                        duree_cumulee_pause = newTotalPause,
                        timestamp_pause_debut = 0 // Reset
                    ))
                } else {
                    // METTRE EN PAUSE
                    // On enregistre juste l'heure de début de pause
                    jeuxDao.updateJeux(jeu.copy(
                        est_en_pause = true,
                        timestamp_pause_debut = System.currentTimeMillis()
                    ))
                }
            }
        }
    }

    fun onStopClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                jeuxDao.updateJeux(jeu.copy(est_termine = true))
                val materiel = materielDao.getMaterielById(jeu.id_materiel)
                if (materiel != null) {
                    val usageActuel = materiel.quantite_utilise
                    val nouvelUsage = if (usageActuel > 0) usageActuel - 1 else 0
                    materielDao.update(materiel.copy(quantite_utilise = nouvelUsage))
                }
            }
        }
    }

    fun onPaymentClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null && !jeu.est_paye) {
                jeuxDao.updateJeux(jeu.copy(est_paye = true))
                val recette = Finance(
                    date_heure = Date(),
                    montant_entrant = jeu.montant_total,
                    montant_sortant = 0.0,
                    description = "Session : ${jeu.titre}",
                    source = "RECETTE"
                )
                financeRepository.insert(recette)
            }
        }
    }

    fun onAddTimeClicked(session: GameSession) {
        // À implémenter
    }

    class Factory(
        private val jeuxDao: JeuxDao,
        private val materielDao: MaterielDao,
        private val playeurDao: PlayeurDao,
        private val financeRepository: FinanceRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameRoomViewModel::class.java))
                return GameRoomViewModel(jeuxDao, materielDao, playeurDao, financeRepository) as T
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}