package com.example.gestion_salle_de_jeux.data.dao

import androidx.room.*
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterielDao {
    // Récupère tout le stock trié par nom
    @Query("SELECT * FROM Materiel ORDER BY nom ASC")
    fun getAllMateriel(): Flow<List<Materiel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(materiel: Materiel)

    @Update
    suspend fun update(materiel: Materiel)

    @Delete
    suspend fun delete(materiel: Materiel)
}