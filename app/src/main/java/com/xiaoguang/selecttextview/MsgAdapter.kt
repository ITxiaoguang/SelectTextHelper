package com.xiaoguang.selecttextview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xiaoguang.selecttext.SelectTextHelper
import com.xiaoguang.selecttext.SelectTextHelper.OnSelectListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 消息
 * hxg 2020.9.13 qq:929842234
 */
class MsgAdapter(private val mContext: Context, private val mList: List<MsgBean>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_1 = 1 // 文本
        private const val VIEW_TYPE_2 = 2 // 图片
        private const val VIEW_TYPE_3 = 3 // 链接

        // 建议 SHOW_DELAY < RESET_DELAY
        // 避免从一个自定义弹窗到另一个自定义弹窗过度时出现闪动的bug
        // https://github.com/ITxiaoguang/SelectTextHelper/issues/5
        private const val SHOW_DELAY = 100L // 显示自定义弹窗延迟
        private const val RESET_DELAY = 130L // 重置自定义弹窗延迟
    }

    override fun getItemViewType(position: Int): Int {
        return mList!![position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate: View
        return when (viewType) {
            VIEW_TYPE_2 -> {
                inflate =
                    LayoutInflater.from(mContext).inflate(R.layout.item_msg_img, parent, false)
                ViewHolderImg(inflate)
            }
            VIEW_TYPE_3 -> {
                inflate =
                    LayoutInflater.from(mContext).inflate(R.layout.item_msg_link, parent, false)
                ViewHolderLink(inflate)
            }
            VIEW_TYPE_1 -> {
                inflate =
                    LayoutInflater.from(mContext).inflate(R.layout.item_msg_text, parent, false)
                ViewHolderText(inflate)
            }
            else -> {
                inflate =
                    LayoutInflater.from(mContext).inflate(R.layout.item_msg_text, parent, false)
                ViewHolderText(inflate)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msgBean = mList!![position]
        when (holder) {
            is ViewHolderText -> {
                holder.iv_head_left.visibility = if (msgBean.isReceive) View.VISIBLE else View.GONE
                holder.iv_head_right.visibility = if (msgBean.isReceive) View.GONE else View.VISIBLE
                holder.textView.text = msgBean.content
                if (msgBean.isReceive) {
                    setGravity(holder.textView, Gravity.START)
                } else {
                    setGravity(holder.textView, Gravity.END)
                }

                // 演示消息列表选择文本
                holder.selectText(msgBean)
            }
            is ViewHolderImg -> {
                holder.iv_head_left.visibility = if (msgBean.isReceive) View.VISIBLE else View.GONE
                holder.iv_head_right.visibility = if (msgBean.isReceive) View.GONE else View.VISIBLE
                holder.iv_img.setBackgroundResource(R.mipmap.ic_launcher_round)
                if (msgBean.isReceive) {
                    setGravity(holder.iv_img, Gravity.START)
                } else {
                    setGravity(holder.iv_img, Gravity.END)
                }
                holder.rl_container.setOnLongClickListener {
                    showCustomPop(holder.rl_container, msgBean)
                    true
                }
            }
            is ViewHolderLink -> {
                holder.iv_head_left.visibility = if (msgBean.isReceive) View.VISIBLE else View.GONE
                holder.iv_head_right.visibility = if (msgBean.isReceive) View.GONE else View.VISIBLE
                holder.tv_link.text = msgBean.content
                if (msgBean.isReceive) {
                    setGravity(holder.tv_link, Gravity.START)
                } else {
                    setGravity(holder.tv_link, Gravity.END)
                }
                holder.rl_container.setOnLongClickListener {
                    showCustomPop(holder.rl_container, msgBean)
                    true
                }
            }
        }
    }

    // 设置FrameLayout子控件的gravity参数
    private fun setGravity(view: View, gravity: Int) {
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = gravity
    }

    private fun showCustomPop(targetView: View, msgBean: MsgBean) {
        val isText = msgBean.type == VIEW_TYPE_1 // 是否文本类型
        val msgPop = CustomPop(mContext, targetView, isText)
        msgPop.addItem(
            R.drawable.ic_msg_copy,
            R.string.copy,
            object : CustomPop.onSeparateItemClickListener {
                override fun onClick() {
                    copy(null, msgBean.content)
                }
            })
        msgPop.addItem(R.drawable.ic_msg_rollback,
            R.string.rollback,
            object : CustomPop.onSeparateItemClickListener {
                override fun onClick() {
                    toast(R.string.rollback)
                }
            })
        msgPop.addItem(R.drawable.ic_msg_forward,
            R.string.forward,
            object : CustomPop.onSeparateItemClickListener {
                override fun onClick() {
                    toast(R.string.forward)
                }
            })
        msgPop.addItem(R.drawable.ic_msg_collect,
            R.string.collect,
            object : CustomPop.onSeparateItemClickListener {
                override fun onClick() {
                    toast(R.string.collect)
                }
            })
        msgPop.addItem(R.drawable.ic_msg_select,
            R.string.select,
            object : CustomPop.onSeparateItemClickListener {
                override fun onClick() {
                    toast(R.string.select)
                }
            })
        msgPop.addItem(R.drawable.ic_msg_quote,
            R.string.quote,
            object : CustomPop.onSeparateItemClickListener {
                override fun onClick() {
                    toast(R.string.quote)
                }
            })
        msgPop.addItem(R.drawable.ic_msg_delete,
            R.string.delete,
            object : CustomPop.onSeparateItemClickListener {
                override fun onClick() {
                    toast(R.string.delete)
                }
            })
        // msgPop.setItemWrapContent(); // 自适应每个item
        msgPop.show()
    }

    private fun toast(strId: Int) {
        toast(mContext.getString(strId))
    }

    private fun toast(str: String) {
        Toast.makeText(mContext.applicationContext, str, Toast.LENGTH_SHORT).show()
    }

    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////   演示消息列表选择文本 start   ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////

    internal inner class ViewHolderText(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text_rl_container: RelativeLayout
        val iv_head_left: ImageView
        val iv_head_right: ImageView
        val textView: TextView
        private var mSelectableTextHelper: SelectTextHelper? = null
        private var textMsgBean: MsgBean? = null
        private var selectedText: String? = null

        init {
            text_rl_container = itemView.findViewById(R.id.rl_container)
            iv_head_left = itemView.findViewById(R.id.iv_head_left)
            iv_head_right = itemView.findViewById(R.id.iv_head_right)
            textView = itemView.findViewById(R.id.tv_content)
        }

        /**
         * 演示消息列表选择文本
         */
        fun selectText(msgBean: MsgBean) {
            textMsgBean = msgBean
            mSelectableTextHelper = SelectTextHelper.Builder(textView) // 放你的textView到这里！！
                .setCursorHandleColor(ContextCompat.getColor(mContext, R.color.colorAccent)) // 游标颜色
                .setCursorHandleSizeInDp(22f) // 游标大小 单位dp
                .setSelectedColor(ContextCompat.getColor(mContext, R.color.colorAccentTransparent)) // 选中文本的颜色
                .setSelectAll(true) // 初次选中是否全选 default true
                .setScrollShow(true) // 滚动时是否继续显示 default true
                .setSelectedAllNoPop(true) // 已经全选无弹窗，设置了监听会回调 onSelectAllShowCustomPop 方法
                .setMagnifierShow(true) // 放大镜 default true
                .setSelectTextLength(2)// 首次选中文本的长度 default 2
                .setPopDelay(100)// 弹窗延迟时间 default 100毫秒
                .setPopAnimationStyle(R.style.Base_Animation_AppCompat_Dialog)// 弹窗动画 default 无动画
                .addItem(R.drawable.ic_msg_copy,
                    R.string.copy,
                    object : SelectTextHelper.Builder.onSeparateItemClickListener {
                        override fun onClick() {
                            copy(mSelectableTextHelper, selectedText)
                        }
                    }).addItem(R.drawable.ic_msg_select_all,
                    R.string.select_all,
                    object : SelectTextHelper.Builder.onSeparateItemClickListener {
                        override fun onClick() {
                            selectAll()
                        }
                    }).addItem(R.drawable.ic_msg_forward,
                    R.string.forward,
                    object : SelectTextHelper.Builder.onSeparateItemClickListener {
                        override fun onClick() {
                            forward()
                        }
                    }).setPopSpanCount(5) // 设置操作弹窗每行个数 default 5
                .setPopStyle(
                    R.drawable.shape_color_4c4c4c_radius_8 /*操作弹窗背*/,
                    R.drawable.ic_arrow /*箭头图片*/
                ) // 设置操作弹窗背景色、箭头图片
                .build()
            mSelectableTextHelper!!.setSelectListener(object : OnSelectListener {
                /**
                 * 点击回调
                 */
                override fun onClick(v: View?, originalContent: String?) {
                    clickTextView(originalContent!!)
                }

                /**
                 * 长按回调
                 */
                override fun onLongClick(v: View?) {
                    postShowCustomPop(Companion.SHOW_DELAY)
                }

                /**
                 * 选中文本回调
                 */
                override fun onTextSelected(content: String?) {
                    selectedText = content
                }

                /**
                 * 弹窗关闭回调
                 */
                override fun onDismiss() {}

                /**
                 * 点击TextView里的url回调
                 *
                 * 已被下面重写
                 * textView.setMovementMethod(new LinkMovementMethodInterceptor());
                 */
                override fun onClickUrl(url: String?) {
                    toast("点击了：  $url")

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    mContext.startActivity(intent)
                }

                /**
                 * 全选显示自定义弹窗回调
                 */
                override fun onSelectAllShowCustomPop() {
                    postShowCustomPop(Companion.SHOW_DELAY)
                }

                /**
                 * 重置回调
                 */
                override fun onReset() {
                    SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissOperatePop"))
                }

                /**
                 * 解除自定义弹窗回调
                 */
                override fun onDismissCustomPop() {
                    SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissOperatePop"))
                }

                /**
                 * 是否正在滚动回调
                 */
                override fun onScrolling() {
                    removeShowSelectView()
                }
            })

            // 注册
            if (!SelectTextEventBus.instance.isRegistered(this)) {
                SelectTextEventBus.instance.register(this, SelectTextEvent::class.java)
            }
        }

        private var downTime: Long = 0

        /**
         * 双击进入查看内容
         *
         * @param content 内容
         */
        private fun clickTextView(content: String) {
            if (System.currentTimeMillis() - downTime < 500) {
                downTime = 0
                val dialog = SelectTextDialog(mContext, content)
                dialog.show()
            } else {
                downTime = System.currentTimeMillis()
            }
        }

        /**
         * 延迟显示CustomPop
         * 防抖
         */
        private fun postShowCustomPop(duration: Long) {
            textView.removeCallbacks(mShowCustomPopRunnable)
            textView.postDelayed(mShowCustomPopRunnable, duration)
        }

        private val mShowCustomPopRunnable =
            Runnable { showCustomPop(text_rl_container, textMsgBean) }

        /**
         * 延迟重置
         * 为了支持滑动不重置
         */
        private fun postReset(duration: Long) {
            textView.removeCallbacks(mShowSelectViewRunnable)
            textView.postDelayed(mShowSelectViewRunnable, duration)
        }

        private fun removeShowSelectView() {
            textView.removeCallbacks(mShowSelectViewRunnable)
        }

        private val mShowSelectViewRunnable = Runnable { mSelectableTextHelper!!.reset() }

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
        private fun forward() {
            SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissAllPop"))
            toast("转发")
        }

        /**
         * 自定义弹窗
         *
         * @param targetView 目标View
         * @param msgBean    实体
         */
        private fun showCustomPop(targetView: View, msgBean: MsgBean?) {
            val isText = msgBean!!.type == VIEW_TYPE_1 // 是否文本类型
            val msgPop = CustomPop(mContext, targetView, isText)
            msgPop.addItem(R.drawable.ic_msg_copy,
                R.string.copy,
                object : CustomPop.onSeparateItemClickListener {
                    override fun onClick() {
                        copy(mSelectableTextHelper, selectedText)
                    }
                })
            msgPop.addItem(R.drawable.ic_msg_rollback,
                R.string.rollback,
                object : CustomPop.onSeparateItemClickListener {
                    override fun onClick() {
                        toast(R.string.rollback)
                    }
                })
            msgPop.addItem(R.drawable.ic_msg_forward,
                R.string.forward,
                object : CustomPop.onSeparateItemClickListener {
                    override fun onClick() {
                        toast(R.string.forward)
                    }
                })
            msgPop.addItem(R.drawable.ic_msg_collect,
                R.string.collect,
                object : CustomPop.onSeparateItemClickListener {
                    override fun onClick() {
                        toast(R.string.collect)
                    }
                })
            msgPop.addItem(R.drawable.ic_msg_select,
                R.string.select,
                object : CustomPop.onSeparateItemClickListener {
                    override fun onClick() {
                        toast(R.string.select)
                    }
                })
            msgPop.addItem(R.drawable.ic_msg_quote,
                R.string.quote,
                object : CustomPop.onSeparateItemClickListener {
                    override fun onClick() {
                        toast(R.string.quote)
                    }
                })
            msgPop.addItem(R.drawable.ic_msg_delete,
                R.string.delete,
                object : CustomPop.onSeparateItemClickListener {
                    override fun onClick() {
                        toast(R.string.delete)
                    }
                })
            // 设置每个item自适应
            // msgPop.setItemWrapContent();
            // 设置背景 和 箭头
            // msgPop.setPopStyle(R.drawable.shape_color_666666_radius_8, R.drawable.ic_arrow_666);
            msgPop.show()
        }

        /**
         * 自定义SelectTextEvent 隐藏 光标
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        fun handleSelector(event: SelectTextEvent) {
            if (null == mSelectableTextHelper) {
                return
            }
            val type = event.type
            if (TextUtils.isEmpty(type)) {
                return
            }
            when (type) {
                "dismissAllPop" -> mSelectableTextHelper!!.reset()
                "dismissAllPopDelayed" -> postReset(Companion.RESET_DELAY)
            }
        }

    }

    /**
     * 复制
     */
    private fun copy(mSelectableTextHelper: SelectTextHelper?, selectedText: String?) {
        SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissAllPop"))
        val cm = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(selectedText, selectedText))
        mSelectableTextHelper?.reset()
        toast("已复制")
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is ViewHolderText) {
            // 注销
            SelectTextEventBus.instance.unregister(holder)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////   演示消息列表选择文本 end  ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////

    internal inner class ViewHolderImg(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rl_container: RelativeLayout
        val iv_head_left: ImageView
        val iv_head_right: ImageView
        val iv_img: ImageView

        init {
            rl_container = itemView.findViewById(R.id.rl_container)
            iv_head_left = itemView.findViewById(R.id.iv_head_left)
            iv_head_right = itemView.findViewById(R.id.iv_head_right)
            iv_img = itemView.findViewById(R.id.iv_img)
        }
    }

    internal inner class ViewHolderLink(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rl_container: RelativeLayout
        val iv_head_left: ImageView
        val iv_head_right: ImageView
        val tv_link: TextView

        init {
            rl_container = itemView.findViewById(R.id.rl_container)
            iv_head_left = itemView.findViewById(R.id.iv_head_left)
            iv_head_right = itemView.findViewById(R.id.iv_head_right)
            tv_link = itemView.findViewById(R.id.tv_link)
        }
    }

}