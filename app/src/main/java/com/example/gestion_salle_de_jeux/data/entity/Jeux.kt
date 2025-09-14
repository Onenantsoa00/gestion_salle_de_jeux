package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Jeux",
    foreignKeys = [
        ForeignKey(
            entity = Playeur::class,
            parentColumns = ["id"],
            childColumns = ["id_playeur"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Materiel::class,
            parentColumns = ["id"],
            childColumns = ["id_materiel"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Finance::class,
            parentColumns = ["id"],
            childColumns = ["id_finance"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)

data class Jeux(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    @ColumnInfo(name = "titre") val titre: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "id_playeur") val id_playeur: Int,
    @ColumnInfo(name = "id_materiel") val id_materiel: Int,
    @ColumnInfo(name = "id_finance") val id_finance: Int,
)