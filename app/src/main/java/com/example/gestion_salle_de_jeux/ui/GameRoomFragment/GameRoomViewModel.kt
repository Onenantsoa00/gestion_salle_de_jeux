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
import com.example.gestion_salle_de_jeux.ui.utils.SingleLiveEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.Locale
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

    private val _alarmTrigger = SingleLiveEvent<GameSession>()
    val alarmTrigger: LiveData<GameSession> = _alarmTrigger

    // État local de la coupure (pour l'affichage immédiat)
    private var isPowerCutActive = false

    init {
        startTimerLoop()

        viewModelScope.launch {
            _dbSessions.collect { sessionsBrutes ->
                val currentLocalList = _uiGameSessions.value ?: emptyList()
                val uiSessions = sessionsBrutes.map { jeu ->
                    val mappedSession = mapEntityToUi(jeu)
                    val locallySounded = currentLocalList.find { it.id == mappedSession.id }?.hasSounded ?: false
                    if (locallySounded) mappedSession.copy(hasSounded = true) else mappedSession
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
            rawTotalPauseDuration = jeu.duree_cumulee_pause,
            rawTotalPrice = jeu.montant_total,
            hasSounded = jeu.a_sonne,
            isPowerCutMode = isPowerCutActive // Transmission de l'état
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
        var justTriggeredAlarm = false

        // Variables pour la coupure
        var cutInfo = ""
        var amountToPay = 0.0
        var amountToRefund = 0.0

        if (diff > 0) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
            timeText = String.format("%02d:%02d", minutes, seconds)
            status = if (minutes < 2 && !session.isPaused) SessionStatus.WARNING else SessionStatus.ONLINE

            // --- LOGIQUE MATHÉMATIQUE COUPURE ---
            if (session.isPowerCutMode) {
                // 1. Calcul du temps écoulé réel (Temps total prévu - Temps restant)
                // Note: diff est le temps restant.
                val totalDurationMs = session.rawDurationMinutes * 60 * 1000
                val timeConsumedMs = totalDurationMs - diff

                // 2. Ratio consommé (0.0 à 1.0)
                val ratio = if (totalDurationMs > 0) timeConsumedMs.toDouble() / totalDurationMs.toDouble() else 1.0

                // 3. Calculs financiers
                val priceConsumed = session.rawTotalPrice * ratio

                if (session.paymentStatus == PaymentStatus.PAID) {
                    // CAS 1 : DÉJÀ PAYÉ -> On doit rendre la différence
                    amountToRefund = session.rawTotalPrice - priceConsumed
                    // Arrondi à 100 Ar près pour éviter la petite monnaie impossible
                    // amountToRefund = (amountToRefund / 100).toInt() * 100.0
                    cutInfo = " | À RENDRE : ${formatMoney(amountToRefund)}"
                } else {
                    // CAS 2 : NON PAYÉ -> Le joueur doit payer ce qu'il a consommé
                    amountToPay = priceConsumed
                    cutInfo = " | À PAYER : ${formatMoney(amountToPay)}"
                }
            }

        } else {
            timeText = "TERMINÉ"
            status = SessionStatus.ERROR

            if (session.isPowerCutMode && session.paymentStatus == PaymentStatus.UNPAID) {
                // Si coupure mais fini, il doit tout payer
                amountToPay = session.rawTotalPrice
                cutInfo = " | À PAYER : ${formatMoney(amountToPay)}"
            }

            if (!session.hasSounded) {
                _alarmTrigger.postValue(session)
                markAsSounded(session.id.toInt())
                justTriggeredAlarm = true
            }
        }

        return session.copy(
            timeRemaining = timeText,
            sessionStatus = status,
            hasSounded = session.hasSounded || justTriggeredAlarm,
            powerCutInfo = cutInfo,
            partialAmountToPay = amountToPay,
            partialAmountToRefund = amountToRefund
        )
    }

    // --- GESTION GLOBALE COUPURE ---
    fun togglePowerCut(isCut: Boolean) {
        isPowerCutActive = isCut
        viewModelScope.launch {
            val activeSessions = jeuxDao.getActiveSessionsList()
            val now = System.currentTimeMillis()

            activeSessions.forEach { jeu ->
                if (isCut) {
                    // COUPURE : On met tout en PAUSE si ce n'est pas déjà fait
                    if (!jeu.est_en_pause) {
                        jeuxDao.updateJeux(jeu.copy(
                            est_en_pause = true,
                            timestamp_pause_debut = now
                        ))
                    }
                } else {
                    // RETOUR COURANT : On remet tout en PLAY
                    if (jeu.est_en_pause) {
                        val pauseDuration = now - jeu.timestamp_pause_debut
                        val newTotalPause = jeu.duree_cumulee_pause + pauseDuration
                        jeuxDao.updateJeux(jeu.copy(
                            est_en_pause = false,
                            duree_cumulee_pause = newTotalPause,
                            timestamp_pause_debut = 0
                        ))
                    }
                }
            }
        }
    }

    // --- PAIEMENT ---
    fun onPaymentClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())

            if (jeu != null) {
                // LOGIQUE SPÉCIALE COUPURE
                if (session.isPowerCutMode) {
                    if (session.paymentStatus == PaymentStatus.UNPAID) {
                        // 1. Marquer comme payé
                        jeuxDao.updateJeux(jeu.copy(est_paye = true, est_termine = true)) // On termine aussi la session

                        // 2. Enregistrer le montant PARTIEL (calculé)
                        val recette = Finance(
                            date_heure = Date(),
                            montant_entrant = session.partialAmountToPay, // Montant calculé par le timer
                            montant_sortant = 0.0,
                            description = "Coupure : ${jeu.titre} (Partiel)",
                            source = "RECETTE"
                        )
                        financeRepository.insert(recette)

                        // 3. Libérer le poste
                        freeMaterial(jeu.id_materiel)
                    }
                }
                // LOGIQUE NORMALE
                else {
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
    }

    private suspend fun freeMaterial(materielId: Int) {
        val materiel = materielDao.getMaterielById(materielId)
        if (materiel != null) {
            val usageActuel = materiel.quantite_utilise
            val nouvelUsage = if (usageActuel > 0) usageActuel - 1 else 0
            materielDao.update(materiel.copy(quantite_utilise = nouvelUsage))
        }
    }

    // --- ACTIONS CLASSIQUES ---
    fun onStopClicked(session: GameSession) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(session.id.toInt())
            if (jeu != null) {
                jeuxDao.updateJeux(jeu.copy(est_termine = true))
                freeMaterial(jeu.id_materiel)
            }
        }
    }

    fun onPlayPauseClicked(session: GameSession) {
        // Désactivé en mode coupure pour ne pas interférer
        if (isPowerCutActive) return

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
                jeuxDao.updateJeux(jeu.copy(
                    titre = newGameTitle,
                    nombre_tranches = newTotalMatches,
                    montant_total = newTotalPrice,
                    duree_totale_prevue = newTotalDuration,
                    est_paye = false,
                    est_termine = false,
                    a_sonne = false
                ))
            }
        }
    }

    fun onAddTimeClicked(session: GameSession) { }

    private fun formatMoney(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply { groupingSeparator = ' ' }
        return "${DecimalFormat("#,##0", symbols).format(amount)} Ar"
    }

    private fun markAsSounded(sessionId: Int) {
        viewModelScope.launch {
            val jeu = jeuxDao.getJeuxById(sessionId)
            if (jeu != null && !jeu.a_sonne) {
                jeuxDao.updateJeux(jeu.copy(a_sonne = true))
            }
        }
    }

    class Factory(private val jeuxDao: JeuxDao, private val materielDao: MaterielDao, private val playeurDao: PlayeurDao, private val financeRepository: FinanceRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameRoomViewModel::class.java)) return GameRoomViewModel(jeuxDao, materielDao, playeurDao, financeRepository) as T
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}