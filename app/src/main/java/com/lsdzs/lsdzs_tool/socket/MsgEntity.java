package com.lsdzs.lsdzs_tool.socket;

public class MsgEntity{
    private String pushId;
    private String senderId;
    private String receiverId;
    private String msgId;
    private int msgType;
    private String msgTime;
    private String bizCode;
    private String targetType;
    private Object content;

    public MsgEntity(String pushId, String senderId, String receiverId, String msgId, String msgTime, String targetType) {
        this.pushId = pushId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.msgId = msgId;
        this.msgTime = msgTime;
        this.targetType = targetType;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}