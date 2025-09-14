package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ReserveAvecMateriel (
    @Embedded val reserve: Reserve,
    @Relation(
        parentColumn = "id",
        entityColumn = "id_reserve",
    )
    val materiel: Materiel
)