package com.example.gestion_salle_de_jeux.ui.finance

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.databinding.FragmentFinanceBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinanceFragment : Fragment(R.layout.fragment_finance) {
    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FinanceViewModel
    private var selectedDate: Date? = null

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

        // Initialiser la base de données et le ViewModel
        val database = AppDatabase.getDatabase(requireContext())
        val financeDao = database.financeDao()
        val viewModelFactory = FinanceViewModel.FinanceViewModelFactory(financeDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[FinanceViewModel::class.java]

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnDateHeure.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnAjouterTransaction.setOnClickListener {
            addTransaction()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val timePicker = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                        selectedDate = calendar.time
                        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        binding.btnDateHeure.text = format.format(selectedDate!!)
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

        // Réinitialiser les champs après l'ajout
        binding.etMontantEntrant.text.clear()
        binding.etMontantSortant.text.clear()
        binding.etDescription.text.clear()
        selectedDate = null

        Toast.makeText(requireContext(), "Transaction ajoutée avec succès", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}