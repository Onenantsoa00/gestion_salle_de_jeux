package com.example.gestion_salle_de_jeux.ui

import android.app.AlertDialog
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.gestion_salle_de_jeux.R
import com.example.gestion_salle_de_jeux.data.AppDatabase
import com.example.gestion_salle_de_jeux.data.repository.FinanceRepository
import com.example.gestion_salle_de_jeux.databinding.ActivityMainBinding
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.GameSession
import com.example.gestion_salle_de_jeux.ui.GameRoomFragment.model.PaymentStatus
import com.example.gestion_salle_de_jeux.ui.gameroom.GameRoomViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameRoomViewModel: GameRoomViewModel
    private var currentRingtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)
        val financeRepo = FinanceRepository(db.financeDao())
        val vmFactory = GameRoomViewModel.Factory(db.jeuxDao(), db.materielDao(), db.playeurDao(), financeRepo)

        gameRoomViewModel = ViewModelProvider(this, vmFactory)[GameRoomViewModel::class.java]

        // --- OBSERVATION DE L'ALARME ---
        gameRoomViewModel.alarmTrigger.observe(this) { session ->
            // Toast rapide
            Toast.makeText(this, "TEMPS ÉCOULÉ : ${session.postName}", Toast.LENGTH_LONG).show()

            playAlarmSound()

            // Dialogue détaillé
            showDetailedAlarmDialog(session)
        }

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment_activity_main
        ) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun playAlarmSound() {
        try {
            var notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (notification == null) {
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            val r = RingtoneManager.getRingtone(this, notification)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                r.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            r.play()
            currentRingtone = r
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun stopAlarmSound() {
        currentRingtone?.stop()
    }

    // --- CORRECTION ICI : Dialogue Détaillé ---
    private fun showDetailedAlarmDialog(session: GameSession) {
        // Traduction du statut
        val statutPaiement = if (session.paymentStatus == PaymentStatus.PAID) {
            "PAYÉ (OK)"
        } else {
            "NON - PAYÉ (Attention !)"
        }

        val message = """
            Poste   : ${session.postName}
            Console : ${session.consoleName}
            Jeu     : ${session.gameName}
            Joueur  : ${session.players}
            --------------------------
            Statut  : $statutPaiement
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("FIN DE SESSION")
            .setMessage(message)
            .setIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPositiveButton("OK, Arrêter l'alarme") { _, _ ->
                stopAlarmSound()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        stopAlarmSound()
        super.onDestroy()
    }
}