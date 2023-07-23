# SelectTextHelper-高仿微信聊天消息列表自由复制文字，双击查看文本内容

## [掘金地址](https://juejin.cn/post/7008080194116255752) [github地址](https://github.com/ITxiaoguang/SelectTextHelper)

`SelectTextHelper`打造一个全网最逼近微信聊天消息自由复制，双击查看文本内容框架。 支持图片和富文本选中，汇聚底层`TextView`框架、原理并加以整理得出的一个实用的`Helper`。
仅用几个类实现便实现如此强大的功能，用法也超级简单，侵入性极低。

[![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1ba9ba25273548878d044c665c51b122~tplv-k3u1fbpfcp-zoom-1.image)](https://jitpack.io/#ITxiaoguang/SelectTextHelper)

## 项目演示

|                                                                                   消息页效果                                                                                   |                                                                   查看内容效果                                                                  |
| :-----------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------------: |
| ![img\_v2\_c8287d37-8b53-43b0-abc9-6788d73f50dg.jpg](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1121f21620a74c13b8353cafcde0df19~tplv-k3u1fbpfcp-watermark.image?) | ![img_v2_0256892e-5ef8-4610-b73f-7ef9e4a9668g.jpg](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ddeab10820e54b9f84e0220a959b990c~tplv-k3u1fbpfcp-watermark.image?) |


|                                                                   消息页效果                                                                   |                                                                   查看内容效果                                                                  |
| :---------------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------------: |
| ![1631677218586.gif](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e6e8d9451a7b4ebd8a1a8292d0c38ded~tplv-k3u1fbpfcp-watermark.image?) | ![1631678150191.gif](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/85c0156baffe4e558a9fa8316fe3260d~tplv-k3u1fbpfcp-watermark.image?) |


|                                                                   消息页效果                                                                   |                                                                   查看内容效果                                                                  |
| :---------------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------------: |
| ![1631677218586.gif](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e6e8d9451a7b4ebd8a1a8292d0c38ded~tplv-k3u1fbpfcp-watermark.image?) | ![1631678150191.gif](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/85c0156baffe4e558a9fa8316fe3260d~tplv-k3u1fbpfcp-watermark.image?) |

|                                                                消息页全选                                                                |                                                              消息页自由复制放大镜                                                             |
| :---------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------: |
| ![demo\_1.jpg](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/99068250b93644d4921c3afe4ef39dbc~tplv-k3u1fbpfcp-watermark.image?) | ![demo\_2.jpg](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c800612998d4415e95c23a201f99c669~tplv-k3u1fbpfcp-watermark.image?) |

|                                                               消息页选中文本                                                               |                                                                 查看内容                                                                |
| :---------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------: |
| ![demo\_3.jpg](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b9567b55e9384dcebf394202a9ec849c~tplv-k3u1fbpfcp-watermark.image?) | ![demo\_4.jpg](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7cd289a09a2142d2a50232e717cbd179~tplv-k3u1fbpfcp-watermark.image?) |

## 特点功能:

*   支持自由选择文本
*   支持`富文本`选择
*   支持自定义文本有：游标颜色、游标大小、选中文本颜色
*   支持默认全选文字或选2个文字
*   支持滑动依然显示弹窗
*   支持放大镜功能
*   支持全选情况下自定义弹窗
*   支持操作弹窗：每行个数、图片、文字、监听回调、弹窗颜色、箭头图片

## Demo

![demo.jpg](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d52501acc3c44315aa5731772b007b24~tplv-k3u1fbpfcp-zoom-1.image)

## 如何添加

### Gradle添加：

#### 1.在Project的`build.gradle`中添加仓库地址

```gradle
allprojects {
  repositories {
     ...
     maven { url "https://jitpack.io" }
  }
}
```

#### 2.在Module目录下的`build.gradle`中添加依赖

[![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1ba9ba25273548878d044c665c51b122~tplv-k3u1fbpfcp-zoom-1.image)](https://jitpack.io/#ITxiaoguang/SelectTextHelper)

```gradle
dependencies {
       implementation 'com.github.ITxiaoguang:SelectTextHelper:1.1.0'
}
```

## 传送门

*   [仿照的例子](https://www.dazhuanlan.com/t0915/topics/1440960)
*   [放大镜](https://developer.android.google.cn/guide/topics/text/magnifier)
*   [TextView](https://developer.android.google.cn/reference/android/widget/TextView)
*   [富文本](https://github.com/zzhoujay/RichText)


### 主要实现

通过 [仿照的例子](https://www.dazhuanlan.com/t0915/topics/1440960) 并改进弹窗坐标位置、加对ImageSpan支持、大小加上`EventBus`实现

### 简单用例

#### 1.导入代

把该项目里的`selecttext Module`放入你的项目里面 或者 按照`Gradle`添加的步骤导入依赖。

#### 2.给你的`TextView`创建`Helper`和加监听

```kotlin
val mSelectableTextHelper = SelectTextHelper.Builder(textView) // 放你的textView到这里！！
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
    .addItem(0/*item的图标*/,"复制"/*item的描述*/, {Log.i("SelectTextHelper","复制")/*item的回调*/}// 操作弹窗的每个item
    .setPopSpanCount(5) // 设置操作弹窗每行个数 default 5
    .setPopStyle(
        R.drawable.shape_color_4c4c4c_radius_8 /*操作弹窗背*/,
        R.drawable.ic_arrow /*箭头图片*/
    ) // 设置操作弹窗背景色、箭头图片
    .build()

mSelectableTextHelper!!.setSelectListener(object : OnSelectListener {
    /**
     * 点击回调
     */
    override fun onClick(v: View?, originalContent: CharSequence?) {
        // 拿原始文本方式
        // clickTextView(msgBean.content!!) // 推荐
        // clickTextView(originalContent!!) // 不推荐 富文本可能被修改值 导致gif动不了
    }

    /**
     * 长按回调
     */
    override fun onLongClick(v: View?) {
    }

    /**
     * 选中文本回调
     */
    override fun onTextSelected(content: CharSequence?) {
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
    }

    /**
     * 全选显示自定义弹窗回调
     */
    override fun onSelectAllShowCustomPop() {
    }

    /**
     * 重置回调
     */
    override fun onReset() {
      // SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissOperatePop"))
    }

    /**
     * 解除自定义弹窗回调
     */
    override fun onDismissCustomPop() {
      // SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissOperatePop"))
    }

    /**
     * 是否正在滚动回调
     */
    override fun onScrolling() {
        // removeShowSelectView()
    }
})

```

#### 3.demo中提供了查看文本内容的`SelectTextDialog`和 消息列表自由复制`MainActivity`

查看文本内容方法：

*   该方法比较简单，将`textView`参照步骤2放入`SelectTextHelper`中，在`dismiss`调用`SelectTextHelper`的`reset()`即可。

```kotlin
override fun dismiss() {
    mSelectableTextHelper.reset()
    super.dismiss()
}
```

高仿微信聊天消息列表自由复制方法：

*   `recycleView` + `adapter` + 多布局的使用在这里不阐述，请看本项目demo。

*   为`adapter`里text类型`ViewHolder`中的`textView`参照步骤2放入`SelectTextHelper`中，注册`SelectTextEventBus`。

*   `SelectTextEventBus`类特别说明、原理：
    `SelectTextEventBus`在`EventBus`基础上加功能。在`register`时记录下类和方法，方便在`Activity/Fragment Destroy`时`unregister`所有`SelectTextEventBus`的`EventBus`。

*   text类型`ViewHolder` 添加`EventBus`监听

```kotlin
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
```

*   重写`adapter`里的`onViewRecycled`方法，该方法在回收`View`时调用

```kotlin
override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    super.onViewRecycled(holder)
    if (holder is ViewHolderText) {
        // 注销
        SelectTextEventBus.instance.unregister(holder)
    }
}
```

- 对ImageSpan表情支持(支持动态表情！！)（https://github.com/ITxiaoguang/SelectTextHelper/issues/4 ）

```kotlin
val emojiMap: MutableMap<String, Int> = HashMap()
emojiMap["\\[笑脸\\]"] = R.drawable.emoji_00
emojiMap["\\[瘪嘴\\]"] = R.drawable.emoji_01
emojiMap["\\[色\\]"] = R.drawable.emoji_02
emojiMap["\\[瞪大眼\\]"] = R.drawable.emoji_03
emojiMap["\\[酷\\]"] = R.drawable.emoji_04
emojiMap["\\[Android\\]"] = R.mipmap.ic_launcher_round
emojiMap["\\[好的\\]"] = R.drawable.emoji_gif
emojiMap["\\[羊驼\\]"] = R.drawable.emoji_gif2
````

- 富文本支持 [富文本用法点这里](https://github.com/zzhoujay/RichText)

```kotlin
// todo 方法一：富文本  需要转行成富文本形式
RichText.initCacheDir(holder.textView.context.applicationContext) // 项目里初始化一次即可
RichText.from(msgBean.content)
    .autoFix(false) // 是否自动修复宽高，默认true
    .autoPlay(true) // gif自动播放
    .singleLoad(false) // RecyclerView里设为false 若同时启动了多个RichText，会并发解析，类似于AsyncTask的executeOnExecutor
    .done { // 在成功回调处理
        // 演示消息列表选择文本
        holder.selectText(msgBean)
    }
    .into(holder.textView)

// todo 方法二：普通文本
holder.textView.text = msgBean.content
// 演示消息列表选择文本
holder.selectText(msgBean)
```
