package com.example.gestion_salle_de_jeux.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestion_salle_de_jeux.data.entity.Finance
import com.example.gestion_salle_de_jeux.data.entity.FinanceAvecJeux
import com.example.gestion_salle_de_jeux.data.entity.Reserve
import com.example.gestion_salle_de_jeux.data.entity.ReserveAvecMateriel

@Dao
interface ReserveDao{
    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReserve(reserve: Reserve)

    //read all
    @Query("SELECT * FROM Reserve")
    suspend fun getAllReserve(): List<Reserve>

    //read by id
    @Query("SELECT * FROM Reserve WHERE id = :id")
    suspend fun getReserveById(id: Int): Reserve?

    //update
    @Update
    suspend fun updateReserve(reserve: Reserve):Int

    //update by id
    @Query("UPDATE Reserve SET nom_reserve = :nom_reserve WHERE id = :id")
    suspend fun updateReserveById(id: Int, nom_reserve: String): Int

    //delete
    @Delete
    suspend fun deleteReserve(reserve: Reserve): Int

    //delete by id
    @Query("DELETE FROM Reserve WHERE id = :id")
    suspend fun deleteReserveById(id: Int): Int

    //read reserve + materiel
    @Query("Select * FROM Reserve")
    suspend fun getReserveAvecMateriel(): List<ReserveAvecMateriel>
}