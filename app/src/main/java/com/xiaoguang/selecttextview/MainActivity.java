package com.xiaoguang.selecttextview;

import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // todo 一：展示在列表中自由复制、双击查看文本
        RecyclerView rv_msg = findViewById(R.id.rv_msg);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv_msg.setLayoutManager(linearLayoutManager);
        List<MsgBean> mList = new ArrayList<>();
        addList(mList);
        MsgAdapter adapter = new MsgAdapter(this, mList);
        rv_msg.setAdapter(adapter);

        // todo 二：展示查看文本
        // SelectTextDialog dialog = new SelectTextDialog(this, TEXT);
        // dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SelectTextEventBus.getDefault().unregister();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissAllPopDelayed"));
        }
        return super.dispatchTouchEvent(ev);
    }


    private void addList(List<MsgBean> mList) {
        //1：文本、2：图片、3：链接
        mList.add(new MsgBean(1, true, TEXT1));
        mList.add(new MsgBean(2, false, IMG1));
        mList.add(new MsgBean(3, true, LINK1));
        mList.add(new MsgBean(1, false, TEXT2));
        mList.add(new MsgBean(1, true, TEXT3));
        mList.add(new MsgBean(1, false, TEXT4));
        mList.add(new MsgBean(1, true, TEXT5));
        mList.add(new MsgBean(1, false, TEXT));
        mList.add(new MsgBean(1, true, TEXT6));
        mList.add(new MsgBean(1, false, TEXT7));
        mList.add(new MsgBean(1, true, TEXT8));
    }

    public static String TEXT1 = "www.baidu.com Android 12是Google研发的操作系统，于2021年5月19日凌晨发布。";
    public static String TEXT2 = "https://github.com/";
    public static String TEXT3 = ".";
    public static String TEXT4 = "Android 12是Google研发的操作系统，https://developer.android.google.cn/";
    public static String TEXT5 = "Android 12通过引入设计语言Material You，用户将能够通过自定义调色板和重新设计的小工具来完全个性化自己的手机。利用颜色提取，用户可以选择自己的壁纸，系统会自动确定适合用户的颜色设置。然后，可以将这些颜色应用于整个操作系统，包括通知栏、锁屏、音量控制、新的小工具等";
    public static String TEXT6 = "Beta 版 2 现已推出，其中包含最新功能和变更，供您在应用中试用。如需开始使用，请先获取 Beta 版并更新您的工具。Android 12 Beta 版现已面向用户和开发者推出，请务必测试您的应用是否兼容，并根据需要发布任何相关更新。欢迎试用新功能，并通过我们的问题跟踪器分享反馈。";
    public static String TEXT7 = "为了在 Android 12 上提供针对短时间运行的前台服务的流畅体验，系统可以为某些前台服务延迟 10 秒显示前台服务通知。此更改使某些短期任务在显示通知之前完成。";
    public static String TEXT8 = "提供更流畅的体验";

    public static String IMG1 = null;

    public static String LINK1 = "https://developer.android.google.cn/";
    public static String LINK2 = "www.baidu.com Android 12是Google研发的操作系统，于2021年5月19日凌晨发布。\n";
    public static String LINK3 = "Android 12是Google研发的操作系统，于2021年5月19日凌晨发布。\n";

    public static String TEXT = "www.baidu.com Android 12是Google研发的操作系统，于2021年5月19日凌晨发布。 [5] \n" +
            "Android 12优化了触发问题，双击背面手势可以截取屏幕截图、召唤谷歌Assistant、打开通知栏、控制媒体播放或打开最近的应用程序列表。 [1] \n" +
            "截至2021年9月9日，Android 12已更新至Beta5。" +
            "自定义个性化\n" +
            "Android 12\n" +
            "Android 12\n" +
            "Android 12通过引入设计语言Material You，用户将能够通过自定义调色板和重新设计的小工具来完全个性化自己的手机。利用颜色提取，用户可以选择自己的壁纸，系统会自动确定适合用户的颜色设置。然后，可以将这些颜色应用于整个操作系统，包括通知栏、锁屏、音量控制、新的小工具等。 [5] \n" +
            "简化互动\n" +
            "Android 12\n" +
            "Android 12\n" +
            "Android 12简化了互动，并重新设计了整个底层系统，包括将核心系统服务所需的CPU时间减少22%，并将系统服务器对大核心的使用减少15%。 [5] \n" +
            "Android 12\n" +
            "Android 12\n" +
            "通知栏\n" +
            "Android 12的通知栏可让用户快速看到应用通知、包括目前正在收听或观看的任何内容。还支持快速设置，让用户通过滑动和点击来控制几乎所有的操作系统。快速设置空间不仅仅是外观和感觉上的不同，Google支付和家庭控制在内都已经被重新设计，同时仍然允许定制。\n";

}