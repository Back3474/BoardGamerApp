package com.example.boardgamerapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;

public class PushNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String msgId = message.getData().get("msgId").toString();
        String titleRaw = message.getData().get("title").toString();
        String title = getString(getResources().getIdentifier(titleRaw, "string", getPackageName()));
        String textRaw = message.getData().get("body").toString();
        String text = getString(getResources().getIdentifier(textRaw, "string", getPackageName()));

        if(message.getData().containsKey("uid")){
            String uid = message.getData().get("uid").toString();
            String uName = message.getData().get("uName").toString();
            text = uName + " " + text;
        }

        if(message.getData().containsKey("latetime")){
            String latetime = message.getData().get("latetime").toString();
            text = text + " " + latetime + " " + getText(R.string.late_late_participants_min);
        }

        final String CHANNEL_ID = "HEADS_UP_NOTIFICATION";
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Heads Up Notification",
                NotificationManager.IMPORTANCE_HIGH
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(Html.fromHtml("<b>"+title+"</b>", Html.FROM_HTML_MODE_COMPACT))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .setStyle(new Notification.BigTextStyle().bigText(text).setBigContentTitle(Html.fromHtml("<b>"+title+"</b>", Html.FROM_HTML_MODE_COMPACT)));

        if(msgId.equals("next_meeting")){
            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            notification.setContentIntent(resultPendingIntent);
        } else if(msgId.equals("rating")){
            Intent resultIntent = new Intent(getApplicationContext(), RatingActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            notification.setContentIntent(resultPendingIntent);
        }

        SharedPreferences prefs = getSharedPreferences(Activity.class.getSimpleName(), Context.MODE_PRIVATE);
        int notificationNumber = prefs.getInt("notificationNumber", 0);

        NotificationManagerCompat.from(this).notify(notificationNumber, notification.build());

        SharedPreferences.Editor editor = prefs.edit();
        notificationNumber++;
        editor.putInt("notificationNumber", notificationNumber);
        editor.commit();
    }


}
