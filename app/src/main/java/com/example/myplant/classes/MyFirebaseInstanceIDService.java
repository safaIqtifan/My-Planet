package com.example.myplant.classes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.myplant.Model.NotificationModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.NotificationActivity;
import com.example.myplant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {

    FirebaseFirestore fireStoreDB;
    UserModel userModelData;
//    NotificationModel notificationModel;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        fireStoreDB = FirebaseFirestore.getInstance();
        userModelData = UtilityApp.getUserData();

        Log.i("FCMService", "Log data " + message.getData());
        if (message.getNotification() != null)
            Log.i("FCMService", "Log notifcation " + message.getNotification().getBody());

        String title = message.getData().get("title");
        String messageBody = message.getData().get("body");

        if (title == null || title.isEmpty())
            title = message.getNotification().getTitle();
        if (messageBody == null || messageBody.isEmpty())
            messageBody = message.getNotification().getBody();
//        String  messageIcon = message.getNotification().getIcon();


        sendNotification(title, messageBody, NotificationActivity.class);

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.NotificationTitle = title;
        notificationModel.NotificationBody = messageBody;
//        notificationModel.NotificationIcon = messageIcon;
        sendPlantToFirebase(notificationModel);
    }

    public void sendNotification(String title, String body, Class<?> cls) {
        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = (PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            flags = PendingIntent.FLAG_ONE_SHOT;
        }
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                flags);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "App Notifications";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "App", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("App Notifications Service");
            notificationChannel.enableLights(true);
//            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(false);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            notificationChannel.setSound(defaultSoundUri, audioAttributes);
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_logo)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(title)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentText(body);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());

    }

    private void sendPlantToFirebase(NotificationModel notificationModel) {

        String notificationId = fireStoreDB.collection(Constants.NOTIFICATION).document().getId(); // this is auto genrat

        Map<String, Object> notificationModelMap = new HashMap<>();
        notificationModelMap.put("plantTypeId", notificationId);
        notificationModelMap.put("userId", userModelData.user_id);
        notificationModelMap.put("NotificationTitle", notificationModel.NotificationTitle);
        notificationModelMap.put("NotificationBody", notificationModel.NotificationBody);
//        notificationModelMap.put("NotificationIcon", notificationModel.NotificationIcon);

        fireStoreDB.collection(Constants.NOTIFICATION).document().set(notificationModelMap, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
//                            Toast.makeText(getApplicationContext(), getString(R.string.success_add_notification), Toast.LENGTH_SHORT).show();

                        } else {
//                            Toast.makeText(getApplicationContext(), getString(R.string.fail_add_notification), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
