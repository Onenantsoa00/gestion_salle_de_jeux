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

        // Injection des dépendances
        val financeRepo = FinanceRepository(db.financeDao())

        // Utilisation de requireActivity() pour partager le ViewModel avec MainActivity (pour l'alarme globale)
        val vmFactory = GameRoomViewModel.Factory(db.jeuxDao(), db.materielDao(), db.playeurDao(), financeRepo)
        gameRoomViewModel = ViewModelProvider(requireActivity(), vmFactory)[GameRoomViewModel::class.java]

        // ViewModel local pour la gestion du matériel (listes déroulantes)
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
            // IMPORTANT : Désactive les animations par défaut pour éviter le clignotement à chaque seconde
            itemAnimator = null
        }
    }

    private fun observeViewModel() {
        // Mise à jour de la liste des sessions (Timer, Statuts, Coupure)
        gameRoomViewModel.gameSessions.observe(viewLifecycleOwner) { sessions ->
            sessionAdapter.submitList(sessions)
        }
        // Note : L'alarme sonore est gérée par MainActivity, pas besoin de l'observer ici.
    }

    private fun setupClickListeners() {
        // Bouton flottant (+) pour ajouter une session
        binding.fabAddSession.setOnClickListener {
            // On empêche l'ajout de session si on est en coupure de courant
            if (!binding.switchPowerCut.isChecked) {
                Toast.makeText(requireContext(), "Impossible d'ajouter une session pendant une coupure !", Toast.LENGTH_SHORT).show()
            } else {
                showAvailableConsolesDialog()
            }
        }

        // --- GESTION INTERRUPTEUR ÉLECTRICITÉ ---
        // Checked (Activé) = Électricité PRÉSENTE (Mode Normal)
        // Unchecked (Désactivé) = Électricité COUPÉE (Mode Coupure)
        binding.switchPowerCut.setOnCheckedChangeListener { _, isChecked ->
            // Si isChecked est TRUE (Courant là) -> isCut est FALSE
            // Si isChecked est FALSE (Pas de courant) -> isCut est TRUE
            val isCut = !isChecked

            gameRoomViewModel.togglePowerCut(isCut)

            if (isCut) {
                Toast.makeText(requireContext(), "COUPURE : Sessions en pause & Calculs affichés", Toast.LENGTH_LONG).show()
                binding.tvPowerCutLabel.text = "Électricité : COUPÉE"
                binding.tvPowerCutLabel.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
            } else {
                Toast.makeText(requireContext(), "COURANT RÉTABLI : Reprise des sessions", Toast.LENGTH_SHORT).show()
                binding.tvPowerCutLabel.text = "Électricité : OK"
                binding.tvPowerCutLabel.setTextColor(requireContext().getColor(com.example.gestion_salle_de_jeux.R.color.dashboard_text_secondary)) // Remplacez par votre couleur
            }
        }
    }

    // ====================================================================================
    // 1. DÉMARRAGE D'UNE NOUVELLE SESSION
    // ====================================================================================

    private fun showAvailableConsolesDialog() {
        lifecycleScope.launch {
            val allMateriel = materielViewModel.allMateriel.first()

            // Filtre : Type CONSOLE et Stock disponible
            val available = allMateriel.filter {
                it.type == "CONSOLE" && it.quantite > it.quantite_utilise
            }

            if (available.isEmpty()) {
                Toast.makeText(requireContext(), "Aucun poste libre (Stock épuisé)", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Affichage : "Nom (Total : X Utilisé : Y | Stock : Z)"
            val names = available.map {
                val stock = it.quantite - it.quantite_utilise
                "${it.nom} (Total : ${it.quantite} Utilisé : ${it.quantite_utilise} | Stock : $stock)"
            }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Choisir un poste")
                .setItems(names) { _, which ->
                    loadLibraryAndShowDialog(available[which])
                }
                .setNegativeButton("Annuler", null)
                .show()
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
            val name = adapter.getItem(position)
            selectedLibGame = libraryList.find { it.nom_jeu == name }
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
                val nbMatchs = nbMatchsStr.toIntOrNull() ?: 1
                startSession(console, selectedLibGame!!, playerName, nbMatchs)
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

            val player = Playeur(nom = playerName, prenom = "", id_tournoi = null)
            val playerId = db.playeurDao().insertPlayeur(player).toInt()

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
                est_paye = false,
                est_termine = false,
                montant_deja_paye = 0.0,
                a_sonne = false
            )
            db.jeuxDao().insertJeux(session)

            // Mise à jour du stock (+1 utilisé)
            val materielAJour = db.materielDao().getMaterielById(console.id)
            if (materielAJour != null && materielAJour.quantite_utilise < materielAJour.quantite) {
                val nouvelUsage = materielAJour.quantite_utilise + 1
                db.materielDao().update(materielAJour.copy(quantite_utilise = nouvelUsage))

                val reste = materielAJour.quantite - nouvelUsage
                Toast.makeText(requireContext(), "Session lancée ! (Reste : $reste)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Erreur : Stock théorique dépassé", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ====================================================================================
    // 2. AJOUT DE TEMPS (Bouton +)
    // ====================================================================================

    override fun onAddTimeClicked(session: GameSession) {
        // On bloque l'ajout de temps si coupure de courant
        if (!binding.switchPowerCut.isChecked) {
            Toast.makeText(requireContext(), "Impossible d'ajouter du temps pendant une coupure !", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val jeuEntity = db.jeuxDao().getJeuxById(session.id.toInt())

            if (jeuEntity != null) {
                libraryList = db.jeuLibraryDao().getJeuxForConsole(jeuEntity.id_materiel).first()
                showAddTimeDialog(session, jeuEntity)
            }
        }
    }

    private fun showAddTimeDialog(session: GameSession, jeuEntity: Jeux) {
        val dBinding = DialogGameSessionBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dBinding.root).create()

        dBinding.etPlayerName.setText(session.players)
        dBinding.etPlayerName.isEnabled = false // On ne change pas le joueur
        dBinding.etPlayerName.alpha = 0.6f

        val gameNames = libraryList.map { it.nom_jeu }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, gameNames)
        dBinding.actvGameSelection.setAdapter(adapter)
        dBinding.actvGameSelection.setText(session.gameName, false)

        var selectedLibGame: JeuLibrary? = libraryList.find { it.nom_jeu == session.gameName }
        selectedLibGame?.let {
            dBinding.etGameType.setText("Tarif: ${it.tarif_par_tranche.toInt()} Ar / ${it.duree_tranche_min} min")
        }

        dBinding.actvGameSelection.setOnItemClickListener { _, _, position, _ ->
            selectedLibGame = libraryList.find { it.nom_jeu == adapter.getItem(position) }
            selectedLibGame?.let {
                dBinding.etGameType.setText("Tarif: ${it.tarif_par_tranche.toInt()} Ar / ${it.duree_tranche_min} min")
                calculateTotal(dBinding, it)
            }
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
                val newGameTitle = dBinding.actvGameSelection.text.toString()

                gameRoomViewModel.addTime(
                    sessionId = session.id.toInt(),
                    addedMatches = nbMatchsToAdd,
                    addedPrice = addedPrice,
                    addedDuration = addedDuration,
                    newGameTitle = newGameTitle
                )
                Toast.makeText(requireContext(), "Temps ajouté !", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Veuillez entrer le nombre de matchs", Toast.LENGTH_SHORT).show()
            }
        }
        dBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ====================================================================================
    // 3. CONTRÔLES ADAPTER (Play, Stop, Pay)
    // ====================================================================================

    override fun onPlayPauseClicked(session: GameSession) {
        // On interdit le Play/Pause manuel si on est en mode coupure globale
        if (!binding.switchPowerCut.isChecked) {
            Toast.makeText(requireContext(), "Coupure active : Contrôles bloqués", Toast.LENGTH_SHORT).show()
            return
        }
        gameRoomViewModel.onPlayPauseClicked(session)
    }

    override fun onStopClicked(session: GameSession) {
        // Si coupure, on peut arrêter mais c'est mieux de passer par le paiement partiel
        showStopConfirmationDialog(session)
    }

    private fun showStopConfirmationDialog(session: GameSession) {
        AlertDialog.Builder(requireContext())
            .setTitle("Terminer la session ?")
            .setMessage("Voulez-vous vraiment arrêter le jeu sur ${session.postName} et libérer le poste ?")
            .setPositiveButton("Oui, Arrêter") { _, _ ->
                gameRoomViewModel.onStopClicked(session)
                Toast.makeText(requireContext(), "Session terminée. Poste libéré.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onPaymentClicked(session: GameSession) {
        // Cette fonction gère le paiement Normal ET le paiement partiel (Coupure)
        // La logique est dans le ViewModel
        gameRoomViewModel.onPaymentClicked(session)
    }
}