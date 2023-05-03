package com.lsdzs.lsdzs_tool.socket;

import com.blankj.utilcode.util.LogUtils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * 创建时间:2022/1/21 13:27
 * 作者:wxh
 */
public class JWebSocketClient extends WebSocketClient {

    public JWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LogUtils.e("opened 已开启连接");
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LogUtils.e("opened 已关闭连接");
    }

    @Override
    public void onError(Exception ex) {

    }
}
