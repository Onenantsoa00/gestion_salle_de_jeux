package com.example.gestion_salle_de_jeux.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gestion_salle_de_jeux.data.converters.DateConverter
import com.example.gestion_salle_de_jeux.data.dao.*
import com.example.gestion_salle_de_jeux.data.entity.*

@Database(
    entities = [Finance::class, Jeux::class, Materiel::class, Playeur::class, Reserve::class, Tournoi::class, JeuLibrary::class],
    version = 8, // Nouvelle version
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao
    abstract fun jeuxDao(): JeuxDao
    abstract fun materielDao(): MaterielDao
    abstract fun playeurDao(): PlayeurDao
    abstract fun jeuLibraryDao(): JeuLibraryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "gestion_salle_jeux_db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}