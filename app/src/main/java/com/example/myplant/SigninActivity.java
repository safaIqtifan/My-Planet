package com.example.myplant;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myplant.databinding.ActivitySignInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SigninActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sign_in);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fAuth = FirebaseAuth.getInstance();

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

    public void LoginAth(String email, String password) {

        binding.progressBar.setVisibility(View.VISIBLE);
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            binding.progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(SigninActivity.this, ChooseMyPlantActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();

                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(SigninActivity.this, "fail_to_login", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}