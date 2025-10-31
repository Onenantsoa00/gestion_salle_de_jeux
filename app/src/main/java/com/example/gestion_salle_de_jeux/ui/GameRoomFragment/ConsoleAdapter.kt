package com.example.gestion_salle_de_jeux.ui.GameRoomFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        val tvPostNumber: TextView = itemView.findViewById(R.id.tvPostNumber)
        val tvConsoleName: TextView = itemView.findViewById(R.id.tvConsoleName)
        val tvPlayerName: TextView = itemView.findViewById(R.id.tvPlayerName)
        val tvGameName: TextView = itemView.findViewById(R.id.tvGameName)
        val tvTimerByMoney: TextView = itemView.findViewById(R.id.tvTimerByMoney)
        val btnConsoleAction: Button = itemView.findViewById(R.id.btnConsoleAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsoleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_console_card, parent, false)
        return ConsoleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConsoleViewHolder, position: Int) {
        val console = consoles[position]
        val postNumber = position + 1 // Numéro du poste commence à 1

        // Formatage des données avec numéro de poste
        holder.tvPostNumber.text = "Poste $postNumber"
        holder.tvConsoleName.text = console.console
        holder.tvPlayerName.text = "Manettes: ${console.nombre_manette}"
        holder.tvGameName.text = "TVs: ${console.nombre_television}"

        // Personnalisation basée sur la disponibilité
        if (console.id_reserve != 0) {
            // Console réservée/occupée
            setupOccupiedConsole(holder, console)
        } else {
            // Console libre
            setupAvailableConsole(holder, console)
        }
    }

    private fun setupOccupiedConsole(holder: ConsoleViewHolder, console: Materiel) {
        // Texte et statut
        holder.tvTimerByMoney.text = "Occupée - ID: ${console.id_reserve}"
        holder.btnConsoleAction.text = "Occupée"

        // Désactivation complète du bouton
        holder.btnConsoleAction.isEnabled = false
        holder.btnConsoleAction.setBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
        )

        // RENDRE LA CARTE NON CLIQUABLE
        holder.itemView.isClickable = false
        holder.itemView.isEnabled = false
        holder.itemView.alpha = 0.6f // Rend la carte semi-transparente

        // Changer la couleur du texte pour indiquer l'occupation
        holder.tvTimerByMoney.setTextColor(
            ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)
        )

        // Supprimer le listener de clic sur la carte
        holder.itemView.setOnClickListener(null)

        // Le bouton reste avec un listener null aussi
        holder.btnConsoleAction.setOnClickListener(null)
    }

    private fun setupAvailableConsole(holder: ConsoleViewHolder, console: Materiel) {
        // Texte et statut
        holder.tvTimerByMoney.text = "Disponible"
        holder.btnConsoleAction.text = "Démarrer"

        // Activation complète du bouton
        holder.btnConsoleAction.isEnabled = true
        holder.btnConsoleAction.setBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, R.color.purple_500)
        )

        // RENDRE LA CARTE CLIQUABLE
        holder.itemView.isClickable = true
        holder.itemView.isEnabled = true
        holder.itemView.alpha = 1.0f // Pleine opacité

        // Couleur normale du texte
        holder.tvTimerByMoney.setTextColor(
            ContextCompat.getColor(holder.itemView.context, R.color.teal_700)
        )

        // AJOUTER LE LISTENER DE CLIC SUR LA CARTE
        holder.itemView.setOnClickListener {
            onConsoleClick(console)
        }

        // Listener pour le bouton d'action
        holder.btnConsoleAction.setOnClickListener {
            onActionClick(console)
        }
    }

    override fun getItemCount() = consoles.size

    fun updateData(newConsoles: List<Materiel>) {
        consoles = newConsoles
        notifyDataSetChanged()
    }
}