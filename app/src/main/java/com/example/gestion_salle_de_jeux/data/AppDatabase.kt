package com.example.gestion_salle_de_jeux.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gestion_salle_de_jeux.data.converters.DateConverter
import com.example.gestion_salle_de_jeux.data.dao.FinanceDao
import com.example.gestion_salle_de_jeux.data.dao.JeuxDao
import com.example.gestion_salle_de_jeux.data.dao.MaterielDao
import com.example.gestion_salle_de_jeux.data.dao.PlayeurDao
import com.example.gestion_salle_de_jeux.data.entity.Finance
import com.example.gestion_salle_de_jeux.data.entity.Jeux
import com.example.gestion_salle_de_jeux.data.entity.Materiel
import com.example.gestion_salle_de_jeux.data.entity.Playeur
import com.example.gestion_salle_de_jeux.data.entity.Reserve
import com.example.gestion_salle_de_jeux.data.entity.Tournoi

@Database(
    entities = [Finance::class, Jeux::class, Materiel::class, Playeur::class, Reserve::class, Tournoi::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao
    abstract fun jeuxDao(): JeuxDao
    abstract fun materielDao(): MaterielDao
    abstract fun playeurDao(): PlayeurDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestion_salle_jeux_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}