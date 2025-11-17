package com.example.gestion_salle_de_jeux.ui.materiel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.databinding.FragmentMaterielBinding
import com.example.gestion_salle_de_jeux.ui.materiel.model.MaterialUiItem

class MaterielFragment : Fragment() {

    private var _binding: FragmentMaterielBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MaterielViewModel
    private lateinit var adapter: MaterielAdapter

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

        // Initialisation ViewModel avec Factory
        val database = AppDatabase.getDatabase(requireContext())
        val dao = database.materielDao()
        val factory = MaterielViewModel.MaterielViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[MaterielViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MaterielAdapter { item ->
            // Action au clic sur le bouton Edit
            onEditItem(item)
        }
        binding.rvMaterielList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MaterielFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddMateriel.setOnClickListener {
            // TODO: Ouvrir un dialogue pour ajouter du matériel
            Toast.makeText(requireContext(), "Ajouter du matériel", Toast.LENGTH_SHORT).show()
        }

        binding.toggleCategory.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.btnTabMateriel.id -> {
                        // TODO: Charger liste matériel
                        Toast.makeText(requireContext(), "Onglet Matériel", Toast.LENGTH_SHORT).show()
                    }
                    binding.btnTabJeux.id -> {
                        // TODO: Charger liste jeux
                        Toast.makeText(requireContext(), "Onglet Jeux", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.materialList.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    private fun onEditItem(item: MaterialUiItem) {
        Toast.makeText(requireContext(), "Modifier : ${item.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}