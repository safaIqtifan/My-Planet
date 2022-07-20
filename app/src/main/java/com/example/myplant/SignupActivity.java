package com.example.myplant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myplant.Model.UserModel;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.ActivitySignupBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    FirebaseFirestore fireStoreDB;
    private FirebaseAuth fAuth;

    String genderResulteStr = "female";
    String nameStr = "";
    String emailStr = "";
    String passwordStr = "";
    UserModel userModel;

    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;
    private static final String TAG = "GOOGLE_SIGN_IN_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_signup);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fireStoreDB = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
//        firebaseAuth = FirebaseAuth.getInstance();
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

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);


        binding.gmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                firebaseAuthWithGoogleAccount(account);
            }catch (Exception e){
                Log.e(TAG, "" + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogleAccount(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        fAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.e(TAG, "onSuccess: Logged In");
                        FirebaseUser firebaseUser = fAuth.getCurrentUser();
                        String uid = firebaseUser.getUid();
                        String email = firebaseUser.getEmail();

                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                        if (acct != null) {
                            userModel.fullName = acct.getDisplayName();
                            String personGivenName = acct.getGivenName();
                            String personFamilyName = acct.getFamilyName();
                            userModel.email = acct.getEmail();
                            userModel.user_id = acct.getId();
                            userModel.userImage = String.valueOf(acct.getPhotoUrl());

//                            Toast.makeText(SignupActivity.this, "Name of the user :" + personName + " user id is : " + personId, Toast.LENGTH_SHORT).show();

                        }

                        if (authResult.getAdditionalUserInfo().isNewUser()){
                            Toast.makeText(SignupActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(SignupActivity.this, "Existing user", Toast.LENGTH_SHORT).show();
                        }

//                        startActivity(new Intent(SignupActivity.this, ChooseMyPlantActivity.class));
                        sendToFireBase();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
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
        userModel.password = passwordStr;
        userModel.email = emailStr;
        userModel.gender = genderResulteStr;

        firebaseAuth();

    }

    private void firebaseAuth() {

        fAuth.createUserWithEmailAndPassword(emailStr, passwordStr).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                binding.progressBar.setVisibility(View.VISIBLE);
                sendToFireBase();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendToFireBase() {

        FirebaseUser firebaseUser = fAuth.getCurrentUser();
        assert firebaseUser != null;
        String userid = firebaseUser.getUid();

        userModel.user_id = userid;
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user_id", userModel.user_id);
        userMap.put("fullName", userModel.fullName);
        userMap.put("password", userModel.password);
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


}