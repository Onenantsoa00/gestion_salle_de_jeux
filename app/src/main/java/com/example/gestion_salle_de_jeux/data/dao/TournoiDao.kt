package com.example.gestion_salle_de_jeux.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestion_salle_de_jeux.data.entity.Reserve
import com.example.gestion_salle_de_jeux.data.entity.ReserveAvecMateriel
import com.example.gestion_salle_de_jeux.data.entity.Tournoi
import com.example.gestion_salle_de_jeux.data.entity.TournoiAvecPlayeur

@Dao
interface TournoiDao{
    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournoi(tournoi: Tournoi)

    //read all
    @Query("SELECT * FROM Tournoi")
    suspend fun getAllTournoi(): List<Tournoi>

    //read by id
    @Query("SELECT * FROM Tournoi WHERE id = :id")
    suspend fun getTournoiById(id: Int): Tournoi?

    //update
    @Update
    suspend fun updateTournoi(tournoi: Tournoi):Int

    //update by id
    @Query("UPDATE Tournoi SET nom_tournoi = :nom_tournoi WHERE id = :id")
    suspend fun updateTournoiById(id: Int, nom_tournoi: String): Int

    //delete
    @Delete
    suspend fun deleteTournoi(tournoi: Tournoi): Int

    //delete by id
    @Query("DELETE FROM Tournoi WHERE id = :id")
    suspend fun deleteTournoiById(id: Int): Int

    //read tournoi + playeur
    @Query("Select * FROM Tournoi")
    suspend fun getTournoiAvecPlayeur(): List<TournoiAvecPlayeur>
}