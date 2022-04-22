package com.xiaoguang.selecttextview;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaoguang.selecttext.SelectTextHelper;


/**
 * 选择文字
 * hxg 2021/9/14 qq:929842234z
 */
public class SelectTextDialog extends Dialog {

    private SelectTextHelper mSelectableTextHelper;
    private String mText;
    private String selectText;

    public SelectTextDialog(Context context, String mText) {
        super(context, R.style.SelectTextFragment);
        this.mText = mText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.fragment_select_text);

        // 一定要在setContentView之后调用，否则无效
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        findViewById(R.id.rl_selector).setOnClickListener(v -> {
            if (mSelectableTextHelper.isPopShowing()) {
                mSelectableTextHelper.reset();
            } else {
                dismiss();
            }
        });
        TextView tv_msg_content = findViewById(R.id.tv_msg_content);
        tv_msg_content.setText(mText);
        if ((mText.length() > 0 && mText.length() > 16)
                || mText.contains("\n")) {
            tv_msg_content.setGravity(Gravity.START);
        } else {
            tv_msg_content.setGravity(Gravity.CENTER);
        }
        mSelectableTextHelper = new SelectTextHelper
                .Builder(tv_msg_content)
                .setCursorHandleColor(getContext().getResources().getColor(R.color.colorAccent))
                .setCursorHandleSizeInDp(24)
                .setSelectedColor(getContext().getResources().getColor(R.color.colorAccentTransparent))
                .setSelectAll(false)
                .addItem(R.drawable.ic_msg_copy, R.string.copy,
                        () -> copy(selectText))
                .addItem(R.drawable.ic_msg_select_all, R.string.select_all,
                        this::selectAll)
                .addItem(R.drawable.ic_msg_forward, R.string.forward,
                        () -> forward(selectText))
                .build();

        mSelectableTextHelper.setSelectListener(new SelectTextHelper.OnSelectListener() {
            @Override
            public void onClick(View v) {
            }

            @Override
            public void onLongClick(View v) {
            }

            @Override
            public void onTextSelected(CharSequence content) {
                selectText = content.toString();
            }

            @Override
            public void onDismiss() {
                dismiss();
            }

            @Override
            public void onClickUrl(String url) {
                toast("点击了：  " + url);
            }

            @Override
            public void onSelectAllShowCustomPop() {
            }

            @Override
            public void onReset() {
            }

            @Override
            public void onDismissCustomPop() {
            }

            @Override
            public void onScrolling() {
            }
        });
    }

    @Override
    public void dismiss() {
        mSelectableTextHelper.reset();
        super.dismiss();
    }

    /**
     * 复制
     */
    private void copy(String selectText) {
        SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissAllPop"));
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(selectText, selectText));
        }
        mSelectableTextHelper.reset();
        toast("已复制");
    }

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
    private void forward(String content) {
        SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissAllPop"));
        // todo 转发
        toast("转发");
    }

    private void toast(String msg) {
        Toast.makeText(getContext().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

}