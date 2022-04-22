package com.xiaoguang.selecttextview

import java.io.Serializable

/**
 * 选择文本的event
 * hxg 2021/08/31 15:26 qq:929842234
 */
class SelectTextEvent(
    // 关闭所有弹窗 dismissAllPop
    // 延迟关闭所有弹窗 dismissAllPopDelayed
    // 关闭操作弹窗 dismissOperatePop
    val type: String
) : Serializable