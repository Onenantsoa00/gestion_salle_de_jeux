package com.example.gestion_salle_de_jeux.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestion_salle_de_jeux.databinding.FragmentDashboardBinding

class FinanceFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ici vous pouvez initialiser vos listeners et mettre à jour l'UI
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPowerOutage.setOnClickListener {
            // Gérer la coupure de courant
        }

        binding.btnTournamentMode.setOnClickListener {
            // Activer le mode tournoi
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Changer le thème sombre/clair
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
