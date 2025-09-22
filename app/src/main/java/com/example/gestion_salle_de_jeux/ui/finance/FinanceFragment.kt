package com.example.gestion_salle_de_jeux.ui.finance

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.databinding.FragmentFinanceBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinanceFragment : Fragment() {
    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FinanceViewModel
    private var selectedDate: Date? = null
    private lateinit var adapter: FinanceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisation
        val database = AppDatabase.getDatabase(requireContext())
        val financeDao = database.financeDao()
        val viewModelFactory = FinanceViewModel.FinanceViewModelFactory(financeDao)
        viewModel = viewModels<FinanceViewModel> { viewModelFactory }.value

        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = FinanceAdapter(emptyList())
        binding.rvFinanceList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFinanceList.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnDateHeure.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnAjouterTransaction.setOnClickListener {
            addTransaction()
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.finances.collect { finances ->
                adapter.updateData(finances)
            }
        }

        lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                updateStats(stats)
            }
        }
    }

    private fun updateStats(stats: FinanceViewModel.FinanceStats) {
        binding.tvTotalEntrees.text = String.format(Locale.getDefault(), "%.0f FCFA", stats.totalEntrees)
        binding.tvTotalSorties.text = String.format(Locale.getDefault(), "%.0f FCFA", stats.totalSorties)

        val soldeText = String.format(Locale.getDefault(), "%.0f FCFA", stats.soldeTotal)
        binding.tvTotalSolde.text = soldeText

        // Changer la couleur selon le solde
        val color = if (stats.soldeTotal >= 0) {
            ContextCompat.getColor(requireContext(), R.color.green)
        } else {
            ContextCompat.getColor(requireContext(), R.color.red)
        }
        binding.tvTotalSolde.setTextColor(color)
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            R.style.DatePickerTheme,
            { _, year, month, dayOfMonth ->
                val timePicker = TimePickerDialog(
                    requireContext(),
                    R.style.TimePickerTheme,
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                        selectedDate = calendar.time
                        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        binding.btnDateHeure.setText(format.format(selectedDate!!))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun addTransaction() {
        val montantEntrant = binding.etMontantEntrant.text.toString().toDoubleOrNull() ?: 0.0
        val montantSortant = binding.etMontantSortant.text.toString().toDoubleOrNull() ?: 0.0
        val description = binding.etDescription.text.toString().trim()

        if (selectedDate == null) {
            Toast.makeText(requireContext(), "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez saisir une description", Toast.LENGTH_SHORT).show()
            return
        }

        if (montantEntrant == 0.0 && montantSortant == 0.0) {
            Toast.makeText(requireContext(), "Veuillez saisir un montant entrant ou sortant", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addFinance(selectedDate!!, montantEntrant, montantSortant, description)

        // Réinitialiser les champs
        resetForm()
        Toast.makeText(requireContext(), "Transaction ajoutée avec succès", Toast.LENGTH_SHORT).show()
    }

    private fun resetForm() {
        binding.etMontantEntrant.text?.clear()
        binding.etMontantSortant.text?.clear()
        binding.etDescription.text?.clear()
        binding.btnDateHeure.setText("Sélectionner la date et l'heure")
        selectedDate = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}