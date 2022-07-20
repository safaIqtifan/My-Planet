package com.example.myplant.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplant.Adapter.MyPlantAdapter;
import com.example.myplant.AddPlantActivity;
import com.example.myplant.Model.AddPlantModel;
import com.example.myplant.Model.ChoosePlantTypeModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.R;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.DataCallBack;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.FragmentMyPlantBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MyPlantFragment extends Fragment {

    FragmentMyPlantBinding binding;
    ArrayList<AddPlantModel> addPlantModelList;
    MyPlantAdapter adapter;
    FirebaseAuth fAuth;
    FirebaseFirestore fireStoreDB;
    UserModel userModelObj;
//    AddPlantModel myPlantList;
    private Map<String, Object> favMap;
    //    public boolean isFavorite;
    Dialog dialog;
    AddPlantModel plantDetailsModel;
    public static boolean referenceList = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMyPlantBinding.inflate(inflater, container, false);

        fAuth = FirebaseAuth.getInstance();
        fireStoreDB = FirebaseFirestore.getInstance();
        userModelObj = UtilityApp.getUserData();
        favMap = new HashMap<>();

        fetchData();
//        fetchUserPlantData();
        binding.include.title.setText("My Plant");
//        binding.include.title.setTextColor(Color.BLACK);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addPlantModelList = new ArrayList<>();
        binding.rvPlants.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.rvPlants);
    }

    AddPlantModel deletedMovie = null;
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT
            | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();

            switch (direction) {

                case ItemTouchHelper.LEFT:

                    deletedMovie = addPlantModelList.get(position);

                    fireStoreDB.collection(Constants.PLANT).document(deletedMovie.plant_id)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG", "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("TAG", "Error deleting document", e);
                                }
                            });

                    addPlantModelList.remove(position);
                    adapter.notifyItemRemoved(position);

                    Snackbar.make(binding.rvPlants, deletedMovie.plantName, Snackbar.LENGTH_LONG)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    addPlantModelList.add(position, deletedMovie);
                                    adapter.notifyItemInserted(position);
                                }
                            }).show();
                    break;

                case ItemTouchHelper.RIGHT:

                    AddPlantModel editePlanObj = addPlantModelList.get(position);
                    Intent intent = new Intent(getActivity(), AddPlantActivity.class);
                    intent.putExtra("editeObj", editePlanObj);
                    startActivity(intent);

                    adapter.notifyItemChanged(position);
                    break;
            }

        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red))
                    .addSwipeLeftActionIcon(R.drawable.delete)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green))
                    .addSwipeRightActionIcon(R.drawable.edit)
//                    .addBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.my_background))
//                    .addActionIcon(R.drawable.my_icon)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };

    private void initAdapter() {

        adapter = new MyPlantAdapter(requireActivity(), addPlantModelList, new DataCallBack() {
            @Override
            public void Result(Object obj, String type, Object otherData) {

                plantDetailsModel = (AddPlantModel) obj;
                showDialog();
            }
        });
        binding.rvPlants.setAdapter(adapter);

        if (addPlantModelList.size() == 0) {
            binding.emptyList.setVisibility(View.VISIBLE);
            binding.rvPlants.setVisibility(View.GONE);
        } else {
            binding.emptyList.setVisibility(View.GONE);
            binding.rvPlants.setVisibility(View.VISIBLE);
        }
//        binding.rvPlants.setVisibility(View.VISIBLE);
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
            fetchData();
        }
        referenceList = false;
    }

    private void showDialog() {

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
        plantSun.setProgress(plantDetailsModel.plantSun * 10);
        plantWater.setProgress(plantDetailsModel.plantWater * 10);
        textDays.setText(plantDetailsModel.plantWateringDayes + " Days");
        textsun.setText((plantDetailsModel.plantSun * 10) + " %");
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
                sendFavouriteData();
//                Toast.makeText(getActivity(), "aaa", Toast.LENGTH_SHORT).show();
//                loadingLY.setVisibility(View.VISIBLE);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void sendFavouriteData() {

        Map<String, Object> favModelMap = new HashMap<>();
        favModelMap.put("plantIsFavourite", plantDetailsModel.plantIsFavourite);

            fireStoreDB.collection(Constants.PLANT).document(plantDetailsModel.plant_id).
                    update(favModelMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
//        }

    }

    public void fetchData() {

        binding.loadingLY.setVisibility(View.VISIBLE);

        FirebaseUser firebaseUser = fAuth.getCurrentUser();
        assert firebaseUser != null;
        String userid = firebaseUser.getUid();

//        fireStoreDB.collection(Constants.PLANT).document().get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
////                            myPlantList = task.getResult().toObject(AddPlantModel.class);
////                                initAdapter();
//                            myPlantList.userId
//                            AddPlantModel myPlantList = task.getResult().toObject(AddPlantModel.class);
//                            for (DocumentSnapshot document : task.getResult().getDocuments()) {
//                                UserModel userModel = document.toObject(UserModel.class);
//                                if ((userModel.user_id).equals(fAuth.getUid())) {
//
//                                }
//                        UserModel userDataModel = UtilityApp.getUserData();
//                                Toast.makeText(SigninActivity.this, user., Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            Toast.makeText(getActivity(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//        if (showLoading) {
//            binding.loadingLY.setVisibility(View.VISIBLE);
//        }

        fireStoreDB.collection(Constants.PLANT)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                binding.loadingLY.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    addPlantModelList.clear();

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        AddPlantModel addPlantModel = document.toObject(AddPlantModel.class);
                        System.out.println("Log addPlantModel " + addPlantModel.plantName);

                        if (addPlantModel.userId.equals(userModelObj.user_id)){
                            if (addPlantModel.plantIsFavourite) {
                                favMap.put(addPlantModel.plant_id, addPlantModel);
                            }
                            addPlantModelList.add(addPlantModel);
                        }

//                        if (addPlantModel.userId.equals(userid)) {
//                            addPlantModelList.add(addPlantModel);
//                        }
                    }
//                    adapter.myArray = addPlantModelList;
//                    adapter.notifyDataSetChanged();
                    initAdapter();
                    fetchUserPlantData(addPlantModelList);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void fetchUserPlantData(ArrayList<AddPlantModel> newAddPlantModels) {

        fireStoreDB.collection(Constants.USER).document(userModelObj.user_id).collection(Constants.TYPE)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        ChoosePlantTypeModel choosePlantTypeModel = document.toObject(ChoosePlantTypeModel.class);

                        for (AddPlantModel addPlantModel : newAddPlantModels) {
                            if (choosePlantTypeModel.choosenPlantName.equals(addPlantModel.plantType)) {
                                addPlantModel.plantWater = choosePlantTypeModel.plantWater;
                                addPlantModel.plantSun = choosePlantTypeModel.plantSun;
                            }
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}