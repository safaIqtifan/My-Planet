package com.example.myplant;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myplant.databinding.ActivityChooseMyPlantBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ChooseMyPlantActivity extends AppCompatActivity {

    ActivityChooseMyPlantBinding binding;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_choose_my_plant);
        binding = ActivityChooseMyPlantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.username.setText(String.valueOf(fAuth.getCurrentUser()));

    }
}