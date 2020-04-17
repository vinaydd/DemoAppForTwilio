package com.hp.demoappfortwilio.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;

import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hp.demoappfortwilio.R;
import com.hp.demoappfortwilio.activity.VideoInviteActivity;
import com.hp.demoappfortwilio.model.Invite;

import java.util.Date;
import java.util.Map;

public class NotifyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "NotifyFCMService";

    /*
     * The Twilio Notify message data keys are as follows:
     *  "twi_title"  // The title of the message
     *  "twi_body"   // The body of the message
     *
     * You can find a more detailed description of all supported fields here:
     * https://www.twilio.com/docs/api/notifications/rest/notifications#generic-payload-parameters
     */
    private static final String NOTIFY_TITLE_KEY = "twi_title";
    private static final String NOTIFY_BODY_KEY = "twi_body";

    /*
     * The keys sent by the notify.api.model.Invite model class
     */
    private static final String NOTIFY_INVITE_FROM_IDENTITY_KEY = "fromIdentity";
    private static final String NOTIFY_INVITE_ROOM_NAME_KEY = "roomName";

    public static final String CHANNEL_ID = "com.fcm.ANDROID";
    private final String GROUP_KEY = "MaiAppKey";

    private final int NOTIFICATION_GROUP_ID = 99;

    /**
     * Called when a message is received.
     *
     * @param message The remote message, containing from, and message data as key/value pairs.
     */
    @Override
    public void onMessageReceived(RemoteMessage message) {
        /*
         * The Notify service adds the message body to the remote message data so that we can
         * show a simple notification.
         */

        Log.e("remotemessageservice","message");
        Map<String,String> messageData = message.getData();
        String title = messageData.get(NOTIFY_TITLE_KEY);
        String body = messageData.get(NOTIFY_BODY_KEY);
        Invite invite =
                new Invite(messageData.get(NOTIFY_INVITE_FROM_IDENTITY_KEY),
                        messageData.get(NOTIFY_INVITE_ROOM_NAME_KEY));

        Log.e(TAG, "From: " + invite.identity);
        Log.e(TAG, "Room Name: " + invite.roomName);
        Log.e(TAG, "Title: " + title);
        Log.e(TAG, "Body: " + body);
        Log.e("messageservice","message");

     //   showNotification("title","body","room");
     showNotification(title,body, "abctest");
      broadcastVideoNotification(title, invite.roomName);
    }

    /**
     * Create and show a simple notification containing the FCM message.
     */
    private void showNotification(String title, String body, String roomName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        PendingIntent pendingIntent;
        int notificationId = getNotificationId();

       // builder.setContentIntent(pendingIntent);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //   builder.setSmallIcon(R.drawable.ic_small_notifications);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentText(body);
        builder.setContentTitle(title);
        builder.setAutoCancel(true);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));
        builder.setGroup(GROUP_KEY);
        builder.setColor(ResourcesCompat.getColor(getApplicationContext().getResources(), R.color.colorAccent, null));
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setSound(defaultSoundUri);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 24) {
            builder.setGroupSummary(true);
            builder.setSmallIcon(R.drawable.ic_call_end_white_24px);
            notificationManager.notify(NOTIFICATION_GROUP_ID, builder.build());
        } else {
            builder.setSmallIcon(R.drawable.ic_call_end_white_24px);
            notificationManager.notify(getNotificationId(), builder.build());
        }

    }

    private int getNotificationId() {
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last4Str = tmpStr.substring(tmpStr.length() - 5);
        return Integer.valueOf(last4Str);
    }

    /*
     * Broadcast the Video Notification to the Activity
     */
    private void broadcastVideoNotification(String title, String roomName) {
        Log.e("video","video");
        Intent intent = new Intent(getApplicationContext(),VideoInviteActivity.class);
        intent.putExtra(VideoInviteActivity.VIDEO_NOTIFICATION_TITLE, title);
        intent.putExtra(VideoInviteActivity.VIDEO_NOTIFICATION_ROOM_NAME, roomName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
      //  LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
