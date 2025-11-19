package com.example.gestion_salle_de_jeux.ui.finance

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.data.repository.FinanceRepository
import com.example.gestion_salle_de_jeux.databinding.FragmentFinanceBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class FinanceFragment : Fragment() {

    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var financeViewModel: FinanceViewModel
    private val selectedDateCalendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFinanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val repository = FinanceRepository(database.financeDao())
        val factory = FinanceViewModel.FinanceViewModelFactory(repository)
        financeViewModel = ViewModelProvider(this, factory)[FinanceViewModel::class.java]

        setupUI()
        setupChartStyle()
        setupForm()
        setupClickListeners()
        observeViewModel()

        // Par défaut
        binding.togglePeriod.check(binding.btnPeriodDay.id)
        financeViewModel.setChartMode("DAY")
        updateChartXAxisFormatter("DAY")
    }

    private fun setupUI() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val dateDuJour = dateFormat.format(Date())
        binding.tvHeaderSubtitle.text = dateDuJour.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun setupChartStyle() {
        binding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisLeft.textColor = Color.WHITE
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = Color.parseColor("#33FFFFFF")
            axisRight.isEnabled = false

            xAxis.textColor = Color.WHITE
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f

            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            animateX(1000)
        }
    }

    // CORRECTION ICI : Gestion de l'axe X pour les années
    private fun updateChartXAxisFormatter(mode: String) {
        val xAxis = binding.lineChart.xAxis

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return when (mode) {
                    "DAY" -> index.toString() // 1, 2, 3...
                    "MONTH" -> {
                        val months = arrayOf("Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc")
                        if (index in 0..11) months[index] else ""
                    }
                    "YEAR" -> {
                        // Pour l'année, la valeur EST l'année (ex: 2024.0 -> "2024")
                        // On utilise String.format pour éviter "2,024" avec virgule
                        String.format(Locale.getDefault(), "%d", index)
                    }
                    else -> ""
                }
            }
        }
    }

    private fun setupForm() {
        val sources = arrayOf("BOSS", "MATERIEL", "JETON")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sources)
        binding.etSource.setAdapter(adapter)
        updateDateInView()
        binding.etDate.setOnClickListener { showDatePickerAdd { d -> selectedDateCalendar.time = d; updateDateInView() } }
    }

    private fun setupClickListeners() {
        binding.togglePeriod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.btnPeriodDay.id -> {
                        financeViewModel.setChartMode("DAY")
                        updateChartXAxisFormatter("DAY")
                    }
                    binding.btnPeriodMonth.id -> {
                        financeViewModel.setChartMode("MONTH")
                        updateChartXAxisFormatter("MONTH")
                    }
                    binding.btnPeriodYear.id -> {
                        financeViewModel.setChartMode("YEAR")
                        updateChartXAxisFormatter("YEAR")
                    }
                }
            }
        }

        binding.btnValidate.setOnClickListener {
            val amountStr = binding.etAmount.text.toString()
            val source = binding.etSource.text.toString()
            val description = binding.etDescription.text.toString()

            if (amountStr.isNotEmpty() && source.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                financeViewModel.addExpense(amount, source, description, selectedDateCalendar.time)
                Toast.makeText(requireContext(), "Dépense enregistrée !", Toast.LENGTH_SHORT).show()
                binding.etAmount.text.clear()
                binding.etDescription.text.clear()
            } else {
                Toast.makeText(requireContext(), "Veuillez remplir le montant et la source", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDetails.setOnClickListener { showDetailsDialog() }
    }

    private fun observeViewModel() {
        financeViewModel.displayWeeklyBalance.observe(viewLifecycleOwner) { binding.tvWeeklyBalance.text = it }
        financeViewModel.displayDailyProfit.observe(viewLifecycleOwner) { binding.tvWeeklyProfit.text = it }
        financeViewModel.displayBossBalance.observe(viewLifecycleOwner) { binding.tvBossBalance.text = it }
        financeViewModel.displayBossDaily.observe(viewLifecycleOwner) { binding.tvBossChange.text = it }
        financeViewModel.displayMaterielBalance.observe(viewLifecycleOwner) { binding.tvMaterielBalance.text = it }
        financeViewModel.displayMaterielDaily.observe(viewLifecycleOwner) { binding.tvMaterielChange.text = it }
        financeViewModel.displayJetonBalance.observe(viewLifecycleOwner) { binding.tvJetonBalance.text = it }
        financeViewModel.displayJetonDaily.observe(viewLifecycleOwner) { binding.tvJetonChange.text = it }
        financeViewModel.displayTotalBalance.observe(viewLifecycleOwner) { binding.tvTotalBalance.text = it }

        financeViewModel.chartData.observe(viewLifecycleOwner) { lineData ->
            if (lineData != null && lineData.dataSetCount > 0) {
                val dataSet = lineData.getDataSetByIndex(0) as LineDataSet
                dataSet.color = ContextCompat.getColor(requireContext(), R.color.dashboard_green)
                dataSet.lineWidth = 2f
                dataSet.setDrawCircles(true)
                dataSet.setCircleColor(Color.WHITE)
                dataSet.circleRadius = 4f
                dataSet.setDrawValues(false)
                dataSet.setDrawFilled(true)
                dataSet.fillColor = ContextCompat.getColor(requireContext(), R.color.dashboard_green)
                dataSet.fillAlpha = 50
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                binding.lineChart.data = lineData
                binding.lineChart.notifyDataSetChanged()
                binding.lineChart.invalidate()
                binding.lineChart.animateY(1000)
            } else {
                binding.lineChart.clear()
            }
        }
    }

    private fun showDetailsDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_finance_details, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rv_finance_details)
        val btnClose = dialogView.findViewById<Button>(R.id.btn_close_dialog)
        val btnMonth = dialogView.findViewById<Button>(R.id.btn_filter_month)
        val btnDay = dialogView.findViewById<Button>(R.id.btn_filter_day)
        val btnReset = dialogView.findViewById<Button>(R.id.btn_reset_filter)
        val tvFilterDisplay = dialogView.findViewById<TextView>(R.id.tv_current_filter_display)

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        financeViewModel.filteredTransactions.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                val adapter = FinanceAdapter(list)
                recyclerView.adapter = adapter
            }
        }

        btnReset.setOnClickListener {
            val today = Date()
            financeViewModel.setDayFilter(today)
            val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            tvFilterDisplay.text = "Période : Aujourd'hui (${format.format(today)})"
        }

        btnMonth.setOnClickListener {
            showDatePickerFilter { selectedDate ->
                financeViewModel.setMonthFilter(selectedDate)
                val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                tvFilterDisplay.text = "Mois : ${format.format(selectedDate).replaceFirstChar { it.uppercase() }}"
            }
        }

        btnDay.setOnClickListener {
            showDatePickerFilter { selectedDate ->
                financeViewModel.setDayFilter(selectedDate)
                val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                tvFilterDisplay.text = "Jour : ${format.format(selectedDate)}"
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // DatePicker pour le formulaire (bloqué au futur)
    private fun showDatePickerAdd(onDateSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        val dialog = DatePickerDialog(requireContext(), { _, y, m, d ->
            val sel = Calendar.getInstance().apply { set(y, m, d) }
            onDateSelected(sel.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    // DatePicker pour les filtres (peut aller dans le passé sans souci)
    private fun showDatePickerFilter(onDateSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        val dialog = DatePickerDialog(requireContext(), { _, y, m, d ->
            val sel = Calendar.getInstance().apply { set(y, m, d) }
            onDateSelected(sel.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun updateDateInView() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etDate.setText(format.format(selectedDateCalendar.time))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}