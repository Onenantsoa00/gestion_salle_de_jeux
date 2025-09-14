package com.example.gestion_salle_de_jeux.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestion_salle_de_jeux.data.entity.Jeux
import com.example.gestion_salle_de_jeux.data.entity.Materiel

@Dao
interface MaterielDao {
    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMateriel(materiel: Materiel)

    //read all
    @Query("SELECT * FROM Materiel")
    suspend fun getAllMateriel(): List<Materiel>

    //read by id
    @Query("SELECT * FROM Materiel WHERE id = :id")
    suspend fun getMaterielById(id: Int): Materiel?

    //update
    @Update
    suspend fun updateMateriel(materiel: Materiel):Int

    //update by id
    @Query("UPDATE Materiel SET console = :console WHERE id = :id")
    suspend fun updateMaterielById(id: Int, console: String): Int

    //delete
    @Delete
    suspend fun deleteMateriel(materiel: Materiel): Int

    //delete by id
    @Query("DELETE FROM Materiel WHERE id = :id")
    suspend fun deleteMaterielById(id: Int): Int
}