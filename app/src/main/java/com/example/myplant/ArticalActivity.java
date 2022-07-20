package com.example.myplant;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myplant.Adapter.ArticalAdapter;
import com.example.myplant.Model.ArticalModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.DataCallBack;
import com.example.myplant.classes.DateUtil;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.ActivityArticalBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArticalActivity extends AppCompatActivity {

    ActivityArticalBinding binding;
    ArrayList<ArticalModel> articalModelArrayList;
    ArticalAdapter adapter;
    FirebaseFirestore fireStoreDB;
    StorageReference storageRef;
    ArticalModel articalDetailsModel;
    UserModel userModel;
    public static boolean referenceList = false;
    private Map<String, Boolean> favMap;
    private Map<String, Boolean> userMap;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_artical);

        binding = ActivityArticalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        articalModelArrayList = new ArrayList<>();
        fireStoreDB = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        articalDetailsModel = new ArticalModel();
        userModel = UtilityApp.getUserData();
        favMap = new HashMap<>();
        userMap = new HashMap<>();

        binding.articalRv.setLayoutManager(new GridLayoutManager(ArticalActivity.this, 2,
                GridLayoutManager.VERTICAL, false));
        binding.articalRv.setNestedScrollingEnabled(false);

//        binding.articalRv.setBackgroundResource(R.drawable.button_background);
//        FlowLayoutManager flowLayoutManager = new FlowLayoutManager();
//        flowLayoutManager.setAutoMeasureEnabled(true);
//        binding.articalRv.setLayoutManager(flowLayoutManager);
//        binding.articalRv.setNestedScrollingEnabled(false);
//        binding.articalRv.setVisibility(View.VISIBLE);

        fetchData();

    }

    private void initAdapter() {

        adapter = new ArticalAdapter(this, articalModelArrayList, "artical", new DataCallBack() {
            @Override
            public void Result(Object obj, String type, Object otherData) {
                articalDetailsModel = (ArticalModel) obj;
                showDialog();
            }
        });
        binding.articalRv.setAdapter(adapter);

    }

    private void showDialog() {

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_artical_details);

        ImageView articalImageDetails = dialog.findViewById(R.id.articalImageDetails);
        TextView articalNameStr = dialog.findViewById(R.id.articalNameTv);
        TextView articalDescriptionStr = dialog.findViewById(R.id.articalDescriptionTv);
        TextView articalTypeStr = dialog.findViewById(R.id.articalTypeTv);
        TextView articalCreatedAtStr = dialog.findViewById(R.id.articalAgeTv);
        TextView title = dialog.findViewById(R.id.articalDetailsTitle);
        ImageView favourite = dialog.findViewById(R.id.articalFavourite);
        Button doneBtn = dialog.findViewById(R.id.doneButton);

        String CreatedAtTime = DateUtil.covertDateToAgo(articalDetailsModel.createdAt);
        articalCreatedAtStr.setText(CreatedAtTime);
        articalNameStr.setText(articalDetailsModel.articalName);
        articalTypeStr.setText(articalDetailsModel.articalType);
        articalDescriptionStr.setText(articalDetailsModel.articalDescription);

        Glide.with(this).asBitmap().load(articalDetailsModel.articalImage)
                .placeholder(R.drawable.camera).into(articalImageDetails);
        title.setText("Artical Details");

        if (favMap.containsKey(articalDetailsModel.articalId)) {
            favourite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.heart_full));
            articalDetailsModel.userId.put(userModel.user_id, true);
        } else {
            favourite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.heart_empty));
            articalDetailsModel.userId.put(userModel.user_id, false);
        }

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favMap.containsKey(articalDetailsModel.articalId)) {
                    favourite.setImageDrawable(ContextCompat.getDrawable(ArticalActivity.this, R.drawable.heart_empty));
                    favMap.remove(articalDetailsModel.articalId);
                    articalDetailsModel.userId.put(userModel.user_id, false);
                } else {
                    favourite.setImageDrawable(ContextCompat.getDrawable(ArticalActivity.this, R.drawable.heart_full));
                    favMap.put(articalDetailsModel.articalId, true);
                    articalDetailsModel.userId.put(userModel.user_id, true);
                }
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToFirebase(articalDetailsModel);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (referenceList) {
            fetchData();
        }
        referenceList = false;
    }

    public void fetchData() {

        binding.loadingLY.setVisibility(View.VISIBLE);

        fireStoreDB.collection(Constants.ARTICAL)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        ArticalModel articalModel = document.toObject(ArticalModel.class);
                        articalModelArrayList.add(articalModel);

                        userMap = articalModel.userId;

//                        for (String userModels : articalModel.userId)
//                            if (userModels.equals(userModel.user_id)){
                                if (userMap.containsKey(userModel.user_id) && userMap.containsValue(true)) {
                                    favMap.put(articalModel.articalId, true);
                                }
//                            }
                    }
                    initAdapter();
                } else {
                    Toast.makeText(ArticalActivity.this, getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                }
                binding.loadingLY.setVisibility(View.GONE);
            }
        });
    }

