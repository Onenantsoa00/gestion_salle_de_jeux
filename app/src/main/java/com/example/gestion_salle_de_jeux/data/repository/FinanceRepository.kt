package com.example.gestion_salle_de_jeux.data.repository

import com.example.gestion_salle_de_jeux.data.dao.FinanceDao
import com.example.gestion_salle_de_jeux.data.entity.Finance
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FinanceRepository @Inject constructor(
    private val financeDao: FinanceDao
) {
    val allFinances: Flow<List<Finance>> = financeDao.getAllFinance()

    suspend fun insert(finance: Finance) = financeDao.insertFinance(finance)

    suspend fun update(finance: Finance) = financeDao.updateFinance(finance)

    suspend fun delete(finance: Finance) = financeDao.deleteFinance(finance)
}