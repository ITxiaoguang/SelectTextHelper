package com.xiaoguang.selecttextview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoguang.selecttext.SelectTextHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 消息
 * hxg 2020.9.13 qq:929842234
 */
public class MsgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_1 = 1;// 文本
    private static final int VIEW_TYPE_2 = 2;// 图片
    private static final int VIEW_TYPE_3 = 3;// 链接

    private Context mContext;
    private List<MsgBean> mList;

    public MsgAdapter(Context mContext, List<MsgBean> list) {
        this.mContext = mContext;
        this.mList = list;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate;
        switch (viewType) {
            case VIEW_TYPE_2:
                inflate = LayoutInflater.from(mContext).inflate(R.layout.item_msg_img, parent, false);
                return new ViewHolderImg(inflate);
            case VIEW_TYPE_3:
                inflate = LayoutInflater.from(mContext).inflate(R.layout.item_msg_link, parent, false);
                return new ViewHolderLink(inflate);
            case VIEW_TYPE_1:
            default:
                inflate = LayoutInflater.from(mContext).inflate(R.layout.item_msg_text, parent, false);
                return new ViewHolderText(inflate);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MsgBean msgBean = mList.get(position);
        if (holder instanceof ViewHolderText) {
            ViewHolderText h = (ViewHolderText) holder;
            h.iv_head_left.setVisibility(msgBean.isReceive() ? View.VISIBLE : View.GONE);
            h.iv_head_right.setVisibility(msgBean.isReceive() ? View.GONE : View.VISIBLE);
            h.textView.setText(msgBean.getContent());
            if (msgBean.isReceive()) {
                setGravity(h.textView, Gravity.START);
            } else {
                setGravity(h.textView, Gravity.END);
            }

            // 演示消息列表选择文本
            h.selectText(msgBean);
        } else if (holder instanceof ViewHolderImg) {
            ViewHolderImg h = (ViewHolderImg) holder;
            h.iv_head_left.setVisibility(msgBean.isReceive() ? View.VISIBLE : View.GONE);
            h.iv_head_right.setVisibility(msgBean.isReceive() ? View.GONE : View.VISIBLE);
            h.iv_img.setBackgroundResource(R.mipmap.ic_launcher_round);
            if (msgBean.isReceive()) {
                setGravity(h.iv_img, Gravity.START);
            } else {
                setGravity(h.iv_img, Gravity.END);
            }

            h.rl_container.setOnLongClickListener(v -> {
                showCustomPop(h.rl_container, msgBean);
                return true;
            });
        } else if (holder instanceof ViewHolderLink) {
            ViewHolderLink h = (ViewHolderLink) holder;
            h.iv_head_left.setVisibility(msgBean.isReceive() ? View.VISIBLE : View.GONE);
            h.iv_head_right.setVisibility(msgBean.isReceive() ? View.GONE : View.VISIBLE);
            h.tv_link.setText(msgBean.getContent());
            if (msgBean.isReceive()) {
                setGravity(h.tv_link, Gravity.START);
            } else {
                setGravity(h.tv_link, Gravity.END);
            }

            h.rl_container.setOnLongClickListener(v -> {
                showCustomPop(h.rl_container, msgBean);
                return true;
            });
        }
    }

    // 设置FrameLayout子控件的gravity参数
    private void setGravity(View view, int gravity) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = gravity;
    }

    private void showCustomPop(View targetView, MsgBean msgBean) {
        boolean isText = msgBean.getType() == VIEW_TYPE_1;// 是否文本类型
        CustomPop msgPop = new CustomPop(mContext, targetView, isText);
        msgPop.addItem(R.drawable.ic_msg_copy, R.string.copy, () -> copy(null, msgBean.getContent()));
        msgPop.addItem(R.drawable.ic_msg_rollback, R.string.rollback, () -> toast(R.string.rollback));
        msgPop.addItem(R.drawable.ic_msg_forward, R.string.forward, () -> toast(R.string.forward));
        msgPop.addItem(R.drawable.ic_msg_collect, R.string.collect, () -> toast(R.string.collect));
        msgPop.addItem(R.drawable.ic_msg_select, R.string.select, () -> toast(R.string.select));
        msgPop.addItem(R.drawable.ic_msg_quote, R.string.quote, () -> toast(R.string.quote));
        msgPop.addItem(R.drawable.ic_msg_delete, R.string.delete, () -> toast(R.string.delete));
        // msgPop.setItemWrapContent(); // 自适应每个item
        msgPop.show();
    }

    private void toast(int strId) {
        toast(mContext.getString(strId));
    }

    private void toast(String str) {
        Toast.makeText(mContext.getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return null == mList ? 0 : mList.size();
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////   演示消息列表选择文本 start   ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////

    class ViewHolderText extends RecyclerView.ViewHolder {
        private static final int SHOW_DELAY = 100;// 显示自定义弹窗延迟
        private static final int RESET_DELAY = 120;// 重置自定义弹窗延迟

        private RelativeLayout text_rl_container;
        private ImageView iv_head_left;
        private ImageView iv_head_right;
        private TextView textView;

        private SelectTextHelper mSelectableTextHelper;
        private MsgBean textMsgBean;
        private String selectedText;

        ViewHolderText(View itemView) {
            super(itemView);
            text_rl_container = itemView.findViewById(R.id.rl_container);
            iv_head_left = itemView.findViewById(R.id.iv_head_left);
            iv_head_right = itemView.findViewById(R.id.iv_head_right);
            textView = itemView.findViewById(R.id.tv_content);
        }

        /**
         * 演示消息列表选择文本
         */
        private void selectText(MsgBean msgBean) {
            textMsgBean = msgBean;
            mSelectableTextHelper = new SelectTextHelper
                    .Builder(textView)// 游标演示
                    .setCursorHandleColor(mContext.getResources().getColor(R.color.colorAccent))// 游标演示
                    .setCursorHandleSizeInDp(22)// 游标大小 单位dp
                    .setSelectedColor(mContext.getResources().getColor(R.color.colorAccentTransparent))// 选中文本的颜色
                    .setSelectAll(true)// 初次选中是否全选 default true
                    .setScrollShow(true)// 滚动时是否继续显示 default true
                    .setSelectedAllNoPop(true)// 已经全选无弹窗，设置了监听会回调 onSelectAllShowCustomPop 方法
                    .setMagnifierShow(true)// 放大镜 default true
                    .addItem(R.drawable.ic_msg_copy, R.string.copy, () -> copy(mSelectableTextHelper, selectedText))
                    .addItem(R.drawable.ic_msg_select_all, R.string.select_all, this::selectAll)
                    .addItem(R.drawable.ic_msg_forward, R.string.forward, this::forward)
                    .build();
            mSelectableTextHelper.setSelectListener(new SelectTextHelper.OnSelectListener() {
                /**
                 * 点击回调
                 */
                @Override
                public void onClick(View v) {
                    clickTextView(textView.getText().toString().trim());
                }

                /**
                 * 长按回调
                 */
                @Override
                public void onLongClick(View v) {
                    postShowCustomPop(SHOW_DELAY);
                }

                /**
                 * 选中文本回调
                 */
                @Override
                public void onTextSelected(CharSequence content) {
                    selectedText = content.toString();
                }

                /**
                 * 弹窗关闭回调
                 */
                @Override
                public void onDismiss() {
                }

                /**
                 * 点击TextView里的url回调
                 *
                 * 已被下面重写
                 * textView.setMovementMethod(new LinkMovementMethodInterceptor());
                 */
                @Override
                public void onClickUrl(String url) {
                    toast("点击了：  " + url);
                }

                /**
                 * 全选显示自定义弹窗回调
                 */
                @Override
                public void onSelectAllShowCustomPop() {
                    postShowCustomPop(SHOW_DELAY);
                }

                /**
                 * 重置回调
                 */
                @Override
                public void onReset() {
                    SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissOperatePop"));
                }

                /**
                 * 解除自定义弹窗回调
                 */
                @Override
                public void onDismissCustomPop() {
                    SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissOperatePop"));
                }

                /**
                 * 是否正在滚动回调
                 */
                @Override
                public void onScrolling() {
                    removeShowSelectView();
                }
            });

            // 注册
            if (!SelectTextEventBus.getDefault().isRegistered(this)) {
                SelectTextEventBus.getDefault().register(this, SelectTextEvent.class);
            }
        }

        private long downTime = 0;

        /**
         * 双击进入查看内容
         *
         * @param content 内容
         */
        private void clickTextView(String content) {
            if (System.currentTimeMillis() - downTime < 500) {
                downTime = 0;
                SelectTextDialog dialog = new SelectTextDialog(mContext, content);
                dialog.show();
            } else {
                downTime = System.currentTimeMillis();
            }
        }

        /**
         * 延迟显示CustomPop
         * 为了短时间多次调用
         */
        private void postShowCustomPop(int duration) {
            textView.removeCallbacks(mShowCustomPopRunnable);
            textView.postDelayed(mShowCustomPopRunnable, duration);
        }

        private final Runnable mShowCustomPopRunnable =
                () -> showCustomPop(text_rl_container, textMsgBean);

        /**
         * 延迟重置
         * 为了支持滑动不重置
         */
        private void postReset(int duration) {
            textView.removeCallbacks(mShowSelectViewRunnable);
            textView.postDelayed(mShowSelectViewRunnable, duration);
        }

        private void removeShowSelectView() {
            textView.removeCallbacks(mShowSelectViewRunnable);
        }

        private final Runnable mShowSelectViewRunnable =
                () -> mSelectableTextHelper.reset();


        /**
         * 全选
         */
        private void selectAll() {
            SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissAllPop"));
            if (null != mSelectableTextHelper) {
                mSelectableTextHelper.selectAll();
            }
        }

        /**
         * 转发
         */
        private void forward() {
            SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissAllPop"));
            toast("转发");
        }

        /**
         * 自定义弹窗
         *
         * @param targetView 目标View
         * @param msgBean    实体
         */
        private void showCustomPop(View targetView, MsgBean msgBean) {
            boolean isText = msgBean.getType() == VIEW_TYPE_1;// 是否文本类型
            CustomPop msgPop = new CustomPop(mContext, targetView, isText);
            msgPop.addItem(R.drawable.ic_msg_copy, R.string.copy, () -> copy(mSelectableTextHelper, selectedText));
            msgPop.addItem(R.drawable.ic_msg_rollback, R.string.rollback, () -> toast(R.string.rollback));
            msgPop.addItem(R.drawable.ic_msg_forward, R.string.forward, () -> toast(R.string.forward));
            msgPop.addItem(R.drawable.ic_msg_collect, R.string.collect, () -> toast(R.string.collect));
            msgPop.addItem(R.drawable.ic_msg_select, R.string.select, () -> toast(R.string.select));
            msgPop.addItem(R.drawable.ic_msg_quote, R.string.quote, () -> toast(R.string.quote));
            msgPop.addItem(R.drawable.ic_msg_delete, R.string.delete, () -> toast(R.string.delete));
            // 设置每个item自适应
            // msgPop.setItemWrapContent();
            // 设置背景 和 箭头
            // msgPop.setPopStyle(R.drawable.shape_color_666666_radius_8, R.drawable.ic_arrow_666);
            msgPop.show();
        }

        /**
         * 自定义SelectTextEvent 隐藏 光标
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void handleSelector(SelectTextEvent event) {
            if (null == mSelectableTextHelper) {
                return;
            }
            String type = event.getType();
            if (TextUtils.isEmpty(type)) {
                return;
            }
            switch (type) {
                case "dismissAllPop":
                    mSelectableTextHelper.reset();
                    break;
                case "dismissAllPopDelayed":
                    postReset(RESET_DELAY);
                    break;
            }
        }
    }

    /**
     * 复制
     */
    private void copy(SelectTextHelper mSelectableTextHelper, String selectedText) {
        SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissAllPop"));
        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(selectedText, selectedText));
        }
        if (null != mSelectableTextHelper) {
            mSelectableTextHelper.reset();
        }
        toast("已复制");
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ViewHolderText) {
            // 注销
            SelectTextEventBus.getDefault().unregister(holder);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////   演示消息列表选择文本 end  ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////

    class ViewHolderImg extends RecyclerView.ViewHolder {
        private RelativeLayout rl_container;
        private ImageView iv_head_left;
        private ImageView iv_head_right;
        private ImageView iv_img;

        ViewHolderImg(View itemView) {
            super(itemView);
            rl_container = itemView.findViewById(R.id.rl_container);
            iv_head_left = itemView.findViewById(R.id.iv_head_left);
            iv_head_right = itemView.findViewById(R.id.iv_head_right);
            iv_img = itemView.findViewById(R.id.iv_img);
        }
    }

    class ViewHolderLink extends RecyclerView.ViewHolder {
        private RelativeLayout rl_container;
        private ImageView iv_head_left;
        private ImageView iv_head_right;
        private TextView tv_link;

        ViewHolderLink(View itemView) {
            super(itemView);
            rl_container = itemView.findViewById(R.id.rl_container);
            iv_head_left = itemView.findViewById(R.id.iv_head_left);
            iv_head_right = itemView.findViewById(R.id.iv_head_right);
            tv_link = itemView.findViewById(R.id.tv_link);
        }
    }
}
