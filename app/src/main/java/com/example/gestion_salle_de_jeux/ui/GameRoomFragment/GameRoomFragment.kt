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
    private lateinit var sessionAdapter: GameSessionAdapter

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

        // Initialisation du ViewModel pour la liste des sessions
        gameRoomViewModel = ViewModelProvider(this)[GameRoomViewModel::class.java]

        // Initialisation du ViewModel pour le Matériel
        val database = AppDatabase.getDatabase(requireContext())
        val materielDao = database.materielDao()
        val jeuLibraryDao = database.jeuLibraryDao() // 1. On récupère le nouveau DAO

        // 2. On passe les deux DAO à la Factory (Correction de l'erreur)
        val viewModelFactory = MaterielViewModel.MaterielViewModelFactory(materielDao, jeuLibraryDao)
        materielViewModel = ViewModelProvider(this, viewModelFactory)[MaterielViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadGames()
    }

    private fun setupRecyclerView() {
        sessionAdapter = GameSessionAdapter(this)
        binding.rvGameSessions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sessionAdapter
        }
    }

    private fun observeViewModel() {
        gameRoomViewModel.gameSessions.observe(viewLifecycleOwner) { sessions ->
            sessionAdapter.submitList(sessions)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddSession.setOnClickListener {
            showAvailableConsolesDialog()
        }

        binding.switchPowerCut.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "Rétabli" else "Coupure"
            Toast.makeText(requireContext(), "Électricité : $status", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAvailableConsolesDialog() {
        lifecycleScope.launch {
            // On récupère tout le matériel
            val allMateriel = materielViewModel.allMateriel.first()

            // On filtre pour n'avoir que ce qui est libre (id_reserve == 0)
            // ET ce qui est de type CONSOLE (car on ne lance pas une session sur un câble HDMI)
            val availableMateriel = allMateriel.filter {
                it.id_reserve == 0 && it.type == "CONSOLE"
            }

            if (availableMateriel.isEmpty()) {
                Toast.makeText(requireContext(), "Aucune console libre", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val consoleNames = availableMateriel.map { "Poste ${it.id} - ${it.nom}" }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Choisir un poste")
                .setItems(consoleNames) { dialog, which ->
                    val selectedMateriel = availableMateriel[which]
                    showGameSessionDialog(selectedMateriel)
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
        gameRoomViewModel.onPlayPauseClicked(session)
    }

    override fun onStopClicked(session: GameSession) {
        gameRoomViewModel.onStopClicked(session)
    }

    override fun onAddTimeClicked(session: GameSession) {
        gameRoomViewModel.onAddTimeClicked(session)
    }

    override fun onPaymentClicked(session: GameSession) {
        gameRoomViewModel.onPaymentClicked(session)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ===================================================================
    //  LOGIQUE DE DIALOGUE
    // ===================================================================

    private fun loadGames() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(requireContext())
                jeuxList = database.jeuxDao().getAllJeux()
            } catch (e: Exception) {
                // Toast.makeText(requireContext(), "Erreur chargement jeux", Toast.LENGTH_SHORT).show()
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
            autoComplete.hint = "Saisir un jeu"
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
            }
        }
    }

    private fun validateSessionForm(dialogBinding: DialogGameSessionBinding): Boolean {
        val selectedGame = dialogBinding.actvGameSelection.text.toString().trim()
        val amount = dialogBinding.etAmount.text.toString().trim()
        val playerName = dialogBinding.etPlayerName.text.toString().trim()

        if (selectedGame.isEmpty()) {
            dialogBinding.actvGameSelection.error = "Requis"
            return false
        }
        if (amount.isEmpty()) {
            dialogBinding.etAmount.error = "Requis"
            return false
        }
        if (playerName.isEmpty()) {
            dialogBinding.etPlayerName.error = "Requis"
            return false
        }
        return true
    }

    private fun startGameSessionFromDialog(materiel: Materiel, dialogBinding: DialogGameSessionBinding) {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(requireContext())
                val selectedGameTitle = dialogBinding.actvGameSelection.text.toString().trim()
                val playerName = dialogBinding.etPlayerName.text.toString().trim()

                val selectedGame = jeuxList.find { it.titre == selectedGameTitle }
                    ?: Jeux(titre = selectedGameTitle, type = "Autre", id_playeur = 0, id_materiel = 0, id_finance = 0)

                val newPlayer = Playeur(
                    nom = playerName,
                    prenom = "",
                    id_tournoi = 0
                )
                val playerId = database.playeurDao().insertPlayeur(newPlayer).toInt()

                val gameSession = Jeux(
                    titre = selectedGame.titre,
                    type = selectedGame.type,
                    id_playeur = playerId,
                    id_materiel = materiel.id,
                    id_finance = 0
                )

                database.jeuxDao().insertJeux(gameSession)

                // Mise à jour du statut du matériel (Occupé)
                // On utilise l'ID de la session ou juste 1 pour dire "occupé"
                val updatedMateriel = materiel.copy(id_reserve = 1, quantite_utilise = 1)
                database.materielDao().update(updatedMateriel)

                Toast.makeText(requireContext(), "Session démarrée !", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}