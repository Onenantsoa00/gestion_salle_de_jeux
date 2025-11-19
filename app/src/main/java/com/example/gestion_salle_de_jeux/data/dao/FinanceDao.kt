package com.example.gestion_salle_de_jeux.data.dao

import androidx.room.*
import com.example.gestion_salle_de_jeux.data.entity.Finance
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FinanceDao {
    @Insert
    suspend fun insert(finance: Finance)

    @Update
    suspend fun update(finance: Finance)

    @Delete
    suspend fun delete(finance: Finance)

    @Query("SELECT * FROM finance ORDER BY date_heure DESC")
    fun getAllFinance(): Flow<List<Finance>>

    // --- ANALYTICS DASHBOARD ---

    @Query("SELECT * FROM finance WHERE date_heure BETWEEN :startDate AND :endDate ORDER BY date_heure DESC")
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Finance>>

    @Query("SELECT COALESCE(SUM(montant_entrant), 0) FROM finance WHERE date_heure BETWEEN :startDate AND :endDate")
    fun getTotalEntrantByRange(startDate: Date, endDate: Date): Flow<Double>

    @Query("SELECT COALESCE(SUM(montant_sortant), 0) FROM finance WHERE source = :source AND date_heure BETWEEN :startDate AND :endDate")
    fun getTotalSortantBySourceAndRange(source: String, startDate: Date, endDate: Date): Flow<Double>

    // --- NOUVEAU : REQUÊTES POUR LE GRAPHIQUE ---

    // Récupère toutes les entrées brutes pour une période, on fera le groupement en Kotlin pour plus de flexibilité avec les dates
    // (C'est plus simple que de faire des manipulations de dates complexes en SQL pur sur Android)
    @Query("SELECT * FROM finance WHERE montant_entrant > 0 AND date_heure BETWEEN :startDate AND :endDate ORDER BY date_heure ASC")
    fun getIncomesByRange(startDate: Date, endDate: Date): Flow<List<Finance>>
}