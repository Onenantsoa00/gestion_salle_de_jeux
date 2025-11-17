package com.example.gestion_salle_de_jeux.ui.finance

import android.R
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gestion_salle_de_jeux.databinding.FragmentFinanceBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.Calendar


class FinanceFragment : Fragment() {

    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var financeViewModel: FinanceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        financeViewModel = ViewModelProvider(this).get(FinanceViewModel::class.java)
        _binding = FragmentFinanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()
        setupForm()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            xAxis.textColor = Color.WHITE
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
        }
    }

    private fun setupForm() {
        // Configurer le dropdown "Retirer de"
        val sources = arrayOf("Boss", "Materiel", "Jeton", "Autre")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, sources)
        binding.etSource.setAdapter(adapter)
        binding.etSource.setTextColor(Color.BLACK) // Les dropdowns sont parfois difficiles à styliser

        // Configurer le sélecteur de date
        binding.etDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupClickListeners() {
        binding.btnValidate.setOnClickListener {
            // TODO: Appeler le ViewModel pour valider
            Toast.makeText(requireContext(), "Dépense validée (Logique à implémenter)", Toast.LENGTH_SHORT).show()
        }

        binding.btnDetails.setOnClickListener {
            // TODO: Naviguer vers un écran de détails
            Toast.makeText(requireContext(), "Afficher les détails (Logique à implémenter)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        financeViewModel.weeklyBalance.observe(viewLifecycleOwner) { binding.tvWeeklyBalance.text = it }
        financeViewModel.weeklyProfit.observe(viewLifecycleOwner) { binding.tvWeeklyProfit.text = it }

        financeViewModel.bossBalance.observe(viewLifecycleOwner) { binding.tvBossBalance.text = it }
        financeViewModel.bossChange.observe(viewLifecycleOwner) { binding.tvBossChange.text = it }

        financeViewModel.materielBalance.observe(viewLifecycleOwner) { binding.tvMaterielBalance.text = it }
        financeViewModel.materielChange.observe(viewLifecycleOwner) { binding.tvMaterielChange.text = it }

        financeViewModel.jetonBalance.observe(viewLifecycleOwner) { binding.tvJetonBalance.text = it }
        financeViewModel.jetonChange.observe(viewLifecycleOwner) { binding.tvJetonChange.text = it }

        financeViewModel.totalBalance.observe(viewLifecycleOwner) { binding.tvTotalBalance.text = it }

        financeViewModel.chartData.observe(viewLifecycleOwner) { lineData ->
            val dataSet = lineData.getDataSetByIndex(0) as LineDataSet
            dataSet.color = ContextCompat.getColor(requireContext(), com.example.gestion_salle_de_jeux.R.color.dashboard_green)
            dataSet.valueTextColor = Color.TRANSPARENT
            dataSet.setDrawCircles(false)
            dataSet.setDrawFilled(true)
            dataSet.fillColor = ContextCompat.getColor(requireContext(), com.example.gestion_salle_de_jeux.R.color.dashboard_green)
            dataSet.fillAlpha = 80
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

            binding.lineChart.data = lineData
            binding.lineChart.invalidate()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.etDate.setText(date)
        }, year, month, day).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}