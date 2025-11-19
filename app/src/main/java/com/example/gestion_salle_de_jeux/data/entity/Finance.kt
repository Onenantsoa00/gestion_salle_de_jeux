package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.gestion_salle_de_jeux.data.converters.DateConverter
import java.util.Date

@Entity(tableName = "finance")
@TypeConverters(DateConverter::class)
data class Finance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date_heure") val date_heure: Date,
    @ColumnInfo(name = "montant_entrant") val montant_entrant: Double,
    @ColumnInfo(name = "montant_sortant") val montant_sortant: Double,
    @ColumnInfo(name = "description") val description: String,

    // C'EST CE CHAMP QUI MANQUAIT ET CAUSAIT L'ERREUR
    @ColumnInfo(name = "source") val source: String // Ex: "BOSS", "MATERIEL", "JETON", "RECETTE"
)