package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FinanceAvecJeux (
    @Embedded val finance: Finance,
    @Relation(
        parentColumn = "id",
        entityColumn = "id_finance",
    )
    val jeux: List<Jeux>
)