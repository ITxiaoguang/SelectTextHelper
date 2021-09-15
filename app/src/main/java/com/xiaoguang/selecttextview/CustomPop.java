package com.xiaoguang.selecttextview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoguang.selecttext.SelectTextHelper;
import com.xiaoguang.selecttext.SelectTextPopAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;

/**
 * 聊天长按弹出
 * hxg 2019.11.20
 */
public class CustomPop extends PopupWindow {
    private Context context;

    private View msgView;
    private RecyclerView rv_content;
    private ImageView iv_arrow_up;
    private ImageView iv_arrow;

    private List<Pair<Integer, String>> itemTextList = new LinkedList<>();
    private List<onSeparateItemClickListener> itemListenerList = new LinkedList<>();

    private SelectTextPopAdapter listAdapter;
    private PopupWindow popupWindow;
    private boolean isText;
    private int mWidth;// 本pop的宽
    private int mHeight;// 本pop的高

    /**
     * public start
     */

    public CustomPop(Context context, View msgView, boolean isText) {
        this.context = context;
        this.msgView = msgView;
        this.isText = isText;
        init();
    }

    /**
     * 图标 和 文字
     */
    public void addItem(@DrawableRes int drawableId, @StringRes int textResId, onSeparateItemClickListener listener) {
        addItem(drawableId, context.getString(textResId), listener);
    }

    /**
     * 图标 和 文字
     */
    public void addItem(@DrawableRes int drawableId, String itemText, onSeparateItemClickListener listener) {
        itemTextList.add(new Pair<>(drawableId, itemText));
        itemListenerList.add(listener);
    }

    /**
     * 只有文字
     */
    public void addItem(String itemText, onSeparateItemClickListener listener) {
        addItem(0, itemText, listener);
    }

    /**
     * 只有文字
     */
    public void addItem(@StringRes int textResId, onSeparateItemClickListener listener) {
        addItem(context.getString(textResId), listener);
    }

    /**
     * 设置背景 和 箭头
     */
    public void setPopStyle(int bgColor, int arrowImg) {
        if (null != rv_content && null != iv_arrow) {
            rv_content.setBackgroundResource(bgColor);
            iv_arrow.setBackgroundResource(arrowImg);
            SelectTextHelper.setWidthHeight(iv_arrow, dp2px(14), dp2px(7));
        }
    }

    /**
     * 设置每个item自适应
     */
    public void setItemWrapContent() {
        if (null != listAdapter) {
            listAdapter.setItemWrapContent(true);
        }
    }

    public void show() {
        if (null != itemTextList && itemTextList.size() <= 0) {
            return;
        }
        updateListView();
    }

    public interface onSeparateItemClickListener {
        void onClick();
    }

    /**
     * public end
     */

    private void init() {
        listAdapter = new SelectTextPopAdapter(context, itemTextList);
        listAdapter.setOnclickItemListener(position -> {
            SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissAllPop"));
            dismiss();
            itemListenerList.get(position).onClick();
        });

        View popWindowView = LayoutInflater.from(context).inflate(R.layout.pop_operate, null);

        rv_content = popWindowView.findViewById(R.id.rv_content);
        iv_arrow_up = popWindowView.findViewById(R.id.iv_arrow_up);
        iv_arrow = popWindowView.findViewById(R.id.iv_arrow);
        if (isText) {
            popupWindow = new PopupWindow(
                    popWindowView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    false);
            popupWindow.setClippingEnabled(false);
            if (!SelectTextEventBus.getDefault().isRegistered(this)) {
                SelectTextEventBus.getDefault().register(this, SelectTextEvent.class);
            }
        } else {
            popupWindow = new PopupWindow(
                    popWindowView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);
            // 使其聚集
            popupWindow.setFocusable(true);
            // 设置允许在外点击消失
            popupWindow.setOutsideTouchable(true);
            // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
        }
    }

