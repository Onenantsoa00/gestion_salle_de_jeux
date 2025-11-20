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
                val uiSessions = sessionsBrutes.map { jeu -> mapEntityToUi(jeu) }
                _uiGameSessions.postValue(uiSessions)
            }
        }
    }

    private fun startTimerLoop() {
        viewModelScope.launch {
            while (true) {
                _uiGameSessions.value?.let { currentList ->
                    val updatedList = currentList.map { session -> updateTimeForSession(session) }
                    _uiGameSessions.postValue(updatedList)
                }
                delay(1000)
            }
        }
    }

    private suspend fun mapEntityToUi(jeu: Jeux): GameSession {
        val materiel = materielDao.getMaterielById(jeu.id_materiel)
        val playeur = playeurDao.getPlayeurById(jeu.id_playeur)

        // Calcul du reste à payer pour affichage éventuel, ou pour déterminer le statut
        val resteAPayer = jeu.montant_total - jeu.montant_deja_paye
        // Si tout est payé (ou trop payé par erreur), c'est PAID, sinon UNPAID
        val statusPaiement = if (resteAPayer <= 0) PaymentStatus.PAID else PaymentStatus.UNPAID

        return GameSession(
            id = jeu.id.toString(),
            postName = "Poste ${materiel?.id}",
            consoleName = materiel?.nom ?: "Inconnu",
            gameName = jeu.titre,
            players = playeur?.nom ?: "Inconnu",
            matchDetails = "${jeu.nombre_tranches} tranches • ${jeu.montant_total.toInt()} Ar",
            timeRemaining = "",
            paymentStatus = statusPaiement,
            sessionStatus = SessionStatus.ONLINE,
            isPaused = jeu.est_en_pause,
            rawStartTime = jeu.timestamp_debut,
            rawDurationMinutes = jeu.duree_totale_prevue,
            rawPauseStartTime = jeu.timestamp_pause_debut,
            rawTotalPauseDuration = jeu.duree_cumulee_pause
        )
    }

    private fun updateTimeForSession(session: GameSession): GameSession {
        if (session.isPaused) return session.copy(timeRemaining = "PAUSE")
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
            }
        }
    }

    // CORRECTION MAJEURE : Gestion du paiement partiel/total
    fun onPaymentClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())

            if (jeu != null) {
                // On calcule ce qui reste à payer
                val resteAPayer = jeu.montant_total - jeu.montant_deja_paye

                if (resteAPayer > 0) {
                    // 1. On met à jour le jeu : Tout est payé maintenant
                    // montant_deja_paye devient égal au total
                    jeuxDao.updateJeux(jeu.copy(
                        est_paye = true,
                        montant_deja_paye = jeu.montant_total
                    ))

                    // 2. On n'enregistre dans Finance QUE le reste à payer (le rajout)
                    val recette = Finance(
                        date_heure = Date(),
                        montant_entrant = resteAPayer, // Seulement le complément !
                        montant_sortant = 0.0,
                        description = "Session : ${jeu.titre} (Complément)",
                        source = "RECETTE"
                    )
                    financeRepository.insert(recette)
                }
                // Si c'est déjà payé, on ne fait rien ou on gère l'annulation (complexe)
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

    // CORRECTION : Ajout de temps
    fun addTime(sessionId: Int, addedMatches: Int, addedPrice: Double, addedDuration: Long, newGameTitle: String) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(sessionId)
            if (jeu != null) {
                val newTotalMatches = jeu.nombre_tranches + addedMatches
                val newTotalPrice = jeu.montant_total + addedPrice
                val newTotalDuration = jeu.duree_totale_prevue + addedDuration

                // On met à jour le total.
                // montant_deja_paye reste le même (l'ancien montant).
                // Donc automatiquement : Total > DejaPaye => ResteAPayer > 0 => Statut deviendra NON-PAYE
                jeuxDao.updateJeux(jeu.copy(
                    titre = newGameTitle,
                    nombre_tranches = newTotalMatches,
                    montant_total = newTotalPrice,
                    duree_totale_prevue = newTotalDuration,
                    est_paye = false, // Statut visuel repasse à non payé
                    est_termine = false
                ))
            }
        }
    }

    fun onAddTimeClicked(session: GameSession) { } // Géré par Fragment

    class Factory(private val jeuxDao: JeuxDao, private val materielDao: MaterielDao, private val playeurDao: PlayeurDao, private val financeRepository: FinanceRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameRoomViewModel::class.java)) return GameRoomViewModel(jeuxDao, materielDao, playeurDao, financeRepository) as T
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}