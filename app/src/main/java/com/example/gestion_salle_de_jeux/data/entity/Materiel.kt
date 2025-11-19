package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Materiel")
data class Materiel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nom") val nom: String,
    @ColumnInfo(name = "quantite") val quantite: Int, // Ceci est maintenant le TOTAL
    @ColumnInfo(name = "quantite_utilise") val quantite_utilise: Int = 0, // NOUVEAU : Matériel en cours d'utilisation
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "id_reserve") val id_reserve: Int = 0
)