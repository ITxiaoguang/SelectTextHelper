package com.xiaoguang.selecttextview

/**
 * hxg 2021/9/13 16:28 qq:929842234
 */
class MsgBean(// 消息类型
    val type: Int, // 判断消息方向，是否是接收到的消息
    val isReceive: Boolean, // 内容
    val content: String?
)