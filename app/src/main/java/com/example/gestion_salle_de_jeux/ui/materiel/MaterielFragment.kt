package com.example.gestion_salle_de_jeux.ui.materiel

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.text.InputType
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
        binding.btnTabMateriel.setOnClickListener { selectMaterielTab() }
        binding.btnTabJeux.setOnClickListener { selectJeuxTab() }
        binding.fabAddMateriel.setOnClickListener { showAddEditMaterielDialog(null) }
    }

    private fun selectMaterielTab() {
        adapter.setGameMode(false)
        viewModel.setTabMode(false)
        binding.fabAddMateriel.show()
        applyWaterEffect(binding.btnTabMateriel)
        clearWaterEffect(binding.btnTabJeux)
    }

    private fun selectJeuxTab() {
        adapter.setGameMode(true)
        viewModel.setTabMode(true)
        binding.fabAddMateriel.hide()
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
    // GESTION AVANCÉE DES JEUX (CORRIGÉE POUR INCLURE PRIX ET DURÉE)
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
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 30 }
        }
        mainLayout.addView(title)

        val scrollContainer = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0
            ).apply { weight = 1f }
        }

        val listLayout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        scrollContainer.addView(listLayout)
        mainLayout.addView(scrollContainer)

        // --- FORMULAIRE D'AJOUT ---
        val addTitle = TextView(context).apply { text = "Ajouter un jeu :"; textSize = 14f; setTypeface(null, Typeface.BOLD); top = 20 }
        mainLayout.addView(addTitle)

        val inputLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        // Champ 1 : Nom
        val inputGame = EditText(context).apply { hint = "Nom du jeu (ex: PES 2024)" }

        // Champ 2 : Prix
        val inputPrice = EditText(context).apply {
            hint = "Tarif (Ar) (ex: 400)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        // Champ 3 : Durée
        val inputDuration = EditText(context).apply {
            hint = "Durée (min) (ex: 10)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val btnAdd = Button(context).apply { text = "Ajouter le jeu" }

        inputLayout.addView(inputGame)
        inputLayout.addView(inputPrice)
        inputLayout.addView(inputDuration)
        inputLayout.addView(btnAdd)
        mainLayout.addView(inputLayout)

        val dialog = AlertDialog.Builder(context)
            .setView(mainLayout)
            .setNegativeButton("Fermer", null)
            .create()

        btnAdd.setOnClickListener {
            val gameName = inputGame.text.toString().trim()
            val priceStr = inputPrice.text.toString().trim()
            val durationStr = inputDuration.text.toString().trim()

            if (gameName.isNotEmpty() && priceStr.isNotEmpty() && durationStr.isNotEmpty()) {
                val price = priceStr.toDoubleOrNull() ?: 0.0
                val duration = durationStr.toIntOrNull() ?: 0

                // Appel ViewModel avec les 4 paramètres
                viewModel.addGameToConsole(consoleItem.id, gameName, price, duration)

                inputGame.text.clear()
                inputPrice.text.clear()
                inputDuration.text.clear()
                Toast.makeText(context, "Jeu ajouté !", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Veuillez tout remplir", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.getGamesForConsole(consoleItem.id).observe(viewLifecycleOwner) { games ->
            listLayout.removeAllViews()
            if (games.isEmpty()) {
                val emptyView = TextView(context).apply { text = "Aucun jeu."; setPadding(10, 20, 10, 20) }
                listLayout.addView(emptyView)
            } else {
                games.forEach { jeu ->
                    val row = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(0, 15, 0, 15)
                    }

                    // Affichage : Nom + Tarif
                    val tvName = TextView(context).apply {
                        text = "${jeu.nom_jeu}\n(${jeu.tarif_par_tranche.toInt()}Ar / ${jeu.duree_tranche_min}min)"
                        textSize = 14f
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
                    listLayout.addView(row)

                    // Séparateur
                    val divider = View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                        setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                        alpha = 0.2f
                    }
                    listLayout.addView(divider)
                }
            }
        }
        dialog.show()
    }

    // --- MODIFICATION D'UN JEU ---
    private fun showEditGameDialog(jeu: JeuLibrary) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val inputName = EditText(context).apply { setText(jeu.nom_jeu); hint = "Nom" }
        val inputPrice = EditText(context).apply {
            setText(jeu.tarif_par_tranche.toInt().toString())
            hint = "Prix"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val inputDuration = EditText(context).apply {
            setText(jeu.duree_tranche_min.toString())
            hint = "Durée (min)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(inputName)
        layout.addView(inputPrice)
        layout.addView(inputDuration)

        AlertDialog.Builder(context)
            .setTitle("Modifier le jeu")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val name = inputName.text.toString().trim()
                val price = inputPrice.text.toString().toDoubleOrNull() ?: jeu.tarif_par_tranche
                val duration = inputDuration.text.toString().toIntOrNull() ?: jeu.duree_tranche_min

                if (name.isNotEmpty()) {
                    viewModel.updateGame(jeu, name, price, duration)
                    Toast.makeText(context, "Modifié !", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showDeleteConfirmDialog(jeu: JeuLibrary) {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer ?")
            .setMessage("Supprimer '${jeu.nom_jeu}' ?")
            .setPositiveButton("Oui") { _, _ ->
                viewModel.deleteGame(jeu)
                Toast.makeText(requireContext(), "Supprimé !", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    // --- GESTION EFFET EAU (Reste inchangé) ---
    private fun applyWaterEffect(button: MaterialButton) {
        val waterDrawable = WaterBorderDrawable(strokeWidth = 8f, cornerRadius = button.cornerRadius.toFloat())
        val bg = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.dashboard_background)).mutate()
        val layers = arrayOf(bg, waterDrawable)
        button.backgroundTintList = null
        button.background = LayerDrawable(layers)
        button.setTextColor(Color.WHITE)
        waterDrawable.start()
        activeWaterEffect?.stop()
        activeWaterEffect = waterDrawable
    }

    private fun clearWaterEffect(button: MaterialButton) {
        button.background = null
        button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.dashboard_background)
        button.strokeWidth = 0
        button.setTextColor(Color.GRAY)
    }

    // --- DIALOGUE AJOUT MATERIEL (Reste inchangé, je le remets pour la complétion) ---
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