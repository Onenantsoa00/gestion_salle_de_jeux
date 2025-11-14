package com.example.gestion_salle_de_jeux.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestion_salle_de_jeux.databinding.FragmentHistoryBinding // <-- CORRIGÉ

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null // <-- CORRIGÉ
    private val binding get() = _binding!!

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        _binding = FragmentHistoryBinding.inflate(inflater, container, false) // <-- CORRIGÉ
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // Vous pouvez initialiser vos listeners ici
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        // Observer la liste de l'historique
        historyViewModel.historyItems.observe(viewLifecycleOwner) { items ->
            historyAdapter.submitList(items)
        }

        // Observer les résumés
        historyViewModel.todayRevenue.observe(viewLifecycleOwner) {
            binding.tvTodayRevenue.text = it
        }
        historyViewModel.todayExpense.observe(viewLifecycleOwner) {
            binding.tvTodayExpense.text = it
        }
        historyViewModel.yesterdayRevenue.observe(viewLifecycleOwner) {
            binding.tvYesterdayRevenue.text = it
        }
        historyViewModel.yesterdayExpense.observe(viewLifecycleOwner) {
            binding.tvYesterdayExpense.text = it
        }
    }

    private fun setupClickListeners() {
        // Exemple pour les filtres
        binding.ivFilter.setOnClickListener {
            // TODO: Afficher la logique de filtre
        }
        binding.ivListView.setOnClickListener {
            // TODO: Changer la vue (si nécessaire)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}