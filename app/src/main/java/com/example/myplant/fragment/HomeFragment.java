package com.example.myplant.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myplant.Adapter.ArticalHomeAdapter;
import com.example.myplant.Adapter.HomeChoosenPlantAdapter;
import com.example.myplant.Model.ArticalModel;
import com.example.myplant.Model.ChoosePlantTypeModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.NotificationActivity;
import com.example.myplant.R;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.DataCallBack;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    UserModel userModel;
    ArrayList<ArticalModel> articalModelArrayList;
    ArrayList<ChoosePlantTypeModel> choosePlantTypeModelArrayList;
    HomeChoosenPlantAdapter adapter;
    ArticalHomeAdapter articalAdapter;
    FirebaseFirestore fireStoreDB;
    public static boolean referenceList = false;
    private Map<String, Boolean> favMap;
    private Map<String, Boolean> userMap;
    Dialog dialog;
    ArticalModel articalModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fireStoreDB = FirebaseFirestore.getInstance();
        favMap = new HashMap<>();
        userMap = new HashMap<>();

//        UserModel userImagObj = UtilityApp.getUserData();
//        binding.name.setText(userImagObj.user_id);
        userModel = UtilityApp.getUserData();
        Glide.with(getActivity())
                .asBitmap()
                .load(userModel.userImage)
                .placeholder(R.drawable.profile)
                .into(binding.profileImag);

        binding.name.setText(userModel.fullName);

        choosePlantTypeModelArrayList = new ArrayList<>();
        binding.plantTypeRv.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));

        articalModelArrayList = new ArrayList<>();
        binding.articalRv.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));

        fetchData();
        getArticalData();

        binding.notificationImag.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), NotificationActivity.class));
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initAdapter() {

        adapter = new HomeChoosenPlantAdapter(getActivity(), choosePlantTypeModelArrayList);
        binding.plantTypeRv.setAdapter(adapter);
    }

    private void initArticalAdapter() {

        articalAdapter = new ArticalHomeAdapter(getActivity(), articalModelArrayList, new DataCallBack() {
            @Override
            public void Result(Object obj, String type, Object otherData) {
                articalModel = (ArticalModel) obj;
                showDialog();
            }
        });
        binding.articalRv.setAdapter(articalAdapter);
    }

    private void showDialog() {

        dialog = new Dialog(getActivity());
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

        articalCreatedAtStr.setText(String.valueOf(articalModel.articalTime));
        articalNameStr.setText(articalModel.articalName);
        articalTypeStr.setText(articalModel.articalType);
        articalDescriptionStr.setText(articalModel.articalDescription);

        Glide.with(this).asBitmap().load(articalModel.articalImage)
                .placeholder(R.drawable.camera).into(articalImageDetails);
        title.setText("Artical Details");

        if (favMap.containsKey(articalModel.articalId)) {
            favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_full));
            articalModel.userId.put(userModel.user_id, true);
        } else {
            favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_empty));
            articalModel.userId.put(userModel.user_id, false);
        }

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favMap.containsKey(articalModel.articalId)) {
                    favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_empty));
                    favMap.remove(articalModel.articalId);
                    articalModel.userId.put(userModel.user_id, false);
                } else {
                    favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_full));
                    favMap.put(articalModel.articalId, true);
                    articalModel.userId.put(userModel.user_id, true);
                }
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFavouriteData(articalModel);
//                Toast.makeText(ArticalActivity.this, "aaa", Toast.LENGTH_SHORT).show();
//                loadingLY.setVisibility(View.VISIBLE);
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

    public void sendFavouriteData(ArticalModel articalModels) {

        Map<String, Object> favModelMap = new HashMap<>();
        favModelMap.put("userId", articalModels.userId);

        fireStoreDB.collection(Constants.ARTICAL).document(articalModels.articalId)
                .set(favModelMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_add_post), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    public void sendFavouriteData() {
//
//        Map<String, Object> favModelMap = new HashMap<>();
//        favModelMap.put("articalIsFavourite", articalModel.articalIsFavourite);
//
//        fireStoreDB.collection(Constants.ARTICAL).document(articalModel.articalId).
//                update(favModelMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//
//                    Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_SHORT).show();
//                    dialog.dismiss();
//
//                } else {
//                    Toast.makeText(getActivity(), getString(R.string.fail_add_post), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
////        }
//
//    }

    public void fetchData() {

        binding.loadingLY.setVisibility(View.VISIBLE);

//        fireStoreDB.collection(Constants.FB_FARMS).document(farmModel.farm_id)
//                .collection(Constants.FB_CATEGORIES)
//
//        fireStoreDB.collection(Constants.FB_FARMS).document(farmId).collection(Constants.FB_CATEGORIES)
//                .document(String.valueOf(categoryModel.id)).set(categoryModel, SetOptions.merge()).addOnCompleteListener

        fireStoreDB.collection(Constants.USER).document(userModel.user_id).collection(Constants.TYPE)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                binding.loadingLY.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    choosePlantTypeModelArrayList.clear();

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        ChoosePlantTypeModel choosePlantTypeModel = document.toObject(ChoosePlantTypeModel.class);

                        if (choosePlantTypeModel.isChecked) {
                            choosePlantTypeModelArrayList.add(choosePlantTypeModel);
                        }
                    }
//                    adapter.myArray = addPlantModelList;
//                    adapter.notifyDataSetChanged();
                    initAdapter();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getArticalData() {

        binding.loadingLY.setVisibility(View.VISIBLE);

        fireStoreDB.collection(Constants.ARTICAL).orderBy("createdAt", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                binding.loadingLY.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    articalModelArrayList.clear();

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        ArticalModel articalModel = document.toObject(ArticalModel.class);
                        articalModelArrayList.add(articalModel);

                        userMap = articalModel.userId;
                        if (userMap.containsKey(userModel.user_id) && userMap.containsValue(true)) {
                            favMap.put(articalModel.articalId, true);
                        }
//                        if (articalModel.articalIsFavourite){
//                            favMap.put(articalModel.articalId, articalModel);
//                        }
                    }
                    initArticalAdapter();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}