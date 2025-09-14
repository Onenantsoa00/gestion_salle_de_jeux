package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Reserve")
data class Reserve (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nom_reserve") val nom: String,
    @ColumnInfo(name = "date_reserve") val date: String,
    @ColumnInfo(name = "prix") val prix: Double,
    @ColumnInfo(name = "nombre_de_reserve") val nombre_de_reserve: Short,
)