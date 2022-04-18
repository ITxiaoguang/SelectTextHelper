package com.xiaoguang.selecttext

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import androidx.annotation.ColorInt

/**
 * Create by gnmmdk
 */
class SelectImageSpan(drawable: Drawable, @ColorInt var bgColor: Int, verticalAlignment: Int) :
    ImageSpan(drawable, verticalAlignment) {

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val b = drawable
        canvas.save()
        paint.color = bgColor
        canvas.drawRect(x, top.toFloat(), x + b.bounds.right, bottom.toFloat(), paint)
        val transY = (bottom - top - b.bounds.bottom) / 2 + top
        canvas.translate(x, transY.toFloat())
        b.draw(canvas)
        canvas.restore()
    }

}