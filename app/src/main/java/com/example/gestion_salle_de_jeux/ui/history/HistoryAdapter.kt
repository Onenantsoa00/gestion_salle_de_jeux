package com.example.gestion_salle_de_jeux.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.databinding.ListItemHistoryBinding
import com.example.gestion_salle_de_jeux.ui.history.model.HistoryIconType
import com.example.gestion_salle_de_jeux.ui.history.model.HistoryItem

class HistoryAdapter : ListAdapter<HistoryItem, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ListItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class HistoryViewHolder(private val binding: ListItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryItem) {
            binding.tvHistoryTitle.text = item.title
            binding.tvHistorySubtitle.text = item.subtitle
            binding.tvHistoryTimestamp.text = item.timestamp

            // Gérer l'icône
            val iconRes = when (item.iconType) {
                HistoryIconType.POWER_OUTAGE -> R.drawable.ic_power // Remplacez par vos icônes
                HistoryIconType.GAME_ROOM -> R.drawable.ic_gamepad // Remplacez par vos icônes
                HistoryIconType.FINANCE -> R.drawable.ic_finance // Remplacez par vos icônes
                HistoryIconType.MATERIAL_EXPENSE -> R.drawable.ic_grid // Remplacez par vos icônes
            }
            binding.ivHistoryIcon.setImageResource(iconRes)

            // Gérer le clic sur "Supprimer"
            binding.ivDeleteIcon.setOnClickListener {
                // TODO: Appeler une fonction du ViewModel pour supprimer l'élément
                Toast.makeText(binding.root.context, "Supprimer ${item.title}", Toast.LENGTH_SHORT).show()
            }

            // Masquer le timestamp si le titre est trop long (comme dans la maquette)
            if (item.title == "Coupure Electriciter") {
                binding.tvHistoryTimestamp.visibility = View.GONE
            } else {
                binding.tvHistoryTimestamp.visibility = View.VISIBLE
            }
        }
    }
}

class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
    override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem == newItem
    }
}