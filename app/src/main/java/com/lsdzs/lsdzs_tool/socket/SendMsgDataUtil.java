package com.lsdzs.lsdzs_tool.socket;

public class SendMsgDataUtil {
    public static SendMsgData createJsonData(int msgType, String token, String bizCode, String content) {
        SendMsgData data = new SendMsgData();
        data.setRequestType(msgType);
        data.setToken(token);
        MsgEntity msgEntity = defaultEntity();
        msgEntity.setBizCode(bizCode);
        msgEntity.setContent(content);
        data.setMsgEntity(msgEntity);
        return data;
    }

    public static SendMsgData createJsonData(int requestType,int msgType, String token, String bizCode, Object content) {
        SendMsgData data = new SendMsgData();
        data.setRequestType(requestType);
        data.setToken(token);
        MsgEntity msgEntity = defaultEntity();
        msgEntity.setBizCode(bizCode);
        msgEntity.setContent(content);
        msgEntity.setMsgType(msgType);
        data.setMsgEntity(msgEntity);
        return data;
    }

    private static MsgEntity defaultEntity() {
        return new MsgEntity("", "", "", "", "", "");
    }
}
