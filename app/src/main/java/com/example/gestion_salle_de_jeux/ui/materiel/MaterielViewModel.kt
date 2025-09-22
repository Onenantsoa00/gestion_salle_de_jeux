package com.example.gestion_salle_de_jeux.ui.materiel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestion_salle_de_jeux.data.dao.FinanceDao
import com.example.gestion_salle_de_jeux.data.dao.MaterielDao
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import com.example.gestion_salle_de_jeux.ui.finance.FinanceViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class MaterielViewModel (private val dao : MaterielDao) : ViewModel() {

    fun addMateriel(console: String, nombre_manette : Short, nombre_television : Short, id_reserve : Int) {
        // Implémentation de l'ajout de matériel
        viewModelScope.launch {
            dao.insertMateriel(
                Materiel(
                    console = console,
                    nombre_manette = nombre_manette,
                    nombre_television = nombre_television,
                    id_reserve = id_reserve
                )
            )
        }
    }

    class MaterielViewModelFactory(private val dao: MaterielDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MaterielViewModel::class.java)) {
                return MaterielViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}