package com.example.gestion_salle_de_jeux.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestion_salle_de_jeux.data.entity.Finance
import com.example.gestion_salle_de_jeux.data.entity.FinanceAvecJeux
import com.example.gestion_salle_de_jeux.data.entity.Jeux

@Dao
interface JeuxDao {
    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJeux(jeux: Jeux)

    //read all
    @Query("SELECT * FROM Jeux")
    suspend fun getAllJeux(): List<Jeux>

    //read by id
    @Query("SELECT * FROM Jeux WHERE id = :id")
    suspend fun getJeuxById(id: Int): Jeux?

    //update
    @Update
    suspend fun updateJeux(jeux: Jeux):Int

    //update by id
    @Query("UPDATE Jeux SET titre = :titre WHERE id = :id")
    suspend fun updateJeuxById(id: Int, titre: String): Int

    //delete
    @Delete
    suspend fun deleteJeux(jeux: Jeux): Int

    //delete by id
    @Query("DELETE FROM Jeux WHERE id = :id")
    suspend fun deleteJeuxById(id: Int): Int
}