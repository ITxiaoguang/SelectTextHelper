# SelectTextHelper-高仿微信消息列表自由复制，双击查看文本内容

![MavenCentral](https://img.shields.io/badge/%20MavenCentral%20-1.0.0-5bc0de.svg)

SelectTextHelper打造一个全网最逼近微信聊天消息自由复制，双击查看文本内容框架。 汇聚底层TextView框架、原理并加以整理得出的一个实用的Helper。
仅用两个类实现便实现如此强大的功能，用法也超级简单。

## 特点功能:

- 支持自由选择文本
- 支持自定义文本有：游标颜色、游标大小、选中文本颜色
- 支持默认全选文字或选2个文字
- 支持滑动依然显示弹窗
- 支持放大镜功能
- 支持全选情况下自定义弹窗
- 支持操作弹窗：每行个数、图片、文字、监听回调、弹窗颜色、箭头图片
- 支持 AndroidX

## Demo

[下载 APK-Demo](https://github.com/ITxiaoguang/SelectTextHelper/看效果.apk)

## 传送门

- [仿照的例子](https://www.dazhuanlan.com/t0915/topics/1440960)
- [放大镜](https://developer.android.google.cn/guide/topics/text/magnifier)

#### 项目演示

|消息页全选|消息页自由复制|
|:---:|:---:|
|![](https://github.com/ITxiaoguang/SelectTextHelper/demo_1.jpg)|![](https://github.com/ITxiaoguang/SelectTextHelper/demo_2.jpg)|

|消息页选中文本|
|:---:|
|![](https://github.com/ITxiaoguang/SelectTextHelper/demo_3.jpg)|

上面这三个是消息页里传递消息逻辑通过EventBus实现，

|查看内容|
|:---:|
|![](https://github.com/ITxiaoguang/SelectTextHelper/demo_4.jpg)|


#### 主要实现

通过 [仿照的例子](https://www.dazhuanlan.com/t0915/topics/1440960) 并改进弹窗坐标位置、大小加上EventBus实现


## 简单用例

#### 1.导入代码
把该项目里的selecttext Module放入你的项目里面

#### 2.给你的 TextView 创建Helper和加监听

```java
SelectTextHelper mSelectableTextHelper=new SelectTextHelper
        .Builder(textView)// 放你的textView到这里！！
        .setCursorHandleColor(0xFF1379D6/*mContext.getResources().getColor(R.color.colorAccent)*/)// 游标颜色 default 0xFF1379D6
        .setCursorHandleSizeInDp(24)// 游标大小 单位dp default 24
        .setSelectedColor(0xFFAFE1F4/*mContext.getResources().getColor(R.color.colorAccentTransparent)*/)// 选中文本的颜色 default 0xFFAFE1F4
        .setSelectAll(true)// 初次选中是否全选 default true
        .setScrollShow(true)// 滚动时是否继续显示 default true
        .setSelectedAllNoPop(true)// 已经全选无弹窗，设置了true在监听会回调 onSelectAllShowCustomPop 方法 default false
        .setMagnifierShow(true)// 放大镜 default true
        .addItem(0/*item的图标*/,"复制"/*item的描述*/, // 操作弹窗的每个item
        ()->Log.i("SelectTextHelper","复制")/*item的回调*/)
        .build();

        mSelectableTextHelper.setSelectListener(new SelectTextHelper.OnSelectListener(){
/**
 * 点击回调
 */
@Override
public void onClick(View v){
        // clickTextView(textView.getText().toString().trim());
        }

/**
 * 长按回调
 */
@Override
public void onLongClick(View v){
        // postShowCustomPop(SHOW_DELAY);
        }

/**
 * 选中文本回调
 */
@Override
public void onTextSelected(CharSequence content){
        // selectedText = content.toString();
        }

/**
 * 弹窗关闭回调
 */
@Override
public void onDismiss(){
        }

/**
 * 点击TextView里的url回调
 */
@Override
public void onClickUrl(String url){
        }

/**
 * 全选显示自定义弹窗回调
 */
@Override
public void onSelectAllShowCustomPop(){
        // postShowCustomPop(SHOW_DELAY);
        }

/**
 * 重置回调
 */
@Override
public void onReset(){
        // SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissOperatePop"));
        }

/**
 * 解除自定义弹窗回调
 */
@Override
public void onDismissCustomPop(){
        // SelectTextEventBus.getDefault().dispatch(new SelectTextEvent("dismissOperatePop"));
        }

/**
 * 是否正在滚动回调
 */
@Override
public void onScrolling(){
        // removeShowSelectView();
        }
        });

```

#### 3.demo中提供了查看文本内容的SelectTextDialog 和 消息列表自由复制MainActivity,请自行参照。

查看文本内容使用方法： 该方法比较简单，将textView参照步骤2放入SelectTextHelper中，在dismiss调用SelectTextHelper的reset()即可。

```java
@Override
public void dismiss(){
        mSelectableTextHelper.reset();
        super.dismiss();
        }
```

高仿微信消息列表自由复制使用方法：

- recycleView + adapter + 多布局的使用在这里不阐述，请看demo。

- 为adapter里text类型ViewHolder中的textView参照步骤2放入SelectTextHelper中，注册SelectTextEventBus。

- SelectTextEventBus类特别说明、原理：
  SelectTextEventBus在register时记录下类和方法，方便在Activity/Fragment Destroy时unregister所有EventBus

- text类型ViewHolder 添加EventBus监听

```java
/**
 * 自定义SelectTextEvent 隐藏 光标
 */
@Subscribe(threadMode = ThreadMode.MAIN)
public void handleSelector(SelectTextEvent event){
        if(null==mSelectableTextHelper){
        return;
        }
        String type=event.getType();
        if(TextUtils.isEmpty(type)){
        return;
        }
        switch(type){
        case"dismissAllPop":
        mSelectableTextHelper.reset();
        break;
        case"dismissAllPopDelayed":
        postReset(RESET_DELAY);
        break;
        }
        }
```

- 重写adapter里的onViewRecycled方法，该方法在回收View时调用

```java
@Override
public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder){
        super.onViewRecycled(holder);
        if(holder instanceof ViewHolderText){
        // 注销
        SelectTextEventBus.getDefault().unregister(holder);
        }
        }
```

- 防抖

```java
/**
 * 延迟显示CustomPop
 * 防抖
 */
private void postShowCustomPop(int duration){
        textView.removeCallbacks(mShowCustomPopRunnable);
        textView.postDelayed(mShowCustomPopRunnable,duration);
        }

private final Runnable mShowCustomPopRunnable=
        ()->showCustomPop(text_rl_container,textMsgBean);

/**
 * 延迟重置
 * 为了支持滑动不重置
 */
private void postReset(int duration){
        textView.removeCallbacks(mShowSelectViewRunnable);
        textView.postDelayed(mShowSelectViewRunnable,duration);
        }

private void removeShowSelectView(){
        textView.removeCallbacks(mShowSelectViewRunnable);
        }

private final Runnable mShowSelectViewRunnable=
        ()->mSelectableTextHelper.reset();
```

如果使用 AndroidX 先在 gradle.properties 中添加，两行都不能少噢~

```
android.useAndroidX=true
android.enableJetifier=true

```
