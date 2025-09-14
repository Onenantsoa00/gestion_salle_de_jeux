package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Tournoi")
data class Tournoi (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nom_tournoi") val nom: String,
    @ColumnInfo(name = "date_tournoi") val date: String,
    @ColumnInfo(name = "nombre_participant") val nombreParticipant: Short,
    @ColumnInfo(name = "jeu_tournoi") val jeu: String,
    @ColumnInfo(name = "prix_tournoi") val prix: Double,
)