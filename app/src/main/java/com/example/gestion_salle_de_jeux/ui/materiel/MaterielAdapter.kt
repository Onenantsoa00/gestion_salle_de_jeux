package com.example.gestion_salle_de_jeux.ui.materiel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.databinding.ListItemMaterielBinding
import com.example.gestion_salle_de_jeux.ui.materiel.model.MaterialUiItem

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

            // Modification de l'affichage comme demandé :
            // tvItemCount affiche le Total
            binding.tvItemCount.text = "Total : ${item.count}"

            // tvStockStatus affiche le calcul fait dans le ViewModel (Utilisé | Dispo)
            binding.tvStockStatus.text = item.stockStatus

            try {
                binding.ivItemIcon.setImageResource(item.iconResId)
            } catch (e: Exception) {
                binding.ivItemIcon.setImageResource(android.R.drawable.ic_menu_help)
            }

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