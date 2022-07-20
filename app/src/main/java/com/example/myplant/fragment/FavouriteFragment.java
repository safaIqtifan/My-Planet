package com.example.myplant.fragment;

import android.app.Dialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myplant.Adapter.FavouriteAdapter;
import com.example.myplant.Model.AddPlantModel;
import com.example.myplant.Model.ArticalModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.R;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.DataCallBack;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.FragmentFavouriteBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavouriteFragment extends Fragment {

    FragmentFavouriteBinding binding;
    ArrayList<Object> favouriteModelList;
    FavouriteAdapter favouriteAdapter;
    //    AddPlantModel addPlantModel;
    FirebaseFirestore fireStoreDB;
    AddPlantModel plantDetailsModel;
    ArticalModel articalDetailsModel;
    UserModel userModelObj;
    Dialog dialog;
    private Map<String, Object> favMap;
    private Map<String, Boolean> userMap;
    public static boolean referenceList = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFavouriteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.include.title.setText("Favourite");
//        binding.include.title.setTextColor(Color.BLACK);
        fireStoreDB = FirebaseFirestore.getInstance();
        userModelObj = UtilityApp.getUserData();
        favMap = new HashMap<>();
        userMap = new HashMap<>();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        favouriteModelList = new ArrayList<>();

        binding.favouritetRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        fetchPlantData();
    }

    private void initAdapter() {

        favouriteAdapter = new FavouriteAdapter(favouriteModelList, getActivity(), new DataCallBack() {
            @Override
            public void Result(Object obj, String type, Object otherData) {

                if (type.equals("VIEW_TYPE_plant")) {
                    plantDetailsModel = (AddPlantModel) obj;
                    showPlantDialog();
                } else {
                    articalDetailsModel = (ArticalModel) obj;
                    showArticalDialog();
                }
            }
        });
        binding.favouritetRv.setAdapter(favouriteAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (referenceList) {
            fetchPlantData();
            fetchArticalData();
        }
        referenceList = false;
    }

    private void showPlantDialog() {

        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_plant_details);

        ImageView plantImageDetails = dialog.findViewById(R.id.plantImageDetails);
        TextView plantNameStr = dialog.findViewById(R.id.plantNameTv);
        TextView plantDescriptionStr = dialog.findViewById(R.id.plantDescriptionTv);
        TextView plantTypeStr = dialog.findViewById(R.id.plantTypeTv);
        TextView plantAgeStr = dialog.findViewById(R.id.plantAgeTv);
        ProgressBar plantSun = dialog.findViewById(R.id.sunProgressbar);
        ProgressBar plantWater = dialog.findViewById(R.id.waterProgressBar);
        TextView textDays = dialog.findViewById(R.id.textDays);
        TextView textsun = dialog.findViewById(R.id.textsun);
        TextView title = dialog.findViewById(R.id.plantDetailsTitle);
        ImageView favourite = dialog.findViewById(R.id.favourite);
        Button doneBtn = dialog.findViewById(R.id.doneButton);

        plantDescriptionStr.setText(plantDetailsModel.plantDescription);
        plantTypeStr.setText(plantDetailsModel.plantType);
        plantAgeStr.setText(plantDetailsModel.plantAge);
        plantNameStr.setText(plantDetailsModel.plantName);
        plantSun.setProgress(plantDetailsModel.plantSun);
        plantWater.setProgress(plantDetailsModel.plantWateringDayes * 10);
        textDays.setText(plantDetailsModel.plantWateringDayes + " Days");
        textsun.setText(plantDetailsModel.plantSun + " %");
        Glide.with(this).asBitmap().load(plantDetailsModel.plantPhoto)
                .placeholder(R.drawable.camera).into(plantImageDetails);
        title.setText("plant Details");

        if (favMap.containsKey(plantDetailsModel.plant_id)) {
            favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_full));
            plantDetailsModel.plantIsFavourite = true;
        } else {
            favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_empty));
            plantDetailsModel.plantIsFavourite = false;
        }

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favMap.containsKey(plantDetailsModel.plant_id)) {
                    favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_empty));
                    favMap.remove(plantDetailsModel.plant_id);
                    plantDetailsModel.plantIsFavourite = false;
                } else {
                    favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_full));
                    favMap.put(plantDetailsModel.plant_id, plantDetailsModel);
                    plantDetailsModel.plantIsFavourite = true;
                }
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFavouritePlantData();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void showArticalDialog() {

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

        articalCreatedAtStr.setText(String.valueOf(articalDetailsModel.articalTime));
        articalNameStr.setText(articalDetailsModel.articalName);
        articalTypeStr.setText(articalDetailsModel.articalType);
        articalDescriptionStr.setText(articalDetailsModel.articalDescription);

        Glide.with(this).asBitmap().load(articalDetailsModel.articalImage)
                .placeholder(R.drawable.camera).into(articalImageDetails);
        title.setText("Artical Details");

        if (favMap.containsKey(articalDetailsModel.articalId)) {
            favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_full));
            articalDetailsModel.userId.put(userModelObj.user_id, true);
        } else {
            favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_empty));
            articalDetailsModel.userId.put(userModelObj.user_id, false);
        }

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favMap.containsKey(articalDetailsModel.articalId)) {
                    favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_empty));
                    favMap.remove(articalDetailsModel.articalId);
                    articalDetailsModel.userId.put(userModelObj.user_id, false);
                } else {
                    favourite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.heart_full));
                    favMap.put(articalDetailsModel.articalId, true);
                    articalDetailsModel.userId.put(userModelObj.user_id, true);
                }
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFavouriteArticalData(articalDetailsModel);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void sendFavouritePlantData() {

        Map<String, Object> favModelMap = new HashMap<>();
        favModelMap.put("plantIsFavourite", plantDetailsModel.plantIsFavourite);
//        favModelMap.put("articalIsFavourite", articalDetailsModel.articalIsFavourite);

        fireStoreDB.collection(Constants.PLANT).document(plantDetailsModel.plant_id).
                update(favModelMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
//                    favouriteModelList.clear();
                    FavouriteFragment.referenceList = true;
                    onResume();
                    Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    initAdapter();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_add_post), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void sendFavouriteArticalData(ArticalModel articalModel) {

        Map<String, Object> favModelMap = new HashMap<>();
        favModelMap.put("userId", articalModel.userId);

        fireStoreDB.collection(Constants.ARTICAL).document(articalDetailsModel.articalId)
                .set(favModelMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    FavouriteFragment.referenceList = true;
                    onResume();
                    initAdapter();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_add_post), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    public void sendFavouriteData() {
//
//        Map<String, Object> favModelMap = new HashMap<>();
//        favModelMap.put("articalIsFavourite", articalDetailsModel.articalIsFavourite);
//        favModelMap.put("userId", userModelObj.user_id);
//
//        fireStoreDB.collection(Constants.ARTICAL).document(articalDetailsModel.articalId)
//                .update(favModelMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//
//                    Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_SHORT).show();
//                    dialog.dismiss();
//                    FavouriteFragment.referenceList = true;
//                    onResume();
//                } else {
//                    Toast.makeText(getActivity(), getString(R.string.fail_add_post), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
////        }
//
//    }

    public void fetchArticalData() {

        fireStoreDB.collection(Constants.ARTICAL)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                binding.loadingLY.setVisibility(View.GONE);

                if (task.isSuccessful()) {
//                    favouriteModelList.clear();

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        ArticalModel articalModelObj = document.toObject(ArticalModel.class);
                        System.out.println("Log addPlantModel " + articalModelObj.articalName);

                        userMap = articalModelObj.userId;
                        if (userMap.containsKey(userModelObj.user_id) && userMap.containsValue(true)) {
                            favMap.put(articalModelObj.articalId, true);
                            favouriteModelList.add(articalModelObj);
                        }

//                        for (String userModels : articalModelObj.userId)
//                            if (userModels.equals(userModelObj.user_id)){
//                                    favMap.put(articalModelObj.articalId, articalModelObj);
//                            }
//                        if (articalModelObj.articalIsFavourite) {
//                                favMap.put(articalModelObj.articalId, articalModelObj);
//                                favouriteModelList.add(articalModelObj);
//                            }
                    }
                    initAdapter();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void fetchPlantData() {

        binding.loadingLY.setVisibility(View.VISIBLE);

        fireStoreDB.collection(Constants.PLANT)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                binding.loadingLY.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    favouriteModelList.clear();
//                    favMap.clear();

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        AddPlantModel addPlantModel = document.toObject(AddPlantModel.class);
                        System.out.println("Log addPlantModel " + addPlantModel.plantName);

                        if (addPlantModel.userId.equals(userModelObj.user_id)) {
                            if (addPlantModel.plantIsFavourite) {
                                favMap.put(addPlantModel.plant_id, addPlantModel);
                                favouriteModelList.add(addPlantModel);
                            }
                        }
                    }
                    fetchArticalData();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}