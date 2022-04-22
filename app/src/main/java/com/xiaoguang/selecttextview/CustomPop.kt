package com.xiaoguang.selecttextview

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.util.Pair
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xiaoguang.selecttext.SelectTextHelper
import com.xiaoguang.selecttext.SelectTextPopAdapter
import com.xiaoguang.selecttext.SelectTextPopAdapter.onClickItemListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 聊天长按弹出
 * hxg 2019.11.20
 */
class CustomPop(
    private val context: Context,
    private val msgView: View,
    private val isText: Boolean
) : PopupWindow() {
    private var rv_content: RecyclerView? = null
    private var iv_arrow_up: ImageView? = null
    private var iv_arrow: ImageView? = null
    private val itemTextList: MutableList<Pair<Int, String>>? = LinkedList()
    private val itemListenerList: MutableList<onSeparateItemClickListener> = LinkedList()
    private var listAdapter: SelectTextPopAdapter? = null
    private var popupWindow: PopupWindow? = null
    private var mWidth = 0// 本pop的宽
    private var mHeight = 0// 本pop的高

    /**
     * 图标 和 文字
     */
    fun addItem(
        @DrawableRes drawableId: Int,
        @StringRes textResId: Int,
        listener: onSeparateItemClickListener
    ) {
        addItem(drawableId, context.getString(textResId), listener)
    }

    /**
     * 图标 和 文字
     */
    fun addItem(
        @DrawableRes drawableId: Int,
        itemText: String,
        listener: onSeparateItemClickListener
    ) {
        itemTextList!!.add(Pair(drawableId, itemText))
        itemListenerList.add(listener)
    }

    /**
     * 只有文字
     */
    fun addItem(itemText: String, listener: onSeparateItemClickListener) {
        addItem(0, itemText, listener)
    }

    /**
     * 只有文字
     */
    fun addItem(@StringRes textResId: Int, listener: onSeparateItemClickListener) {
        addItem(context.getString(textResId), listener)
    }

    /**
     * 设置背景 和 箭头
     */
    fun setPopStyle(bgColor: Int, arrowImg: Int) {
        if (null != rv_content && null != iv_arrow) {
            rv_content!!.setBackgroundResource(bgColor)
            iv_arrow!!.setBackgroundResource(arrowImg)
            SelectTextHelper.Companion.setWidthHeight(iv_arrow!!, dp2px(14), dp2px(7))
        }
    }

    /**
     * 设置每个item自适应
     */
    fun setItemWrapContent() {
        if (null != listAdapter) {
            listAdapter!!.setItemWrapContent(true)
        }
    }

    fun show() {
        if (null != itemTextList && itemTextList.size <= 0) {
            return
        }
        updateListView()
    }

    interface onSeparateItemClickListener {
        fun onClick()
    }

    /**
     * public end
     */
    private fun init() {
        listAdapter = SelectTextPopAdapter(context, itemTextList)
        listAdapter!!.setOnclickItemListener(object : onClickItemListener {
            override fun onClick(position: Int) {
                SelectTextEventBus.default.dispatch(SelectTextEvent("dismissAllPop"))
                dismiss()
                itemListenerList[position].onClick()
            }
        })
        val popWindowView = LayoutInflater.from(context).inflate(R.layout.pop_operate, null)
        rv_content = popWindowView.findViewById(R.id.rv_content)
        iv_arrow_up = popWindowView.findViewById(R.id.iv_arrow_up)
        iv_arrow = popWindowView.findViewById(R.id.iv_arrow)
        if (isText) {
            popupWindow = PopupWindow(
                popWindowView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                false
            )
            popupWindow!!.isClippingEnabled = false
            if (!SelectTextEventBus.default.isRegistered(this)) {
                SelectTextEventBus.default.register(this, SelectTextEvent::class.java)
            }
        } else {
            popupWindow = PopupWindow(
                popWindowView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            // 使其聚集
            popupWindow!!.isFocusable = true
            // 设置允许在外点击消失
            popupWindow!!.isOutsideTouchable = true
            // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow!!.setBackgroundDrawable(BitmapDrawable())
        }
    }

    private fun updateListView() {
        listAdapter!!.notifyDataSetChanged()
        if (rv_content != null) {
            rv_content!!.adapter = listAdapter
        }
        val size = itemTextList!!.size
        val deviceWidth = SelectTextHelper.Companion.displayWidth
        val deviceHeight = SelectTextHelper.Companion.displayHeight
        val statusHeight = SelectTextHelper.Companion.statusHeight
        //计算箭头显示的位置
        val location = IntArray(2)
        msgView.getLocationOnScreen(location)
        val msgViewWidth = msgView.width
        val msgViewHeight = msgView.height
        // view中心坐标 = view的位置 + view的宽度 / 2
        val centerWidth = location[0] + msgViewWidth / 2
        if (size > 5) {
            mWidth = dp2px(12 * 4 + 52 * 5)
            mHeight = dp2px(12 * 3 + 52 * 2 + 5)
        } else {
            mWidth = dp2px(12 * 4 + 52 * size)
            mHeight = dp2px(12 * 2 + 52 + 5)
        }
        // topUI true pop显示在顶部
        val topUI = location[1] > mHeight + statusHeight
        val arrowView: View?
        if (topUI) {
            iv_arrow!!.visibility = View.VISIBLE
            iv_arrow_up!!.visibility = View.GONE
            arrowView = iv_arrow
        } else {
            iv_arrow_up!!.visibility = View.VISIBLE
            iv_arrow!!.visibility = View.GONE
            arrowView = iv_arrow_up
        }
        if (size > 5) {
            rv_content!!.layoutManager =
                GridLayoutManager(context, 5, GridLayoutManager.VERTICAL, false)
            // x轴 （屏幕 - mWidth）/ 2
            val posX = (deviceWidth - mWidth) / 2
            // topUI ?
            // msgView的y轴 - popupWindow的高度
            // ：msgView的y轴 + msgView高度 + 8dp间距
            var posY = if (topUI) location[1] - mHeight else location[1] + msgViewHeight + dp2px(8)
            if (!topUI // 反向的ui
                // 底部已经超过了 屏幕高度 - （弹窗高度 + 输入框）
                && location[1] + msgView.height > deviceHeight - dp2px(52 * 2 + 60)
            ) {
                // 显示在屏幕3/4高度
                posY = deviceHeight * 3 / 4
            }
            popupWindow!!.showAtLocation(msgView, Gravity.NO_GRAVITY, posX, posY)
            val arrX = mWidth / 2 - dp2px(12 + 4)
            arrowView!!.translationX = arrX.toFloat()
        } else {
            rv_content!!.layoutManager =
                GridLayoutManager(context, size, GridLayoutManager.VERTICAL, false)
            // x轴 （屏幕 - mWidth）/ 2
            var posX = centerWidth - mWidth / 2
            // 右侧的最大宽度
            val max = centerWidth + mWidth / 2
            if (posX < 0) {
                posX = 0
            } else if (max > deviceWidth) {
                posX = deviceWidth - mWidth
            }
            // topUI ?
            // msgView的y轴 - popupWindow的高度
            // ：msgView的y轴 + msgView高度 + 8dp间距
            var posY = if (topUI) location[1] - mHeight else location[1] + msgViewHeight + dp2px(8)
            if (!topUI // 反向的ui
                // 底部已经超过了 屏幕高度 - （弹窗高度 + 输入框）
                && location[1] + msgView.height > deviceHeight - dp2px(52 * 2 + 60)
            ) {
                // 显示在屏幕3/4高度
                posY = deviceHeight * 3 / 4
            }
            popupWindow!!.showAtLocation(msgView, Gravity.NO_GRAVITY, posX, posY)
            //         view中心坐标 - pop坐标 - 16dp padding
            val arrX = centerWidth - posX - dp2px(16)
            arrowView!!.translationX = arrX.toFloat()
        }
    }

    // 隐藏 弹窗
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleSelector(event: SelectTextEvent) {
        // 隐藏操作弹窗
        if ("dismissOperatePop" == event.type) {
            dismiss()
        }
    }

    override fun dismiss() {
        popupWindow!!.dismiss()
        SelectTextEventBus.default.unregister(this)
    }

    companion object {
        private fun dp2px(num: Int): Int {
            return (num * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
        }
    }

    /**
     * public start
     */
    init {
        init()
    }
}