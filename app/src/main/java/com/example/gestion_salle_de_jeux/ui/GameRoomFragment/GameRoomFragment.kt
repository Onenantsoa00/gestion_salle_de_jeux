package com.example.gestion_salle_de_jeux.ui.GameRoomFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_salle_de_jeux.R

class GameRoomFragment : Fragment() {

    private lateinit var etPlayerName: EditText
    private lateinit var etAmount: EditText
    private lateinit var btnAddMoney: Button
    private lateinit var btnSubtractMoney: Button
    private lateinit var btnReservation: Button
    private lateinit var rvGameSessions: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gameroom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadGameSessions()
    }

    private fun initViews(view: View) {
        etPlayerName = view.findViewById(R.id.et_player_name)
        etAmount = view.findViewById(R.id.et_amount)
        btnAddMoney = view.findViewById(R.id.btn_add_money)
        btnSubtractMoney = view.findViewById(R.id.btn_subtract_money)
        btnReservation = view.findViewById(R.id.btn_reservation)
        rvGameSessions = view.findViewById(R.id.rv_game_sessions)
    }

    private fun setupRecyclerView() {

    }

    private fun setupClickListeners() {
        btnAddMoney.setOnClickListener {
            handleMoneyTransaction(true)
        }

        btnSubtractMoney.setOnClickListener {
            handleMoneyTransaction(false)
        }

        btnReservation.setOnClickListener {
            handleReservation()
        }
    }

    private fun handleMoneyTransaction(isAddition: Boolean) {
        val playerName = etPlayerName.text.toString()
        val amount = etAmount.text.toString()

        if (playerName.isNotEmpty() && amount.isNotEmpty()) {
            val action = if (isAddition) "ajouté" else "soustrait"
            Toast.makeText(context, "$amount FCFA $action pour $playerName", Toast.LENGTH_SHORT).show()

            // Ici vous ajouteriez la logique pour sauvegarder la transaction
            etPlayerName.text.clear()
            etAmount.text.clear()
        } else {
            Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleReservation() {
        val playerName = etPlayerName.text.toString()
        if (playerName.isNotEmpty()) {
            Toast.makeText(context, "Réservation créée pour $playerName", Toast.LENGTH_SHORT).show()
            etPlayerName.text.clear()
        } else {
            Toast.makeText(context, "Veuillez entrer le nom du joueur", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadGameSessions() {

    }
}