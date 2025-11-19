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
) : RecyclerView.Adapter<ConsoleAdapter.ConsoleViewHolder>() {

    class ConsoleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPostName: TextView = itemView.findViewById(R.id.tv_post_name)
        val tvGameName: TextView = itemView.findViewById(R.id.tv_game_name)
        val tvPlayerNames: TextView = itemView.findViewById(R.id.tv_player_names)
        val tvTimeRemaining: TextView = itemView.findViewById(R.id.tv_time_remaining)
        val tvMatchDetails: TextView = itemView.findViewById(R.id.tv_match_details)
        val statusIndicator: View = itemView.findViewById(R.id.view_status_indicator)
        val sessionInfoLayout: LinearLayout = itemView.findViewById(R.id.session_info_layout)
        val controlsLayout: LinearLayout = itemView.findViewById(R.id.controls_layout)
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

        // CORRECTION ICI : on utilise 'console.nom' au lieu de 'console.console'
        holder.tvPostName.text = "Poste $postNumber : ${console.nom}"

        // CORRECTION : id_reserve fonctionne maintenant car on l'a remis dans l'Entité
        if (console.id_reserve != 0) {
            setupOccupiedConsole(holder, console, postNumber)
        } else {
            setupAvailableConsole(holder, console, postNumber)
        }
    }

    private fun setupOccupiedConsole(holder: ConsoleViewHolder, console: Materiel, postNumber: Int) {
        // Exemple statique pour l'instant (à connecter plus tard à la table Reservation)
        holder.tvGameName.text = "Jeu : PES"
        holder.tvPlayerNames.text = "Joueur : David - Laura"
        holder.tvMatchDetails.text = "Match en cours"

        // CORRECTION : id_reserve est maintenant reconnu
        holder.tvTimeRemaining.text = "Occupée - ID: ${console.id_reserve}"

        holder.sessionInfoLayout.visibility = View.VISIBLE
        holder.controlsLayout.visibility = View.VISIBLE

        val dotColor = ContextCompat.getColor(holder.itemView.context, R.color.dashboard_red)
        (holder.statusIndicator.background as? GradientDrawable)?.setColor(dotColor)
        holder.statusIndicator.background.setTint(dotColor)

        holder.ivPlayPause.setImageResource(R.drawable.ic_pause)
        holder.ivPlayPause.isEnabled = true
        holder.ivStop.isEnabled = true
        holder.ivAddTime.isEnabled = true

        holder.ivPlayPause.setOnClickListener { /* Gérer la pause */ }
        holder.ivStop.setOnClickListener { /* Gérer l'arrêt */ }
        holder.ivAddTime.setOnClickListener { /* Gérer l'ajout de temps */ }

        holder.itemView.setOnClickListener {
            onConsoleClick(console)
        }
    }

    private fun setupAvailableConsole(holder: ConsoleViewHolder, console: Materiel, postNumber: Int) {
        holder.sessionInfoLayout.visibility = View.GONE
        holder.controlsLayout.visibility = View.GONE

        // CORRECTION ICI : 'console.nom' au lieu de 'console.console'
        holder.tvPostName.text = "Poste $postNumber : ${console.nom} (Libre)"

        val dotColor = ContextCompat.getColor(holder.itemView.context, R.color.dashboard_green)
        (holder.statusIndicator.background as? GradientDrawable)?.setColor(dotColor)
        holder.statusIndicator.background.setTint(dotColor)

        holder.itemView.isClickable = true
        holder.itemView.isEnabled = true
        holder.itemView.alpha = 1.0f

        holder.itemView.setOnClickListener {
            onActionClick(console)
        }

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