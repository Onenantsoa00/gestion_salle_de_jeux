package com.example.gestion_salle_de_jeux.ui.materiel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.entity.Materiel

class MaterielAdapter(private var materiel: List<Materiel>) :
    RecyclerView.Adapter<MaterielAdapter.MaterielViewHolder>() {

    class MaterielViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMaterielId: TextView = itemView.findViewById(R.id.tvMaterielId)
        val tvMaterielConsole: TextView = itemView.findViewById(R.id.tvMaterielConsole)
        val tvMaterielManette: TextView = itemView.findViewById(R.id.tvMaterielManette)
        val tvMaterielTelevision: TextView = itemView.findViewById(R.id.tvMaterielTelevision)
        val tvMaterielIdReserve: TextView = itemView.findViewById(R.id.tvMaterielIdReserve)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterielViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_materiel, parent, false)
        return MaterielViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaterielViewHolder, position: Int) {
        val materiel = materiel[position]

        // Formatage des données
        holder.tvMaterielId.text = "ID: ${materiel.id}"
        holder.tvMaterielConsole.text = "Console: ${materiel.console}"
        holder.tvMaterielManette.text = "Manettes: ${materiel.nombre_manette}"
        holder.tvMaterielTelevision.text = "Télévisions: ${materiel.nombre_television}"
        holder.tvMaterielIdReserve.text = "ID Réservation: ${materiel.id_reserve?.takeIf { it != 0 }?.toString() ?: "N/A"}"
    }

    override fun getItemCount() = materiel.size

    fun updateData(newMateriel: List<Materiel>) {
        materiel = newMateriel
        notifyDataSetChanged()
    }
}