package com.xiaoguang.selecttext

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Pair
import android.view.*
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.widget.ImageView
import android.widget.Magnifier
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xiaoguang.selecttext.SelectTextPopAdapter.onClickItemListener
import java.util.*


/**
 * Created by hxg on 2021/9/13 929842234@qq.com
 *
 * 仿照的例子：https://github.com/laobie
 * 放大镜 Magnifier：https://developer.android.google.cn/guide/topics/text/magnifier
 */
class SelectTextHelper(builder: Builder) {
    private var mTextView: TextView

    private var mOriginalContent: CharSequence // 原本的文本
    private var mStartHandle: CursorHandle? = null // 开始操作标
    private var mEndHandle: CursorHandle? = null // 结束操作标
    private var mOperateWindow: OperateWindow? = null // 操作弹窗
    private var mMagnifier: Magnifier? = null // 放大镜组件
    private val mSelectionInfo = SelectionInfo()
    private var mSelectListener: OnSelectListener? = null
    private val mContext: Context
    private var mSpannable: Spannable? = null
    private var mTouchX = 0
    private var mTouchY = 0
    private var mTextViewMarginStart = 0 // textView的marginStart值
    private val mSelectedColor: Int // 选中文本的颜色
    private val mCursorHandleColor: Int // 游标的颜色
    private val mCursorHandleSize: Int // 游标大小
    private val mSelectAll: Boolean // 全选
    private val mSelectedAllNoPop: Boolean // 已经全选无弹窗
    private val mSelectTextLength: Int // 首次选择文字长度
    private val mScrollShow: Boolean // 滑动依然显示弹窗
    private val mMagnifierShow: Boolean // 显示放大镜
    private val mPopSpanCount: Int // 弹窗每行个数
    private val mPopBgResource: Int // 弹窗箭头
    private val mPopDelay: Int // 弹窗延迟时间
    private val mPopAnimationStyle: Int // 弹窗动画
    private val mPopArrowImg: Int // 弹窗箭头
    private val itemTextList: List<Pair<Int, String>> // 操作弹窗item文本
    private var itemListenerList: List<Builder.onSeparateItemClickListener> =
        LinkedList() // 操作弹窗item监听
    private var mSpan: BackgroundColorSpan? = null
    private var isHideWhenScroll = false
    private var isHide = true
    private var usedClickListener = false // 消费了点击事件
    private var mOnPreDrawListener: ViewTreeObserver.OnPreDrawListener? = null
    private var mOnScrollChangedListener: OnScrollChangedListener? = null
    private var mRootTouchListener: OnTouchListener? = null

    init {
        mTextView = builder.mTextView
        mOriginalContent = mTextView.text
        mContext = mTextView.context
        mSelectedColor = builder.mSelectedColor
        mCursorHandleColor = builder.mCursorHandleColor
        mSelectAll = builder.mSelectAll
        mScrollShow = builder.mScrollShow
        mMagnifierShow = builder.mMagnifierShow
        mSelectedAllNoPop = builder.mSelectedAllNoPop
        mSelectTextLength = builder.mSelectTextLength
        mPopSpanCount = builder.mPopSpanCount
        mPopBgResource = builder.mPopBgResource
        mPopDelay = builder.mPopDelay
        mPopAnimationStyle = builder.mPopAnimationStyle
        mPopArrowImg = builder.mPopArrowImg
        itemTextList = builder.itemTextList
        itemListenerList = builder.itemListenerList
        mCursorHandleSize = SelectUtils.dp2px(builder.mCursorHandleSizeInDp)
        init()
    }

    /**
     * public start
     */
    companion object {
        private const val DEFAULT_SELECTION_LENGTH = 2 // 选2个字节长度 例:表情属于2个字节
        private const val DEFAULT_SHOW_DURATION = 100 // 弹窗100毫秒延迟

        @Volatile
        var emojiMap: MutableMap<String, Int> = HashMap()

        @Synchronized
        fun putAllEmojiMap(map: Map<String, Int>?) {
            emojiMap.putAll(map!!)
        }

        @Synchronized
        fun putEmojiMap(emojiKey: String, @DrawableRes drawableRes: Int) {
            emojiMap[emojiKey] = drawableRes
        }

    }

