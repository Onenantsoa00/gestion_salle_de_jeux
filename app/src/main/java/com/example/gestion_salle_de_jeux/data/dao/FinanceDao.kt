package com.example.gestion_salle_de_jeux.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestion_salle_de_jeux.data.entity.Finance
import com.example.gestion_salle_de_jeux.data.entity.FinanceAvecJeux
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinance(finance: Finance)

    //read all
    @Query("SELECT * FROM Finance")
    fun getAllFinance(): Flow<List<Finance>>

    //read by id
    @Query("SELECT * FROM Finance WHERE id = :id")
    suspend fun getFinanceById(id: Int): Finance?

    //update
    @Update
    suspend fun updateFinance(finance: Finance):Int

    //update by id
    @Query("UPDATE Finance SET montant_entrant = :montant WHERE id = :id")
    suspend fun updateFinanceById(id: Int, montant: Double): Int

    //delete
    @Delete
    suspend fun deleteFinance(finance: Finance): Int

    //delete by id
    @Query("DELETE FROM Finance WHERE id = :id")
    suspend fun deleteFinanceById(id: Int): Int

    //read console + jeux
    @Query("Select * FROM Finance")
    suspend fun getFinanceAvecJeux(): List<FinanceAvecJeux>
}