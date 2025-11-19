package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "JeuLibrary")
data class JeuLibrary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val id_materiel: Int, // L'ID de la console (ex: PS5)
    val nom_jeu: String   // Ex: "FIFA 23", "GTA V"
)