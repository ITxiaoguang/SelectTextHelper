package com.xiaoguang.selecttextview

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.xiaoguang.selecttext.SelectTextHelper
import com.zzhoujay.richtext.RichText

/**
 * 选择文字
 * hxg 2021/9/14 qq:929842234z
 */
class SelectTextDialog(context: Context?, private val mText: CharSequence) : Dialog(
    context!!, R.style.SelectTextFragment
) {
    private lateinit var mSelectableTextHelper: SelectTextHelper
    private var selectText: CharSequence? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setContentView(R.layout.dialog_select_text)

        // 一定要在setContentView之后调用，否则无效
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        findViewById<View>(R.id.rl_selector).setOnClickListener { v: View? ->
            if (mSelectableTextHelper.isPopShowing) {
                mSelectableTextHelper.reset()
            } else {
                dismiss()
            }
        }
        val tv_msg_content = findViewById<TextView>(R.id.tv_msg_content)
        if (mText.isNotEmpty() && (mText.length > 16 || mText.contains("\n"))) {
            tv_msg_content.gravity = Gravity.START
        } else {
            tv_msg_content.gravity = Gravity.CENTER
        }

        // 不推荐 富文本可能被修改值 导致gif动不了
        if (mText is Spanned || mText is Spannable) {
            tv_msg_content.text = mText
            setSelectText(tv_msg_content)
        }
        // 推荐
        else {
            RichText.initCacheDir(context.applicationContext)
            RichText.from(mText.toString())
                .autoFix(false) // 是否自动修复宽高，默认true
                .autoPlay(true) // gif自动播放
                .done { // 在成功回调处理
                    // 演示消息列表选择文本
                    setSelectText(tv_msg_content)
                }
                .into(tv_msg_content)
        }
    }

    private fun setSelectText(textView: TextView) {
        mSelectableTextHelper = SelectTextHelper.Builder(textView)
            .setCursorHandleColor(ContextCompat.getColor(context, R.color.colorAccent))
            .setCursorHandleSizeInDp(24f)
            .setSelectedColor(ContextCompat.getColor(context, R.color.colorAccentTransparent))
            .setSelectAll(false)
            .addItem(R.drawable.ic_msg_copy, R.string.copy,
                object : SelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
                        copy(selectText.toString())
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
                        forward(selectText.toString())
                    }
                })
            .build()
        mSelectableTextHelper.setSelectListener(object : SelectTextHelper.OnSelectListenerImpl() {
            override fun onClick(v: View?, originalContent: CharSequence?) {}
            override fun onLongClick(v: View?) {}
            override fun onTextSelected(content: CharSequence?) {
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
        })
    }

    override fun dismiss() {
        mSelectableTextHelper.reset()
        super.dismiss()
    }

    /**
     * 复制
     */
    private fun copy(selectText: String?) {
        SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissAllPop"))
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(selectText, selectText))
        mSelectableTextHelper.reset()
        toast("已复制")
    }

    /**
     * 全选
     */
    private fun selectAll() {
        SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissAllPop"))
        mSelectableTextHelper.selectAll()
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