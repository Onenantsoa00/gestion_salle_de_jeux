package com.example.gestion_salle_de_jeux.ui.finance.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.ui.finance.adapter.PartAdapter
import com.example.gestion_salle_de_jeux.ui.finance.adapter.PartItem

class PartDialog : DialogFragment() {

    private lateinit var adapter: PartAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_part, null)

        // Données d'exemple avec la logique de répartition
        val donneesPart = listOf(
            PartItem("Janvier 2024", 5000000.0),  // Ex: 5M → Boss:1.67M, Jetons:1.67M, Matériel:1.66M
            PartItem("Février 2024", 7000000.0),  // Ex: 7M → Boss:2.33M, Jetons:2.33M, Matériel:2.34M
            PartItem("Mars 2024", 4500000.0),
            PartItem("Avril 2024", 8000000.0),
            PartItem("Mai 2024", 6000000.0),
            PartItem("Juin 2024", 5500000.0)
        )

        // Configuration du RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvPartList)
        adapter = PartAdapter(donneesPart)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Calcul des totaux
        val totalGeneral = donneesPart.sumOf { it.total }
        val totalBoss = donneesPart.sumOf { it.partBoss }
        val totalJetons = donneesPart.sumOf { it.partJetons }
        val totalMateriel = donneesPart.sumOf { it.partMateriel }

        view.findViewById<TextView>(R.id.tvTotalGeneral).text =
            String.format("%,.0f Ariary", totalGeneral)
        view.findViewById<TextView>(R.id.tvTotalBoss).text =
            String.format("%,.0f Ariary", totalBoss)
        view.findViewById<TextView>(R.id.tvTotalJetons).text =
            String.format("%,.0f Ariary", totalJetons)
        view.findViewById<TextView>(R.id.tvTotalMateriel).text =
            String.format("%,.0f Ariary", totalMateriel)

        // Bouton fermer
        view.findViewById<android.widget.Button>(R.id.btnFermerPart).setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setView(view)
            .create()
    }

    companion object {
        fun newInstance(): PartDialog {
            return PartDialog()
        }
    }
}