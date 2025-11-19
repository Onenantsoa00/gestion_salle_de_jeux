package com.example.gestion_salle_de_jeux.ui.materiel

import androidx.lifecycle.*
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.dao.MaterielDao
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import com.example.gestion_salle_de_jeux.ui.materiel.model.MaterialUiItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MaterielViewModel(private val materielDao: MaterielDao) : ViewModel() {

    val materialList: LiveData<List<MaterialUiItem>> = materielDao.getAllMateriel().asLiveData().map { listEntities ->
        listEntities.map { entity ->
            mapEntityToUiModel(entity)
        }
    }

    val allMateriel: Flow<List<Materiel>> = materielDao.getAllMateriel()

    // Mise à jour pour inclure quantite_utilise
    fun addMateriel(nom: String, quantiteTotal: Int, quantiteUtilise: Int, type: String) {
        viewModelScope.launch {
            val newItem = Materiel(
                nom = nom,
                quantite = quantiteTotal,
                quantite_utilise = quantiteUtilise,
                type = type
            )
            materielDao.insert(newItem)
        }
    }

    fun updateMateriel(id: Int, nom: String, quantiteTotal: Int, quantiteUtilise: Int, type: String) {
        viewModelScope.launch {
            val updatedItem = Materiel(
                id = id,
                nom = nom,
                quantite = quantiteTotal,
                quantite_utilise = quantiteUtilise,
                type = type
            )
            materielDao.update(updatedItem)
        }
    }

    private fun mapEntityToUiModel(entity: Materiel): MaterialUiItem {
        val iconRes = when (entity.type.uppercase()) {
            "CONSOLE" -> R.drawable.ic_console
            "ECRAN", "TV" -> R.drawable.ic_tv
            else -> R.drawable.ic_gamepad
        }

        // CALCUL LOGIQUE ICI
        val enStock = entity.quantite - entity.quantite_utilise

        // On formate le texte pour l'affichage "En Stock"
        val statusText = if (enStock > 0) "$enStock En Stock" else "Rupture"

        // On passe le "Total" dans count, mais on utilisera le statusText pour afficher le reste
        // Note : J'utilise une astuce ici. Je passe le nombre "Utilisé" dans le nom de l'item pour l'adapter ?
        // Non, on va gérer ça proprement dans l'adapter.
        // MaterialUiItem(id, name, count(Total), stockStatus(String), icon)

        return MaterialUiItem(
            id = entity.id,
            name = entity.nom,
            count = entity.quantite, // Ici on met le TOTAL
            stockStatus = "Utilisé: ${entity.quantite_utilise} | Dispo: $enStock", // On combine l'affichage ici
            iconResId = iconRes
        )
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