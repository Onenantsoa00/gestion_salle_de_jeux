package com.example.gestion_salle_de_jeux.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import com.example.gestion_salle_de_jeux.data.entity.Playeur

@Dao
interface PlayeurDao{
    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayeur(playeur: Playeur)

    //read all
    @Query("SELECT * FROM Playeur")
    suspend fun getAllPlayeur(): List<Playeur>

    //read by id
    @Query("SELECT * FROM Playeur WHERE id = :id")
    suspend fun getPlayeurById(id: Int): Playeur?

    //update
    @Update
    suspend fun updatePlayeur(playeur: Playeur):Int

    //update by id
    @Query("UPDATE Playeur SET nom_playeur = :nom_playeur WHERE id = :id")
    suspend fun updatePlayeurById(id: Int, nom_playeur: String): Int

    //delete
    @Delete
    suspend fun deletePlayeur(playeur: Playeur): Int

    //delete by id
    @Query("DELETE FROM Playeur WHERE id = :id")
    suspend fun deletePlayeurById(id: Int): Int
}