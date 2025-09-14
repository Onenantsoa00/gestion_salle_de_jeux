package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TournoiAvecPlayeur (
    @Embedded val tournoi: Tournoi,
    @Relation(
        parentColumn = "id",
        entityColumn = "id_tournoi",
    )
    val playeurs: List<Playeur>
)