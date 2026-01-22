package com.example.myownlauncher.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class FastScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val letters = ('A'..'Z').toList()
    private val paint = Paint().apply {
        color = 0xFFAAAAAA.toInt()
        textSize = 10f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private var letterHeight = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        letterHeight = h.toFloat() / letters.size
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        
        letters.forEachIndexed { index, letter ->
            val y = (index + 0.7f) * letterHeight
            canvas.drawText(letter.toString(), centerX, y, paint)
        }
    }
}