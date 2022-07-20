package com.example.myplant;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myplant.Adapter.NotificationAdapter;
import com.example.myplant.Model.NotificationModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.ActivityNotificationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    ActivityNotificationBinding binding;
    FirebaseFirestore fireStoreDB;
    ArrayList<NotificationModel> notificationModelArrayList;
    NotificationModel notificationModel;
    UserModel userModel = UtilityApp.getUserData();
    NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_edit_my_profile);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.include.title.setText("Notification");
        fireStoreDB = FirebaseFirestore.getInstance();
        notificationModelArrayList = new ArrayList<>();
        notificationModel = new NotificationModel();

        binding.switch1.setOnClickListener(view -> {
            if (binding.switch1.isChecked()) {
                FirebaseMessaging.getInstance().subscribeToTopic("android");
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("android");
            }
            UtilityApp.setIsNotificationEnabled(binding.switch1.isChecked());
        });

        binding.switch1.setChecked(UtilityApp.getNotificationEnabled());

        fetchNotificationData();

    }

    private void initNotificationAdapter() {

        notificationAdapter = new NotificationAdapter(NotificationActivity.this, notificationModelArrayList);
        binding.rvNotification.setAdapter(notificationAdapter);
    }

    public void fetchNotificationData() {

        fireStoreDB.collection(Constants.NOTIFICATION)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                binding.loadingLY.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    notificationModelArrayList.clear();

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        NotificationModel notificationModel = document.toObject(NotificationModel.class);

                        if (notificationModel.userId.equals(userModel.user_id)) {
                            notificationModelArrayList.add(notificationModel);
                        }

                    }
//                    adapter.myArray = addPlantModelList;
                    initNotificationAdapter();

                } else {
                    Toast.makeText(NotificationActivity.this, getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}