package com.example.gestion_salle_de_jeux.ui.GameRoomFragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.ViewModelProvider
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.data.entity.Jeux
import com.example.gestion_salle_de_jeux.data.entity.Playeur
import com.example.gestion_salle_de_jeux.databinding.DialogGameSessionBinding
import com.example.gestion_salle_de_jeux.databinding.FragmentGameroomBinding
import com.example.gestion_salle_de_jeux.ui.materiel.MaterielViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GameRoomFragment : Fragment() {

    private var _binding: FragmentGameroomBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MaterielViewModel
    private lateinit var consoleAdapter: ConsoleAdapter
    private var jeuxList: List<Jeux> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameroomBinding.inflate(inflater, container, false)
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
        observeConsoles()
        loadGames()
    }

    private fun setupRecyclerView() {
        consoleAdapter = ConsoleAdapter(
            emptyList(),
            onConsoleClick = { materiel ->
                // Ouvrir le dialogue de session de jeu
                showGameSessionDialog(materiel)
            },
            onActionClick = { materiel ->
                // Action pour démarrer/arrêter la session
                startGameSession(materiel)
            }
        )

        binding.rvConsoles.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = consoleAdapter
        }
    }

    private fun loadGames() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(requireContext())
                jeuxList = database.jeuxDao().getAllJeux()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur chargement jeux: ${e.message}", Toast.LENGTH_SHORT).show()
                jeuxList = emptyList()
            }
        }
    }

    private fun showGameSessionDialog(materiel: com.example.gestion_salle_de_jeux.data.entity.Materiel) {
        val dialogBinding = DialogGameSessionBinding.inflate(LayoutInflater.from(requireContext()))

        // Configuration de l'AutoCompleteTextView pour les jeux
        setupGameAutoComplete(dialogBinding.actvGameSelection, dialogBinding.etGameType)

        // Configuration de la conversion argent/temps
        setupAmountToTimeConversion(dialogBinding)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnStart.setOnClickListener {
            if (validateSessionForm(dialogBinding)) {
                startGameSessionFromDialog(materiel, dialogBinding)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun setupGameAutoComplete(autoComplete: AutoCompleteTextView, gameTypeField: com.google.android.material.textfield.TextInputEditText) {
        if (jeuxList.isEmpty()) {
            autoComplete.isEnabled = false
            autoComplete.hint = "Aucun jeu disponible"
            return
        }

        val gameTitles = jeuxList.map { it.titre }.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, gameTitles)
        autoComplete.setAdapter(adapter)

        autoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedGame = jeuxList[position]
            gameTypeField.setText(selectedGame.type)
        }
    }

    private fun setupAmountToTimeConversion(dialogBinding: DialogGameSessionBinding) {
        dialogBinding.etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                calculateAndDisplayTime(dialogBinding)
            }
        }

        // Écouteur pour les changements de texte en temps réel
        dialogBinding.etAmount.setOnKeyListener { _, _, _ ->
            calculateAndDisplayTime(dialogBinding)
            false
        }
    }

    private fun calculateAndDisplayTime(dialogBinding: DialogGameSessionBinding) {
        val amountText = dialogBinding.etAmount.text.toString().trim()
        if (amountText.isNotEmpty()) {
            try {
                val amount = amountText.toInt()
                val minutes = (amount / 400.0 * 10).toInt()
                dialogBinding.tvCalculatedTime.text = "Temps: ${minutes} minutes"

                // Afficher un message si le montant est insuffisant
                if (amount < 400) {
                    dialogBinding.tvCalculatedTime.text = "Minimum 400 Ariary (10 min)"
                    dialogBinding.tvCalculatedTime.setTextColor(
                        requireContext().getColor(android.R.color.holo_red_dark)
                    )
                } else {
                    dialogBinding.tvCalculatedTime.setTextColor(
                        requireContext().getColor(R.color.teal_700)
                    )
                }
            } catch (e: NumberFormatException) {
                dialogBinding.tvCalculatedTime.text = "Montant invalide"
                dialogBinding.tvCalculatedTime.setTextColor(
                    requireContext().getColor(android.R.color.holo_red_dark)
                )
            }
        } else {
            dialogBinding.tvCalculatedTime.text = "Temps: 0 minutes"
            dialogBinding.tvCalculatedTime.setTextColor(
                requireContext().getColor(R.color.teal_700)
            )
        }
    }

    private fun validateSessionForm(dialogBinding: DialogGameSessionBinding): Boolean {
        val selectedGame = dialogBinding.actvGameSelection.text.toString().trim()
        val amount = dialogBinding.etAmount.text.toString().trim()
        val playerName = dialogBinding.etPlayerName.text.toString().trim()

        if (selectedGame.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez sélectionner un jeu", Toast.LENGTH_SHORT).show()
            dialogBinding.actvGameSelection.requestFocus()
            return false
        }

        if (amount.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez entrer un montant", Toast.LENGTH_SHORT).show()
            dialogBinding.etAmount.requestFocus()
            return false
        }

        if (playerName.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez entrer le nom du joueur", Toast.LENGTH_SHORT).show()
            dialogBinding.etPlayerName.requestFocus()
            return false
        }

        val amountValue = try {
            amount.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Montant invalide", Toast.LENGTH_SHORT).show()
            dialogBinding.etAmount.requestFocus()
            return false
        }

        if (amountValue < 400) {
            Toast.makeText(requireContext(), "Le montant minimum est de 400 Ariary", Toast.LENGTH_SHORT).show()
            dialogBinding.etAmount.requestFocus()
            return false
        }

        return true
    }

    private fun startGameSessionFromDialog(materiel: com.example.gestion_salle_de_jeux.data.entity.Materiel, dialogBinding: DialogGameSessionBinding) {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(requireContext())

                // Récupérer les données du formulaire
                val selectedGameTitle = dialogBinding.actvGameSelection.text.toString().trim()
                val amount = dialogBinding.etAmount.text.toString().toInt()
                val playerName = dialogBinding.etPlayerName.text.toString().trim()

                // Trouver le jeu sélectionné
                val selectedGame = jeuxList.find { it.titre == selectedGameTitle }

                if (selectedGame != null) {
                    // Créer un nouveau joueur
                    val newPlayer = Playeur(
                        nom = playerName,
                        prenom = "", // Vous pouvez ajouter un champ prénom si nécessaire
                        id_tournoi = 0 // 0 si pas de tournoi
                    )

                    // CORRECTION : insertPlayeur retourne un Long, on le convertit en Int
                    val playerId = database.playeurDao().insertPlayeur(newPlayer).toInt()

                    // TODO: Créer une entrée finance si nécessaire
                    // val finance = Finance(montant = amount, date = LocalDateTime.now(), ...)
                    // val financeId = database.financeDao().insertFinance(finance).toInt()

                    // Créer la session de jeu
                    val gameSession = Jeux(
                        titre = selectedGame.titre,
                        type = selectedGame.type,
                        id_playeur = playerId,
                        id_materiel = materiel.id,
                        id_finance = 0 // Remplacez par l'ID finance créé
                    )

                    database.jeuxDao().insertJeux(gameSession)

                    // Mettre à jour le matériel comme réservé
                    val updatedMateriel = materiel.copy(id_reserve = gameSession.id)
                    database.materielDao().updateMateriel(updatedMateriel)

                    Toast.makeText(requireContext(),
                        "Session démarrée avec succès!\n$playerName joue à ${selectedGame.titre}",
                        Toast.LENGTH_LONG
                    ).show()

                    // Recharger les données pour mettre à jour l'interface
                    observeConsoles()

                } else {
                    Toast.makeText(requireContext(), "Jeu non trouvé", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun observeConsoles() {
        lifecycleScope.launch {
            try {
                viewModel.allMateriel.collect { materielList ->
                    consoleAdapter.updateData(materielList)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur chargement consoles: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAddConsole.setOnClickListener {
            Toast.makeText(requireContext(), "Ajouter une nouvelle console", Toast.LENGTH_SHORT).show()
            // Navigation vers le fragment d'ajout de matériel
            // val navController = findNavController()
            // navController.navigate(R.id.action_gameRoomFragment_to_materielFragment)
        }

        binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "Activé" else "Désactivé"
            binding.tvPowerStatus.text = "Alimentation: $status"
            Toast.makeText(requireContext(), "Coupure de courant $status", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGameSession(materiel: com.example.gestion_salle_de_jeux.data.entity.Materiel) {
        // Cette méthode peut être utilisée pour d'autres actions
        // Par exemple, arrêter une session en cours
        if (materiel.id_reserve != 0) {
            lifecycleScope.launch {
                try {
                    val database = AppDatabase.getDatabase(requireContext())

                    // Réinitialiser le matériel
                    val updatedMateriel = materiel.copy(id_reserve = 0)
                    database.materielDao().updateMateriel(updatedMateriel)

                    Toast.makeText(requireContext(), "Session arrêtée: ${materiel.console}", Toast.LENGTH_SHORT).show()
                    observeConsoles()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Si la console est libre, ouvrir le dialogue
            showGameSessionDialog(materiel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}