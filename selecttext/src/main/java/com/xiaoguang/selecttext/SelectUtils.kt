package com.xiaoguang.selecttext

import android.content.res.Resources
import android.text.Layout
import android.text.Spannable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import java.util.regex.Pattern

/**
 * 弹窗 适配器
 * hxg 2023.1.5 qq:929842234@qq.com
 */
class SelectUtils {
    /**
     * public start
     */
    companion object {

        fun getPreciseOffset(textView: TextView, x: Int, y: Int): Int {
            val layout = textView.layout
            return if (layout != null) {
                val topVisibleLine = layout.getLineForVertical(y)
                val offset = layout.getOffsetForHorizontal(topVisibleLine, x.toFloat())
                val offsetX = layout.getPrimaryHorizontal(offset).toInt()
                if (offsetX > x) {
                    layout.getOffsetToLeftOf(offset)
                } else {
                    offset
                }
            } else {
                -1
            }
        }

        fun getHysteresisOffset(textView: TextView, x: Int, y: Int, previousOffset: Int): Int {
            var previousOffsetCopy = previousOffset
            val layout = textView.layout ?: return -1
            var line = layout.getLineForVertical(y)

            // The "HACK BLOCK"S in this function is required because of how Android Layout for
            // TextView works - if 'offset' equals to the last character of a line, then
            //
            // * getLineForOffset(offset) will result the NEXT line
            // * getPrimaryHorizontal(offset) will return 0 because the next insertion point is on the next line
            // * getOffsetForHorizontal(line, x) will not return the last offset of a line no matter where x is
            // These are highly undesired and is worked around with the HACK BLOCK
            //
            // @see Moon+ Reader/Color Note - see how it can't select the last character of a line unless you move
            // the cursor to the beginning of the next line.
            //
            ////////////////////HACK BLOCK////////////////////////////////////////////////////
            if (isEndOfLineOffset(layout, previousOffsetCopy)) { // we have to minus one from the offset so that the code below to find
                // the previous line can work correctly.
                val left = layout.getPrimaryHorizontal(previousOffsetCopy - 1).toInt()
                val right = layout.getLineRight(line).toInt()
                val threshold = (right - left) / 2 // half the width of the last character
                if (x > right - threshold) {
                    previousOffsetCopy -= 1
                }
            } ///////////////////////////////////////////////////////////////////////////////////
            val previousLine = layout.getLineForOffset(previousOffsetCopy)
            val previousLineTop = layout.getLineTop(previousLine)
            val previousLineBottom = layout.getLineBottom(previousLine)
            val hysteresisThreshold = (previousLineBottom - previousLineTop) / 2

            // If new line is just before or after previous line and y position is less than
            // hysteresisThreshold away from previous line, keep cursor on previous line.
            if (line == previousLine + 1 && y - previousLineBottom < hysteresisThreshold || line == previousLine - 1 && (previousLineTop - y) < hysteresisThreshold) {
                line = previousLine
            }
            var offset = layout.getOffsetForHorizontal(line, x.toFloat())

            // This allow the user to select the last character of a line without moving the
            // cursor to the next line. (As Layout.getOffsetForHorizontal does not return the
            // offset of the last character of the specified line)
            //
            // But this function will probably get called again immediately, must decrement the offset
            // by 1 to compensate for the change made below. (see previous HACK BLOCK)
            /////////////////////HACK BLOCK///////////////////////////////////////////////////
            if (offset < textView.text.length - 1) {
                if (isEndOfLineOffset(layout, offset + 1)) {
                    val left = layout.getPrimaryHorizontal(offset).toInt()
                    val right = layout.getLineRight(line).toInt()
                    val threshold = (right - left) / 2 // half the width of the last character
                    if (x > right - threshold) {
                        offset += 1
                    }
                }
            } //////////////////////////////////////////////////////////////////////////////////
            return offset
        }

        private fun isEndOfLineOffset(layout: Layout, offset: Int): Boolean {
            return offset > 0 && layout.getLineForOffset(offset) == layout.getLineForOffset(offset - 1) + 1
        }


        @JvmStatic
        val displayWidth: Int
            get() = Resources.getSystem().displayMetrics.widthPixels

        @JvmStatic
        val displayHeight: Int
            get() = Resources.getSystem().displayMetrics.heightPixels

        fun dp2px(dpValue: Float): Int {
            return (dpValue * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
        }

        /**
         * 设置宽高
         *
         * @param v
         * @param w
         * @param h
         */
        @JvmStatic
        fun setWidthHeight(v: View, w: Int, h: Int) {
            val params = v.layoutParams
            params.width = w
            params.height = h
            v.layoutParams = params
        }

        /**
         * 通知栏的高度
         */
        private var STATUS_HEIGHT = 0

        /**
         * 获取通知栏的高度
         */
        @JvmStatic
        val statusHeight: Int
            get() {
                if (0 != STATUS_HEIGHT) {
                    return STATUS_HEIGHT
                }
                val resid =
                    Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
                if (resid > 0) {
                    STATUS_HEIGHT = Resources.getSystem().getDimensionPixelSize(resid)
                    return STATUS_HEIGHT
                }
                return -1
            }

        /**
         * 反射获取对象属性值
         */
        fun getFieldValue(obj: Any?, fieldName: String?): Any? {
            if (obj == null || TextUtils.isEmpty(fieldName)) {
                return null
            }
            var clazz: Class<*> = obj.javaClass
            while (clazz != Any::class.java) {
                try {
                    val field = clazz.getDeclaredField(fieldName!!)
                    field.isAccessible = true
                    return field[obj]
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                clazz = clazz.superclass
            }
            return null
        }

        /**
         * 判断是否为emoji表情符
         *
         * @param c 字符
         * @return 是否为emoji字符
         */
        fun isEmojiText(c: Char): Boolean {
            return !(c.code == 0x0 || c.code == 0x9 || c.code == 0xA || c.code == 0xD || c.code in 0x20..0xD7FF || c.code in 0xE000..0xFFFD || c.code in 0x100000..0x10FFFF)
        }

        /**
         * 利用反射检测文本是否是ImageSpan文本
         */
        fun isImageSpanText(mSpannable: Spannable): Boolean {
            if (TextUtils.isEmpty(mSpannable)) {
                return false
            }
            try {
                val mSpans = getFieldValue(mSpannable, "mSpans") as Array<*>?
                if (null != mSpans) {
                    for (mSpan in mSpans) {
                        if (mSpan is SelectImageSpan) {
                            return true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        /**
         * 匹配Image
         *
         * @param content Target content
         */
        fun matchImageSpan(content: String): Boolean {
            if (SelectTextHelper.emojiMap.isEmpty()) {
                return false
            }
            for ((key) in SelectTextHelper.emojiMap) {
                val matcher = Pattern.compile(key).matcher(content)
                if (matcher.find()) {
                    return true
                }
            }
            return false
        }
    }

}