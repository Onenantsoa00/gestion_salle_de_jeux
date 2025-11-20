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
            onDelete = ForeignKey.SET_NULL // Si le tournoi est supprimé, le joueur reste mais sans tournoi
        )
    ])
data class Playeur(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nom_playeur") val nom: String,
    @ColumnInfo(name = "prenom_playeur") val prenom: String,

    // CORRECTION ICI : Int? (Nullable) au lieu de Int
    @ColumnInfo(name = "id_tournoi") val id_tournoi: Int? = null
)