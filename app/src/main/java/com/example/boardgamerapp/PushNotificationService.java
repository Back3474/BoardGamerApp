package com.example.boardgamerapp;

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

public class PushNotificationService extends FirebaseMessagingService {
    String user = "USER";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String id = message.getData().get("msgId").toString();
        String titleRaw = message.getData().get("title").toString();
        String title = getString(getResources().getIdentifier(titleRaw, "string", getPackageName()));
        String textRaw = message.getData().get("body").toString();
        String text = getString(getResources().getIdentifier(textRaw, "string", getPackageName()));

        if(message.getData().containsKey("uid")){
            String uid = message.getData().get("uid").toString();
            DatabaseReference ref = FirebaseDatabase
                    .getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com")
                    .getReference("users/" + uid);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    user = snapshot.child("firstname").getValue().toString() + " " + snapshot.child("lastname").getValue().toString();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            text = user + " " + text;
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

        if(id.equals("next_meeting")){
            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            notification.setContentIntent(resultPendingIntent);
        } else if(id.equals("rating")){
            Intent resultIntent = new Intent(getApplicationContext(), RatingActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            notification.setContentIntent(resultPendingIntent);
        }

        NotificationManagerCompat.from(this).notify(1, notification.build());
    }

}
