package com.example.gestion_salle_de_jeux.ui.finance.dialog

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.gestion_salle_de_jeux.R
import com.google.android.material.button.MaterialButtonToggleGroup

class DiagrammesDialog : DialogFragment() {

    private var typeDiagramme = "column" // Par défaut

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_statistiques_diagrammes, null)

        val ivDiagramme = view.findViewById<ImageView>(R.id.ivDiagramme)

        // Gestion du toggle group - CORRECTION : ID correct
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupDiagrammes)
        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnColumn -> {
                        typeDiagramme = "column"
                        ivDiagramme.setImageBitmap(creerDiagrammeColonnes())
                    }
                    R.id.btnPie -> {
                        typeDiagramme = "pie"
                        ivDiagramme.setImageBitmap(creerDiagrammeCamembert())
                    }
                    R.id.btnLine -> {
                        typeDiagramme = "line"
                        ivDiagramme.setImageBitmap(creerDiagrammeLignes())
                    }
                }
            }
        }

        // Diagramme par défaut
        ivDiagramme.setImageBitmap(creerDiagrammeColonnes())

        // Bouton fermer
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFermerDiagrammes).setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setView(view)
            .create()
    }

    private fun creerDiagrammeColonnes(): Bitmap {
        val width = 600
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Fond
        canvas.drawColor(Color.parseColor("#374151"))

        // Données d'exemple
        val donnees = listOf(2500000.0, 1800000.0, 3200000.0, 2800000.0, 3500000.0)
        val maxValue = donnees.maxOrNull() ?: 1.0
        val barWidth = (width / (donnees.size * 2)).toFloat() // CORRECTION : Conversion en Float

        for ((index, valeur) in donnees.withIndex()) {
            val barHeight = (valeur / maxValue * height * 0.8).toFloat()
            // CORRECTIONS : Conversions en Float
            val left = (index * (width / donnees.size) + barWidth / 2).toFloat()
            val top = (height - barHeight).toFloat()
            val right = (left + barWidth).toFloat()
            val bottom = height.toFloat()

            paint.color = if (index % 2 == 0) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
            canvas.drawRect(left, top, right, bottom, paint)
        }

        return bitmap
    }

    private fun creerDiagrammeCamembert(): Bitmap {
        val size = 400
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Fond
        canvas.drawColor(Color.parseColor("#374151"))

        // Données d'exemple
        val donnees = listOf(2500000.0, 1800000.0)
        val total = donnees.sum()
        var startAngle = 0f

        for ((index, valeur) in donnees.withIndex()) {
            val sweepAngle = (valeur / total * 360).toFloat()
            paint.color = if (index == 0) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
            canvas.drawArc(50f, 50f, (size - 50).toFloat(), (size - 50).toFloat(), startAngle, sweepAngle, true, paint)
            startAngle += sweepAngle
        }

        return bitmap
    }

    private fun creerDiagrammeLignes(): Bitmap {
        val width = 600
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }

        // Fond
        canvas.drawColor(Color.parseColor("#374151"))

        // Données d'exemple
        val donnees = listOf(2500000.0, 1800000.0, 3200000.0, 2800000.0, 3500000.0)
        val maxValue = donnees.maxOrNull() ?: 1.0

        paint.color = Color.parseColor("#10B981")
        var lastX = 0f
        var lastY = height.toFloat()

        for ((index, valeur) in donnees.withIndex()) {
            // CORRECTIONS : Conversions en Float
            val x = (index.toFloat() / (donnees.size - 1) * width * 0.8f) + width * 0.1f
            val y = height - (valeur / maxValue * height * 0.8f).toFloat()

            if (index > 0) {
                canvas.drawLine(lastX, lastY, x, y, paint)
            }

            // Points
            paint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, 6f, paint)
            paint.style = Paint.Style.STROKE

            lastX = x
            lastY = y
        }

        return bitmap
    }

    companion object {
        fun newInstance(): DiagrammesDialog {
            return DiagrammesDialog()
        }
    }
}