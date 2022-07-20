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

import com.bumptech.glide.Glide;
import com.example.myplant.Model.AddPlantModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.DateUtil;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.ActivityAddPlantBinding;
import com.example.myplant.fragment.MyPlantFragment;
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
    int plantTypePosition;
    int selectedPlantWateringDays;
    UserModel userModel;
    Bundle bundle;
    boolean update = false;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    int[] plantWateringDaysList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_plant);
        binding = ActivityAddPlantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storageRef = FirebaseStorage.getInstance().getReference();
        fireStoreDB = FirebaseFirestore.getInstance();
        addPlantModel = new AddPlantModel();


        plantWateringDaysList = getResources().getIntArray(R.array.plantWateringDays);

//                Calendar calendar = Calendar.getInstance();
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm/dd/yyy");
//                String dateFormat = simpleDateFormat.format(calendar.getTime());
//                addPlantModel.choosenPlantCurrentTime = dateFormat;
//
//                calendar.add(Calendar.DATE, );

        bundle = getIntent().getExtras();
        if (bundle != null) {
            addPlantModel = (AddPlantModel) bundle.getSerializable("editeObj");
            update = true;

            binding.plantDescriptionEd.setText(addPlantModel.plantDescription);
            binding.plantTypeSpinner.setSelection(addPlantModel.plantTypePosition);
            binding.plantAgeEd.setText(addPlantModel.plantAge);
            binding.plantNameEd.setText(addPlantModel.plantName);
            binding.include.title.setText(addPlantModel.plantName);
//            binding.include.title.setTextColor(Color.BLACK);
            Glide.with(this).asBitmap().load(addPlantModel.plantPhoto)
                    .placeholder(R.drawable.camera).into(binding.plantPhoto);

        } else {
            binding.include.title.setText("Add Your Plant");
//            binding.include.title.setTextColor(Color.BLACK);
        }

        userModel = UtilityApp.getUserData();

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkData();
            }
        });

        binding.plantPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                update = false;
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
                if (i > 0) {
                    plantTypeSpinnerStr = binding.plantTypeSpinner.getSelectedItem().toString();
                    plantTypePosition = binding.plantTypeSpinner.getSelectedItemPosition();
                    selectedPlantWateringDays = plantWateringDaysList[i - 1];
                    addPlantModel.plantWateringDayes = selectedPlantWateringDays;
                } else {
                    plantTypeSpinnerStr = "";
                }
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
            }
        }).addOnSuccessListener(taskSnapshot -> {

            imgRef.getDownloadUrl().addOnCompleteListener(task -> {

                addPlantModel.plantPhoto = task.getResult().toString();
                sendDataToFirebase();
            });


        });
    }

    private void checkData() {

        String plantNameStr = binding.plantNameEd.getText().toString();
        String plantAgeStr = binding.plantAgeEd.getText().toString();
        String plantDescriptionStr = binding.plantDescriptionEd.getText().toString();

        boolean hasError = false;
        if (!update) {
            if (plantPhotoUri == null) {
                Toast.makeText(this, getString(R.string.please_add_photo), Toast.LENGTH_SHORT).show();
                hasError = true;
            }
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
        addPlantModel.plantType = plantTypeSpinnerStr;
        addPlantModel.plantTypePosition = plantTypePosition;
        addPlantModel.plantDescription = plantDescriptionStr;
        addPlantModel.plantWateringDayes = selectedPlantWateringDays;
        addPlantModel.plantSun = 50;
        addPlantModel.plantWater = 30;
        addPlantModel.choosenPlantCurrentTime = DateUtil.GetDateWithAddNextDays(selectedPlantWateringDays);

//        fetchData();

        if (plantPhotoUri != null) {
            uploadPhoto(plantPhotoUri);
        } else {
            sendDataToFirebase();
        }

    }

    private void sendDataToFirebase() {

        String plantId;
        if (bundle != null) {
            plantId = addPlantModel.plant_id;
        } else {
            plantId = fireStoreDB.collection(Constants.PLANT).document().getId(); // this is auto genrat
        }
        Map<String, Object> postModelMap = new HashMap<>();
        postModelMap.put("plant_id", plantId);
        postModelMap.put("userId", userModel.user_id);
        postModelMap.put("plantName", addPlantModel.plantName);
        postModelMap.put("plantAge", addPlantModel.plantAge);
        postModelMap.put("plantType", addPlantModel.plantType);
        postModelMap.put("plantSun", addPlantModel.plantSun);
        postModelMap.put("plantWater", addPlantModel.plantWater);
        postModelMap.put("choosenPlantCurrentTime", addPlantModel.choosenPlantCurrentTime);
        postModelMap.put("plantWateringDayes", addPlantModel.plantWateringDayes);
        postModelMap.put("plantTypePosition", addPlantModel.plantTypePosition);
        postModelMap.put("plantPhoto", addPlantModel.plantPhoto);
        postModelMap.put("plantDescription", addPlantModel.plantDescription);
        postModelMap.put("plantIsFavourite", addPlantModel.plantIsFavourite);

        fireStoreDB.collection(Constants.PLANT).document(plantId).set(postModelMap, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            MyPlantFragment.referenceList = true;
                            if (bundle == null) {
                                Toast.makeText(getApplicationContext(), getString(R.string.success_add_plant), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.success_update_plant), Toast.LENGTH_SHORT).show();
                            }
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

//    public void fetchData() {
//
//        fireStoreDB.collection(Constants.USER).document(userModel.user_id).collection(Constants.TYPE)
//                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                if (task.isSuccessful()) {
//
//                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
//                        ChoosePlantTypeModel choosePlantTypeModel = document.toObject(ChoosePlantTypeModel.class);
////                        choosePlantTypeModelArrayList.add(choosePlantTypeModel);
//                        if (choosePlantTypeModel.choosenPlantName.equals(plantTypeSpinnerStr)) {
////                            Calendar c = Calendar.getInstance();
////                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss");
////                            String reg_date = df.format(c.getTime());
////                            showtoast("Currrent Date Time : "+reg_date);
//
////                            c.add(Calendar.DAY_OF_MONTH, Integer.parseInt(choosePlantTypeModel.choosenPlantWateringDayes));  // number of days to add
////                            String end_date = df.format(c.getTime());
////                            showtoast("end Time : "+end_date);
//
//                            addPlantModel.choosenPlantCurrentTime = DateUtil.GetDateWithAddNextDays(choosePlantTypeModel.choosenPlantWateringDayes);
//                            break;
//                        }
//                    }
//
//                } else {
//                    Toast.makeText(AddPlantActivity.this, getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//    }

}