package com.example.gestion_salle_de_jeux.ui.GameRoomFragment

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.entity.Materiel

class ConsoleAdapter(
    private var consoles: List<Materiel>,
    private val onConsoleClick: (Materiel) -> Unit,
    private val onActionClick: (Materiel) -> Unit
    // Vous aurez probablement besoin de plus de listeners pour les boutons play/stop/add
) : RecyclerView.Adapter<ConsoleAdapter.ConsoleViewHolder>() {

    /**
     * Le ViewHolder fait maintenant référence aux IDs corrects de item_console_card.xml
     */
    class ConsoleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // IDs corrigés
        val tvPostName: TextView = itemView.findViewById(R.id.tv_post_name)
        val tvGameName: TextView = itemView.findViewById(R.id.tv_game_name)
        val tvPlayerNames: TextView = itemView.findViewById(R.id.tv_player_names)
        val tvTimeRemaining: TextView = itemView.findViewById(R.id.tv_time_remaining)
        val tvMatchDetails: TextView = itemView.findViewById(R.id.tv_match_details)

        // Indicateur de statut
        val statusIndicator: View = itemView.findViewById(R.id.view_status_indicator)

        // Layout qui contient les infos (pour cacher/montrer)
        val sessionInfoLayout: LinearLayout = itemView.findViewById(R.id.session_info_layout)
        val controlsLayout: LinearLayout = itemView.findViewById(R.id.controls_layout) // J'ai ajouté un ID au layout des contrôles dans le XML

        // Boutons de contrôle (ImageViews)
        val ivPlayPause: ImageView = itemView.findViewById(R.id.iv_play_pause_control)
        val ivStop: ImageView = itemView.findViewById(R.id.iv_stop_control)
        val ivAddTime: ImageView = itemView.findViewById(R.id.iv_add_time_large)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsoleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_console_card, parent, false)
        return ConsoleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConsoleViewHolder, position: Int) {
        val console = consoles[position]
        val postNumber = position + 1

        // Logique de l'ancien adapter fusionnée :
        holder.tvPostName.text = "Poste $postNumber : ${console.console}"

        // Personnalisation basée sur la disponibilité
        if (console.id_reserve != 0) {
            // Console réservée/occupée
            setupOccupiedConsole(holder, console, postNumber)
        } else {
            // Console libre
            setupAvailableConsole(holder, console, postNumber)
        }
    }

    private fun setupOccupiedConsole(holder: ConsoleViewHolder, console: Materiel, postNumber: Int) {
        // Remplir les champs avec les données de la réservation (vous devrez les récupérer)
        holder.tvGameName.text = "Jeu : PES" // Exemple statique
        holder.tvPlayerNames.text = "Joueur : David - Laura" // Exemple statique
        holder.tvMatchDetails.text = "Nombre de match : 4 * 500 Ar = 2000 Ar" // Exemple statique
        holder.tvTimeRemaining.text = "Occupée - ID: ${console.id_reserve}"

        // Afficher les détails de la session et les contrôles
        holder.sessionInfoLayout.visibility = View.VISIBLE
        holder.controlsLayout.visibility = View.VISIBLE

        // Changer la couleur du statut
        val dotColor = ContextCompat.getColor(holder.itemView.context, R.color.dashboard_red) // Assurez-vous que cette couleur existe
        (holder.statusIndicator.background as? GradientDrawable)?.setColor(dotColor)
        holder.statusIndicator.background.setTint(dotColor)


        // Configurer les boutons pour une session en cours
        holder.ivPlayPause.setImageResource(R.drawable.ic_pause) // Mettre l'icône "Pause"
        holder.ivPlayPause.isEnabled = true
        holder.ivStop.isEnabled = true
        holder.ivAddTime.isEnabled = true

        // Gérer les clics (vous devrez implémenter la logique)
        holder.ivPlayPause.setOnClickListener { /* Gérer la pause */ }
        holder.ivStop.setOnClickListener { /* Gérer l'arrêt */ }
        holder.ivAddTime.setOnClickListener { /* Gérer l'ajout de temps */ }

        // La carte est cliquable pour voir les détails (comportement de l'ancien adapter)
        holder.itemView.setOnClickListener {
            onConsoleClick(console)
        }
    }

    private fun setupAvailableConsole(holder: ConsoleViewHolder, console: Materiel, postNumber: Int) {
        // Cacher les infos de session et les contrôles, car la console est libre
        holder.sessionInfoLayout.visibility = View.GONE
        holder.controlsLayout.visibility = View.GONE

        // Mettre à jour le texte du poste (déjà fait en partie)
        holder.tvPostName.text = "Poste $postNumber : ${console.console} (Libre)"

        // Changer la couleur du statut en vert
        val dotColor = ContextCompat.getColor(holder.itemView.context, R.color.dashboard_green) // Assurez-vous que cette couleur existe
        (holder.statusIndicator.background as? GradientDrawable)?.setColor(dotColor)
        holder.statusIndicator.background.setTint(dotColor)

        // La carte entière agit comme le bouton "Démarrer"
        holder.itemView.isClickable = true
        holder.itemView.isEnabled = true
        holder.itemView.alpha = 1.0f

        // onActionClick est l'action "Démarrer"
        holder.itemView.setOnClickListener {
            onActionClick(console)
        }

        // Nettoyer les listeners des contrôles (car ils sont cachés)
        holder.ivPlayPause.setOnClickListener(null)
        holder.ivStop.setOnClickListener(null)
        holder.ivAddTime.setOnClickListener(null)
    }

    override fun getItemCount() = consoles.size

    fun updateData(newConsoles: List<Materiel>) {
        consoles = newConsoles
        notifyDataSetChanged()
    }
}