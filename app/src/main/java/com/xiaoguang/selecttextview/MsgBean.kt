package com.xiaoguang.selecttextview;


/**
 * hxg 2021/9/13 16:28 qq:929842234
 */
public class MsgBean {
    // 消息类型
    private int type;
    // 判断消息方向，是否是接收到的消息
    private boolean isReceive;
    // 内容
    private String content;

    public MsgBean(int type, boolean isReceive, String content) {
        this.type = type;
        this.isReceive = isReceive;
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public boolean isReceive() {
        return isReceive;
    }

    public String getContent() {
        return content;
    }
}
