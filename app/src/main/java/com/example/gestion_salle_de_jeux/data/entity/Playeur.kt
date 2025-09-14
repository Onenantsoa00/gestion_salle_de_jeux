package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Playeur",
    foreignKeys = [
        ForeignKey(
            entity = Tournoi::class,
            parentColumns = ["id"],
            childColumns = ["id_tournoi"],
            onDelete = ForeignKey.CASCADE,
        ),
    ])
data class Playeur(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nom_playeur") val nom: String,
    @ColumnInfo(name = "prenom_playeur") val prenom: String,
    @ColumnInfo(name = "id_tournoi") val id_tournoi: Int,
)