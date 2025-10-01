package com.example.gestion_salle_de_jeux.ui.materiel

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import com.example.gestion_salle_de_jeux.databinding.DialogEditMaterielBinding
import com.example.gestion_salle_de_jeux.databinding.FragmentMaterielBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MaterielFragment : Fragment() {
    private var _binding: FragmentMaterielBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MaterielViewModel
    private lateinit var materielAdapter: MaterielAdapter

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

        setupRecyclerView()
        setupClickListeners()
        observeMaterielList()
    }

    private fun setupRecyclerView() {
        materielAdapter = MaterielAdapter(
            emptyList(),
            onEditClick = { materiel ->
                showEditDialog(materiel)
            },
            onDeleteClick = { materiel ->
                showDeleteConfirmationDialog(materiel)
            }
        )
        binding.rvMaterielList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = materielAdapter
        }
    }

    private fun showEditDialog(materiel: Materiel) {
        val dialogBinding = DialogEditMaterielBinding.inflate(LayoutInflater.from(requireContext()))

        // Pré-remplir les champs avec les données actuelles
        dialogBinding.etEditConsole.setText(materiel.console)
        dialogBinding.etEditNombreManette.setText(materiel.nombre_manette.toString())
        dialogBinding.etEditNombreTelevision.setText(materiel.nombre_television.toString())
        dialogBinding.etEditIdReserve.setText(
            if (materiel.id_reserve != 0) materiel.id_reserve.toString() else ""
        )

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelEdit.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSaveEdit.setOnClickListener {
            val console = dialogBinding.etEditConsole.text.toString().trim()
            val nombreManetteText = dialogBinding.etEditNombreManette.text.toString().trim()
            val nombreTelevisionText = dialogBinding.etEditNombreTelevision.text.toString().trim()
            val idReserveText = dialogBinding.etEditIdReserve.text.toString().trim()

            if (validateEditForm(console, nombreManetteText, nombreTelevisionText)) {
                val nombreManette = nombreManetteText.toShort()
                val nombreTelevision = nombreTelevisionText.toShort()
                val idReserve = if (idReserveText.isNotEmpty()) idReserveText.toInt() else 0

                val updatedMateriel = materiel.copy(
                    console = console,
                    nombre_manette = nombreManette,
                    nombre_television = nombreTelevision,
                    id_reserve = idReserve
                )

                viewModel.updateMateriel(updatedMateriel)
                Toast.makeText(requireContext(), "Matériel modifié avec succès", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun validateEditForm(console: String, nombreManetteText: String, nombreTelevisionText: String): Boolean {
        if (console.isEmpty()) {
            Toast.makeText(requireContext(), "Le champ console est obligatoire", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nombreManetteText.isEmpty() || nombreTelevisionText.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez remplir tous les champs numériques", Toast.LENGTH_SHORT).show()
            return false
        }

        val nombreManette = try {
            nombreManetteText.toShort()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Nombre de manettes invalide", Toast.LENGTH_SHORT).show()
            return false
        }

        val nombreTelevision = try {
            nombreTelevisionText.toShort()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Nombre de télévisions invalide", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nombreManette <= 0 || nombreTelevision <= 0) {
            Toast.makeText(requireContext(), "Le nombre de manette et de télévision doit être supérieur à 0", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun showDeleteConfirmationDialog(materiel: Materiel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmation de suppression")
            .setMessage("Êtes-vous sûr de vouloir supprimer le matériel \"${materiel.console}\" ?")
            .setPositiveButton("Supprimer") { dialog, which ->
                viewModel.deleteMateriel(materiel)
                Toast.makeText(requireContext(), "Matériel supprimé avec succès", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .create()
            .show()
    }

    private fun observeMaterielList() {
        lifecycleScope.launch {
            viewModel.allMateriel.collect { materielList ->
                materielAdapter.updateData(materielList)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAjouter.setOnClickListener {
            addMateriel()
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