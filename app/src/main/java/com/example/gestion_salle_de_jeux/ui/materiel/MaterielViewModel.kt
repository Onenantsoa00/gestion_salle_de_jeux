package com.example.gestion_salle_de_jeux.ui.materiel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestion_salle_de_jeux.data.dao.MaterielDao
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MaterielViewModel(private val dao: MaterielDao) : ViewModel() {

    val allMateriel: Flow<List<Materiel>> = dao.getAllMateriel()

    fun addMateriel(console: String, nombre_manette: Short, nombre_television: Short, id_reserve: Int) {
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

    fun updateMateriel(materiel: Materiel) {
        viewModelScope.launch {
            dao.updateMateriel(materiel)
        }
    }

    fun deleteMateriel(materiel: Materiel) {
        viewModelScope.launch {
            dao.deleteMateriel(materiel)
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