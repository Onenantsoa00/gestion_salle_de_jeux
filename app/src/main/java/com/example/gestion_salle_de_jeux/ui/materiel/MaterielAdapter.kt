package com.example.gestion_salle_de_jeux.ui.materiel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.databinding.ListItemJeuxBinding // Généré automatiquement depuis list_item_jeux.xml
import com.example.gestion_salle_de_jeux.databinding.ListItemMaterielBinding
import com.example.gestion_salle_de_jeux.ui.materiel.model.MaterialUiItem

class MaterielAdapter(
    private val onEditClick: (MaterialUiItem) -> Unit,
    private val onGamesClick: (MaterialUiItem) -> Unit // Nouveau listener pour les jeux
) : ListAdapter<MaterialUiItem, RecyclerView.ViewHolder>(MaterielDiffCallback()) {

    // Variable pour savoir dans quel mode on est
    private var isGameMode = false

    companion object {
        const val VIEW_TYPE_MATERIEL = 0
        const val VIEW_TYPE_JEUX = 1
    }

    fun setGameMode(isGame: Boolean) {
        this.isGameMode = isGame
        // On ne fait pas notifyDataSetChanged ici, car le submitList du fragment va s'en charger
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGameMode) VIEW_TYPE_JEUX else VIEW_TYPE_MATERIEL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_JEUX) {
            val binding = ListItemJeuxBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            JeuxViewHolder(binding)
        } else {
            val binding = ListItemMaterielBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            MaterielViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is MaterielViewHolder) {
            holder.bind(item)
        } else if (holder is JeuxViewHolder) {
            holder.bind(item)
        }
    }

    // --- Ancien ViewHolder (Matériel) ---
    inner class MaterielViewHolder(private val binding: ListItemMaterielBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MaterialUiItem) {
            binding.tvItemName.text = item.name
            binding.tvItemCount.text = "Total : ${item.count}"
            binding.tvStockStatus.text = item.stockStatus
            try { binding.ivItemIcon.setImageResource(item.iconResId) } catch (e: Exception) {}

            binding.ivEditAction.setOnClickListener { onEditClick(item) }
        }
    }

    // --- Nouveau ViewHolder (Jeux) ---
    inner class JeuxViewHolder(private val binding: ListItemJeuxBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MaterialUiItem) {
            binding.tvConsoleName.text = item.name
            // On peut changer l'icone si besoin, ou garder celle par défaut
            try { binding.ivConsoleIcon.setImageResource(item.iconResId) } catch (e: Exception) {}

            binding.btnManageGames.setOnClickListener {
                onGamesClick(item)
            }
        }
    }
}

class MaterielDiffCallback : DiffUtil.ItemCallback<MaterialUiItem>() {
    override fun areItemsTheSame(oldItem: MaterialUiItem, newItem: MaterialUiItem): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: MaterialUiItem, newItem: MaterialUiItem): Boolean = oldItem == newItem
}