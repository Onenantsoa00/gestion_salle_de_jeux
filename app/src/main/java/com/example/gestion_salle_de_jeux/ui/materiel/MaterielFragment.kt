package com.example.gestion_salle_de_jeux.ui.materiel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestion_salle_de_jeux.databinding.FragmentMaterielBinding

class MaterielFragment : Fragment() {
    private var _binding: FragmentMaterielBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaterielBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ici vous pouvez initialiser vos listeners et mettre à jour l'UI
        setupClickListeners()
    }

    private fun setupClickListeners() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