    open class OnSelectListenerImpl : OnSelectListener {
        override fun onClick(v: View?, originalContent: CharSequence?) = Unit
        override fun onLongClick(v: View?) = Unit
        override fun onTextSelected(content: CharSequence?) = Unit
        override fun onDismiss() = Unit
        override fun onClickUrl(url: String?) = Unit
        override fun onSelectAllShowCustomPop() = Unit
        override fun onReset() = Unit
        override fun onDismissCustomPop() = Unit
        override fun onScrolling() = Unit
    }

    interface OnSelectListener {
        fun onClick(v: View?, originalContent: CharSequence?) // 点击textView
        fun onLongClick(v: View?) // 长按textView
        fun onTextSelected(content: CharSequence?) // 选中文本回调
        fun onDismiss() // 解除弹窗回调
        fun onClickUrl(url: String?) // 点击文本里的url回调
        fun onSelectAllShowCustomPop() // 全选显示自定义弹窗回调
        fun onReset() // 重置回调
        fun onDismissCustomPop() // 解除自定义弹窗回调
        fun onScrolling() // 正在滚动回调
    }

    class Builder(val mTextView: TextView) {
        var mCursorHandleColor = -0xec862a
        var mSelectedColor = -0x501e0c
        var mCursorHandleSizeInDp = 24f
        var mSelectAll = true
        var mSelectedAllNoPop = false
        var mSelectTextLength = DEFAULT_SELECTION_LENGTH
        var mScrollShow = true
        var mMagnifierShow = true
        var mPopSpanCount = 5
        var mPopBgResource = 0
        var mPopDelay = DEFAULT_SHOW_DURATION
        var mPopAnimationStyle = 0
        var mPopArrowImg = 0
        val itemTextList: MutableList<Pair<Int, String>> = LinkedList()
        val itemListenerList: MutableList<onSeparateItemClickListener> = LinkedList()

        /**
         * 选择游标颜色
         */
        fun setCursorHandleColor(@ColorInt cursorHandleColor: Int): Builder {
            mCursorHandleColor = cursorHandleColor
            return this
        }

        /**
         * 选择游标大小
         */
        fun setCursorHandleSizeInDp(cursorHandleSizeInDp: Float): Builder {
            mCursorHandleSizeInDp = cursorHandleSizeInDp
            return this
        }

        /**
         * 选中文本的颜色
         */
        fun setSelectedColor(@ColorInt selectedBgColor: Int): Builder {
            mSelectedColor = selectedBgColor
            return this
        }

        /**
         * 全选
         */
        fun setSelectAll(selectAll: Boolean): Builder {
            mSelectAll = selectAll
            return this
        }

        /**
         * 已经全选无弹窗
         */
        fun setSelectedAllNoPop(selectedAllNoPop: Boolean): Builder {
            mSelectedAllNoPop = selectedAllNoPop
            return this
        }

        /**
         * 选择选择个数
         */
        fun setSelectTextLength(selectTextLength: Int): Builder {
            mSelectTextLength = selectTextLength
            return this
        }

        /**
         * 滑动依然显示弹窗
         */
        fun setScrollShow(scrollShow: Boolean): Builder {
            mScrollShow = scrollShow
            return this
        }

        /**
         * 显示放大镜
         */
        fun setMagnifierShow(magnifierShow: Boolean): Builder {
            mMagnifierShow = magnifierShow
            return this
        }

        /**
         * 弹窗每行个数
         */
        fun setPopSpanCount(popSpanCount: Int): Builder {
            mPopSpanCount = popSpanCount
            return this
        }

        /**
         * 弹窗背景颜色、弹窗箭头
         */
        fun setPopStyle(popBgResource: Int, popArrowImg: Int): Builder {
            mPopBgResource = popBgResource
            mPopArrowImg = popArrowImg
            return this
        }

        /**
         * 弹窗延迟
         */
        fun setPopDelay(popDelay: Int): Builder {
            mPopDelay = popDelay
            return this
        }

        /**
         * 弹窗动画
         */
        fun setPopAnimationStyle(popAnimationStyle: Int): Builder {
            mPopAnimationStyle = popAnimationStyle
            return this
        }

        fun addItem(
            @DrawableRes drawableId: Int,
            @StringRes textResId: Int,
            listener: onSeparateItemClickListener
        ): Builder {
            itemTextList.add(Pair(drawableId, mTextView.context.resources.getString(textResId)))
            itemListenerList.add(listener)
            return this
        }

        fun addItem(
            @DrawableRes drawableId: Int, itemText: String, listener: onSeparateItemClickListener
        ): Builder {
            itemTextList.add(Pair(drawableId, itemText))
            itemListenerList.add(listener)
            return this
        }

