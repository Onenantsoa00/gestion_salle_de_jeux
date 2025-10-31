package com.example.gestion_salle_de_jeux.ui.dashboard

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.databinding.ListItemActiveSessionBinding
import com.example.gestion_salle_de_jeux.ui.dashboard.model.ActiveSession
import com.example.gestion_salle_de_jeux.ui.dashboard.model.SessionStatus

class ActiveSessionAdapter : ListAdapter<ActiveSession, ActiveSessionAdapter.ActiveSessionViewHolder>(ActiveSessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveSessionViewHolder {
        val binding = ListItemActiveSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActiveSessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActiveSessionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ActiveSessionViewHolder(private val binding: ListItemActiveSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val context: Context = binding.root.context

        fun bind(session: ActiveSession) {
            binding.tvPostName.text = "${session.postName} - ${session.consoleName}"
            binding.tvPlayerNames.text = session.players
            binding.tvDuration.text = session.duration
            binding.tvPaymentStatus.text = session.paymentInfo

            // Gérer l'icône Play/Pause
            val timerIcon = if (session.isPaused) R.drawable.ic_play else R.drawable.ic_pause
            binding.ivTimerControl.setImageResource(timerIcon) // Assurez-vous d'avoir ic_play et ic_pause

            // Gérer la couleur du statut
            val statusColor = when (session.status) {
                SessionStatus.ONLINE -> R.color.dashboard_green
                SessionStatus.BUSY -> R.color.dashboard_yellow
                SessionStatus.OFFLINE -> R.color.dashboard_red
            }
            binding.viewStatusIndicator.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, statusColor))

            // TODO: Gérer les icônes de console (PS5, PS4, etc.)
            // Vous pouvez ajouter une logique ici pour changer binding.ivConsoleIcon.setImageResource()
            // en fonction de session.consoleName
            binding.ivConsoleIcon.setImageResource(R.drawable.ic_console) // Placeholder
        }
    }
}

class ActiveSessionDiffCallback : DiffUtil.ItemCallback<ActiveSession>() {
    override fun areItemsTheSame(oldItem: ActiveSession, newItem: ActiveSession): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ActiveSession, newItem: ActiveSession): Boolean {
        return oldItem == newItem
    }
}