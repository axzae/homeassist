package com.axzae.homeassistant.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.axzae.homeassistant.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class HaFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d("YouQi", "getFrom: " + remoteMessage.getFrom());
        Log.d("YouQi", "getTo: " + remoteMessage.getTo());
        Log.d("YouQi", "getNotification: " + remoteMessage.getNotification());
        Log.d("YouQi", "getNotification getBody: " + remoteMessage.getNotification().getBody());
        Log.d("YouQi", "getNotification getTitle: " + remoteMessage.getNotification().getTitle());
        Log.d("YouQi", "getSentTime: " + remoteMessage.getSentTime());
        Log.d("YouQi", "getMessageType: " + remoteMessage.getMessageType());
        //Log.d("YouQi", "Notification Message Body: " + remoteMessage.getNotification().getBody());
        //sendNotification(remoteMessage.getNotification().getBody());
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Log.d("YouQi", "sendNotification; " + messageBody);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                //.setSmallIcon(R.drawable.ic_home_assistant_24dp)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
