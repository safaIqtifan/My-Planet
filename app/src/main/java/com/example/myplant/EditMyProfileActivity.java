package com.example.myplant;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myplant.Model.UserModel;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.ActivityEditMyProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditMyProfileActivity extends AppCompatActivity {

    ActivityEditMyProfileBinding binding;
    FirebaseFirestore fireStoreDB;
    UserModel userImagObj;
    Uri profilePhotoUri;
    StorageReference storageRef;
    String genderResulteStr = "";

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_edit_my_profile);
        binding = ActivityEditMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.include.title.setText("Edit my profile");
        fireStoreDB = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        userImagObj = UtilityApp.getUserData();

        binding.fullNameEd.setText(userImagObj.fullName);
        binding.emailEd.setText(userImagObj.email);
        binding.genderResultEd.setText(userImagObj.gender);
        genderResulteStr = userImagObj.gender;
        Glide.with(this)
                .asBitmap()
                .load(userImagObj.userImage)
                .placeholder(R.drawable.profile)
                .into(binding.profileImag);

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

        binding.passwordTv.setOnClickListener(view -> {
            showDialog();
        });

        binding.profileImag.setOnClickListener(view -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {
                    String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permission, PERMISSION_CODE);
                } else {
                    pickImageFromGallery();
                }
            } else {
                pickImageFromGallery();
            }
        });

        binding.saveBtn.setOnClickListener(view -> {

            userImagObj.fullName = binding.fullNameEd.getText().toString();
            userImagObj.email = binding.emailEd.getText().toString();
            userImagObj.gender = binding.genderResultEd.getText().toString();
            sendImageToFirebase();
        });

    }

    private void showDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_edit_profile);

        Button doneBtn = dialog.findViewById(R.id.editDoneButton);
        TextInputEditText newPasswordEd = dialog.findViewById(R.id.newPasswordEd);
        TextInputEditText confirmPasswordEd = dialog.findViewById(R.id.confirmPasswordEd);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPasswordStr = newPasswordEd.getText().toString();
                String confirmPasswordStr = confirmPasswordEd.getText().toString();

                if (newPasswordStr.equals(userImagObj.password)){
                    Toast.makeText(EditMyProfileActivity.this,
                            "password must be different form pervious passwords", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPasswordStr.equals(confirmPasswordStr)){
                    userImagObj.password = newPasswordStr;
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void pickImageFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            profilePhotoUri = data.getData();

            Glide.with(this)
                    .asBitmap()
                    .load(profilePhotoUri)
                    .placeholder(R.drawable.profile)
                    .into(binding.profileImag);
            uploadPhoto(profilePhotoUri);
        }
    }

    private void uploadPhoto(Uri photoUri) {

        binding.loadingLY.setVisibility(View.VISIBLE);

        StorageReference imgRef = storageRef.child("UsersImages" + "/" + UUID.randomUUID().toString());
        UploadTask uploadTask = imgRef.putFile(photoUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
                binding.loadingLY.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(taskSnapshot -> {
            imgRef.getDownloadUrl().addOnCompleteListener(task -> {
                userImagObj.userImage = task.getResult().toString();
                binding.loadingLY.setVisibility(View.GONE);
//                sendImageToFirebase();
            });
        });
    }

    private void sendImageToFirebase() {

        Map<String, Object> profileModelMap = new HashMap<>();
        profileModelMap.put("fullName", userImagObj.fullName);
        profileModelMap.put("email", userImagObj.email);
        profileModelMap.put("gender", userImagObj.gender);
        profileModelMap.put("userImage", userImagObj.userImage);
        profileModelMap.put("password", userImagObj.password);

        fireStoreDB.collection(Constants.USER).document(userImagObj.user_id)
                .update(profileModelMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            binding.loadingLY.setVisibility(View.GONE);
                            UtilityApp.setUserData(userImagObj);
                            Toast.makeText(EditMyProfileActivity.this, getString(R.string.success_add), Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(EditMyProfileActivity.this, getString(R.string.fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}