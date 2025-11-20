package com.example.gestion_salle_de_jeux.ui.GameRoomFragment

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
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
        val binding = ItemConsoleCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameSessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameSessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GameSessionViewHolder(private val binding: ItemConsoleCardBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context: Context = binding.root.context
        private var rotationAnimator: ObjectAnimator? = null

        fun bind(session: GameSession) {
            binding.tvPostName.text = "${session.postName} : ${session.consoleName}"
            binding.tvGameName.text = "Jeu : ${session.gameName}"
            binding.tvPlayerNames.text = "Joueur : ${session.players}"
            binding.tvMatchDetails.text = "Détails : ${session.matchDetails}"

            // --- AFFICHAGE TIMER + INFO COUPURE ---
            if (session.isPowerCutMode) {
                // On affiche le timer figé + l'info financière en rouge
                binding.tvTimeRemaining.text = "${session.timeRemaining} ${session.powerCutInfo}"
                binding.tvTimeRemaining.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                binding.tvTimeRemaining.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                binding.tvTimeRemaining.text = "Temps restant : ${session.timeRemaining}"
                // La couleur sera reset plus bas
            }

            // --- ANIMATION ---
            val targetView = binding.ivAddTimeLarge
            if (rotationAnimator == null) {
                rotationAnimator = ObjectAnimator.ofFloat(targetView, "rotation", 0f, 360f).apply {
                    duration = 2000
                    repeatCount = ObjectAnimator.INFINITE
                    interpolator = LinearInterpolator()
                }
            }

            val isRunning = (session.sessionStatus == SessionStatus.ONLINE || session.sessionStatus == SessionStatus.WARNING)
            val shouldAnimate = isRunning && !session.isPaused

            if (shouldAnimate) {
                if (!rotationAnimator!!.isRunning) rotationAnimator!!.start()
            } else {
                if (rotationAnimator!!.isRunning) rotationAnimator!!.cancel()
                targetView.rotation = 0f
            }

            // --- COULEURS ---
            if (session.sessionStatus == SessionStatus.ERROR) {
                binding.viewStatusIndicator.visibility = View.GONE
                binding.ivStatusIcon.visibility = View.VISIBLE
                binding.ivStatusIcon.setImageResource(R.drawable.ic_custom_warning)
                binding.ivStatusIcon.imageTintList = null
                if (!session.isPowerCutMode) binding.tvTimeRemaining.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            } else {
                binding.viewStatusIndicator.visibility = View.VISIBLE
                binding.ivStatusIcon.visibility = View.GONE
                val colorRes = if (session.paymentStatus == PaymentStatus.PAID) R.color.dashboard_green else R.color.dashboard_yellow
                ViewCompat.setBackgroundTintList(binding.viewStatusIndicator, ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)))

                if (!session.isPowerCutMode) {
                    val timerColor = if (session.sessionStatus == SessionStatus.WARNING) R.color.dashboard_yellow else R.color.dashboard_text_secondary
                    binding.tvTimeRemaining.setTextColor(ContextCompat.getColor(context, timerColor))
                }
            }

            val playPauseRes = if (session.isPaused) R.drawable.ic_play else R.drawable.ic_pause
            binding.ivPlayPauseControl.setImageResource(playPauseRes)

            when (session.paymentStatus) {
                PaymentStatus.PAID -> {
                    binding.btnPaymentStatus.text = "Payé"
                    binding.btnPaymentStatus.setBackgroundResource(R.drawable.bg_status_paid)
                }
                PaymentStatus.UNPAID -> {
                    binding.btnPaymentStatus.text = "NON - Payé"
                    binding.btnPaymentStatus.setBackgroundResource(R.drawable.bg_status_unpaid)
                }
            }

            binding.ivPlayPauseControl.setOnClickListener { listener.onPlayPauseClicked(session) }
            binding.ivStopControl.setOnClickListener { listener.onStopClicked(session) }
            binding.ivAddTimeLarge.setOnClickListener { listener.onAddTimeClicked(session) }
            binding.ivAddTimeSmall.setOnClickListener { listener.onAddTimeClicked(session) }
            binding.btnPaymentStatus.setOnClickListener { listener.onPaymentClicked(session) }
        }
    }
}

class GameSessionDiffCallback : DiffUtil.ItemCallback<GameSession>() {
    override fun areItemsTheSame(oldItem: GameSession, newItem: GameSession) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: GameSession, newItem: GameSession) = oldItem == newItem
}