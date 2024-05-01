package com.jmblfma.wheely.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class OutlineTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = 4f
        strokePaint.color = Color.WHITE
        strokePaint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        // Draw the stroke
        strokePaint.textSize = textSize
        strokePaint.typeface = typeface
        canvas.drawText(text.toString(), paddingLeft.toFloat(), baseline.toFloat(), strokePaint)
        // Draw the text as usual
        super.onDraw(canvas)
    }
}
