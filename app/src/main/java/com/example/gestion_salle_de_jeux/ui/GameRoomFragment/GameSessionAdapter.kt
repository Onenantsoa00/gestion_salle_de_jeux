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

        // Animation pour le sablier (ic_timer)
        private var rotationAnimator: ObjectAnimator? = null

        fun bind(session: GameSession) {
            binding.tvPostName.text = "${session.postName} : ${session.consoleName}"
            binding.tvGameName.text = "Jeu : ${session.gameName}"
            binding.tvPlayerNames.text = "Joueur : ${session.players}"
            binding.tvMatchDetails.text = "Détails : ${session.matchDetails}"
            binding.tvTimeRemaining.text = "Temps restant : ${session.timeRemaining}"

            // --- 1. ANIMATION DU SABLIER ---
            val targetView = binding.ivAddTimeLarge
            if (rotationAnimator == null) {
                rotationAnimator = ObjectAnimator.ofFloat(targetView, "rotation", 0f, 360f).apply {
                    duration = 2000
                    repeatCount = ObjectAnimator.INFINITE
                    interpolator = LinearInterpolator()
                }
            }

            // Tourne si session active (ONLINE/WARNING) et pas en pause
            val isRunning = (session.sessionStatus == SessionStatus.ONLINE || session.sessionStatus == SessionStatus.WARNING)
            val shouldAnimate = isRunning && !session.isPaused

            if (shouldAnimate) {
                if (!rotationAnimator!!.isRunning) rotationAnimator!!.start()
            } else {
                if (rotationAnimator!!.isRunning) rotationAnimator!!.cancel()
                targetView.rotation = 0f
            }

            // --- 2. GESTION DES STATUTS (Pastille vs Icone Warning) ---

            // Si le temps est écoulé (ERROR)
            if (session.sessionStatus == SessionStatus.ERROR) {
                // On cache la pastille (dot)
                binding.viewStatusIndicator.visibility = View.GONE
                // On affiche l'icône warning
                binding.ivStatusIcon.visibility = View.VISIBLE

                // On met votre image personnalisée
                binding.ivStatusIcon.setImageResource(R.drawable.ic_custom_warning)
                // IMPORTANT : On retire le filtre de couleur pour voir la couleur originale (#afa878)
                binding.ivStatusIcon.imageTintList = null

                // Texte Timer en rouge
                binding.tvTimeRemaining.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))

            } else {
                // Si le jeu est en cours (ONLINE ou WARNING)
                binding.viewStatusIndicator.visibility = View.VISIBLE
                binding.ivStatusIcon.visibility = View.GONE

                // COULEUR DE LA PASTILLE : Dépend du paiement
                val colorRes = if (session.paymentStatus == PaymentStatus.PAID) {
                    R.color.dashboard_green // Vert si payé
                } else {
                    R.color.dashboard_yellow // Jaune si non payé
                }

                val color = ContextCompat.getColor(context, colorRes)
                ViewCompat.setBackgroundTintList(binding.viewStatusIndicator, ColorStateList.valueOf(color))

                // Couleur du texte timer (Jaune si bientôt fini, sinon Gris/Normal)
                val timerColor = if (session.sessionStatus == SessionStatus.WARNING) R.color.dashboard_yellow else R.color.dashboard_text_secondary
                binding.tvTimeRemaining.setTextColor(ContextCompat.getColor(context, timerColor))
            }

            // --- 3. BOUTONS ---
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

            // Listeners
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