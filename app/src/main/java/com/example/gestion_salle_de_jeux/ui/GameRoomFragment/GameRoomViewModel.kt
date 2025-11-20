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

    // NOUVEAU : Gestion de l'alarme
    private val _alarmTrigger = MutableLiveData<String>() // Envoie le nom du poste qui sonne
    val alarmTrigger: LiveData<String> = _alarmTrigger

    // Pour éviter que l'alarme ne sonne en boucle toutes les secondes, on mémorise celles qui ont déjà sonné
    private val sessionsAlerted = HashSet<String>()

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
            matchDetails = "${jeu.nombre_tranches} tranches • ${jeu.montant_total.toInt()} Ar",
            timeRemaining = "",
            paymentStatus = if (jeu.est_paye) PaymentStatus.PAID else PaymentStatus.UNPAID,
            sessionStatus = SessionStatus.ONLINE,
            isPaused = jeu.est_en_pause,
            rawStartTime = jeu.timestamp_debut,
            rawDurationMinutes = jeu.duree_totale_prevue,
            rawPauseStartTime = jeu.timestamp_pause_debut,
            rawTotalPauseDuration = jeu.duree_cumulee_pause
        )
    }

    private fun updateTimeForSession(session: GameSession): GameSession {
        val now = System.currentTimeMillis()
        val originalEndTime = session.rawStartTime + (session.rawDurationMinutes * 60 * 1000)
        val effectiveEndTime = originalEndTime + session.rawTotalPauseDuration
        val referenceTime = if (session.isPaused) session.rawPauseStartTime else now
        val diff = effectiveEndTime - referenceTime

        val status: SessionStatus
        val timeText: String

        if (diff > 0) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
            timeText = String.format("%02d:%02d", minutes, seconds)
            status = if (minutes < 2 && !session.isPaused) SessionStatus.WARNING else SessionStatus.ONLINE
        } else {
            timeText = "TERMINÉ"
            status = SessionStatus.ERROR

            // CORRECTION ICI : DÉCLENCHEMENT ALARME
            // Si c'est terminé ET qu'on n'a pas encore sonné pour cette session
            if (!sessionsAlerted.contains(session.id)) {
                sessionsAlerted.add(session.id) // On marque comme "a sonné"
                _alarmTrigger.postValue(session.postName) // On déclenche l'alarme
            }
        }

        if (session.isPaused) {
            return session.copy(timeRemaining = timeText, sessionStatus = status) // On garde le temps calculé figé
        }

        return session.copy(timeRemaining = timeText, sessionStatus = status)
    }

    // --- ACTIONS UTILISATEUR ---

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
                // Nettoyage mémoire alarme
                sessionsAlerted.remove(session.id)
            }
        }
    }

    fun onPaymentClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                val resteAPayer = jeu.montant_total - jeu.montant_deja_paye
                if (resteAPayer > 0) {
                    jeuxDao.updateJeux(jeu.copy(est_paye = true, montant_deja_paye = jeu.montant_total))
                    val recette = Finance(
                        date_heure = Date(),
                        montant_entrant = resteAPayer,
                        montant_sortant = 0.0,
                        description = "Session : ${jeu.titre} (Complément)",
                        source = "RECETTE"
                    )
                    financeRepository.insert(recette)
                }
            }
        }
    }

    fun onPlayPauseClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                val newPauseState = !jeu.est_en_pause
                if (newPauseState) {
                    jeuxDao.updateJeux(jeu.copy(est_en_pause = true, timestamp_pause_debut = System.currentTimeMillis()))
                } else {
                    val pauseDuration = System.currentTimeMillis() - jeu.timestamp_pause_debut
                    val newTotalPause = jeu.duree_cumulee_pause + pauseDuration
                    jeuxDao.updateJeux(jeu.copy(est_en_pause = false, duree_cumulee_pause = newTotalPause, timestamp_pause_debut = 0))
                }
            }
        }
    }

    fun addTime(sessionId: Int, addedMatches: Int, addedPrice: Double, addedDuration: Long, newGameTitle: String) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(sessionId)
            if (jeu != null) {
                val newTotalMatches = jeu.nombre_tranches + addedMatches
                val newTotalPrice = jeu.montant_total + addedPrice
                val newTotalDuration = jeu.duree_totale_prevue + addedDuration

                // IMPORTANT : On retire la session de la liste des "alertés" pour qu'elle puisse re-sonner à la fin du nouveau temps
                sessionsAlerted.remove(sessionId.toString())

                jeuxDao.updateJeux(jeu.copy(
                    titre = newGameTitle,
                    nombre_tranches = newTotalMatches,
                    montant_total = newTotalPrice,
                    duree_totale_prevue = newTotalDuration,
                    est_paye = false,
                    est_termine = false
                ))
            }
        }
    }

    fun onAddTimeClicked(session: GameSession) { }

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