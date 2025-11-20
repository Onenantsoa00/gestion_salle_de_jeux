package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Jeux",
    foreignKeys = [
        ForeignKey(entity = Playeur::class, parentColumns = ["id"], childColumns = ["id_playeur"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Materiel::class, parentColumns = ["id"], childColumns = ["id_materiel"], onDelete = ForeignKey.CASCADE)
    ]
)
data class Jeux(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "titre") val titre: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "id_playeur") val id_playeur: Int,
    @ColumnInfo(name = "id_materiel") val id_materiel: Int,
    @ColumnInfo(name = "id_finance") val id_finance: Int? = null,

    @ColumnInfo(name = "timestamp_debut") val timestamp_debut: Long,
    @ColumnInfo(name = "nombre_tranches") val nombre_tranches: Int,
    @ColumnInfo(name = "duree_totale_prevue") val duree_totale_prevue: Long,
    @ColumnInfo(name = "montant_total") val montant_total: Double,

    @ColumnInfo(name = "est_paye") val est_paye: Boolean = false,
    @ColumnInfo(name = "est_termine") val est_termine: Boolean = false,

    @ColumnInfo(name = "est_en_pause") val est_en_pause: Boolean = false,
    @ColumnInfo(name = "timestamp_pause_debut") val timestamp_pause_debut: Long = 0,
    @ColumnInfo(name = "duree_cumulee_pause") val duree_cumulee_pause: Long = 0,

    @ColumnInfo(name = "montant_deja_paye") val montant_deja_paye: Double = 0.0,

    // NOUVEAU CHAMP : Pour se souvenir que l'alarme a déjà sonné
    @ColumnInfo(name = "a_sonne") val a_sonne: Boolean = false
)