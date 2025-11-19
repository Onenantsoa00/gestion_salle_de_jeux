package com.example.gestion_salle_de_jeux.data.repository

import com.example.gestion_salle_de_jeux.data.dao.FinanceDao
import com.example.gestion_salle_de_jeux.data.entity.Finance
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class FinanceRepository @Inject constructor(
    private val financeDao: FinanceDao
) {
    // CRUD
    val allFinances: Flow<List<Finance>> = financeDao.getAllFinance()
    suspend fun insert(finance: Finance) = financeDao.insert(finance)
    suspend fun update(finance: Finance) = financeDao.update(finance)
    suspend fun delete(finance: Finance) = financeDao.delete(finance)

    // Analytics
    fun getTransactionsByDateRange(startDate: Date, endDate: Date) =
        financeDao.getTransactionsByDateRange(startDate, endDate)

    fun getTotalEntrantByRange(startDate: Date, endDate: Date) =
        financeDao.getTotalEntrantByRange(startDate, endDate)

    fun getTotalSortantBySourceAndRange(source: String, startDate: Date, endDate: Date) =
        financeDao.getTotalSortantBySourceAndRange(source, startDate, endDate)

    // Pour le graphique
    fun getIncomesByRange(startDate: Date, endDate: Date) =
        financeDao.getIncomesByRange(startDate, endDate)
}