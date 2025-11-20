package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "JeuLibrary")
data class JeuLibrary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "id_materiel") val id_materiel: Int,
    @ColumnInfo(name = "nom_jeu") val nom_jeu: String,

    // CES CHAMPS SONT OBLIGATOIRES MAINTENANT
    @ColumnInfo(name = "tarif_par_tranche") val tarif_par_tranche: Double, // ex: 400.0
    @ColumnInfo(name = "duree_tranche_min") val duree_tranche_min: Int     // ex: 10
)