        fun addItem(@StringRes textResId: Int, listener: onSeparateItemClickListener): Builder {
            itemTextList.add(Pair(0, mTextView.context.resources.getString(textResId)))
            itemListenerList.add(listener)
            return this
        }

        fun addItem(itemText: String, listener: onSeparateItemClickListener): Builder {
            itemTextList.add(Pair(0, itemText))
            itemListenerList.add(listener)
            return this
        }

        fun build(): SelectTextHelper {
            return SelectTextHelper(this)
        }

        interface onSeparateItemClickListener {
            fun onClick()
        }
    }

    /**
     * 重置弹窗
     */
    fun reset() {
        hideSelectView()
        resetSelectionInfo()
        // 重置弹窗回调
        mSelectListener?.onReset()
    }

    /**
     * 操作弹窗是否显示中
     */
    val isPopShowing: Boolean
        get() = if (null != mOperateWindow) {
            mOperateWindow!!.isShowing
        } else false

    /**
     * 选择文本监听
     */
    fun setSelectListener(selectListener: OnSelectListener?) {
        mSelectListener = selectListener
    }

    /**
     * 销毁
     */
    fun destroy() {
        mTextView.viewTreeObserver.removeOnScrollChangedListener(mOnScrollChangedListener)
        mTextView.viewTreeObserver.removeOnPreDrawListener(mOnPreDrawListener)
        mTextView.rootView.setOnTouchListener(null)
        reset()
        mStartHandle = null
        mEndHandle = null
        mOperateWindow = null
    }

    /**
     * 全选
     */
    fun selectAll() {
        hideSelectView()
        selectText(0, mTextView.text.length)
        isHide = false
        showCursorHandle(mStartHandle)
        showCursorHandle(mEndHandle)
        showOperateWindow()
    }

    /**
     * public end
     */

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        val spanStr = SpannableStringBuilder(mOriginalContent)
        // 处理空格 把不间断空格(\u00A0)/半角空格(\u0020)转成全角空格(\u3000)
        // 为什么处理2个，而不是1个呢？
        // 避免英文单词出现断节
        SelectUtils.replaceContent(spanStr, mOriginalContent, "\u00A0\u00A0", "\u3000\u3000")
        SelectUtils.replaceContent(spanStr, mOriginalContent, "\u0020\u0020", "\u3000\u3000")
        // 文字转化成图片背景
        SelectUtils.replaceText2Emoji(mContext, emojiMap, spanStr, mOriginalContent)

