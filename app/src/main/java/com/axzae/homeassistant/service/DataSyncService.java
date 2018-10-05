package com.axzae.homeassistant.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.EventResponse;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.model.rest.RxPayload;
import com.axzae.homeassistant.provider.HomeAssitantWebSocket;
import com.axzae.homeassistant.provider.ServiceProvider;
import com.axzae.homeassistant.shared.DataSyncInterface;
import com.axzae.homeassistant.util.CommonUtil;
import com.crashlytics.android.Crashlytics;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class DataSyncService extends Service {
    private static final int NOTIFICATION_ID = 6882;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private int nextNum = 100;
    private Subject<RxPayload> mEventEmitter = PublishSubject.create();
    private WebSocket mWebSocket;
    private static DataSyncService mInstance;
    private String mPassword;
    private String mBaseUrl;
    private HashMap<String, Entity> mEntities = new HashMap<>();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public DataSyncService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DataSyncService.this;
        }

        public Subject<RxPayload> getEventSubject() {
            return mEventEmitter;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        output("binding");
        //startWebSocket();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        output("unbinding");
        //https://github.com/Nilhcem/android-websocket-example/blob/master/websockets-example/src/main/java/com/nilhcem/websockets/WebSocketsService.java
        stopWebSocket();
        return super.onUnbind(intent);
    }

    public boolean isWebSocketRunning() {
        return mWebSocket != null;
    }

    public void startWebSocket(HomeAssistantServer server) {
        startWebSocket(server, false);
    }

    public void startWebSocket(HomeAssistantServer server, boolean isSilence) {

        mPassword = server.getPassword();
        mBaseUrl = server.getWebsocketUrl();
        output("mBaseUrl: " + mBaseUrl);
        if (mWebSocket == null) {
            output("startWebSocket: " + mBaseUrl);
            Request request = new Request.Builder().url(mBaseUrl).build();
            EchoWebSocketListener listener = new EchoWebSocketListener();
            OkHttpClient client = ServiceProvider.getWebSocketOkHttpClientInstance();
            mWebSocket = client.newWebSocket(request, listener);
        } else {
            if (!isSilence) {
                Toast.makeText(getApplicationContext(), R.string.toast_websocket_running, Toast.LENGTH_SHORT).show();
            }
        }
        //client.dispatcher().executorService().shutdown();
    }

    public void showNotification() {
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("DoNothing"), 0);

        NotificationManager mNotifyManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        PendingIntent pendingCancelDownloadIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("TERMINATE_CONNECTION"), 0);

        mBuilder.setSmallIcon(R.drawable.ic_notification_24dp)
                .setContentTitle(getString(R.string.notification_websocket_title))
                .setContentText(mBaseUrl)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(0)
                .addAction(R.drawable.ic_clear_white_24dp, getString(R.string.action_disconnect), pendingCancelDownloadIntent);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) notificationManager.cancel(NOTIFICATION_ID);

    }

    public void stopWebSocket() {
        if (mWebSocket != null) mWebSocket.cancel();
        clearNotification();
        mWebSocket = null;
    }

    /**
     * method for clients
     */
    @SuppressLint("StaticFieldLeak")
    public void getRandomNumber(final DataSyncInterface dataSyncInterface) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dataSyncInterface.onResponse(++nextNum);
            }
        }.execute();
    }

    //https://medium.com/@ssaurel/learn-to-use-websockets-on-android-with-okhttp-ba5f00aea988
    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        public void onOpen(WebSocket webSocket, Response response) {
            showNotification();
            Crashlytics.log("websocket open");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output("Receiving : " + text);
            processResponse(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            output("Closing : " + code + " / " + reason);
            Crashlytics.log("websocket onClosing");
            stopWebSocket();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            output("Error : " + t.getMessage());
            Crashlytics.log("websocket failure");
            stopWebSocket();
        }
    }

    private void output(String s) {
        Log.d("Websocket", s);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        output("onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        output("onDestroy");
        if (mWebSocket != null) mWebSocket.cancel();
        mEventEmitter.onComplete();
    }

    public void sendCommand(String command) {
        Log.d("YouQi", "command: " + command);
        mWebSocket.send(command);
    }

    public void callService(final String domain, final String service, CallServiceRequest serviceRequest) {
        try {
            String command = new JSONObject()
                    .put("type", "call_service")
                    .put("domain", domain)
                    .put("service", service)
                    .put("service_data", new JSONObject(CommonUtil.deflate(serviceRequest)))
                    .put("id", ++nextNum)
                    .toString();
            sendCommand(command);
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    public void processResponse(String s) {
        //CommonUtil.logLargeString("YouQi", "RECEIVEDSTRING !" + s);
        try {
            final JSONObject response = new JSONObject(s);
            final String type = response.getString("type");
            //CommonUtil.logLargeString("YouQi", "type: " + type);

            switch (type) {
                case "auth_required":
                    sendCommand(new JSONObject()
                            .put("type", "auth")
                            .put("access_token", mPassword)
                            .toString());
                    break;
                case "auth_ok":
                    sendCommand(new JSONObject()
                            .put("id", HomeAssitantWebSocket.getNextIdentifier())
                            .put("type", "subscribe_events")
                            .put("event_type", "state_changed")
                            .toString());

                    sendCommand(new JSONObject()
                            .put("id", HomeAssitantWebSocket.getNextIdentifier())
                            .put("type", "get_states")
                            .toString());
                    break;
                case "event":
                    EventResponse eventResponse = CommonUtil.inflate(s, EventResponse.class);
                    if (eventResponse != null) {

                        final Entity newEntity = eventResponse.event.data.newState;
                        final String entityId = newEntity.entityId;
                        final Entity oldEntity = mEntities.get(entityId);

                        if (oldEntity == null || !oldEntity.lastChanged.equals(newEntity.lastChanged)) {
                            RxPayload payload = RxPayload.getInstance("UPDATE");
                            payload.entity = newEntity;
                            mEventEmitter.onNext(payload);

                            getContentResolver().update(Uri.parse("content://com.axzae.homeassistant.provider.EntityContentProvider/"), newEntity.getContentValues(), "ENTITY_ID='" + newEntity.entityId + "'", null);
                        }
                        mEntities.put(entityId, eventResponse.event.data.newState);
                    }
                    //CommonUtil.logLargeString("YouQi", "Deflate: " + CommonUtil.deflate(eventResponse));
                    break;
                case "result":
                    //ResultResponse resultResponse = CommonUtil.inflate(s, ResultResponse.class);
                    break;
                default:
                    CommonUtil.logLargeString("YouQi", "Not Mapped: " + s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class TerminateConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("YouQi", "Received Cancelled Event");
            if (mInstance != null) mInstance.stopWebSocket();
        }
    }
}