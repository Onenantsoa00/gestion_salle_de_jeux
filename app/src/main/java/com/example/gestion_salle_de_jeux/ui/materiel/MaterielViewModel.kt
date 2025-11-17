package com.example.gestion_salle_de_jeux.ui.materiel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.dao.MaterielDao
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import com.example.gestion_salle_de_jeux.ui.materiel.model.MaterialUiItem
import kotlinx.coroutines.flow.Flow

class MaterielViewModel(private val materielDao: MaterielDao) : ViewModel() {

    // 1. Pour l'interface "Gestion Matériel" (Liste avec cartes)
    private val _materialList = MutableLiveData<List<MaterialUiItem>>()
    val materialList: LiveData<List<MaterialUiItem>> = _materialList

    // 2. AJOUTÉ : Pour l'interface "Game Room" (Dialogues de sélection)
    // C'est cette ligne qui manquait et qui causait l'erreur "Unresolved reference: allMateriel"
    val allMateriel: Flow<List<Materiel>> = materielDao.getAllMateriel()

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        // Données fictives pour l'UI Inventaire
        val list = listOf(
            MaterialUiItem(1, "Ecran Plat", 4, "1 En Stock", R.drawable.ic_tv),
            MaterialUiItem(2, "Manettes PS2", 14, "1 En Stock", R.drawable.ic_gamepad),
            MaterialUiItem(3, "Cable Hdmi", 2, "0 En Stock", R.drawable.ic_gamepad),
            MaterialUiItem(4, "House PS2", 40, "40 En Stock", R.drawable.ic_gamepad),
            MaterialUiItem(5, "Console PS5", 2, "0 En Stock", R.drawable.ic_console)
        )
        _materialList.value = list
    }

    class MaterielViewModelFactory(private val materielDao: MaterielDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MaterielViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MaterielViewModel(materielDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}