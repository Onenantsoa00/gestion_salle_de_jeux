package com.example.gestion_salle_de_jeux.ui.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.entity.Finance
import java.text.SimpleDateFormat
import java.util.Locale

class FinanceAdapter(private var finances: List<Finance>) :
    RecyclerView.Adapter<FinanceAdapter.FinanceViewHolder>() {

    class FinanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDateHeure: TextView = itemView.findViewById(R.id.tvFinanceDateHeure)
        val tvMontantEntrant: TextView = itemView.findViewById(R.id.tvFinanceMontantEntrant)
        val tvMontantSortant: TextView = itemView.findViewById(R.id.tvFinanceMontantSortant)
        val tvDescription: TextView = itemView.findViewById(R.id.tvFinanceDescription)
        val tvSolde: TextView = itemView.findViewById(R.id.tvFinanceSolde)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_finance, parent, false)
        return FinanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: FinanceViewHolder, position: Int) {
        val finance = finances[position]
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        // Formatage des données
        holder.tvDateHeure.text = dateFormat.format(finance.date_heure)
        holder.tvMontantEntrant.text = String.format(Locale.getDefault(), "%.2f Ariary", finance.montant_entrant)
        holder.tvMontantSortant.text = String.format(Locale.getDefault(), "%.2f Ariary", finance.montant_sortant)
        holder.tvDescription.text = finance.description

        // Calcul et affichage du solde
        val solde = finance.montant_entrant - finance.montant_sortant
        holder.tvSolde.text = String.format(Locale.getDefault(), "%.2f Ariary", solde)

        // Couleur selon le solde
        val color = if (solde >= 0) {
            ContextCompat.getColor(holder.itemView.context, R.color.green)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.red)
        }
        holder.tvSolde.setTextColor(color)
    }

    override fun getItemCount() = finances.size

    fun updateData(newFinances: List<Finance>) {
        finances = newFinances
        notifyDataSetChanged()
    }
}