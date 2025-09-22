package com.example.gestion_salle_de_jeux.ui.materiel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.databinding.FragmentMaterielBinding

class MaterielFragment : Fragment() {
    private var _binding: FragmentMaterielBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MaterielViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaterielBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisation du ViewModel
        val database = AppDatabase.getDatabase(requireContext())
        val materielDao = database.materielDao()
        val viewModelFactory = MaterielViewModel.MaterielViewModelFactory(materielDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[MaterielViewModel::class.java]

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnAjouter.setOnClickListener {
            addMateriel()
        }

        // Vous pouvez ajouter les autres boutons plus tard
        binding.btnModifier.setOnClickListener {
            Toast.makeText(requireContext(), "Fonctionnalité à implémenter", Toast.LENGTH_SHORT).show()
        }

        binding.btnSupprimer.setOnClickListener {
            Toast.makeText(requireContext(), "Fonctionnalité à implémenter", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMateriel() {
        val console = binding.etConsole.text.toString().trim()
        val nombreManetteText = binding.etNombreManette.text.toString().trim()
        val nombreTelevisionText = binding.etNombreTelevision.text.toString().trim()
        val idReserveText = binding.etIdReserve.text.toString().trim()

        // Validation des champs
        if (console.isEmpty()) {
            Toast.makeText(requireContext(), "Le champ console est obligatoire", Toast.LENGTH_SHORT).show()
            return
        }

        if (nombreManetteText.isEmpty() || nombreTelevisionText.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez remplir tous les champs numériques", Toast.LENGTH_SHORT).show()
            return
        }

        val nombreManette = try {
            nombreManetteText.toShort()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Nombre de manettes invalide", Toast.LENGTH_SHORT).show()
            return
        }

        val nombreTelevision = try {
            nombreTelevisionText.toShort()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Nombre de télévisions invalide", Toast.LENGTH_SHORT).show()
            return
        }

        if (nombreManette <= 0 || nombreTelevision <= 0) {
            Toast.makeText(requireContext(), "Le nombre de manette et de télévision doit être supérieur à 0", Toast.LENGTH_SHORT).show()
            return
        }

        // ID de réserve optionnel
        val idReserve = if (idReserveText.isNotEmpty()) {
            try {
                idReserveText.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "ID de réserve invalide", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            // Valeur par défaut si non renseigné
            0
        }

        viewModel.addMateriel(console, nombreManette, nombreTelevision, idReserve)
        resetForm()
        Toast.makeText(requireContext(), "Matériel ajouté avec succès", Toast.LENGTH_SHORT).show()
    }

    private fun resetForm() {
        binding.etConsole.text?.clear()
        binding.etNombreManette.text?.clear()
        binding.etNombreTelevision.text?.clear()
        binding.etIdReserve.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}