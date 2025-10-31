package com.example.gestion_salle_de_jeux.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gestion_salle_de_jeux.ui.dashboard.model.ActiveSession
import com.example.gestion_salle_de_jeux.ui.dashboard.model.SessionStatus
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class DashboardViewModel : ViewModel() {

    // N.B: Vous devrez injecter vos Repositories ici pour obtenir de vraies données

    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate

    private val _totalRevenue = MutableLiveData<String>()
    val totalRevenue: LiveData<String> = _totalRevenue

    private val _dailyRevenue = MutableLiveData<String>()
    val dailyRevenue: LiveData<String> = _dailyRevenue

    private val _dailyExpenses = MutableLiveData<String>()
    val dailyExpenses: LiveData<String> = _dailyExpenses

    private val _dailyProfit = MutableLiveData<String>()
    val dailyProfit: LiveData<String> = _dailyProfit

    private val _activeSessions = MutableLiveData<List<ActiveSession>>()
    val activeSessions: LiveData<List<ActiveSession>> = _activeSessions

    private val _chartData = MutableLiveData<LineData>()
    val chartData: LiveData<LineData> = _chartData

    init {
        loadData()
    }

    private fun loadData() {
        // Simule le chargement de données réelles
        // TODO: Remplacez ceci par des appels à votre Repository

        _currentDate.value = SimpleDateFormat("E. dd MMM yyyy", Locale.FRENCH).format(Date())
        _totalRevenue.value = "Recettes : 300 000 Ariary"
        _dailyRevenue.value = "+ 30 000 Ar"
        _dailyExpenses.value = "- 10 000 Ar"
        _dailyProfit.value = "+ 20 000 Ar"

        _activeSessions.value = createDummySessions()
        _chartData.value = createDummyChartData()
    }

    private fun createDummySessions(): List<ActiveSession> {
        return listOf(
            ActiveSession(
                id = "1",
                postName = "Post 1",
                consoleName = "PS5",
                players = "Laura - Davida",
                duration = "40 : 50 min",
                paymentInfo = "2 000 Ariary - Payé",
                status = SessionStatus.ONLINE,
                isPaused = true
            ),
            ActiveSession(
                id = "2",
                postName = "Post 2",
                consoleName = "PS4",
                players = "Mainty - Nekena",
                duration = "18 : 39 min",
                paymentInfo = "800 Ariary - Non Payé",
                status = SessionStatus.ONLINE,
                isPaused = true
            ),
            ActiveSession(
                id = "3",
                postName = "Post 3",
                consoleName = "PS3",
                players = "Joueur Seul",
                duration = "00 : 00 min",
                paymentInfo = "En attente",
                status = SessionStatus.BUSY,
                isPaused = true
            ),
            ActiveSession(
                id = "4",
                postName = "Post 4",
                consoleName = "PS3",
                players = "Mainty - Nekena",
                duration = "07 : 25 min",
                paymentInfo = "400 Ariary - Non Payé",
                status = SessionStatus.OFFLINE,
                isPaused = false
            )
        )
    }

    private fun createDummyChartData(): LineData {
        val entries = ArrayList<Entry>()
        for (i in 0..10) {
            entries.add(Entry(i.toFloat(), Random.nextFloat() * 100))
        }

        val dataSet = LineDataSet(entries, "Statistiques")
        // TODO: Styliser le dataSet pour correspondre à la maquette
        // (couleur verte, remplissage, etc.)

        return LineData(dataSet)
    }
}