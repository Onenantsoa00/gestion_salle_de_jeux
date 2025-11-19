package com.example.gestion_salle_de_jeux.ui.materiel

import androidx.lifecycle.*
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.dao.JeuLibraryDao
import com.example.gestion_salle_de_jeux.data.dao.MaterielDao
import com.example.gestion_salle_de_jeux.data.entity.JeuLibrary
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import com.example.gestion_salle_de_jeux.ui.materiel.model.MaterialUiItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MaterielViewModel(
    private val materielDao: MaterielDao,
    private val jeuLibraryDao: JeuLibraryDao
) : ViewModel() {

    private val _allUiItems = materielDao.getAllMateriel().asLiveData().map { list ->
        list.map { mapEntityToUiModel(it) }
    }

    val allMateriel: Flow<List<Materiel>> = materielDao.getAllMateriel()

    private val _filterMode = MutableLiveData("ALL")

    val displayList: LiveData<List<MaterialUiItem>> = MediatorLiveData<List<MaterialUiItem>>().apply {
        addSource(_allUiItems) { items -> value = filterItems(items, _filterMode.value) }
        addSource(_filterMode) { mode -> value = filterItems(_allUiItems.value, mode) }
    }

    private fun filterItems(items: List<MaterialUiItem>?, mode: String?): List<MaterialUiItem> {
        if (items == null) return emptyList()
        return if (mode == "CONSOLES") {
            items.filter { it.iconResId == R.drawable.ic_console }
        } else {
            items
        }
    }

    fun setTabMode(isGamesTab: Boolean) {
        _filterMode.value = if (isGamesTab) "CONSOLES" else "ALL"
    }

    // --- Gestion Matériel ---
    fun addMateriel(nom: String, quantiteTotal: Int, quantiteUtilise: Int, type: String) {
        viewModelScope.launch {
            materielDao.insert(Materiel(nom = nom, quantite = quantiteTotal, quantite_utilise = quantiteUtilise, type = type))
        }
    }
    fun updateMateriel(id: Int, nom: String, quantiteTotal: Int, quantiteUtilise: Int, type: String) {
        viewModelScope.launch {
            materielDao.update(Materiel(id = id, nom = nom, quantite = quantiteTotal, quantite_utilise = quantiteUtilise, type = type))
        }
    }

    // --- Gestion Jeux (CORRIGÉ) ---
    fun addGameToConsole(consoleId: Int, gameName: String) {
        viewModelScope.launch {
            jeuLibraryDao.insert(JeuLibrary(id_materiel = consoleId, nom_jeu = gameName))
        }
    }

    // NOUVEAU : Modifier un jeu
    fun updateGame(jeu: JeuLibrary, newName: String) {
        viewModelScope.launch {
            val updatedJeu = jeu.copy(nom_jeu = newName)
            jeuLibraryDao.update(updatedJeu)
        }
    }

    // NOUVEAU : Supprimer un jeu
    fun deleteGame(jeu: JeuLibrary) {
        viewModelScope.launch {
            jeuLibraryDao.delete(jeu)
        }
    }

    fun getGamesForConsole(consoleId: Int): LiveData<List<JeuLibrary>> {
        return jeuLibraryDao.getJeuxForConsole(consoleId).asLiveData()
    }

    private fun mapEntityToUiModel(entity: Materiel): MaterialUiItem {
        val iconRes = when (entity.type.uppercase()) {
            "CONSOLE" -> R.drawable.ic_console
            "ECRAN", "TV" -> R.drawable.ic_tv
            else -> R.drawable.ic_gamepad
        }
        val enStock = entity.quantite - entity.quantite_utilise
        return MaterialUiItem(
            id = entity.id,
            name = entity.nom,
            count = entity.quantite,
            stockStatus = "Utilisé: ${entity.quantite_utilise} | Dispo: $enStock",
            iconResId = iconRes
        )
    }

    class MaterielViewModelFactory(
        private val materielDao: MaterielDao,
        private val jeuLibraryDao: JeuLibraryDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MaterielViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MaterielViewModel(materielDao, jeuLibraryDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}