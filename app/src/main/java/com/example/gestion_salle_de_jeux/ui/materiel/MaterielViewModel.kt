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

    // --- Gestion Jeux ---
    fun addGameToConsole(consoleId: Int, gameName: String, tarif: Double, duree: Int) {
        viewModelScope.launch {
            val nouveauJeu = JeuLibrary(
                id_materiel = consoleId,
                nom_jeu = gameName,
                tarif_par_tranche = tarif,
                duree_tranche_min = duree
            )
            jeuLibraryDao.insert(nouveauJeu)
        }
    }

    fun updateGame(jeu: JeuLibrary, newName: String, newTarif: Double, newDuree: Int) {
        viewModelScope.launch {
            val updatedJeu = jeu.copy(
                nom_jeu = newName,
                tarif_par_tranche = newTarif,
                duree_tranche_min = newDuree
            )
            jeuLibraryDao.update(updatedJeu)
        }
    }

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

        // CORRECTION ICI : Format "Utilisé : Y | Stock : Z"
        return MaterialUiItem(
            id = entity.id,
            name = entity.nom,
            count = entity.quantite, // Sera affiché comme "Total : X" par l'adapter
            stockStatus = "Utilisé : ${entity.quantite_utilise} | Stock : $enStock",
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