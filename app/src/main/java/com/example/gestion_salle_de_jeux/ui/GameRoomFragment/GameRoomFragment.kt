package com.example.gestion_salle_de_jeux.ui.GameRoomFragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.data.entity.JeuLibrary
import com.example.gestion_salle_de_jeux.data.entity.Jeux
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import com.example.gestion_salle_de_jeux.data.entity.Playeur
import com.example.gestion_salle_de_jeux.data.repository.FinanceRepository
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
    private lateinit var gameRoomViewModel: GameRoomViewModel
    private lateinit var sessionAdapter: GameSessionAdapter
    private lateinit var materielViewModel: MaterielViewModel
    private var libraryList: List<JeuLibrary> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameroomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        val financeRepo = FinanceRepository(db.financeDao())
        val vmFactory = GameRoomViewModel.Factory(db.jeuxDao(), db.materielDao(), db.playeurDao(), financeRepo)
        gameRoomViewModel = ViewModelProvider(this, vmFactory)[GameRoomViewModel::class.java]
        val matFactory = MaterielViewModel.MaterielViewModelFactory(db.materielDao(), db.jeuLibraryDao())
        materielViewModel = ViewModelProvider(this, matFactory)[MaterielViewModel::class.java]
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
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
        binding.fabAddSession.setOnClickListener { showAvailableConsolesDialog() }
        binding.switchPowerCut.setOnCheckedChangeListener { _, _ -> Toast.makeText(requireContext(), "État électricité changé", Toast.LENGTH_SHORT).show() }
    }

    private fun showAvailableConsolesDialog() {
        lifecycleScope.launch {
            val allMateriel = materielViewModel.allMateriel.first()
            val available = allMateriel.filter { it.type == "CONSOLE" && it.quantite > it.quantite_utilise }
            if (available.isEmpty()) {
                Toast.makeText(requireContext(), "Aucun poste libre (Stock épuisé)", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val names = available.map { "${it.nom} (Total : ${it.quantite} Utilisé : ${it.quantite_utilise} | Stock : ${it.quantite - it.quantite_utilise})" }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle("Choisir un poste")
                .setItems(names) { _, which -> loadLibraryAndShowDialog(available[which]) }
                .setNegativeButton("Annuler", null).show()
        }
    }

    private fun loadLibraryAndShowDialog(console: Materiel) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            libraryList = db.jeuLibraryDao().getJeuxForConsole(console.id).first()
            if (libraryList.isEmpty()) {
                Toast.makeText(requireContext(), "Aucun jeu installé sur cette console", Toast.LENGTH_LONG).show()
                return@launch
            }
            showStartSessionDialog(console)
        }
    }

    private fun showStartSessionDialog(console: Materiel) {
        val dBinding = DialogGameSessionBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dBinding.root).create()
        val gameNames = libraryList.map { it.nom_jeu }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, gameNames)
        dBinding.actvGameSelection.setAdapter(adapter)
        var selectedLibGame: JeuLibrary? = null
        dBinding.actvGameSelection.setOnItemClickListener { _, _, position, _ ->
            selectedLibGame = libraryList.find { it.nom_jeu == adapter.getItem(position) }
            selectedLibGame?.let {
                dBinding.etGameType.setText("Tarif: ${it.tarif_par_tranche.toInt()} Ar / ${it.duree_tranche_min} min")
                calculateTotal(dBinding, it)
            }
        }
        dBinding.etAmount.hint = "Nombre de matchs"
        dBinding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { selectedLibGame?.let { calculateTotal(dBinding, it) } }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        dBinding.btnStart.setOnClickListener {
            val playerName = dBinding.etPlayerName.text.toString()
            val nbMatchsStr = dBinding.etAmount.text.toString()
            if (playerName.isNotEmpty() && nbMatchsStr.isNotEmpty() && selectedLibGame != null) {
                startSession(console, selectedLibGame!!, playerName, nbMatchsStr.toIntOrNull() ?: 1)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Veuillez tout remplir", Toast.LENGTH_SHORT).show()
            }
        }
        dBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun calculateTotal(binding: DialogGameSessionBinding, game: JeuLibrary) {
        val nbMatchs = binding.etAmount.text.toString().toIntOrNull() ?: 0
        val totalPrix = nbMatchs * game.tarif_par_tranche
        val totalTemps = nbMatchs * game.duree_tranche_min
        binding.tvCalculatedTime.text = "Temps: $totalTemps min | Total: ${totalPrix.toInt()} Ar"
    }

    private fun startSession(console: Materiel, gameLib: JeuLibrary, playerName: String, nbMatchs: Int) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val playerId = db.playeurDao().insertPlayeur(Playeur(nom = playerName, prenom = "", id_tournoi = null)).toInt()
            val durationMin = (nbMatchs * gameLib.duree_tranche_min).toLong()
            val totalPrice = nbMatchs * gameLib.tarif_par_tranche

            val session = Jeux(
                titre = gameLib.nom_jeu,
                type = console.type,
                id_playeur = playerId,
                id_materiel = console.id,
                id_finance = null,
                timestamp_debut = System.currentTimeMillis(),
                nombre_tranches = nbMatchs,
                duree_totale_prevue = durationMin,
                montant_total = totalPrice,

                // CORRECTION : Initialisation
                est_paye = false,
                montant_deja_paye = 0.0, // Rien n'est payé au début

                est_termine = false
            )
            db.jeuxDao().insertJeux(session)

            val materielAJour = db.materielDao().getMaterielById(console.id)
            if (materielAJour != null && materielAJour.quantite_utilise < materielAJour.quantite) {
                db.materielDao().update(materielAJour.copy(quantite_utilise = materielAJour.quantite_utilise + 1))
                Toast.makeText(requireContext(), "Session lancée !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAddTimeClicked(session: GameSession) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val jeuEntity = db.jeuxDao().getJeuxById(session.id.toInt())
            if (jeuEntity != null) {
                libraryList = db.jeuLibraryDao().getJeuxForConsole(jeuEntity.id_materiel).first()
                showAddTimeDialog(session)
            }
        }
    }

    private fun showAddTimeDialog(session: GameSession) {
        val dBinding = DialogGameSessionBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dBinding.root).create()
        dBinding.etPlayerName.setText(session.players)
        dBinding.etPlayerName.isEnabled = false
        dBinding.etPlayerName.alpha = 0.6f
        val gameNames = libraryList.map { it.nom_jeu }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, gameNames)
        dBinding.actvGameSelection.setAdapter(adapter)
        dBinding.actvGameSelection.setText(session.gameName, false)
        var selectedLibGame: JeuLibrary? = libraryList.find { it.nom_jeu == session.gameName }
        selectedLibGame?.let { dBinding.etGameType.setText("Tarif: ${it.tarif_par_tranche.toInt()} Ar / ${it.duree_tranche_min} min") }
        dBinding.actvGameSelection.setOnItemClickListener { _, _, position, _ ->
            selectedLibGame = libraryList.find { it.nom_jeu == adapter.getItem(position) }
            selectedLibGame?.let { dBinding.etGameType.setText("Tarif: ${it.tarif_par_tranche.toInt()} Ar / ${it.duree_tranche_min} min"); calculateTotal(dBinding, it) }
        }
        dBinding.etAmount.hint = "Matchs à ajouter"
        dBinding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { selectedLibGame?.let { calculateTotal(dBinding, it) } }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        dBinding.btnStart.text = "Ajouter"
        dBinding.btnStart.setOnClickListener {
            val nbMatchsToAddStr = dBinding.etAmount.text.toString()
            if (nbMatchsToAddStr.isNotEmpty() && selectedLibGame != null) {
                val nbMatchsToAdd = nbMatchsToAddStr.toIntOrNull() ?: 0
                val addedPrice = nbMatchsToAdd * selectedLibGame!!.tarif_par_tranche
                val addedDuration = (nbMatchsToAdd * selectedLibGame!!.duree_tranche_min).toLong()
                gameRoomViewModel.addTime(session.id.toInt(), nbMatchsToAdd, addedPrice, addedDuration, dBinding.actvGameSelection.text.toString())
                Toast.makeText(requireContext(), "Temps ajouté !", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else { Toast.makeText(requireContext(), "Veuillez entrer le nombre de matchs", Toast.LENGTH_SHORT).show() }
        }
        dBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onPlayPauseClicked(session: GameSession) { gameRoomViewModel.onPlayPauseClicked(session) }
    override fun onStopClicked(session: GameSession) { gameRoomViewModel.onStopClicked(session) }
    override fun onPaymentClicked(session: GameSession) { gameRoomViewModel.onPaymentClicked(session) }
}