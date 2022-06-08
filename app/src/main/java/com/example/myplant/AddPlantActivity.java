package com.example.myplant;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myplant.Model.AddPlantModel;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.ActivityAddPlantBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddPlantActivity extends AppCompatActivity {

    ActivityAddPlantBinding binding;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    Uri plantPhotoUri;
    StorageReference storageRef;
    AddPlantModel addPlantModel;
    FirebaseFirestore fireStoreDB;
    String plantTypeSpinnerStr = "";
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_plant);
        binding = ActivityAddPlantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storageRef = FirebaseStorage.getInstance().getReference();
        fireStoreDB = FirebaseFirestore.getInstance();
        addPlantModel = new AddPlantModel();

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkData();
            }
        });

        binding.plantPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);

                    } else {
                        pickImageFromGallery();
                    }
                } else {
                    pickImageFromGallery();
                }
            }
        });

        binding.plantTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0)
                    plantTypeSpinnerStr = binding.plantTypeSpinner.getSelectedItem().toString();
                else
                    plantTypeSpinnerStr = "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.plantAgeEd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month + 1;
                        Log.d("TAG", "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                        String date = year + "/" + month + "/" + day;
                        binding.plantAgeEd.setText(date);
                    }
                };
                getDateOfBirth();
            }
        });

    }

    private void pickImageFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void getDateOfBirth() {

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                AddPlantActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateSetListener,
                year, month, day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {

            plantPhotoUri = data.getData();
            binding.plantPhoto.setImageURI(plantPhotoUri);
        }
    }

    private void uploadPhoto(Uri photoUri) {

        StorageReference imgRef = storageRef.child("plantImages" + "/" + UUID.randomUUID().toString());

        binding.loadingLY.setVisibility(View.VISIBLE);
        UploadTask uploadTask = imgRef.putFile(photoUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
//                GlobalHelper.hideProgressDialog();
//                binding.loadingLY.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(taskSnapshot -> {

            imgRef.getDownloadUrl().addOnCompleteListener(task -> {

                addPlantModel.plantPhoto = task.getResult().toString();
                System.out.println("Log uploaded url " + addPlantModel.plantPhoto);
                sendPostToFirebase();
//                binding.loadingLY.setVisibility(View.GONE);
            });


        });
    }

    private void checkData() {

        String plantNameStr = binding.plantNameEd.getText().toString();
        String plantAgeStr = binding.plantAgeEd.getText().toString();
        String plantDescriptionStr = binding.plantDescriptionEd.getText().toString();

        boolean hasError = false;
        if (plantPhotoUri == null) {
            Toast.makeText(this, getString(R.string.please_add_photo), Toast.LENGTH_SHORT).show();
            hasError = true;
        }
        if (plantNameStr.isEmpty()) {
            binding.plantNameEd.setError(getString(R.string.invalid_input));
            hasError = true;
        }
        if (plantTypeSpinnerStr.isEmpty()) {
//            binding.plantTypeSpinner.setError(getString(R.string.invalid_input));
            System.out.println("Log housingTypeSpinnerStr hasError");
            hasError = true;
        }
        if (plantAgeStr.isEmpty()) {
            binding.plantAgeEd.setError(getString(R.string.invalid_input));
            hasError = true;
        }
        if (plantDescriptionStr.isEmpty()) {
            binding.plantDescriptionEd.setError(getString(R.string.invalid_input));
            hasError = true;
        }
        if (hasError)
            return;

        addPlantModel.plantName = plantNameStr;
        addPlantModel.plantAge = plantAgeStr;
        addPlantModel.plantType =plantTypeSpinnerStr;
        addPlantModel.plantDescription = plantDescriptionStr;

        uploadPhoto(plantPhotoUri);

    }


    private void sendPostToFirebase() {

        String plantId = fireStoreDB.collection(Constants.PLANT).document().getId(); // this is auto genrat

        Map<String, Object> postModelMap = new HashMap<>();
        postModelMap.put("plant_id", plantId);
        postModelMap.put("userId", UtilityApp.getUserData());
        postModelMap.put("plantName", addPlantModel.plantName);
        postModelMap.put("plantAge", addPlantModel.plantAge);
        postModelMap.put("plantType", addPlantModel.plantType);
        postModelMap.put("plantPhoto", addPlantModel.plantPhoto);
        postModelMap.put("plantDescription", addPlantModel.plantDescription);

        fireStoreDB.collection(Constants.PLANT).document(plantId).set(postModelMap, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(getApplicationContext(), getString(R.string.success_add_post), Toast.LENGTH_SHORT).show();
                            onBackPressed();
                            binding.loadingLY.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.fail_add_post), Toast.LENGTH_SHORT).show();
                            binding.loadingLY.setVisibility(View.GONE);
                        }
                    }
                });
//        postModelMap.put("description", postModel.description);

    }

}