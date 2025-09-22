package com.example.gestion_salle_de_jeux.ui.finance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R

data class PartItem(
    val mois: String,
    val total: Double
) {
    // Logique de répartition : total/3 avec arrondi intelligent
    val partBoss: Double get() = calculerPart(0)
    val partJetons: Double get() = calculerPart(1)
    val partMateriel: Double get() = calculerPart(2)

    private fun calculerPart(index: Int): Double {
        val partBase = total / 3
        val parts = DoubleArray(3)

        // Arrondi à l'entier inférieur pour tous
        parts[0] = Math.floor(partBase)
        parts[1] = Math.floor(partBase)
        parts[2] = Math.floor(partBase)

        // Répartition du reste
        val reste = total - (parts[0] + parts[1] + parts[2])

        when (reste.toInt()) {
            1 -> parts[0] += 1.0  // Boss prend le reste
            2 -> {
                parts[0] += 1.0  // Boss et Jetons prennent 1 chacun
                parts[1] += 1.0
            }
            3 -> {
                parts[0] += 1.0  // Tous prennent 1
                parts[1] += 1.0
                parts[2] += 1.0
            }
        }

        return parts[index]
    }
}

class PartAdapter(private var data: List<PartItem>) :
    RecyclerView.Adapter<PartAdapter.PartViewHolder>() {

    class PartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMois: TextView = itemView.findViewById(R.id.tvMoisPart)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotalPart)
        val tvBoss: TextView = itemView.findViewById(R.id.tvBossPart)
        val tvJetons: TextView = itemView.findViewById(R.id.tvJetonsPart)
        val tvMateriel: TextView = itemView.findViewById(R.id.tvMaterielPart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_part, parent, false)
        return PartViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartViewHolder, position: Int) {
        val item = data[position]
        holder.tvMois.text = item.mois
        holder.tvTotal.text = String.format("%,.0f Ariary", item.total)
        holder.tvBoss.text = String.format("%,.0f Ariary", item.partBoss)
        holder.tvJetons.text = String.format("%,.0f Ariary", item.partJetons)
        holder.tvMateriel.text = String.format("%,.0f Ariary", item.partMateriel)
    }

    override fun getItemCount() = data.size

    fun updateData(newData: List<PartItem>) {
        data = newData
        notifyDataSetChanged()
    }
}