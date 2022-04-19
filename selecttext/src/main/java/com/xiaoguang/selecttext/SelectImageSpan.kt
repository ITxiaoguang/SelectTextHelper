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

        // From gnmmdk
        paint.color = bgColor
        canvas.drawRect(x, top.toFloat(), x + b.bounds.right, bottom.toFloat(), paint)

        // From super.draw(canvas, text, start, end, x, top, y, bottom, paint)
        var transY = bottom - b.bounds.bottom
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.fontMetricsInt.descent
        } else if (mVerticalAlignment == ALIGN_CENTER) {
            transY = top + (bottom - top) / 2 - b.bounds.height() / 2
        }

        canvas.translate(x, transY.toFloat())
        b.draw(canvas)
        canvas.restore()
    }

}