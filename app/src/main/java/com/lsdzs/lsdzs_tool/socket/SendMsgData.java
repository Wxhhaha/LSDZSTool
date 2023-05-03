package com.lsdzs.lsdzs_tool.socket;

public class SendMsgData {
    private int requestType;
    private String token;
    private MsgEntity msgEntity;

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MsgEntity getMsgEntity() {
        return msgEntity;
    }

    public void setMsgEntity(MsgEntity msgEntity) {
        this.msgEntity = msgEntity;
    }
}
