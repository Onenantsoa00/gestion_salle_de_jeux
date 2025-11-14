package com.example.gestion_salle_de_jeux.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gestion_salle_de_jeux.ui.history.model.HistoryIconType
import com.example.gestion_salle_de_jeux.ui.history.model.HistoryItem

class HistoryViewModel : ViewModel() {

    // TODO: Injecter un Repository pour obtenir de vraies données

    // LiveData pour les résumés
    private val _todayRevenue = MutableLiveData<String>()
    val todayRevenue: LiveData<String> = _todayRevenue

    private val _todayExpense = MutableLiveData<String>()
    val todayExpense: LiveData<String> = _todayExpense

    private val _yesterdayRevenue = MutableLiveData<String>()
    val yesterdayRevenue: LiveData<String> = _yesterdayRevenue

    private val _yesterdayExpense = MutableLiveData<String>()
    val yesterdayExpense: LiveData<String> = _yesterdayExpense

    // LiveData pour la liste de l'historique
    private val _historyItems = MutableLiveData<List<HistoryItem>>()
    val historyItems: LiveData<List<HistoryItem>> = _historyItems

    init {
        loadData()
    }

    private fun loadData() {
        // Simule le chargement de données réelles
        // TODO: Remplacez ceci par des appels à votre Repository

        _todayRevenue.value = "+15 000 Ar"
        _todayExpense.value = "- 0 Ar"
        _yesterdayRevenue.value = "+50 000Ar"
        _yesterdayExpense.value = "- 3000 Ar"

        _historyItems.value = createDummyHistory()
    }

    private fun createDummyHistory(): List<HistoryItem> {
        return listOf(
            HistoryItem(
                id = "1",
                title = "Coupure Electriciter",
                subtitle = "12:00 à 12:34",
                timestamp = "23-10-2025 à 12:00",
                iconType = HistoryIconType.POWER_OUTAGE
            ),
            HistoryItem(
                id = "2",
                title = "Game room",
                subtitle = "Poste 1 - Occuper pendant 10 min",
                timestamp = "23-10-2025 à 13:12",
                iconType = HistoryIconType.GAME_ROOM
            ),
            HistoryItem(
                id = "3",
                title = "Finance",
                subtitle = "Mama nindrana 2 000 Ar",
                timestamp = "23-10-2025 à 13:14",
                iconType = HistoryIconType.FINANCE
            ),
            HistoryItem(
                id = "4",
                title = "Dépense Materiel",
                subtitle = "Ajout nouvel materiel",
                timestamp = "23-10-2025 à 13:18",
                iconType = HistoryIconType.MATERIAL_EXPENSE
            ),
            HistoryItem(
                id = "5",
                title = "Finance",
                subtitle = "remise jeton 10 000 Ar",
                timestamp = "23-10-2025 à 13:14", // Timestamp différent dans la maquette ? J'en mets un
                iconType = HistoryIconType.FINANCE
            )
        )
    }

    // TODO: Ajouter une fonction pour supprimer un item
    // fun deleteHistoryItem(item: HistoryItem) { ... }
}