package com.example.gestion_salle_de_jeux.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.databinding.FragmentDashboardBinding
// Ces imports fonctionneront après la synchronisation de Gradle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var sessionAdapter: ActiveSessionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupChart()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        sessionAdapter = ActiveSessionAdapter()
        binding.rvActiveSessions.apply {
            adapter = sessionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupChart() {
        // Style de base pour le graphique pour correspondre à la maquette
        binding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)
            setViewPortOffsets(0f, 0f, 0f, 0f) // Enlève tout padding
        }
    }

    private fun observeViewModel() {
        dashboardViewModel.currentDate.observe(viewLifecycleOwner) {
            binding.tvHeaderDate.text = it
        }
        dashboardViewModel.totalRevenue.observe(viewLifecycleOwner) {
            binding.tvTotalRevenue.text = it
        }
        dashboardViewModel.dailyRevenue.observe(viewLifecycleOwner) {
            binding.tvDailyRevenue.text = it
        }
        dashboardViewModel.dailyExpenses.observe(viewLifecycleOwner) {
            binding.tvDailyExpenses.text = it
        }
        dashboardViewModel.dailyProfit.observe(viewLifecycleOwner) {
            binding.tvDailyProfit.text = it
        }

        dashboardViewModel.activeSessions.observe(viewLifecycleOwner) { sessions ->
            sessionAdapter.submitList(sessions)
        }

        dashboardViewModel.chartData.observe(viewLifecycleOwner) { lineData ->
            styleChartData(lineData)
            binding.lineChart.data = lineData
            binding.lineChart.invalidate()
        }
    }

    private fun styleChartData(lineData: LineData) {
        val dataSet = lineData.getDataSetByIndex(0) as LineDataSet
        dataSet.apply {
            color = ContextCompat.getColor(requireContext(), R.color.dashboard_green)
            valueTextColor = Color.TRANSPARENT
            setDrawCircles(false)
            setDrawFilled(true)

            // --- MODIFICATION ICI ---
            // Nous utilisons une couleur de remplissage + alpha, au lieu d'un drawable
            fillColor = ContextCompat.getColor(requireContext(), R.color.dashboard_green)
            fillAlpha = 80 // Transparence (0 = transparent, 255 = opaque)
            // --- FIN DE LA MODIFICATION ---

            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 3f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}