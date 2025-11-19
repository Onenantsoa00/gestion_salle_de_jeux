package com.example.gestion_salle_de_jeux.ui.materiel

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.data.entity.JeuLibrary
import com.example.gestion_salle_de_jeux.databinding.FragmentMaterielBinding
import com.example.gestion_salle_de_jeux.ui.materiel.model.MaterialUiItem
import com.example.gestion_salle_de_jeux.ui.utils.WaterBorderDrawable
import com.google.android.material.button.MaterialButton

class MaterielFragment : Fragment() {

    private var _binding: FragmentMaterielBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MaterielViewModel
    private lateinit var adapter: MaterielAdapter

    // Variable pour gérer l'animation active
    private var activeWaterEffect: WaterBorderDrawable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMaterielBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val factory = MaterielViewModel.MaterielViewModelFactory(database.materielDao(), database.jeuLibraryDao())
        viewModel = ViewModelProvider(this, factory)[MaterielViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Initialisation : On sélectionne "Matériel" par défaut
        selectMaterielTab()
    }

    private fun setupRecyclerView() {
        adapter = MaterielAdapter(
            onEditClick = { item -> showAddEditMaterielDialog(item) },
            onGamesClick = { item -> showGamesManagementDialog(item) }
        )
        binding.rvMaterielList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MaterielFragment.adapter
        }
    }

    private fun setupClickListeners() {
        // CORRECTION : Gestion manuelle des clics (plus de ToggleGroup)

        binding.btnTabMateriel.setOnClickListener {
            selectMaterielTab()
        }

        binding.btnTabJeux.setOnClickListener {
            selectJeuxTab()
        }

        binding.fabAddMateriel.setOnClickListener {
            showAddEditMaterielDialog(null)
        }
    }

    // Fonctions pour changer d'onglet proprement
    private fun selectMaterielTab() {
        // Logique Métier
        adapter.setGameMode(false)
        viewModel.setTabMode(false)
        binding.fabAddMateriel.show()

        // Logique Visuelle (Effet Eau)
        applyWaterEffect(binding.btnTabMateriel)
        clearWaterEffect(binding.btnTabJeux)
    }

    private fun selectJeuxTab() {
        // Logique Métier
        adapter.setGameMode(true)
        viewModel.setTabMode(true)
        binding.fabAddMateriel.hide()

        // Logique Visuelle (Effet Eau)
        applyWaterEffect(binding.btnTabJeux)
        clearWaterEffect(binding.btnTabMateriel)
    }

    private fun observeViewModel() {
        viewModel.displayList.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            adapter.notifyDataSetChanged()
        }
    }

    // ================================================================================
    // EFFET VISUEL EAU (Water Flow)
    // ================================================================================

    private fun applyWaterEffect(button: MaterialButton) {
        // 1. Créer l'effet d'eau
        val waterDrawable = WaterBorderDrawable(
            strokeWidth = 8f,
            cornerRadius = button.cornerRadius.toFloat()
        )

        // 2. Créer le fond normal du bouton
        // Note : on utilise la couleur de fond du dashboard pour que l'intérieur soit opaque
        val backgroundDrawable = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.dashboard_background))

        // 3. Combiner (Fond + Bordure Eau par dessus)
        val layers = arrayOf(backgroundDrawable, waterDrawable)
        val layerDrawable = LayerDrawable(layers)

        // 4. Appliquer au bouton
        button.backgroundTintList = null // Important : désactiver le tint par défaut
        button.background = layerDrawable
        button.setTextColor(Color.WHITE) // Texte brillant pour l'actif
    }

    private fun clearWaterEffect(button: MaterialButton) {
        // On remet le bouton à son état "inactif"
        button.background = null
        // On réapplique le tint pour avoir la couleur de fond simple
        button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.dashboard_background)
        button.strokeWidth = 0
        button.setTextColor(Color.GRAY) // Texte grisé pour l'inactif
    }

    // ================================================================================
    // GESTION AVANCÉE DES JEUX (Modifier / Supprimer)
    // ================================================================================
    private fun showGamesManagementDialog(consoleItem: MaterialUiItem) {
        val context = requireContext()

        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 20)
        }

        val title = TextView(context).apply {
            text = "Jeux sur ${consoleItem.name}"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 30 }
        }
        mainLayout.addView(title)

        val scrollContainer = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply { weight = 1f }
        }

        val listLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollContainer.addView(listLayout)
        mainLayout.addView(scrollContainer)

        val addLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 20 }
        }

        val inputGame = EditText(context).apply {
            hint = "Nouveau jeu..."
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 1f }
        }

        val btnAdd = Button(context).apply {
            text = "Ajouter"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        addLayout.addView(inputGame)
        addLayout.addView(btnAdd)
        mainLayout.addView(addLayout)

        val dialog = AlertDialog.Builder(context)
            .setView(mainLayout)
            .setNegativeButton("Fermer", null)
            .create()

        btnAdd.setOnClickListener {
            val gameName = inputGame.text.toString().trim()
            if (gameName.isNotEmpty()) {
                viewModel.addGameToConsole(consoleItem.id, gameName)
                inputGame.text.clear()
            }
        }

        viewModel.getGamesForConsole(consoleItem.id).observe(viewLifecycleOwner) { games ->
            listLayout.removeAllViews()

            if (games.isEmpty()) {
                val emptyView = TextView(context).apply {
                    text = "Aucun jeu installé."
                    setPadding(10, 20, 10, 20)
                }
                listLayout.addView(emptyView)
            } else {
                games.forEach { jeu ->
                    val row = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(0, 15, 0, 15)
                    }

                    val tvName = TextView(context).apply {
                        text = "• ${jeu.nom_jeu}"
                        textSize = 16f
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 1f }
                    }

                    val btnEdit = ImageView(context).apply {
                        setImageResource(android.R.drawable.ic_menu_edit)
                        setColorFilter(ContextCompat.getColor(context, R.color.dashboard_blue))
                        setPadding(15, 10, 15, 10)
                        setOnClickListener { showEditGameDialog(jeu) }
                    }

                    val btnDelete = ImageView(context).apply {
                        setImageResource(android.R.drawable.ic_menu_delete)
                        setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                        setPadding(15, 10, 15, 10)
                        setOnClickListener { showDeleteConfirmDialog(jeu) }
                    }

                    row.addView(tvName)
                    row.addView(btnEdit)
                    row.addView(btnDelete)

                    val divider = View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                        setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                        alpha = 0.2f
                    }

                    listLayout.addView(row)
                    listLayout.addView(divider)
                }
            }
        }
        dialog.show()
    }

    private fun showEditGameDialog(jeu: JeuLibrary) {
        val input = EditText(requireContext())
        input.setText(jeu.nom_jeu)
        input.setSelection(input.text.length)

        AlertDialog.Builder(requireContext())
            .setTitle("Modifier le jeu")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.updateGame(jeu, newName)
                    Toast.makeText(requireContext(), "Modifié !", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showDeleteConfirmDialog(jeu: JeuLibrary) {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer ce jeu ?")
            .setMessage("Voulez-vous vraiment supprimer '${jeu.nom_jeu}' ?")
            .setPositiveButton("Oui, Supprimer") { _, _ ->
                viewModel.deleteGame(jeu)
                Toast.makeText(requireContext(), "Supprimé !", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    // ================================================================================
    // GESTION MATÉRIEL
    // ================================================================================
    private fun showAddEditMaterielDialog(itemToEdit: MaterialUiItem?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_materiel, null)
        val etNom = dialogView.findViewById<EditText>(R.id.et_materiel_nom)
        val etQuantiteTotal = dialogView.findViewById<EditText>(R.id.et_materiel_quantite)
        val etQuantiteUtilise = dialogView.findViewById<EditText>(R.id.et_materiel_utilise)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rg_type)

        if (itemToEdit != null) {
            etNom.setText(itemToEdit.name)
            etQuantiteTotal.setText(itemToEdit.count.toString())
            etQuantiteUtilise.setText("0")
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (itemToEdit == null) "Ajouter Matériel" else "Modifier Matériel")
            .setView(dialogView)
            .setPositiveButton("Enregistrer", null)
            .setNegativeButton("Annuler", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val nom = etNom.text.toString()
                val quantiteTotal = etQuantiteTotal.text.toString().toIntOrNull() ?: 0
                val quantiteUtilise = etQuantiteUtilise.text.toString().toIntOrNull() ?: 0
                val type = when (rgType.checkedRadioButtonId) {
                    R.id.rb_console -> "CONSOLE"
                    R.id.rb_tv -> "ECRAN"
                    else -> "ACCESSOIRE"
                }

                if (nom.isNotEmpty() && quantiteTotal >= quantiteUtilise) {
                    if (itemToEdit == null) viewModel.addMateriel(nom, quantiteTotal, quantiteUtilise, type)
                    else viewModel.updateMateriel(itemToEdit.id, nom, quantiteTotal, quantiteUtilise, type)
                    dialog.dismiss()
                } else {
                    if(nom.isEmpty()) etNom.error = "Requis"
                    if(quantiteTotal < quantiteUtilise) etQuantiteTotal.error = "Erreur qté"
                }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activeWaterEffect?.stop()
        _binding = null
    }
}