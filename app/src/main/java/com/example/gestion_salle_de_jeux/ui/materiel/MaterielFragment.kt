package com.example.gestion_salle_de_jeux.ui.materiel

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.databinding.FragmentMaterielBinding
import com.example.gestion_salle_de_jeux.ui.materiel.model.MaterialUiItem

class MaterielFragment : Fragment() {

    private var _binding: FragmentMaterielBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MaterielViewModel
    private lateinit var adapter: MaterielAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaterielBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val dao = database.materielDao()
        val factory = MaterielViewModel.MaterielViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[MaterielViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MaterielAdapter { item ->
            showAddEditDialog(item)
        }
        binding.rvMaterielList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MaterielFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddMateriel.setOnClickListener {
            showAddEditDialog(null)
        }
        // Ici, ajoutez vos listeners pour les onglets (Toggle Group) si nécessaire
    }

    private fun observeViewModel() {
        viewModel.materialList.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    private fun showAddEditDialog(itemToEdit: MaterialUiItem?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_materiel, null)

        val etNom = dialogView.findViewById<EditText>(R.id.et_materiel_nom)
        val etQuantiteTotal = dialogView.findViewById<EditText>(R.id.et_materiel_quantite)
        val etQuantiteUtilise = dialogView.findViewById<EditText>(R.id.et_materiel_utilise)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rg_type)

        // Pré-remplissage si modification
        if (itemToEdit != null) {
            etNom.setText(itemToEdit.name)
            etQuantiteTotal.setText(itemToEdit.count.toString())
            // On met 0 par défaut car on n'a pas l'info brute dans l'item UI,
            // mais l'utilisateur peut le corriger manuellement.
            etQuantiteUtilise.setText("0")
        }

        // Création de la dialogue SANS définir le listener du bouton positif tout de suite
        // pour éviter qu'elle ne se ferme automatiquement en cas d'erreur.
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (itemToEdit == null) "Ajouter Matériel" else "Modifier Matériel")
            .setView(dialogView)
            .setPositiveButton("Enregistrer", null) // On met null ici intentionnellement
            .setNegativeButton("Annuler", null)
            .create()

        // On définit le comportement du bouton une fois la dialogue affichée
        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val nom = etNom.text.toString().trim()
                val quantiteTotal = etQuantiteTotal.text.toString().toIntOrNull() ?: 0
                val quantiteUtilise = etQuantiteUtilise.text.toString().toIntOrNull() ?: 0

                val type = when (rgType.checkedRadioButtonId) {
                    R.id.rb_console -> "CONSOLE"
                    R.id.rb_tv -> "ECRAN"
                    else -> "ACCESSOIRE"
                }

                // --- DÉBUT DES VALIDATIONS ---

                var isValid = true

                if (nom.isEmpty()) {
                    etNom.error = "Le nom est requis"
                    isValid = false
                }

                // LA VALIDATION QUE TU AS DEMANDÉE
                if (quantiteTotal < quantiteUtilise) {
                    etQuantiteTotal.error = "Impossible : Total < Utilisé"
                    etQuantiteUtilise.error = "Vérifiez vos quantités"
                    Toast.makeText(requireContext(), "Erreur : Vous ne pouvez pas utiliser plus de matériel que vous n'en possédez !", Toast.LENGTH_LONG).show()
                    isValid = false
                }

                if (isValid) {
                    // Tout est bon, on sauvegarde
                    if (itemToEdit == null) {
                        viewModel.addMateriel(nom, quantiteTotal, quantiteUtilise, type)
                    } else {
                        viewModel.updateMateriel(itemToEdit.id, nom, quantiteTotal, quantiteUtilise, type)
                    }
                    dialog.dismiss() // On ferme la dialogue manuellement
                }
                // Si isValid est false, la dialogue reste ouverte et l'utilisateur peut corriger
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}