        // 去除超链接点击背景色 https://github.com/ITxiaoguang/SelectTextHelper/issues/2
        mTextView.highlightColor = Color.TRANSPARENT
        mTextView.setText(spanStr, TextView.BufferType.SPANNABLE)
        mTextView.setOnTouchListener { _: View?, event: MotionEvent ->
            mTouchX = event.x.toInt()
            mTouchY = event.y.toInt()
            false
        }
        mTextView.setOnClickListener {
            if (usedClickListener) {
                usedClickListener = false
                return@setOnClickListener
            }
            if (null == mOperateWindow || !mOperateWindow!!.isShowing) {
                mSelectListener?.onDismiss()
            }
            reset()
            mSelectListener?.onClick(mTextView, mOriginalContent)
        }
        mTextView.setOnLongClickListener {
            mTextView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {}
                override fun onViewDetachedFromWindow(v: View) {
                    destroy()
                }
            })
            mOnPreDrawListener = ViewTreeObserver.OnPreDrawListener {
                if (isHideWhenScroll) {
                    isHideWhenScroll = false
                    postShowSelectView(mPopDelay)
                }
                // 拿textView的x坐标
                if (0 == mTextViewMarginStart) {
                    val location = IntArray(2)
                    mTextView.getLocationInWindow(location)
                    mTextViewMarginStart = location[0]
                }
                return@OnPreDrawListener true
            }
            mTextView.viewTreeObserver.addOnPreDrawListener(mOnPreDrawListener)

            // 根布局监听
            mRootTouchListener = OnTouchListener { _, _ ->
                reset()
                mTextView.rootView.setOnTouchListener(null)
                return@OnTouchListener false
            }
            mTextView.rootView.setOnTouchListener(mRootTouchListener)
            mOnScrollChangedListener = OnScrollChangedListener {
                if (mScrollShow) {
                    if (!isHideWhenScroll && !isHide) {
                        isHideWhenScroll = true
                        mOperateWindow?.dismiss()
                        mStartHandle?.dismiss()
                        mEndHandle?.dismiss()
                    }
                    mSelectListener?.onScrolling()
                } else {
                    reset()
                }
            }
            mTextView.viewTreeObserver.addOnScrollChangedListener(mOnScrollChangedListener)
            if (null == mOperateWindow) {
                mOperateWindow = OperateWindow(mContext)
            }
            if (mSelectAll) {
                showAllView()
            } else {
                showSelectView(mTouchX, mTouchY)
            }
            mSelectListener?.onLongClick(mTextView)
            true
        }
        // 此setMovementMethod可被修改
        mTextView.movementMethod = LinkMovementMethodInterceptor()
    }

    private fun postShowSelectView(duration: Int) {
        mTextView.removeCallbacks(mShowSelectViewRunnable)
        if (duration <= 0) {
            mShowSelectViewRunnable.run()
        } else {
            mTextView.postDelayed(mShowSelectViewRunnable, duration.toLong())
        }
    }

    private val mShowSelectViewRunnable = Runnable {
        if (isHide) return@Runnable
        if (null != mOperateWindow) {
            showOperateWindow()
        }
        mStartHandle?.let { showCursorHandle(mStartHandle) }
        mEndHandle?.let { showCursorHandle(mEndHandle) }
    }

    private fun hideSelectView() {
        isHide = true
        usedClickListener = false
        mStartHandle?.dismiss()
        mEndHandle?.dismiss()
        mOperateWindow?.dismiss()
    }

    private fun resetSelectionInfo() {
        resetEmojiBackground()
        mSelectionInfo.mSelectionContent = null
        if (mSpannable != null && mSpan != null) {
            mSpannable!!.removeSpan(mSpan)
            mSpan = null
        }
    }

    /**
     * @param x 长按时的手指的x坐标
     * @param y 长按时的手指的y坐标
     */
    private fun showSelectView(x: Int, y: Int) {
        reset()
        isHide = false
        if (mStartHandle == null) mStartHandle = CursorHandle(true)
        if (mEndHandle == null) mEndHandle = CursorHandle(false)
        val startOffset = SelectUtils.getPreciseOffset(mTextView, x, y)
        var endOffset = startOffset + mSelectTextLength
        if (mTextView.text is Spannable) {
            mSpannable = mTextView.text as Spannable
        }
        if (mSpannable == null || endOffset - 1 >= mTextView.text.length) {
            endOffset = mTextView.text.length
        }
        endOffset = changeEndOffset(startOffset, endOffset)
        selectText(startOffset, endOffset)
        showCursorHandle(mStartHandle)
        showCursorHandle(mEndHandle)
        showOperateWindow()
    }

    /**
     * 处理endOffset位置
     * ImageSpan文本，则会加够ImageSpan匹配的字符
     * Emoji文本，则去除最后的文字emoji字符
     *
     * @param startOffset 开始文字坐标
     * @param endOffset   结束文字坐标
     * @return endOffset
     */
    private fun changeEndOffset(startOffset: Int, endOffset: Int): Int {
        var endOffsetCopy = endOffset
        var selectText =
            mSpannable!!.subSequence(startOffset, endOffsetCopy) as Spannable
        // 是否ImageSpan文本
        if (SelectUtils.isImageSpanText(selectText)) {
            // 是否匹配Image
            while (!SelectUtils.matchImageSpan(emojiMap, selectText.toString())) {
                endOffsetCopy++
                selectText = mSpannable!!.subSequence(startOffset, endOffsetCopy) as Spannable
            }
        }
        // 选中的文字倒数第二个是文字 且 倒数第一个字符是文字emoji
        // 则去除最后的文字emoji字符
        val selectTextString = selectText.toString()
        if (selectTextString.length > 1) {
            if (!SelectUtils.isEmojiText(selectTextString[selectTextString.length - 2])
                && SelectUtils.isEmojiText(selectTextString[selectTextString.length - 1])
            ) {
                endOffsetCopy--
            }
        }
        return endOffsetCopy
    }

    /**
     * 显示操作弹窗
     * 可能多次调用
     */
    private fun showOperateWindow() {
        if (null == mOperateWindow) {
            mOperateWindow = OperateWindow(mContext)
        } // 开启已经全选无弹窗
        if (mSelectedAllNoPop && mSelectionInfo.mSelectionContent.toString() == mTextView.text.toString()) {
            mOperateWindow!!.dismiss()
            mSelectListener?.onSelectAllShowCustomPop()
        } else {
            mOperateWindow!!.show()
        }
    }

    /**
     * 全选
     * Select all
     */
    private fun showAllView() {
        reset()
        isHide = false
        if (mStartHandle == null) mStartHandle = CursorHandle(true)
        if (mEndHandle == null) mEndHandle = CursorHandle(false)
        if (mTextView.text is Spannable) {
            mSpannable = mTextView.text as Spannable
        }
        if (mSpannable == null) {
            return
        }
        selectText(0, mTextView.text.length)
        showCursorHandle(mStartHandle)
        showCursorHandle(mEndHandle)
        showOperateWindow()
    }

    private fun showCursorHandle(cursorHandle: CursorHandle?) {
        val layout = mTextView.layout
        val offset = if (cursorHandle!!.isLeft) mSelectionInfo.mStart else mSelectionInfo.mEnd
        var x = layout.getPrimaryHorizontal(offset).toInt()
        var y = layout.getLineBottom(layout.getLineForOffset(offset))

        // 右游标
        // mSelectionInfo.mEnd != 0 不是第一位
        // x == 0 右游标在最后面
        // 把右游标水平坐标定位在减去一个字的线条右侧
        // 把右游标底部线坐标定位在上一行
        if (!cursorHandle.isLeft && mSelectionInfo.mEnd != 0 && x == 0) {
            x = layout.getLineRight(layout.getLineForOffset(mSelectionInfo.mEnd - 1)).toInt()
            y = layout.getLineBottom(layout.getLineForOffset(mSelectionInfo.mEnd - 1))
        }
        cursorHandle.show(x, y)
    }

    private fun selectText(startPos: Int, endPos: Int) {
        if (startPos != -1) {
            mSelectionInfo.mStart = startPos
        }
        if (endPos != -1) {
            mSelectionInfo.mEnd = endPos
        }
        if (mSelectionInfo.mStart > mSelectionInfo.mEnd) {
            val temp = mSelectionInfo.mStart
            mSelectionInfo.mStart = mSelectionInfo.mEnd
            mSelectionInfo.mEnd = temp
        }
        if (mSpannable != null) {
            if (mSpan == null) {
                mSpan = BackgroundColorSpan(mSelectedColor)
            }
            mSelectionInfo.mSelectionContent =
                mSpannable!!.subSequence(mSelectionInfo.mStart, mSelectionInfo.mEnd).toString()
            mSpannable!!.setSpan(
                mSpan, mSelectionInfo.mStart, mSelectionInfo.mEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            mSelectListener?.onTextSelected(mSelectionInfo.mSelectionContent)

            // 设置图片表情选中背景
            setEmojiBackground()
        }
    }

    /**
     * 设置图片表情选中背景
     */
    private fun setEmojiBackground() {
        if (emojiMap.isEmpty()) {
            return
        }

        // 前部 透明背景
        setEmojiBackground(
            mSpannable!!.subSequence(0, mSelectionInfo.mStart) as Spannable,
            Color.TRANSPARENT
        )
        // 中间 选择背景
        setEmojiBackground(
            mSpannable!!.subSequence(
                mSelectionInfo.mStart,
                mSelectionInfo.mEnd
            ) as Spannable, mSelectedColor
        )
        // 尾部 透明背景
        setEmojiBackground(
            mSpannable!!.subSequence(
                mSelectionInfo.mEnd,
                mSpannable!!.length
            ) as Spannable, Color.TRANSPARENT
        )
    }

    /**
     * 利用反射改变图片背景颜色
     *
     * @param mSpannable Spannable
     * @param bgColor    background
     */
    private fun setEmojiBackground(mSpannable: Spannable, @ColorInt bgColor: Int) {
        if (TextUtils.isEmpty(mSpannable)) {
            return
        }
        val mSpans = SelectUtils.getFieldValue(mSpannable, "mSpans") as Array<*>?
        if (null != mSpans) {
            for (mSpan in mSpans) {
                if (mSpan is SelectImageSpan) {
                    if (mSpan.bgColor != bgColor) {
                        mSpan.bgColor = bgColor
                    }
                }
            }
        }
    }

    /**
     * 重置emoji选择背景
     */
    private fun resetEmojiBackground() {
        mSpannable?.let { setEmojiBackground(it, Color.TRANSPARENT) }
    }

    /**
     * 操作弹窗
     * 提供全选时可另外配置自定义弹窗
     * 自定义功能：复制、全选、等等
     * Custom function:Copy, Select all, And so on.
     */
    @SuppressLint("InflateParams")
    private inner class OperateWindow(context: Context?) {
        private val mWindow: PopupWindow?
        private val mTempCoors = IntArray(2)
        private val mWidth: Int
        private val mHeight: Int
        private val listAdapter: SelectTextPopAdapter
        private val rvContent: RecyclerView?
        private val ivArrow: ImageView

        init {
            val contentView = LayoutInflater.from(context).inflate(R.layout.pop_operate, null)
            rvContent = contentView.findViewById(R.id.rv_content)
            ivArrow = contentView.findViewById(R.id.iv_arrow)
            if (0 != mPopBgResource) {
                rvContent.setBackgroundResource(mPopBgResource)
            }
            if (0 != mPopArrowImg) {
                ivArrow.setBackgroundResource(mPopArrowImg)
            }
            val size = itemTextList.size
            // 宽 个数超过mPopSpanCount 取 mPopSpanCount
            mWidth = SelectUtils.dp2px((12 * 4 + 52 * size.coerceAtMost(mPopSpanCount)).toFloat())
            // 行数
            val row = (size / mPopSpanCount // 行数
                    + if (size % mPopSpanCount == 0) 0 else 1) // 有余数 加一行
            // 高
            mHeight = SelectUtils.dp2px((12 * (1 + row) + 52 * row + 5).toFloat())
            mWindow = PopupWindow(
                contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                false
            )
            mWindow.isClippingEnabled = false
            // 动画
            if (0 != mPopAnimationStyle) {
                mWindow.animationStyle = mPopAnimationStyle
            }
            listAdapter = SelectTextPopAdapter(context!!, itemTextList)
            listAdapter.setOnclickItemListener(object : onClickItemListener {
                override fun onClick(position: Int) {
                    dismiss()
                    itemListenerList[position].onClick()
                }
            })
            rvContent.adapter = listAdapter
        }

        fun show() {
            val deviceWidth = SelectUtils.displayWidth
            val size = itemTextList.size
            if (size > mPopSpanCount) {
                rvContent!!.layoutManager =
                    GridLayoutManager(mContext, mPopSpanCount, GridLayoutManager.VERTICAL, false)
            } else {
                rvContent!!.layoutManager =
                    GridLayoutManager(mContext, size, GridLayoutManager.VERTICAL, false)
            }
            mTextView.getLocationInWindow(mTempCoors)
            val layout = mTextView.layout
            var posX: Int
            var posXTemp = 0
            val startX = layout.getPrimaryHorizontal(mSelectionInfo.mStart).toInt() + mTempCoors[0]
            val startY = layout.getLineTop(layout.getLineForOffset(mSelectionInfo.mStart))
            val endY = layout.getLineTop(layout.getLineForOffset(mSelectionInfo.mEnd))
            var posY = startY + mTempCoors[1] - mHeight
            if (posY < 0) posY = 0

            // 在同一行
            posX = if (startY == endY) {
                val endX = layout.getPrimaryHorizontal(mSelectionInfo.mEnd)
                    .toInt() + mTempCoors[0] // posX = (起始点 + 终点) / 2 - (向左移动 mWidth / 2)
                (startX + endX) / 2 - mWidth / 2
            } else {
                // posX = (起始点 + (文本左边距  + 文本宽度                - 文本右padding)) / 2         - (向左移动 mWidth / 2)
                (startX + (mTempCoors[0] + mTextView.width - mTextView.paddingRight)) / 2 - mWidth / 2
            }
            if (posX <= 0) {
                posXTemp = posX
                posX = 0
            } else if (posX + mWidth > deviceWidth) {
                posXTemp = posX
                posX = deviceWidth - mWidth
            }
            mWindow!!.showAtLocation(mTextView, Gravity.NO_GRAVITY, posX, posY) // view中心位置
            // 在中间
            var arrowTranslationX = when {
                posXTemp == 0 -> {
                    // - SelectUtils.dp2px(mContext, 16) 是 margin
                    mWidth / 2 - SelectUtils.dp2px(16f)
                }
                posXTemp < 0 -> {
                    posXTemp + mWidth / 2
                }
                else -> {
                    // arrowTranslationX = 两坐标中心点   - 弹窗左侧点 - iv_arrow的margin
                    posXTemp + mWidth / 2 - posX - SelectUtils.dp2px(16f)
                }
            }
            if (arrowTranslationX < SelectUtils.dp2px(4f)) {
                arrowTranslationX = SelectUtils.dp2px(4f)
            } else if (arrowTranslationX > mWidth - SelectUtils.dp2px(4f)) {
                arrowTranslationX = mWidth - SelectUtils.dp2px(4f)
            }
            ivArrow.translationX = arrowTranslationX.toFloat()
        }

        fun dismiss() {
            mWindow!!.dismiss()
            mSelectListener?.onDismissCustomPop()
        }

        val isShowing: Boolean
            get() = mWindow?.isShowing ?: false
    }

    /**
     * 游标
     */
    private inner class CursorHandle(var isLeft: Boolean) : View(mContext) {
        private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val mPopupWindow: PopupWindow
        private val mCircleRadius = mCursorHandleSize / 2
        private val mWidth = mCursorHandleSize
        private val mHeight = mCursorHandleSize
        private val mPadding = 32 // 游标padding

        init {
            mPaint.color = mCursorHandleColor
            mPopupWindow = PopupWindow(this)
            mPopupWindow.isClippingEnabled = false
            mPopupWindow.width = mWidth + mPadding * 2
            mPopupWindow.height = mHeight + mPadding / 2
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawCircle(
                (mCircleRadius + mPadding).toFloat(),
                mCircleRadius.toFloat(),
                mCircleRadius.toFloat(),
                mPaint
            )
            if (isLeft) {
                canvas.drawRect(
                    (mCircleRadius + mPadding).toFloat(),
                    0f,
                    (mCircleRadius * 2 + mPadding).toFloat(),
                    mCircleRadius.toFloat(),
                    mPaint
                )
            } else {
                canvas.drawRect(
                    mPadding.toFloat(),
                    0f,
                    (mCircleRadius + mPadding).toFloat(),
                    mCircleRadius.toFloat(),
                    mPaint
                )
            }
        }

        private var mAdjustX = 0
        private var mAdjustY = 0
        private var mBeforeDragStart = 0
        private var mBeforeDragEnd = 0

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mBeforeDragStart = mSelectionInfo.mStart
                    mBeforeDragEnd = mSelectionInfo.mEnd
                    mAdjustX = event.x.toInt()
                    mAdjustY = event.y.toInt()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    showOperateWindow()
                    if (mMagnifierShow) {
                        // android 9 放大镜
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && null != mMagnifier) {
                            mMagnifier!!.dismiss()
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    mOperateWindow!!.dismiss()
                    mSelectListener?.onDismissCustomPop()
                    val rawX = event.rawX.toInt()
                    val rawY = event.rawY.toInt()
                    // x y不准 x 减去textView距离x轴距离值  y减去字体大小的像素值
                    update(
                        rawX + mAdjustX - mWidth - mTextViewMarginStart,
                        rawY + mAdjustY - mHeight - mTextView.textSize.toInt()
                    )
                    if (mMagnifierShow) {
                        // android 9 放大镜功能
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            if (null == mMagnifier) {
                                mMagnifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    Magnifier.Builder(mTextView).build()
                                } else {
                                    Magnifier(mTextView)
                                }
                            }
                            val viewPosition = IntArray(2)
                            mTextView.getLocationOnScreen(viewPosition)
                            val magnifierX = rawX - viewPosition[0]
                            val magnifierY = rawY - viewPosition[1] - SelectUtils.dp2px(32f)
                            mMagnifier!!.show(
                                magnifierX.toFloat(), magnifierY.coerceAtLeast(0).toFloat()
                            )
                        }
                    }
                }
            }
            return true
        }

        private fun changeDirection() {
            isLeft = !isLeft
            invalidate()
        }

        fun dismiss() {
            mPopupWindow.dismiss()
        }

        private val mTempCoors = IntArray(2)
        fun update(x: Int, y: Int) {
            var yCopy = y
            mTextView.getLocationInWindow(mTempCoors)
            val oldOffset = if (isLeft) {
                mSelectionInfo.mStart
            } else {
                mSelectionInfo.mEnd
            }
            yCopy -= mTempCoors[1]
            val offset = SelectUtils.getHysteresisOffset(mTextView, x, yCopy, oldOffset)
            if (offset != oldOffset) {
                resetSelectionInfo()
                if (isLeft) {
                    if (offset > mBeforeDragEnd) {
                        val handle = getCursorHandle(false)
                        changeDirection()
                        handle!!.changeDirection()
                        mBeforeDragStart = mBeforeDragEnd
                        selectText(mBeforeDragEnd, offset)
                        handle.updateCursorHandle()
                    } else {
                        selectText(offset, -1)
                    }
                    updateCursorHandle()
                } else {
                    if (offset < mBeforeDragStart) {
                        val handle = getCursorHandle(true)
                        handle!!.changeDirection()
                        changeDirection()
                        mBeforeDragEnd = mBeforeDragStart
                        selectText(offset, mBeforeDragStart)
                        handle.updateCursorHandle()
                    } else {
                        selectText(mBeforeDragStart, offset)
                    }
                    updateCursorHandle()
                }
            }
        }

        private fun updateCursorHandle() {
            mTextView.getLocationInWindow(mTempCoors)
            val layout = mTextView.layout
            if (isLeft) {
                mPopupWindow.update(
                    layout.getPrimaryHorizontal(mSelectionInfo.mStart).toInt() - mWidth + extraX,
                    layout.getLineBottom(layout.getLineForOffset(mSelectionInfo.mStart)) + extraY,
                    -1,
                    -1
                )
            } else {
                var horizontalEnd = layout.getPrimaryHorizontal(mSelectionInfo.mEnd).toInt()
                var lineBottomEnd =
                    layout.getLineBottom(layout.getLineForOffset(mSelectionInfo.mEnd)) // 右游标 // mSelectionInfo.mEnd != 0 不是第一位
                // horizontalEnd == 0 右游标在最后面
                // 把右游标水平坐标定位在减去一个字的线条右侧
                // 把右游标底部线坐标定位在上一行
                if (mSelectionInfo.mEnd != 0 && horizontalEnd == 0) {
                    horizontalEnd =
                        layout.getLineRight(layout.getLineForOffset(mSelectionInfo.mEnd - 1))
                            .toInt()
                    lineBottomEnd =
                        layout.getLineBottom(layout.getLineForOffset(mSelectionInfo.mEnd - 1))
                }
                mPopupWindow.update(horizontalEnd + extraX, lineBottomEnd + extraY, -1, -1)
            }
        }

        fun show(x: Int, y: Int) {
            mTextView.getLocationInWindow(mTempCoors)
            val offset = if (isLeft) mWidth else 0
            mPopupWindow.showAtLocation(
                mTextView, Gravity.NO_GRAVITY, x - offset + extraX, y + extraY
            )
        }

        val extraX: Int
            get() = mTempCoors[0] - mPadding + mTextView.paddingLeft
        val extraY: Int
            get() = mTempCoors[1] + mTextView.paddingTop
    }

    private fun getCursorHandle(isLeft: Boolean): CursorHandle? {
        return if (mStartHandle!!.isLeft == isLeft) {
            mStartHandle
        } else {
            mEndHandle
        }
    }

    private inner class SelectionInfo {
        var mStart = 0
        var mEnd = 0
        var mSelectionContent: CharSequence? = null
    }

    /**
     * 处理内容链接跳转
     */
    private inner class LinkMovementMethodInterceptor : LinkMovementMethod() {
        private var downLinkTime: Long = 0
        override fun onTouchEvent(
            widget: TextView,
            buffer: Spannable,
            event: MotionEvent
        ): Boolean {
            val action = event.action
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                var x = event.x.toInt()
                var y = event.y.toInt()
                x -= widget.totalPaddingLeft
                y -= widget.totalPaddingTop
                x += widget.scrollX
                y += widget.scrollY
                val layout = widget.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x.toFloat())
                val links = buffer.getSpans(off, off, ClickableSpan::class.java)
                if (links.isNotEmpty()) {
                    if (action == MotionEvent.ACTION_UP) { // 长按
                        if (downLinkTime + ViewConfiguration.getLongPressTimeout() < System.currentTimeMillis()) {
                            return false
                        } // 点击
                        if (links[0] is URLSpan) {
                            val url = links[0] as URLSpan
                            if (!TextUtils.isEmpty(url.url)) {
                                if (null != mSelectListener) {
                                    usedClickListener = true
                                    mSelectListener!!.onClickUrl(url.url)
                                }
                                return true
                            } else {
                                links[0].onClick(widget)
                            }
                        }
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        downLinkTime = System.currentTimeMillis()
                        Selection.setSelection(
                            buffer, buffer.getSpanStart(links[0]), buffer.getSpanEnd(links[0])
                        )
                    }
                    return true
                } else {
                    Selection.removeSelection(buffer)
                }
            }
            return super.onTouchEvent(widget, buffer, event)
        }
    }

}
