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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myplant.Model.AddPlantModel;
import com.example.myplant.Model.NotificationModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.NotificationActivity;
import com.example.myplant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WorkManagerRequest extends Worker {

    //        MyFirebaseInstanceIDService myFirebaseMessagingService = new MyFirebaseInstanceIDService();
    Context context;
    AddPlantModel addPlantModel;
    FirebaseFirestore fireStoreDB = FirebaseFirestore.getInstance();
    UserModel userModel = UtilityApp.getUserData();
    NotificationModel notificationModel = new NotificationModel();

    public WorkManagerRequest(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        System.out.println("Log doWork check Plants watering");
        fetchData();
        return Result.success();
    }

    public void fetchData() {

        fireStoreDB.collection(Constants.PLANT)
                .whereEqualTo("userId", userModel.user_id)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR, 9);
                calendar.set(Calendar.MINUTE, 0);

                if (task.isSuccessful()) {

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        addPlantModel = document.toObject(AddPlantModel.class);

                        if (addPlantModel == null)
                            return;

                        System.out.println("Log addPlantModel.name " + addPlantModel.plantName + " time " + addPlantModel.choosenPlantCurrentTime);
                        if (calendar.getTime().getTime() >= addPlantModel.choosenPlantCurrentTime.getTime()) {
                            sendNotification(addPlantModel.plantName, "Your Plant Needs watering",
                                    NotificationActivity.class);

                            OneTimeWorkRequest workRequest =
                                    new OneTimeWorkRequest.Builder(WorkManagerRequest.class)
                                            .setInitialDelay(15, TimeUnit.SECONDS)
                                            .build();
                            WorkManager workManager = WorkManager.getInstance(context);
                            workManager.enqueueUniqueWork(Constants.CHECK_PLANTS_WORK_TAG,
                                    ExistingWorkPolicy.KEEP, workRequest);

                            notificationModel.NotificationTitle = addPlantModel.plantName;
                            notificationModel.NotificationBody = "Your Plant Needs watering";
                            sendPlantToFirebase(notificationModel);
                            //here call set new watering date
                            addPlantModel.choosenPlantCurrentTime = DateUtil.GetDateWithAddNextDays(addPlantModel.plantWateringDayes);
                            setPlantNewWateringDate(addPlantModel.plant_id);
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "fail get data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setPlantNewWateringDate(String plantId){

            Map<String, Object> postModelMap = new HashMap<>();
            postModelMap.put("plant_id", plantId);
            postModelMap.put("choosenPlantCurrentTime", addPlantModel.choosenPlantCurrentTime);

            fireStoreDB.collection(Constants.PLANT).document(plantId).set(postModelMap, SetOptions.merge())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "success add notification", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed data notification", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
//        postModelMap.put("description", postModel.description);


    }

    public void sendNotification(String title, String body, Class<?> cls) {

        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = (PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            flags = PendingIntent.FLAG_ONE_SHOT;
        }
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                flags);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

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
        notificationModelMap.put("notificationId", notificationId);
        notificationModelMap.put("userId", userModel.user_id);
        notificationModelMap.put("NotificationTitle", notificationModel.NotificationTitle);
        notificationModelMap.put("NotificationBody", notificationModel.NotificationBody);
        notificationModelMap.put("NotificationIcon", addPlantModel.plantPhoto);

        fireStoreDB.collection(Constants.NOTIFICATION).document().set(notificationModelMap, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "success add notification", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getApplicationContext(), "Failed data notification", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
