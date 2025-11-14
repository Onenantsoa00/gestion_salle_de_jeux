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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.data.entity.Jeux
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import com.example.gestion_salle_de_jeux.data.entity.Playeur
import com.example.gestion_salle_de_jeux.databinding.DialogGameSessionBinding
import com.example.gestion_salle_de_jeux.databinding.FragmentGameroomBinding
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.GameSession
import com.example.gestion_salle_de_jeux.ui.gameroom.GameRoomViewModel
import com.example.gestion_salle_de_jeux.ui.materiel.MaterielViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameRoomFragment : Fragment(), GameSessionAdapter.OnSessionControlsListener {

    private var _binding: FragmentGameroomBinding? = null
    private val binding get() = _binding!!

    // Nouveaux ViewModel et Adapter pour la nouvelle UI
    private lateinit var gameRoomViewModel: GameRoomViewModel
    private lateinit var sessionAdapter: GameSessionAdapter // Cette ligne est maintenant correcte

    // ViewModel existant pour la logique de démarrage de session
    private lateinit var materielViewModel: MaterielViewModel

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

        // Initialisation du ViewModel pour la liste des sessions (Nouvelle UI)
        gameRoomViewModel = ViewModelProvider(this)[GameRoomViewModel::class.java]

        // Initialisation du ViewModel pour le Matériel (Logique existante)
        val database = AppDatabase.getDatabase(requireContext())
        val materielDao = database.materielDao()
        val viewModelFactory = MaterielViewModel.MaterielViewModelFactory(materielDao)
        materielViewModel = ViewModelProvider(this, viewModelFactory)[MaterielViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadGames() // Charge la liste des jeux pour le dialogue
    }

    private fun setupRecyclerView() {
        sessionAdapter = GameSessionAdapter(this) // 'this' implémente l'interface
        binding.rvGameSessions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sessionAdapter
        }
    }

    private fun observeViewModel() {
        // Observe les sessions actives (factices pour l'instant)
        // Cette ligne est maintenant correcte car le type de 'sessions' peut être déduit
        gameRoomViewModel.gameSessions.observe(viewLifecycleOwner) { sessions ->
            sessionAdapter.submitList(sessions)
        }

        // TODO: Vous devrez remplacer les données factices par de vraies données
        // en observant votre base de données (probablement les 'Jeux' actifs)
    }

    private fun setupClickListeners() {
        // Bouton '+' flottant
        binding.fabAddSession.setOnClickListener {
            showAvailableConsolesDialog()
        }

        // Toggle de coupure de courant
        binding.switchPowerCut.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "Rétabli" else "Coupure"
            Toast.makeText(requireContext(), "Électricité : $status", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAvailableConsolesDialog() {
        lifecycleScope.launch {
            // Récupérer tout le matériel
            val allMateriel = materielViewModel.allMateriel.first()

            // TODO: Filtrer le matériel qui est déjà dans une session active
            // Pour l'instant, nous utilisons la logique 'id_reserve == 0' de votre ancien code
            val availableMateriel = allMateriel.filter { it.id_reserve == 0 }

            if (availableMateriel.isEmpty()) {
                Toast.makeText(requireContext(), "Aucun poste libre", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val consoleNames = availableMateriel.map { "Poste ${it.id} - ${it.console}" }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Choisir un poste")
                .setItems(consoleNames) { dialog, which ->
                    val selectedMateriel = availableMateriel[which]
                    showGameSessionDialog(selectedMateriel) // Ouvre votre dialogue existant
                    dialog.dismiss()
                }
                .setNegativeButton("Annuler") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    // --- INTERFACE CALLBACKS ---
    override fun onPlayPauseClicked(session: GameSession) {
        Toast.makeText(requireContext(), "Play/Pause: ${session.postName}", Toast.LENGTH_SHORT).show()
        gameRoomViewModel.onPlayPauseClicked(session)
    }

    override fun onStopClicked(session: GameSession) {
        Toast.makeText(requireContext(), "Stop: ${session.postName}", Toast.LENGTH_SHORT).show()
        gameRoomViewModel.onStopClicked(session)
    }

    override fun onAddTimeClicked(session: GameSession) {
        Toast.makeText(requireContext(), "Ajout temps: ${session.postName}", Toast.LENGTH_SHORT).show()
        gameRoomViewModel.onAddTimeClicked(session)
    }

    override fun onPaymentClicked(session: GameSession) {
        Toast.makeText(requireContext(), "Paiement: ${session.postName}", Toast.LENGTH_SHORT).show()
        gameRoomViewModel.onPaymentClicked(session)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ===================================================================
    //  LOGIQUE DE DIALOGUE EXISTANTE (Conservée de votre ancien fichier)
    // ===================================================================

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

    private fun showGameSessionDialog(materiel: Materiel) {
        val dialogBinding = DialogGameSessionBinding.inflate(LayoutInflater.from(requireContext()))

        setupGameAutoComplete(dialogBinding.actvGameSelection, dialogBinding.etGameType)
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

    private fun startGameSessionFromDialog(materiel: Materiel, dialogBinding: DialogGameSessionBinding) {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(requireContext())
                val selectedGameTitle = dialogBinding.actvGameSelection.text.toString().trim()
                val amount = dialogBinding.etAmount.text.toString().toInt()
                val playerName = dialogBinding.etPlayerName.text.toString().trim()
                val selectedGame = jeuxList.find { it.titre == selectedGameTitle }

                if (selectedGame != null) {
                    val newPlayer = Playeur(
                        nom = playerName,
                        prenom = "",
                        id_tournoi = 0
                    )
                    val playerId = database.playeurDao().insertPlayeur(newPlayer).toInt()

                    // TODO: Gérer la finance ici

                    val gameSession = Jeux(
                        titre = selectedGame.titre,
                        type = selectedGame.type,
                        id_playeur = playerId,
                        id_materiel = materiel.id,
                        id_finance = 0 // TODO: Mettre l'ID finance
                    )
                    // TODO: Vous devez obtenir l'ID du 'Jeu' inséré
                    database.jeuxDao().insertJeux(gameSession)

                    // TODO: Mettre à jour le matériel avec l'ID du 'Jeu'
                    // val insertedGameId = ... (vous devez le récupérer)
                    // val updatedMateriel = materiel.copy(id_reserve = insertedGameId)
                    // database.materielDao().updateMateriel(updatedMateriel)

                    Toast.makeText(requireContext(),
                        "Session démarrée avec succès!",
                        Toast.LENGTH_LONG
                    ).show()

                    // Recharger les vraies données (pas juste les factices)
                    // (votre `observeViewModel` devrait idéalement écouter un `Flow` de Room)

                } else {
                    Toast.makeText(requireContext(), "Jeu non trouvé", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}