//    public void sendFavouriteData() {
//
//        Map<String, Object> favModelMap = new HashMap<>();
//        favModelMap.put("articalIsFavourite", articalDetailsModel.articalIsFavourite);
//
//        fireStoreDB.collection(Constants.USER).document(userModel.user_id)
//                .collection(Constants.ARTICALFav).document(articalDetailsModel.articalId)
//                .update(favModelMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//
//                    Toast.makeText(ArticalActivity.this, getString(R.string.success), Toast.LENGTH_SHORT).show();
//                    dialog.dismiss();
//
//                    FavouriteFragment.referenceList = true;
////                    onResume();
//                } else {
//                    Toast.makeText(ArticalActivity.this, getString(R.string.fail_add_post), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
////        }
//
//    }

    private void sendToFirebase(ArticalModel sendArticalModle) {

//        String articalId = fireStoreDB.collection(Constants.ARTICAL).document().getId(); // this is auto genrat

        Map<String, Object> favModelMap = new HashMap<>();
        favModelMap.put("userId", sendArticalModle.userId);
//        favModelMap.put("articalIsFavourite", sendArticalModle.articalIsFavourite);

        fireStoreDB.collection(Constants.ARTICAL).document(sendArticalModle.articalId)
                .set(favModelMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ArticalActivity.this, getString(R.string.success), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ArticalActivity.this, getString(R.string.fail_add_post), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    private void getDataFromUser() {
//
//        fireStoreDB.collection(Constants.ARTICAL)
//                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                binding.loadingLY.setVisibility(View.GONE);
//
//                if (task.isSuccessful()) {
//                    articalModelArrayList.clear();
//
//                } else {
//                    Toast.makeText(ArticalActivity.this, getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

//    public void sendUserFavouriteData() {
//
//        String favId = fireStoreDB.collection(Constants.ARTICAL).document().getId(); // this is auto genrat
//
//        Map<String, Object> favModelMap = new HashMap<>();
//        favModelMap.put("userId", userModel.user_id);
//        favModelMap.put("userId", favId);
//        favModelMap.put("articalName", articalDetailsModel.articalName);
//        favModelMap.put("articalType", articalDetailsModel.articalType);
//        favModelMap.put("articalImage", articalDetailsModel.articalImage);
//        favModelMap.put("articalTime", articalDetailsModel.articalTime);
//        favModelMap.put("articalIsFavourite", articalDetailsModel.articalIsFavourite);
//
//        fireStoreDB.collection(Constants.USER).document(userModel.user_id)
//                .collection(Constants.ARTICALFav).document(favId)
//                .set(favModelMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//
//                    Toast.makeText(ArticalActivity.this, getString(R.string.success), Toast.LENGTH_SHORT).show();
//                    dialog.dismiss();
//
//                } else {
//                    Toast.makeText(ArticalActivity.this, getString(R.string.fail_add_post), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
////        }
//
//    }


}