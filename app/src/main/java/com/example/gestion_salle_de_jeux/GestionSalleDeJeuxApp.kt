package com.example.gestion_salle_de_jeux

import android.app.Application
import com.example.gestion_salle_de_jeux.data.AppDatabase

class GestionSalleDeJeuxApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}