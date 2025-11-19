package com.example.gestion_salle_de_jeux.ui.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.entity.Finance
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
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

        // Helper de formatage local
        fun formatMoney(amount: Double): String {
            val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                groupingSeparator = ' '
            }
            val formatter = DecimalFormat("#,##0", symbols)
            return "${formatter.format(amount)} Ar"
        }

        holder.tvDateHeure.text = dateFormat.format(finance.date_heure)

        // Affichage Entrant
        if (finance.montant_entrant > 0) {
            holder.tvMontantEntrant.text = "+ ${formatMoney(finance.montant_entrant)}"
            holder.tvMontantEntrant.visibility = View.VISIBLE
            holder.tvMontantEntrant.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.dashboard_green))
        } else {
            holder.tvMontantEntrant.visibility = View.GONE
        }

        // Affichage Sortant
        if (finance.montant_sortant > 0) {
            holder.tvMontantSortant.text = "- ${formatMoney(finance.montant_sortant)}"
            holder.tvMontantSortant.visibility = View.VISIBLE
            holder.tvMontantSortant.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.dashboard_red))
        } else {
            holder.tvMontantSortant.visibility = View.GONE
        }

        holder.tvDescription.text = "${finance.description} (${finance.source})"

        // Solde
        val solde = finance.montant_entrant - finance.montant_sortant
        holder.tvSolde.text = formatMoney(solde)

        val color = if (solde >= 0) {
            ContextCompat.getColor(holder.itemView.context, R.color.dashboard_green)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.dashboard_red)
        }
        holder.tvSolde.setTextColor(color)
    }

    override fun getItemCount() = finances.size

    fun updateData(newFinances: List<Finance>) {
        finances = newFinances
        notifyDataSetChanged()
    }
}