package com.example.gestion_salle_de_jeux.ui.finance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.random.Random

class FinanceViewModel : ViewModel() {

    // TODO: Injecter un Repository pour obtenir de vraies données

    // Carte Principale
    private val _weeklyBalance = MutableLiveData<String>()
    val weeklyBalance: LiveData<String> = _weeklyBalance

    private val _weeklyProfit = MutableLiveData<String>()
    val weeklyProfit: LiveData<String> = _weeklyProfit

    // Cartes Résumé
    private val _bossBalance = MutableLiveData<String>()
    val bossBalance: LiveData<String> = _bossBalance
    private val _bossChange = MutableLiveData<String>()
    val bossChange: LiveData<String> = _bossChange

    private val _materielBalance = MutableLiveData<String>()
    val materielBalance: LiveData<String> = _materielBalance
    private val _materielChange = MutableLiveData<String>()
    val materielChange: LiveData<String> = _materielChange

    private val _jetonBalance = MutableLiveData<String>()
    val jetonBalance: LiveData<String> = _jetonBalance
    private val _jetonChange = MutableLiveData<String>()
    val jetonChange: LiveData<String> = _jetonChange

    // Graphique
    private val _chartData = MutableLiveData<LineData>()
    val chartData: LiveData<LineData> = _chartData

    // Carte Totale (bas)
    private val _totalBalance = MutableLiveData<String>()
    val totalBalance: LiveData<String> = _totalBalance

    init {
        loadData()
    }

    private fun loadData() {
        _weeklyBalance.value = "290 400 Ariary"
        _weeklyProfit.value = "30 000 Ariary"

        _bossBalance.value = "96 800 Ar"
        _bossChange.value = "10 000 Ar"
        _materielBalance.value = "96 800 Ar"
        _materielChange.value = "10 000 Ar"
        _jetonBalance.value = "96 800 Ar"
        _jetonChange.value = "10 000 Ar"

        _totalBalance.value = "12 290 400 Ariary"

        _chartData.value = createDummyChartData()
    }

    private fun createDummyChartData(): LineData {
        val entries = ArrayList<Entry>()
        for (i in 0..10) {
            entries.add(Entry(i.toFloat(), Random.nextFloat() * 100))
        }
        val dataSet = LineDataSet(entries, "Statistiques")
        // TODO: Styliser le dataSet pour correspondre à la maquette
        return LineData(dataSet)
    }

    // TODO: Ajouter une fonction pour valider la Dépense
    // fun submitExpense(amount: String, source: String, description: String, date: String) { ... }
}