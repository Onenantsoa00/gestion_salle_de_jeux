package com.example.gestion_salle_de_jeux.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestion_salle_de_jeux.data.dao.FinanceDao
import com.example.gestion_salle_de_jeux.data.entity.Finance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class FinanceViewModel(private val dao: FinanceDao) : ViewModel() {

    val finances = dao.getAllFinance()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Statistiques calculées en temps réel
    val stats = finances
        .map { list ->
            val totalEntrees = list.sumOf { it.montant_entrant }
            val totalSorties = list.sumOf { it.montant_sortant }
            val soldeTotal = totalEntrees - totalSorties

            FinanceStats(totalEntrees, totalSorties, soldeTotal)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, FinanceStats(0.0, 0.0, 0.0))
    fun addFinance(date_heure: Date, montant_entrant: Double, montant_sortant: Double, description: String) {
        viewModelScope.launch {
            dao.insertFinance(
                Finance(
                    date_heure = date_heure,
                    montant_entrant = montant_entrant,
                    montant_sortant = montant_sortant,
                    description = description
                )
            )
        }
    }

    data class FinanceStats(
        val totalEntrees: Double,
        val totalSorties: Double,
        val soldeTotal: Double
    )

    class FinanceViewModelFactory(private val dao: FinanceDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                return FinanceViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}