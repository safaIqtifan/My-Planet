package com.example.myplant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myplant.Model.UserModel;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    FirebaseFirestore fireStoreDB;
    private FirebaseAuth fAuth;

    String genderResulteStr = "";
    String nameStr = "";
    String emailStr = "";
    String passwordStr = "";
    UserModel userModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_signup);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fireStoreDB = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userModel = new UserModel();

        binding.female.setOnClickListener(view -> {
            genderResulteStr = "female";
            binding.genderResultEd.setText(genderResulteStr);
            binding.female.setBackgroundResource(R.drawable.female_btn);
            binding.male.setBackgroundResource(R.drawable.male_btn_border);
        });

        binding.male.setOnClickListener(view -> {
            genderResulteStr = "male";
            binding.genderResultEd.setText(genderResulteStr);
            binding.female.setBackgroundResource(R.drawable.female_btn_border);
            binding.male.setBackgroundResource(R.drawable.male_btn);
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SignupActivity.this, SigninActivity.class));
                finish();
            }
        });

        binding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nameStr = binding.fullNameEd.getText().toString();
                emailStr = binding.emailEd.getText().toString();
                passwordStr = binding.passwordEd.getText().toString();

                checkData();
            }
        });


    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        FirebaseUser user = fAuth.getCurrentUser();
//
//        if (user != null) {
//            Intent intent = new Intent(getApplicationContext(), ChooseMyPlantActivity.class);
//            startActivity(intent);
//            finish();
//        }
//
//    }

    private void checkData() {

        if (nameStr.isEmpty()) {
            binding.fullNameEd.setError("name is Requird");
            return;
        }
        if (emailStr.isEmpty()) {
            binding.emailEd.setError("Email is Requird");
            return;
        }

        if (passwordStr.isEmpty()) {
            binding.passwordEd.setError("password is Requird");
            return;
        }

        if (passwordStr.length() < 6) {
            binding.passwordEd.setError("password Must be 6 or more characters");
        }
        if (binding.termsConditionsCheck.isChecked()) {

        } else {
            binding.termsConditions.setError("Checke Terms and Conditions to continue");
            return;
        }

        userModel.fullName = nameStr;
        userModel.email = emailStr;
        userModel.gender = genderResulteStr;

        firebaseAuth();

    }

    private void firebaseAuth() {

        fAuth.createUserWithEmailAndPassword(emailStr, passwordStr).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                binding.progressBar.setVisibility(View.VISIBLE);

                FirebaseUser firebaseUser = fAuth.getCurrentUser();
                assert firebaseUser != null;
                String userid = firebaseUser.getUid();

                userModel.user_id = userid;
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("user_id", userModel.user_id);
                userMap.put("fullName", userModel.fullName);
                userMap.put("email", userModel.email);
                userMap.put("gender", userModel.gender);
                userMap.put("userImage", userModel.userImage);

                fireStoreDB.collection(Constants.USER).document(userid).set(userMap, SetOptions.merge())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    Toast.makeText(SignupActivity.this, "User created", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignupActivity.this, ChooseMyPlantActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    intent.putExtra("name", userMode.lusername);
                                    startActivity(intent);
                                    finish();
                                    UtilityApp.setUserData(userModel);


                                    binding.progressBar.setVisibility(View.GONE);
                                } else {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(SignupActivity.this, "fail_add_user", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}