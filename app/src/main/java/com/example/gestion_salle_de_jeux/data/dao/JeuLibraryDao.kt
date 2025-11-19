package com.example.gestion_salle_de_jeux.data.dao

import androidx.room.*
import com.example.gestion_salle_de_jeux.data.entity.JeuLibrary
import kotlinx.coroutines.flow.Flow

@Dao
interface JeuLibraryDao {
    @Insert
    suspend fun insert(jeu: JeuLibrary)

    // NOUVEAU : Permet de modifier le nom du jeu
    @Update
    suspend fun update(jeu: JeuLibrary)

    // NOUVEAU : Permet de supprimer un jeu
    @Delete
    suspend fun delete(jeu: JeuLibrary)

    @Query("SELECT * FROM JeuLibrary WHERE id_materiel = :consoleId ORDER BY nom_jeu ASC")
    fun getJeuxForConsole(consoleId: Int): Flow<List<JeuLibrary>>
}