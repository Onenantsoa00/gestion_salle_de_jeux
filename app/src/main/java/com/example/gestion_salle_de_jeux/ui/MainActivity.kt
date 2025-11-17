package com.example.gestion_salle_de_jeux.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupérer le NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment_activity_main
        ) as NavHostFragment
        val navController = navHostFragment.navController

        // --- CORRECTION ---
        // Les lignes pour "AppBarConfiguration" et "setupActionBarWithNavController"
        // ont été supprimées car le thème est maintenant "NoActionBar".
        // --- FIN CORRECTION ---

        // On connecte uniquement la barre de navigation du bas
        binding.bottomNavigation.setupWithNavController(navController)
    }
}