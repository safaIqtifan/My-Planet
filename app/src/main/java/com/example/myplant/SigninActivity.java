package com.example.myplant;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myplant.Model.UserModel;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.ActivitySignInBinding;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SigninActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    FirebaseAuth fAuth;
    FirebaseFirestore fireStoreDB;

    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;
//    private FirebaseAuth firebaseAuth;
    private static final String TAG = "GOOGLE_SIGN_IN_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sign_in);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fAuth = FirebaseAuth.getInstance();
        fireStoreDB = FirebaseFirestore.getInstance();

        binding.signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidData();
            }
        });

        binding.createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SigninActivity.this, SignupActivity.class));
                finish();
            }
        });

        binding.forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText resetMail = new EditText(v.getContext());
                androidx.appcompat.app.AlertDialog.Builder passWordResetDialog = new AlertDialog.Builder(v.getContext());
                passWordResetDialog.setTitle("Reset Password ?");
                passWordResetDialog.setMessage("Enter your E-mail to Reset Link ");
                passWordResetDialog.setView(resetMail);
                passWordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SigninActivity.this, "Reset Link Sent to Your Email .", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SigninActivity.this, "Error ! Reset Link is Not Sent ." + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passWordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                passWordResetDialog.create().show();
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
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(SigninActivity.this, NavigationActivity.class));
            finish();
        }
    }

    public void ValidData() {

        String emailStr = binding.emailEd.getText().toString();
        String passwordStr = binding.passwordEd.getText().toString();

        if (emailStr.isEmpty()) {
            binding.emailEd.setError("Email is Missing");
            return;
        }
        if (passwordStr.isEmpty()) {
            binding.passwordEd.setError("Password is Missing");
            return;
        }

        LoginAth(emailStr, passwordStr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                getData();
            }catch (Exception e){
                Log.e(TAG, "" + e.getMessage());
            }
        }
    }

//    private void firebaseAuthWithGoogleAccount(GoogleSignInAccount account) {
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
//        fAuth.signInWithCredential(credential)
//                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                    @Override
//                    public void onSuccess(AuthResult authResult) {
//                        Log.e(TAG, "onSuccess: Logged In");
//                        FirebaseUser firebaseUser = fAuth.getCurrentUser();
//                        String uid = firebaseUser.getUid();
//                        String email = firebaseUser.getEmail();
//
//                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
//                        if (acct != null) {
//                            userModel.fullName = acct.getDisplayName();
//                            String personGivenName = acct.getGivenName();
//                            String personFamilyName = acct.getFamilyName();
//                            userModel.email = acct.getEmail();
//                            userModel.user_id = acct.getId();
//                            userModel.userImage = String.valueOf(acct.getPhotoUrl());
//
////                            Toast.makeText(SignupActivity.this, "Name of the user :" + personName + " user id is : " + personId, Toast.LENGTH_SHORT).show();
//
//                        }
//
//                        if (authResult.getAdditionalUserInfo().isNewUser()){
//                            Toast.makeText(SigninActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
//                        }else {
//                            Toast.makeText(SigninActivity.this, "Existing user", Toast.LENGTH_SHORT).show();
//                        }
//
////                        startActivity(new Intent(SignupActivity.this, ChooseMyPlantActivity.class));
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e(TAG, "onFailure: " + e.getMessage());
//                    }
//                });
//
//    }

    public void LoginAth(String email, String password) {

        binding.progressBar.setVisibility(View.VISIBLE);
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            getData();
                        } else {
                            Toast.makeText(SigninActivity.this, "fail_to_login", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void getData() {

        FirebaseUser firebaseUser = fAuth.getCurrentUser();
        assert firebaseUser != null;
        String userid = firebaseUser.getUid();

        fireStoreDB.collection(Constants.USER).document(userid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            UserModel user = task.getResult().toObject(UserModel.class);
//                            for (DocumentSnapshot document : task.getResult().getDocuments()) {
//                                UserModel userModel = document.toObject(UserModel.class);
//                                if ((userModel.user_id).equals(fAuth.getUid())) {
                                    UtilityApp.setUserData(user);
                            startActivity(new Intent(SigninActivity.this, NavigationActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
//                                }
//                        UserModel userDataModel = UtilityApp.getUserData();
//                                Toast.makeText(SigninActivity.this, user.fullName, Toast.LENGTH_SHORT).show();
//                            }
                        } else {
                            Toast.makeText(SigninActivity.this, getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}