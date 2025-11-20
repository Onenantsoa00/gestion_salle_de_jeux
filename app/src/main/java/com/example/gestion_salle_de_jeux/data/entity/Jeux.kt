package com.example.gestion_salle_de_jeux.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Jeux",
    foreignKeys = [
        ForeignKey(entity = Playeur::class, parentColumns = ["id"], childColumns = ["id_playeur"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Materiel::class, parentColumns = ["id"], childColumns = ["id_materiel"], onDelete = ForeignKey.CASCADE)
        // J'ai retiré la FK vers Finance ici pour éviter le crash, ou alors il faut la mettre nullable comme Playeur.
        // Pour faire simple et éviter les crashs au démarrage : on retire la contrainte stricte sur finance pour l'instant.
    ]
)
data class Jeux(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "titre") val titre: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "id_playeur") val id_playeur: Int,
    @ColumnInfo(name = "id_materiel") val id_materiel: Int,

    // CORRECTION : Int? pour permettre de créer le jeu sans avoir encore l'ID finance
    @ColumnInfo(name = "id_finance") val id_finance: Int? = null,

    // Champs Timer (ajoutés précédemment)
    @ColumnInfo(name = "timestamp_debut") val timestamp_debut: Long,
    @ColumnInfo(name = "nombre_tranches") val nombre_tranches: Int,
    @ColumnInfo(name = "duree_totale_prevue") val duree_totale_prevue: Long,
    @ColumnInfo(name = "montant_total") val montant_total: Double,
    @ColumnInfo(name = "est_paye") val est_paye: Boolean = false,
    @ColumnInfo(name = "est_termine") val est_termine: Boolean = false,
    @ColumnInfo(name = "est_en_pause") val est_en_pause: Boolean = false,
    @ColumnInfo(name = "timestamp_pause_debut") val timestamp_pause_debut: Long = 0
)