    private void updateListView() {
        listAdapter.notifyDataSetChanged();
        if (rv_content != null) {
            rv_content.setAdapter(listAdapter);
        }
        int size = itemTextList.size();
        int deviceWidth = SelectTextHelper.getDisplayWidth();
        int deviceHeight = SelectTextHelper.getDisplayHeight();
        int statusHeight = SelectTextHelper.getStatusHeight();
        //计算箭头显示的位置
        int[] location = new int[2];
        msgView.getLocationOnScreen(location);
        int msgViewWidth = msgView.getWidth();
        int msgViewHeight = msgView.getHeight();
        // view中心坐标 = view的位置 + view的宽度 / 2
        int centerWidth = location[0] + msgViewWidth / 2;
        if (size > 5) {
            mWidth = dp2px(12 * 4 + 52 * 5);
            mHeight = dp2px(12 * 3 + 52 * 2 + 5);
        } else {
            mWidth = dp2px(12 * 4 + 52 * size);
            mHeight = dp2px(12 * 2 + 52 + 5);
        }
        // topUI true pop显示在顶部
        boolean topUI = location[1] > mHeight + statusHeight;
        View arrowView;
        if (topUI) {
            iv_arrow.setVisibility(View.VISIBLE);
            iv_arrow_up.setVisibility(View.GONE);
            arrowView = iv_arrow;
        } else {
            iv_arrow_up.setVisibility(View.VISIBLE);
            iv_arrow.setVisibility(View.GONE);
            arrowView = iv_arrow_up;
        }
        if (size > 5) {
            rv_content.setLayoutManager(new GridLayoutManager(context, 5, GridLayoutManager.VERTICAL, false));
            // x轴 （屏幕 - mWidth）/ 2
            int posX = (deviceWidth - mWidth) / 2;
            // topUI ?
            // msgView的y轴 - popupWindow的高度
            // ：msgView的y轴 + msgView高度 + 8dp间距
            int posY = topUI ? location[1] - mHeight : location[1] + msgViewHeight + dp2px(8);
            if (!topUI// 反向的ui
                    // 底部已经超过了 屏幕高度 - （弹窗高度 + 输入框）
                    && location[1] + msgView.getHeight() > deviceHeight - dp2px(52 * 2 + 60)) {
                // 显示在屏幕3/4高度
                posY = deviceHeight * 3 / 4;
            }
            popupWindow.showAtLocation(msgView, Gravity.NO_GRAVITY, posX, posY);
            int arrX = mWidth / 2 - dp2px(12 + 4);
            arrowView.setTranslationX(arrX);
        } else {
            rv_content.setLayoutManager(new GridLayoutManager(context, size, GridLayoutManager.VERTICAL, false));
            // x轴 （屏幕 - mWidth）/ 2
            int posX = centerWidth - mWidth / 2;
            // 右侧的最大宽度
            int max = centerWidth + mWidth / 2;
            if (posX < 0) {
                posX = 0;
            }
            // 右侧最大宽度 > 屏幕宽度  就取宽度 - （左右padding12dp + 左右margin12dp + 每个item52dp * size个
            else if (max > deviceWidth) {
                posX = deviceWidth - mWidth;
            }
            // topUI ?
            // msgView的y轴 - popupWindow的高度
            // ：msgView的y轴 + msgView高度 + 8dp间距
            int posY = topUI ? location[1] - mHeight : location[1] + msgViewHeight + dp2px(8);
            if (!topUI// 反向的ui
                    // 底部已经超过了 屏幕高度 - （弹窗高度 + 输入框）
                    && location[1] + msgView.getHeight() > deviceHeight - dp2px(52 * 2 + 60)) {
                // 显示在屏幕3/4高度
                posY = deviceHeight * 3 / 4;
            }
            popupWindow.showAtLocation(msgView, Gravity.NO_GRAVITY, posX, posY);
            //         view中心坐标 - pop坐标 - 16dp padding
            int arrX = centerWidth - posX - dp2px(16);
            arrowView.setTranslationX(arrX);
        }
    }

    // 隐藏 弹窗
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleSelector(SelectTextEvent event) {
        // 隐藏操作弹窗
        if ("dismissOperatePop".equals(event.getType())) {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        popupWindow.dismiss();
        SelectTextEventBus.getDefault().unregister(this);
    }

    private static int dp2px(int num) {
        return (int) (num * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

}