package com.example.gestion_salle_de_jeux.ui.finance

import androidx.lifecycle.*
import com.example.gestion_salle_de_jeux.data.entity.Finance
import com.example.gestion_salle_de_jeux.data.repository.FinanceRepository
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    // --- GESTION DES DATES ---

    private fun getStartOfDay(): Date { val c = Calendar.getInstance(); c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0); return c.time }
    private fun getEndOfDay(): Date { val c = Calendar.getInstance(); c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999); return c.time }

    private fun getStartOfMonth(): Date { val c = Calendar.getInstance(); c.set(Calendar.DAY_OF_MONTH, 1); c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); return c.time }
    private fun getEndOfMonth(): Date { val c = Calendar.getInstance(); c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH)); c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); return c.time }

    private fun getStartOfYear(): Date { val c = Calendar.getInstance(); c.set(Calendar.DAY_OF_YEAR, 1); c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); return c.time }
    private fun getEndOfYear(): Date { val c = Calendar.getInstance(); c.set(Calendar.MONTH, 11); c.set(Calendar.DAY_OF_MONTH, 31); c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); return c.time }

    // NOUVEAU : Date de départ fixe (1er Janvier 2024) pour le graphique Annuel
    private fun getStartOf2024(): Date {
        val c = Calendar.getInstance()
        c.set(2024, Calendar.JANUARY, 1, 0, 0, 0)
        return c.time
    }

    // --- DONNÉES DASHBOARD (KPIs) ---
    val dailyIncome = repository.getTotalEntrantByRange(getStartOfDay(), getEndOfDay()).asLiveData()
    val monthlyIncome = repository.getTotalEntrantByRange(getStartOfMonth(), getEndOfMonth()).asLiveData()
    val yearlyIncome = repository.getTotalEntrantByRange(getStartOfYear(), getEndOfYear()).asLiveData()

    val monthlyExpenseBoss = repository.getTotalSortantBySourceAndRange("BOSS", getStartOfMonth(), getEndOfMonth()).asLiveData()
    val monthlyExpenseMateriel = repository.getTotalSortantBySourceAndRange("MATERIEL", getStartOfMonth(), getEndOfMonth()).asLiveData()
    val monthlyExpenseJeton = repository.getTotalSortantBySourceAndRange("JETON", getStartOfMonth(), getEndOfMonth()).asLiveData()

    // --- LISTE DÉTAILLÉE ---
    private val _transactionFilterRange = MutableLiveData<Pair<Date, Date>>(Pair(getStartOfMonth(), getEndOfMonth()))
    val filteredTransactions: LiveData<List<Finance>> = _transactionFilterRange.switchMap { range ->
        repository.getTransactionsByDateRange(range.first, range.second).asLiveData()
    }

    fun setMonthFilter(date: Date) {
        val c = Calendar.getInstance().apply { time = date }
        c.set(Calendar.DAY_OF_MONTH, 1); c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        val start = c.time
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH)); c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59)
        val end = c.time
        _transactionFilterRange.value = Pair(start, end)
    }

    fun setDayFilter(date: Date) {
        val c = Calendar.getInstance().apply { time = date }
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        val start = c.time
        c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59)
        val end = c.time
        _transactionFilterRange.value = Pair(start, end)
    }


    // --- LOGIQUE DU GRAPHIQUE (CORRIGÉE POUR L'ANNÉE) ---

    private val _chartMode = MutableLiveData("DAY") // "DAY", "MONTH", "YEAR"

    val chartData: LiveData<LineData> = _chartMode.switchMap { mode ->
        val range = when (mode) {
            "DAY" -> Pair(getStartOfMonth(), getEndOfMonth()) // Jours du mois en cours
            "MONTH" -> Pair(getStartOfYear(), getEndOfYear()) // Mois de l'année en cours
            "YEAR" -> Pair(getStartOf2024(), getEndOfYear())  // Années depuis 2024
            else -> Pair(getStartOfMonth(), getEndOfMonth())
        }

        repository.getIncomesByRange(range.first, range.second).asLiveData().map { list ->
            processChartData(list, mode)
        }
    }

    fun setChartMode(mode: String) {
        _chartMode.value = mode
    }

    private fun processChartData(list: List<Finance>, mode: String): LineData {
        val entries = ArrayList<Entry>()
        val calendar = Calendar.getInstance()
        val sums = TreeMap<Int, Double>() // Utilise TreeMap pour trier automatiquement les clés (Dates)

        when (mode) {
            "DAY" -> {
                // Initialiser à 0 pour tous les jours du mois actuel
                val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                for (i in 1..maxDays) sums[i] = 0.0

                list.forEach { finance ->
                    calendar.time = finance.date_heure
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    sums[day] = (sums[day] ?: 0.0) + finance.montant_entrant
                }
            }
            "MONTH" -> {
                // Initialiser à 0 pour les 12 mois
                for (i in 0..11) sums[i] = 0.0

                list.forEach { finance ->
                    calendar.time = finance.date_heure
                    val month = calendar.get(Calendar.MONTH)
                    sums[month] = (sums[month] ?: 0.0) + finance.montant_entrant
                }
            }
            "YEAR" -> {
                // CORRECTION : Logique pour les Années (à partir de 2024)
                val startYear = 2024
                val currentYear = calendar.get(Calendar.YEAR)

                // Initialiser à 0 de 2024 jusqu'à l'année actuelle
                for (i in startYear..currentYear) sums[i] = 0.0

                list.forEach { finance ->
                    calendar.time = finance.date_heure
                    val year = calendar.get(Calendar.YEAR)
                    if (year >= startYear) {
                        sums[year] = (sums[year] ?: 0.0) + finance.montant_entrant
                    }
                }
            }
        }

        // Conversion Map -> Entries
        // Pour l'année, X sera directement l'année (ex: 2024.0, 2025.0)
        sums.forEach { (index, amount) ->
            entries.add(Entry(index.toFloat(), amount.toFloat()))
        }

        val label = when(mode) {
            "DAY" -> "Recette Journalière"
            "MONTH" -> "Recette Mensuelle"
            else -> "Recette Annuelle"
        }

        val dataSet = LineDataSet(entries, label)
        return LineData(dataSet)
    }


    // --- AFFICHAGE DASHBOARD (KPIs) ---
    val displayWeeklyBalance: LiveData<String> = monthlyIncome.map { formatMoney(it) }
    val displayDailyProfit: LiveData<String> = dailyIncome.map { formatMoney(it) }

    val displayBossBalance = MediatorLiveData<String>().apply {
        fun update() { value = formatMoney((monthlyIncome.value ?: 0.0) / 3 - (monthlyExpenseBoss.value ?: 0.0)) }
        addSource(monthlyIncome) { update() }; addSource(monthlyExpenseBoss) { update() }
    }
    val displayBossDaily = dailyIncome.map { formatMoney(it / 3) }

    val displayMaterielBalance = MediatorLiveData<String>().apply {
        fun update() { value = formatMoney((monthlyIncome.value ?: 0.0) / 3 - (monthlyExpenseMateriel.value ?: 0.0)) }
        addSource(monthlyIncome) { update() }; addSource(monthlyExpenseMateriel) { update() }
    }
    val displayMaterielDaily = dailyIncome.map { formatMoney(it / 3) }

    val displayJetonBalance = MediatorLiveData<String>().apply {
        fun update() { value = formatMoney((monthlyIncome.value ?: 0.0) / 3 - (monthlyExpenseJeton.value ?: 0.0)) }
        addSource(monthlyIncome) { update() }; addSource(monthlyExpenseJeton) { update() }
    }
    val displayJetonDaily = dailyIncome.map { formatMoney(it / 3) }

    val displayTotalBalance: LiveData<String> = yearlyIncome.map { formatMoney(it) }

    fun addExpense(amount: Double, source: String, description: String, date: Date) {
        viewModelScope.launch {
            repository.insert(Finance(date_heure = date, montant_entrant = 0.0, montant_sortant = amount, description = description, source = source))
        }
    }

    private fun formatMoney(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply { groupingSeparator = ' ' }
        return "${DecimalFormat("#,##0", symbols).format(amount)} Ar"
    }

    class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) return FinanceViewModel(repository) as T
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}