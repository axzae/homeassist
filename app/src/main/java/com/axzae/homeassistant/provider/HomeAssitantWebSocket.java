package com.axzae.homeassistant.provider;

import android.util.Log;

import com.axzae.homeassistant.model.ResultResponse;
import com.axzae.homeassistant.util.CommonUtil;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONObject;

public class HomeAssitantWebSocket {
    public static int identifier = 1;

    public static int getNextIdentifier() {
        return ++identifier;
    }

    public static Future<WebSocket> getInstance() {
        final String uri = "ws://192.168.2.50:8123/api/websocket";
        return AsyncHttpClient.getDefaultInstance().websocket(uri, null, new AsyncHttpClient.WebSocketConnectCallback() {

            @Override
            public void onCompleted(Exception ex, final WebSocket webSocket) {

                if (ex != null) {
                    Log.d("YouQi", ex.toString());
                    ex.printStackTrace();
                    throw new RuntimeException("Conn Failed" + ex.getMessage());
                }

                //mWebSocket = webSocket;
//
//                try {
//                    //PERFORM LOGIN
//                    webSocket.send(new JSONObject()
//                            .put("type", "auth")
//                            .put("api_password", "youqi123")
//                            .toString());
//
//                } catch (Exception e) {
//                    Log.d("YouQi", e.toString());
//                    e.printStackTrace();
//                }

//                webSocket.setStringCallback(new WebSocket.StringCallback() {
//                    @Override
//                    public void onStringAvailable(String s) {
//                        processResponse(s, webSocket);
//                    }
//                });
                webSocket.setDataCallback(new DataCallback() {
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList byteBufferList) {
                        CommonUtil.logLargeString("YouQi", "RECEIVED BYTE!");
                        // note that this data has been read
                        byteBufferList.recycle();
                    }
                });
            }
        });
    }


}