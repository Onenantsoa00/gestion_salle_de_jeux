package com.example.gestion_salle_de_jeux.ui.GameRoomFragment

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.databinding.ItemConsoleCardBinding
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.GameSession
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.PaymentStatus
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.SessionStatus

class GameSessionAdapter(
    private val listener: OnSessionControlsListener
) : ListAdapter<GameSession, GameSessionAdapter.GameSessionViewHolder>(GameSessionDiffCallback()) {

    interface OnSessionControlsListener {
        fun onPlayPauseClicked(session: GameSession)
        fun onStopClicked(session: GameSession)
        fun onAddTimeClicked(session: GameSession)
        fun onPaymentClicked(session: GameSession)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameSessionViewHolder {
        val binding = ItemConsoleCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameSessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameSessionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class GameSessionViewHolder(private val binding: ItemConsoleCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val context: Context = binding.root.context

        fun bind(session: GameSession) {
            // Header: poste + console
            binding.tvPostName.text = "${session.postName} : ${session.consoleName}"

            // Infos session
            binding.tvGameName.text = "Jeu : ${session.gameName}"
            binding.tvPlayerNames.text = "Joueur : ${session.players}"
            binding.tvMatchDetails.text = "Nombre de match : ${session.matchDetails}"
            binding.tvTimeRemaining.text = "Temps restant : ${session.timeRemaining}"

            // Status indicator (dot) vs icon
            when (session.sessionStatus) {
                SessionStatus.ONLINE -> {
                    binding.viewStatusIndicator.visibility = View.VISIBLE
                    binding.ivStatusIcon.visibility = View.GONE
                    val color = ContextCompat.getColor(context, R.color.dashboard_green)
                    ViewCompat.setBackgroundTintList(binding.viewStatusIndicator, ColorStateList.valueOf(color))
                }
                SessionStatus.WARNING -> {
                    binding.viewStatusIndicator.visibility = View.VISIBLE
                    binding.ivStatusIcon.visibility = View.GONE
                    val color = ContextCompat.getColor(context, R.color.dashboard_yellow)
                    ViewCompat.setBackgroundTintList(binding.viewStatusIndicator, ColorStateList.valueOf(color))
                }
                SessionStatus.ERROR -> {
                    binding.viewStatusIndicator.visibility = View.GONE
                    binding.ivStatusIcon.visibility = View.VISIBLE
                    // iv_status_icon a déjà sa tint définie dans XML (dashboard_yellow) ; si besoin on peut changer ici
                }
            }

            // Play / Pause icon
            val playPauseRes = if (session.isPaused) R.drawable.ic_play else R.drawable.ic_pause
            binding.ivPlayPauseControl.setImageResource(playPauseRes)

            // Payment status button
            when (session.paymentStatus) {
                PaymentStatus.PAID -> {
                    binding.btnPaymentStatus.text = context.getString(R.string.paid_text, "Payé").takeIf { false } ?: "Payé"
                    binding.btnPaymentStatus.text = "Payé"
                    binding.btnPaymentStatus.setBackgroundResource(R.drawable.bg_status_paid)
                }
                PaymentStatus.UNPAID -> {
                    binding.btnPaymentStatus.text = "NON - Payé"
                    binding.btnPaymentStatus.setBackgroundResource(R.drawable.bg_status_unpaid)
                }
            }

            // Click listeners
            binding.ivPlayPauseControl.setOnClickListener { listener.onPlayPauseClicked(session) }
            binding.ivStopControl.setOnClickListener { listener.onStopClicked(session) }
            binding.ivAddTimeLarge.setOnClickListener { listener.onAddTimeClicked(session) }
            binding.ivAddTimeSmall.setOnClickListener { listener.onAddTimeClicked(session) }
            binding.btnPaymentStatus.setOnClickListener { listener.onPaymentClicked(session) }

            // Optionnel : click sur la carte entière
            binding.root.setOnClickListener {
                // comportement par défaut : ouvre détail ou toggle sélection
            }
        }
    }
}

class GameSessionDiffCallback : DiffUtil.ItemCallback<GameSession>() {
    override fun areItemsTheSame(oldItem: GameSession, newItem: GameSession): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GameSession, newItem: GameSession): Boolean {
        return oldItem == newItem
    }
}
