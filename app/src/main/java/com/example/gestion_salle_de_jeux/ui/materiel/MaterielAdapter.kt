package com.example.gestion_salle_de_jeux.ui.materiel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter // Important pour submitList
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.databinding.ListItemMaterielBinding
import com.example.gestion_salle_de_jeux.ui.materiel.model.MaterialUiItem

// Le constructeur ne prend maintenant qu'un seul paramètre : le listener
class MaterielAdapter(
    private val onEditClick: (MaterialUiItem) -> Unit
) : ListAdapter<MaterialUiItem, MaterielAdapter.MaterielViewHolder>(MaterielDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterielViewHolder {
        val binding = ListItemMaterielBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MaterielViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaterielViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MaterielViewHolder(private val binding: ListItemMaterielBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MaterialUiItem) {
            binding.tvItemName.text = item.name
            binding.tvItemCount.text = "Nombre : ${item.count}"
            binding.tvStockStatus.text = item.stockStatus

            // Assurez-vous d'avoir les icônes ou utilisez une par défaut
            try {
                binding.ivItemIcon.setImageResource(item.iconResId)
            } catch (e: Exception) {
                // Fallback si l'image plante
                binding.ivItemIcon.setImageResource(android.R.drawable.ic_menu_help)
            }

            // Clic sur le bouton modifier (l'icône crayon)
            binding.ivEditAction.setOnClickListener {
                onEditClick(item)
            }
        }
    }
}

class MaterielDiffCallback : DiffUtil.ItemCallback<MaterialUiItem>() {
    override fun areItemsTheSame(oldItem: MaterialUiItem, newItem: MaterialUiItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MaterialUiItem, newItem: MaterialUiItem): Boolean {
        return oldItem == newItem
    }
}