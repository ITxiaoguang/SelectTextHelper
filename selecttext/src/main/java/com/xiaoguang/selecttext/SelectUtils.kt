package com.xiaoguang.selecttext

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.DynamicDrawableSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.regex.Pattern

/**
 * 弹窗 适配器
 * hxg 2023.1.5 qq:929842234@qq.com
 */
class SelectUtils {

    companion object {

        /**
         * 替换内容
         *
         * @param stringBuilder       SpannableStringBuilder text
         * @param mOriginalContent CharSequence text
         * @param targetText       Target Text
         * @param replaceText       Replace Text
         */
        fun replaceContent(
            stringBuilder: SpannableStringBuilder,
            mOriginalContent: CharSequence,
            targetText: String,
            replaceText: String,
        ) {
            val startIndex = mOriginalContent.toString().indexOf(targetText)
            if (-1 != startIndex) {
                val endIndex = startIndex + targetText.length
                stringBuilder.replace(startIndex, endIndex, replaceText)
            }
        }

        /**
         * 文字转化成图片背景
         *
         * @param context       Context
         * @param stringBuilder SpannableStringBuilder text
         * @param content       Target content
         */
        fun replaceText2Emoji(
            context: Context?,
            emojiMap: MutableMap<String, Int>,
            stringBuilder: SpannableStringBuilder,
            content: CharSequence
        ) {
            if (emojiMap.isEmpty()) {
                return
            }
            for ((key, drawableRes) in emojiMap) {
                val matcher = Pattern.compile(key).matcher(content)
                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()
                    val drawable = ContextCompat.getDrawable(context!!, drawableRes)
                    // 动画图（加载多张 Drawable 图片资源组合而成的动画）
                    if (drawable is AnimationDrawable) {
                        drawable.start() // 开始播放动画
                    }
                    // 动态图
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (drawable is AnimatedImageDrawable) {
                            drawable.start()
                        }
                    }
                    // 动态矢量图
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (drawable is AnimatedVectorDrawable) {
                            drawable.start()
                        }
                    }
                    drawable!!.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                    val span = SelectImageSpan(
                        drawable, Color.TRANSPARENT, DynamicDrawableSpan.ALIGN_CENTER
                    )
                    stringBuilder.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }
        }

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
                val right = layout.getLineRight(line).toInt()
                val isEnd = x >= right // 是否选到了最后
                // FIX 这里的 offset + 1 不一定是对的，需要判断最后一个字符长度，
                if (isEnd) {
                    val left = layout.getPrimaryHorizontal(offset).toInt()
                    val right = layout.getLineRight(line).toInt()
                    val threshold = (right - left) / 2 // half the width of the last character
                    if (x > right - threshold) {
                        val index = getLastTextLength(layout, offset) // 得到最后一个字符长度
                        offset += index // offset + 最后一个字符长度
                    }
                }
            } //////////////////////////////////////////////////////////////////////////////////
            return offset
        }

        /**
         * 得到最后一个字符长度
         */
        private fun getLastTextLength(layout: Layout, offset: Int): Int {
            var index = 1 // 得到最后一个字符长度
            val num = 1..20
            for (i in num) {
                if (isEndOfLineOffset(layout, offset + i)) {
                    index = i
                    break
                }
            }
            return index
        }

        private fun isEndOfLineOffset(layout: Layout, offset: Int): Boolean {
            return offset > 0 && layout.getLineForOffset(offset) == layout.getLineForOffset(offset - 1) + 1
        }

        val displayWidth: Int
            get() = Resources.getSystem().displayMetrics.widthPixels

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
         * @param emojiMap Emoji picture
         * @param content Target content
         */
        fun matchImageSpan(emojiMap: MutableMap<String, Int>, content: String): Boolean {
            if (emojiMap.isEmpty()) {
                return false
            }
            for ((key) in emojiMap) {
                val matcher = Pattern.compile(key).matcher(content)
                if (matcher.find()) {
                    return true
                }
            }
            return false
        }
    }

}