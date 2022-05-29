package com.example.myplant;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {

    final private static int splashTimeOut = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    startActivity(new Intent(SplashScreenActivity.this, OnboardingActivity.class));
                } else {
                    Intent i = new Intent(SplashScreenActivity.this, SigninActivity.class);
                    startActivity(i);
                }
                finish();
            }
        }, splashTimeOut);
    }
}