package com.example.gestion_salle_de_jeux.ui.utils

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator

class WaterBorderDrawable(
    private val strokeWidth: Float = 10f, // Épaisseur de l'eau
    private val cornerRadius: Float = 20f // Arrondi du bouton
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@WaterBorderDrawable.strokeWidth
    }

    // Couleurs de l'eau (Bleu foncé -> Cyan -> Blanc -> Bleu foncé)
    private val colors = intArrayOf(
        Color.parseColor("#0D47A1"), // Bleu
        Color.parseColor("#00E5FF"), // Cyan (Eau claire)
        Color.WHITE,                 // Écume
        Color.parseColor("#0D47A1")  // Retour au bleu
    )

    private var rotation = 0f
    private val animator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 2000 // Vitesse de l'eau (2 secondes pour un tour)
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            rotation = it.animatedValue as Float
            invalidateSelf()
        }
    }

    private val rect = RectF()

    fun start() {
        if (!animator.isRunning) animator.start()
    }

    fun stop() {
        if (animator.isRunning) animator.cancel()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        // On réduit légèrement le rect pour que la bordure ne soit pas coupée
        val inset = strokeWidth / 2
        rect.set(bounds.left + inset, bounds.top + inset, bounds.right - inset, bounds.bottom - inset)

        // Création du dégradé circulaire (SweepGradient)
        paint.shader = SweepGradient(rect.centerX(), rect.centerY(), colors, null)
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()

        // On fait tourner la "Matrice" du shader, pas le canvas lui-même
        // Cela donne l'impression que les couleurs circulent sur le chemin
        val matrix = Matrix()
        matrix.setRotate(rotation, rect.centerX(), rect.centerY())
        paint.shader.setLocalMatrix(matrix)

        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        canvas.restoreToCount(saveCount)
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}