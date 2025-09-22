package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Materiel",
    foreignKeys = [
        ForeignKey(
            entity = Reserve::class,
            parentColumns = ["id"],
            childColumns = ["id_reserve"],
            onDelete = ForeignKey.CASCADE,
        )
    ])
data class Materiel (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "console") val console: String,
    @ColumnInfo(name = "nombre_manette") val nombre_manette: Short,
    @ColumnInfo(name = "nombre_television") val nombre_television: Short,
    @ColumnInfo(name = "id_reserve") val id_reserve: Int = 0,
)