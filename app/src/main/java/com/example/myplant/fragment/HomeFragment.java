package com.example.myplant.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myplant.Adapter.HomeChoosenPlantAdapter;
import com.example.myplant.Model.ChoosePlantTypeModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.R;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    UserModel userModel;
    ArrayList<ChoosePlantTypeModel> choosePlantTypeModelArrayList;
    HomeChoosenPlantAdapter adapter;
    FirebaseFirestore fireStoreDB;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fireStoreDB = FirebaseFirestore.getInstance();

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

        fetchData();
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
                        choosePlantTypeModelArrayList.add(choosePlantTypeModel);
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


}