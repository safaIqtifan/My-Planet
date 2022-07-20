package com.example.myplant.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myplant.ArticalActivity;
import com.example.myplant.EditMyProfileActivity;
import com.example.myplant.Model.UserModel;
import com.example.myplant.R;
import com.example.myplant.SigninActivity;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseFirestore fireStoreDB;
    UserModel userImagObj;
    Uri profilePhotoUri;
    StorageReference storageRef;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fireStoreDB = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

//        getUserImage();
        userImagObj = UtilityApp.getUserData();

//        if (userImagObj.userImage.isEmpty()){
            Glide.with(getActivity())
                    .asBitmap()
                    .load(userImagObj.userImage)
                    .placeholder(R.drawable.profile)
                    .into(binding.profileImag);
//        Log.e("aa", userImagObj.userImage);
//        }else {
//            binding.profileImag.setImageURI(Uri.parse(userImagObj.userImage));
//        }

        binding.proUserName.setText(userImagObj.fullName);
        binding.include.title.setText("My Profile");
//        binding.include.title.setTextColor(Color.BLACK);

        binding.logout.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Logout Acount");
            builder.setMessage("Are you sure to Logout ?");

            builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), SigninActivity.class));
                getActivity().finish();
            });

            builder.setNegativeButton("No", (dialogInterface, i) -> {

            });
            builder.create().show();
        });

//        binding.profileImag.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
//                            == PackageManager.PERMISSION_DENIED) {
//                        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
//                        requestPermissions(permission, PERMISSION_CODE);
//                    } else {
//                        pickImageFromGallery();
//                    }
//                } else {
//                    pickImageFromGallery();
//                }
//            }
//        });

        binding.termsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        binding.articalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ArticalActivity.class));
            }
        });

        binding.myProfileInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EditMyProfileActivity.class));
            }
        });

        return root;
    }

    private void initData() {

        Glide.with(getActivity())
                .asBitmap()
                .load(userImagObj.userImage)
                .placeholder(R.drawable.profile)
                .into(binding.profileImag);
        binding.proUserName.setText(userImagObj.fullName);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void getUserImage() {

        fireStoreDB.collection(Constants.USER).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                UserModel userModel = document.toObject(UserModel.class);
                                if ((userModel.user_id).equals(UtilityApp.getUserData())) {
                                    Glide.with(getActivity()).asBitmap().load(userModel.userImage).placeholder(R.drawable.camera).into(binding.profileImag);
                                }
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                    Toast.makeText(getActivity(), "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            profilePhotoUri = data.getData();

            Glide.with(getActivity())
                    .asBitmap()
                    .load(profilePhotoUri)
                    .placeholder(R.drawable.profile)
                    .into(binding.profileImag);

            uploadPhoto(profilePhotoUri);
        }
    }

    private void uploadPhoto(Uri photoUri) {

//        binding.loadingLY.setVisibility(View.VISIBLE);

        StorageReference imgRef = storageRef.child("UsersImages" + "/" + UUID.randomUUID().toString());
        UploadTask uploadTask = imgRef.putFile(photoUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        }).addOnSuccessListener(taskSnapshot -> {
            imgRef.getDownloadUrl().addOnCompleteListener(task -> {

                userImagObj.userImage = task.getResult().toString();
                sendImageToFirebase(userImagObj.userImage);

            });


        });
    }

    private void sendImageToFirebase(String photoUrl) {

        Map<String, Object> profileModelMap = new HashMap<>();
        profileModelMap.put("userImage", photoUrl);

        fireStoreDB.collection(Constants.USER).document(userImagObj.user_id)
                .update(profileModelMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        binding.loadingLY.setVisibility(View.GONE);

                        UtilityApp.setUserData(userImagObj);
//                        Glide.with(getActivity())
//                                .asBitmap()
//                                .load(photoUrl)
//                                .placeholder(R.drawable.profile)
//                                .into(binding.profileImag);

                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), getString(R.string.success_add), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showDialog() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_terms);

        Button doneBtn = dialog.findViewById(R.id.doneButton);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

}