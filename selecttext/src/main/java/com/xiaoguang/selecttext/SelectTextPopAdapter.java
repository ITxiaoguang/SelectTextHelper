package com.xiaoguang.selecttext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 弹窗 适配器
 * hxg 2020.9.13 qq:929842234
 */
public class SelectTextPopAdapter extends RecyclerView.Adapter<SelectTextPopAdapter.ViewHolder> {

    private Context mContext;

    private List<Pair<Integer, String>> mList;
    private boolean itemWrapContent;

    public void setItemWrapContent(boolean itemWrapContent) {
        this.itemWrapContent = itemWrapContent;
    }

    private onClickItemListener listener;

    public void setOnclickItemListener(onClickItemListener l) {
        listener = l;
    }

    public interface onClickItemListener {
        void onClick(int position);
    }

    public SelectTextPopAdapter(Context mContext, List<Pair<Integer, String>> list) {
        this.mContext = mContext;
        this.mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_select_text_pop, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int drawableId = mList.get(position).first;
        String text = mList.get(position).second;
        if (itemWrapContent) {
            ViewGroup.LayoutParams params = holder.tv_pop_func.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.tv_pop_func.setLayoutParams(params);
            holder.tv_pop_func.setPadding(SelectTextHelper.dp2px(8), 0, SelectTextHelper.dp2px(8), 0);
        }
        if (drawableId != 0) {
            holder.iv_pop_icon.setBackgroundResource(drawableId);
        } else {
            holder.iv_pop_icon.setBackground(null);
        }
        holder.tv_pop_func.setText(text);
        holder.ll_pop_item.setOnClickListener(v -> listener.onClick(position));
    }

    @Override
    public int getItemCount() {
        return null == mList ? 0 : mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout ll_pop_item;
        private ImageView iv_pop_icon;
        private TextView tv_pop_func;

        ViewHolder(View itemView) {
            super(itemView);
            ll_pop_item = itemView.findViewById(R.id.ll_pop_item);
            iv_pop_icon = itemView.findViewById(R.id.iv_pop_icon);
            tv_pop_func = itemView.findViewById(R.id.tv_pop_func);
        }
    }
}