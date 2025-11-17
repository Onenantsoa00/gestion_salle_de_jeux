package com.example.gestion_salle_de_jeux.ui.materiel.model

data class MaterialUiItem(
    val id: Int,
    val name: String,
    val count: Int,
    val stockStatus: String,
    val iconResId: Int
)