package com.xiaoguang.selecttextview

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.xiaoguang.selecttext.SelectTextHelper
import com.xiaoguang.selecttext.SelectTextHelper.OnSelectListener

/**
 * 选择文字
 * hxg 2021/9/14 qq:929842234z
 */
class SelectTextDialog(context: Context?, private val mText: String) : Dialog(
    context!!, R.style.SelectTextFragment) {
    private var mSelectableTextHelper: SelectTextHelper? = null
    private var selectText: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setContentView(R.layout.fragment_select_text)

        // 一定要在setContentView之后调用，否则无效
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        findViewById<View>(R.id.rl_selector).setOnClickListener { v: View? ->
            if (mSelectableTextHelper!!.isPopShowing) {
                mSelectableTextHelper!!.reset()
            } else {
                dismiss()
            }
        }
        val tv_msg_content = findViewById<TextView>(R.id.tv_msg_content)
        tv_msg_content.text = mText
        if (mText.length > 0 && mText.length > 16
            || mText.contains("\n")
        ) {
            tv_msg_content.gravity = Gravity.START
        } else {
            tv_msg_content.gravity = Gravity.CENTER
        }
        mSelectableTextHelper = SelectTextHelper.Builder(tv_msg_content)
            .setCursorHandleColor(ContextCompat.getColor(context, R.color.colorAccent))
            .setCursorHandleSizeInDp(24f)
            .setSelectedColor(ContextCompat.getColor(context, R.color.colorAccentTransparent))
            .setSelectAll(false)
            .addItem(R.drawable.ic_msg_copy, R.string.copy,
                object : SelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
                        copy(selectText)
                    }
                })
            .addItem(
                R.drawable.ic_msg_select_all,
                R.string.select_all,
                object : SelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
                        selectAll()
                    }
                })
            .addItem(R.drawable.ic_msg_forward, R.string.forward,
                object : SelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
                        forward(selectText)
                    }
                })
            .build()
        mSelectableTextHelper!!.setSelectListener(object : OnSelectListener {
            override fun onClick(v: View?, originalContent: String?) {}
            override fun onLongClick(v: View?) {}
            override fun onTextSelected(content: String?) {
                selectText = content
            }

            override fun onDismiss() {
                dismiss()
            }

            override fun onClickUrl(url: String?) {
                toast("点击了：  $url")

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }

            override fun onSelectAllShowCustomPop() {}
            override fun onReset() {}
            override fun onDismissCustomPop() {}
            override fun onScrolling() {}
        })
    }

    override fun dismiss() {
        mSelectableTextHelper!!.reset()
        super.dismiss()
    }

    /**
     * 复制
     */
    private fun copy(selectText: String?) {
        SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissAllPop"))
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(selectText, selectText))
        mSelectableTextHelper!!.reset()
        toast("已复制")
    }

    /**
     * 全选
     */
    private fun selectAll() {
        SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissAllPop"))
        if (null != mSelectableTextHelper) {
            mSelectableTextHelper!!.selectAll()
        }
    }

    /**
     * 转发
     */
    private fun forward(content: String?) {
        SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissAllPop"))
        toast("转发")
    }

    private fun toast(msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
}