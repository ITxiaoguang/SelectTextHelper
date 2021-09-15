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
SelectTextHelper mSelectableTextHelper = new SelectTextHelper
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
 */
@Override
public void onClickUrl(String url) {
      
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

```

如果使用 AndroidX 先在 gradle.properties 中添加，两行都不能少噢~

```
android.useAndroidX=true
android.enableJetifier=true

```
