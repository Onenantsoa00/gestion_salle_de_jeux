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
import java.text.DecimalFormat
import kotlin.math.floor

class DiagrammesDialog : DialogFragment() {

    private var typeDiagramme = "column"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_statistiques_diagrammes, null)

        val ivDiagramme = view.findViewById<ImageView>(R.id.ivDiagramme)

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

        ivDiagramme.setImageBitmap(creerDiagrammeColonnes())

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFermerDiagrammes).setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setView(view)
            .create()
    }

    private fun creerDiagrammeColonnes(): Bitmap {
        val width = 800
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            isAntiAlias = true
        }

        // Fond
        canvas.drawColor(Color.parseColor("#1F2937"))

        // Données d'exemple pour 7 jours
        val jours = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
        val entrees = listOf(500000.0, 750000.0, 300000.0, 900000.0, 600000.0, 1200000.0, 800000.0)
        val sorties = listOf(200000.0, 300000.0, 150000.0, 400000.0, 250000.0, 500000.0, 350000.0)

        val maxValue = maxOf(entrees.maxOrNull() ?: 1.0, sorties.maxOrNull() ?: 1.0)

        // Marges pour les axes
        val marginLeft = 80f
        val marginRight = 40f
        val marginTop = 40f
        val marginBottom = 80f

        val graphWidth = width - marginLeft - marginRight
        val graphHeight = height - marginTop - marginBottom

        // Dessiner les axes
        paint.color = Color.WHITE
        paint.strokeWidth = 2f

        // Axe Y (vertical)
        canvas.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom, paint)

        // Axe X (horizontal)
        canvas.drawLine(marginLeft, height - marginBottom, width - marginRight, height - marginBottom, paint)

        // Graduations et labels de l'axe Y
        val ySteps = 5
        val yStepValue = maxValue / ySteps
        val decimalFormat = DecimalFormat("#.#")

        for (i in 0..ySteps) {
            val y = marginTop + (graphHeight / ySteps) * i
            val value = maxValue - (yStepValue * i)

            // Ligne de grille
            paint.color = Color.parseColor("#374151")
            paint.strokeWidth = 1f
            canvas.drawLine(marginLeft, y, width - marginRight, y, paint)

            // Label
            paint.color = Color.WHITE
            textPaint.textAlign = Paint.Align.RIGHT
            val label = if (value >= 1000000) {
                "${decimalFormat.format(value / 1000000)}M"
            } else if (value >= 1000) {
                "${decimalFormat.format(value / 1000)}K"
            } else {
                value.toInt().toString()
            }
            canvas.drawText("${label} Ar", marginLeft - 10f, y + 5f, textPaint)
        }

        // Labels de l'axe X
        textPaint.textAlign = Paint.Align.CENTER
        val barWidth = graphWidth / (jours.size * 3) // 3 barres par jour (espace, entrée, sortie)

        for ((index, jour) in jours.withIndex()) {
            val x = marginLeft + (index * 3 + 1.5f) * barWidth
            canvas.drawText(jour, x, height - marginBottom + 20f, textPaint)
        }

        // Dessiner les barres
        for ((index, valeurEntree) in entrees.withIndex()) {
            val valeurSortie = sorties[index]

            val xBase = marginLeft + (index * 3 + 1f) * barWidth

            // Barre entrées (verte)
            val heightEntree = (valeurEntree / maxValue * graphHeight).toFloat()
            paint.color = Color.parseColor("#10B981")
            canvas.drawRect(
                xBase,
                height - marginBottom - heightEntree,
                xBase + barWidth,
                height - marginBottom,
                paint
            )

            // Barre sorties (rouge)
            val heightSortie = (valeurSortie / maxValue * graphHeight).toFloat()
            paint.color = Color.parseColor("#EF4444")
            canvas.drawRect(
                xBase + barWidth,
                height - marginBottom - heightSortie,
                xBase + barWidth * 2,
                height - marginBottom,
                paint
            )

            // Labels sur les barres
            textPaint.color = Color.WHITE
            textPaint.textSize = 10f

            // Label entrées
            if (heightEntree > 30) {
                val labelEntree = if (valeurEntree >= 1000000) {
                    "${decimalFormat.format(valeurEntree / 1000000)}M"
                } else if (valeurEntree >= 1000) {
                    "${decimalFormat.format(valeurEntree / 1000)}K"
                } else {
                    valeurEntree.toInt().toString()
                }
                canvas.drawText(
                    labelEntree,
                    xBase + barWidth / 2,
                    height - marginBottom - heightEntree - 5f,
                    textPaint
                )
            }

            // Label sorties
            if (heightSortie > 30) {
                val labelSortie = if (valeurSortie >= 1000000) {
                    "${decimalFormat.format(valeurSortie / 1000000)}M"
                } else if (valeurSortie >= 1000) {
                    "${decimalFormat.format(valeurSortie / 1000)}K"
                } else {
                    valeurSortie.toInt().toString()
                }
                canvas.drawText(
                    labelSortie,
                    xBase + barWidth * 1.5f,
                    height - marginBottom - heightSortie - 5f,
                    textPaint
                )
            }
        }

        return bitmap
    }

    private fun creerDiagrammeCamembert(): Bitmap {
        val width = 600
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            isAntiAlias = true
        }

        canvas.drawColor(Color.parseColor("#1F2937"))

        val donnees = listOf(2500000.0, 1800000.0)
        val labels = listOf("Entrées", "Sorties")
        val colors = listOf(Color.parseColor("#10B981"), Color.parseColor("#EF4444"))
        val total = donnees.sum()
        var startAngle = 0f

        val centerX = width / 2f
        val centerY = height / 2f - 30f
        val radius = 150f

        for ((index, valeur) in donnees.withIndex()) {
            val sweepAngle = (valeur / total * 360).toFloat()
            paint.color = colors[index]
            canvas.drawArc(
                centerX - radius, centerY - radius,
                centerX + radius, centerY + radius,
                startAngle, sweepAngle, true, paint
            )

            // Légende
            val legendX = 50f
            val legendY = height - 100f + index * 30f
            paint.color = colors[index]
            canvas.drawRect(legendX, legendY - 10f, legendX + 20f, legendY + 10f, paint)

            textPaint.textAlign = Paint.Align.LEFT
            val percentage = (valeur / total * 100).toInt()
            canvas.drawText("${labels[index]} : $percentage% (${formatMontant(valeur)})", legendX + 30f, legendY + 5f, textPaint)

            startAngle += sweepAngle
        }

        // Titre au centre
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 16f
        textPaint.color = Color.WHITE
        canvas.drawText("Répartition", centerX, centerY, textPaint)
        textPaint.textSize = 12f
        canvas.drawText("Total: ${formatMontant(total)}", centerX, centerY + 20f, textPaint)

        return bitmap
    }

    private fun creerDiagrammeLignes(): Bitmap {
        val width = 800
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            isAntiAlias = true
        }

        canvas.drawColor(Color.parseColor("#1F2937"))

        val jours = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
        val entrees = listOf(500000.0, 750000.0, 300000.0, 900000.0, 600000.0, 1200000.0, 800000.0)
        val sorties = listOf(200000.0, 300000.0, 150000.0, 400000.0, 250000.0, 500000.0, 350000.0)

        val maxValue = maxOf(entrees.maxOrNull() ?: 1.0, sorties.maxOrNull() ?: 1.0)

        val marginLeft = 80f
        val marginRight = 40f
        val marginTop = 40f
        val marginBottom = 80f

        val graphWidth = width - marginLeft - marginRight
        val graphHeight = height - marginTop - marginBottom

        // Axes
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        canvas.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom, paint)
        canvas.drawLine(marginLeft, height - marginBottom, width - marginRight, height - marginBottom, paint)

        // Ligne des entrées (verte)
        paint.color = Color.parseColor("#10B981")
        var lastX = 0f
        var lastY = 0f

        for ((index, valeur) in entrees.withIndex()) {
            val x = marginLeft + (index.toFloat() / (jours.size - 1)) * graphWidth
            val y = height - marginBottom - (valeur / maxValue * graphHeight).toFloat()

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

        // Ligne des sorties (rouge)
        paint.color = Color.parseColor("#EF4444")
        lastX = 0f
        lastY = 0f

        for ((index, valeur) in sorties.withIndex()) {
            val x = marginLeft + (index.toFloat() / (jours.size - 1)) * graphWidth
            val y = height - marginBottom - (valeur / maxValue * graphHeight).toFloat()

            if (index > 0) {
                canvas.drawLine(lastX, lastY, x, y, paint)
            }

            paint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, 6f, paint)
            paint.style = Paint.Style.STROKE

            lastX = x
            lastY = y
        }

        // Labels des jours
        textPaint.textAlign = Paint.Align.CENTER
        for ((index, jour) in jours.withIndex()) {
            val x = marginLeft + (index.toFloat() / (jours.size - 1)) * graphWidth
            canvas.drawText(jour, x, height - marginBottom + 20f, textPaint)
        }

        return bitmap
    }

    private fun formatMontant(montant: Double): String {
        return when {
            montant >= 1000000 -> "${String.format("%.1f", montant / 1000000)}M"
            montant >= 1000 -> "${String.format("%.0f", montant / 1000)}K"
            else -> montant.toInt().toString()
        } + " Ar"
    }

    companion object {
        fun newInstance(): DiagrammesDialog {
            return DiagrammesDialog()
        }
    }
}