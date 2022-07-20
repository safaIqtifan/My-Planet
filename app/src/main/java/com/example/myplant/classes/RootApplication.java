package com.example.myplant.classes;

import com.akexorcist.localizationactivity.ui.LocalizationApplication;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class RootApplication extends LocalizationApplication {

    private static RootApplication instance;
    private  SharedPManger sharedPManger;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        sharedPManger = new SharedPManger(instance);

        boolean isNotificationEnabled = UtilityApp.getNotificationEnabled();
        if (isNotificationEnabled) {
            FirebaseMessaging.getInstance().subscribeToTopic("android");
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("android");
        }

    }

    public static RootApplication getInstance() {
        return instance;
    }

    public  SharedPManger getSharedPManger() {
        return sharedPManger;
    }

    @NotNull
    @Override
    public Locale getDefaultLanguage() {
        return Locale.ENGLISH;
    }
}