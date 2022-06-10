package com.example.myplant.fragment;

import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplant.Adapter.MyPlantAdapter;
import com.example.myplant.Model.AddPlantModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.R;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.FragmentMyPlantBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MyPlantFragment extends Fragment {

    FragmentMyPlantBinding binding;
    ArrayList<AddPlantModel> addPlantModelList;
    MyPlantAdapter adapter;
    FirebaseAuth fAuth;
    FirebaseFirestore fireStoreDB;
    UserModel userModelObj;
    AddPlantModel myPlantList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMyPlantBinding.inflate(inflater, container, false);

        fAuth = FirebaseAuth.getInstance();
        userModelObj = UtilityApp.getUserData();
        fireStoreDB = FirebaseFirestore.getInstance();
        fetchData();
        binding.include.title.setText("My Plant");

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

//                    Snackbar.make(binding.rvPlants, deletedMovie, Snackbar.LENGTH_LONG)
//                            .setAction("Undo", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    addPlantModelList.add(position, deletedMovie);
//                                    adapter.notifyItemInserted(position);
//                                }
//                            });
                    break;

                case ItemTouchHelper.RIGHT:

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

        System.out.println("Log addPlantModelList " + addPlantModelList.size());
        adapter = new MyPlantAdapter(requireActivity(), addPlantModelList);
        binding.rvPlants.setAdapter(adapter);

        if (addPlantModelList.size() == 0){
            binding.emptyList.setVisibility(View.VISIBLE);
            binding.rvPlants.setVisibility(View.GONE);
        }else {
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

                        if (addPlantModel.userId.equals(userid)){
                            addPlantModelList.add(addPlantModel);
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
}