package com.example.gestion_salle_de_jeux.data.dao

import androidx.room.*
import com.example.gestion_salle_de_jeux.data.entity.Jeux
import kotlinx.coroutines.flow.Flow

@Dao
interface JeuxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJeux(jeux: Jeux): Long

    @Update
    suspend fun updateJeux(jeux: Jeux)

    // Récupérer uniquement les sessions EN COURS (non terminées)
    @Query("SELECT * FROM Jeux WHERE est_termine = 0 ORDER BY timestamp_debut DESC")
    fun getActiveSessions(): Flow<List<Jeux>>

    @Query("SELECT * FROM Jeux WHERE id = :id")
    suspend fun getJeuxById(id: Int): Jeux?
}