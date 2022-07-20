package com.example.myplant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.myplant.Adapter.ChoosePlantTypeAdapter;
import com.example.myplant.Adapter.SelectedPlantAdapter;
import com.example.myplant.Model.ChoosePlantTypeModel;
import com.example.myplant.Model.UserModel;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.DataCallBack;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.ActivityChooseMyPlantBinding;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChooseMyPlantActivity extends AppCompatActivity {

    ActivityChooseMyPlantBinding binding;
    ArrayList<ChoosePlantTypeModel> choosePlantTypeModelArrayList;
    ArrayList<ChoosePlantTypeModel> selectedPlantType = new ArrayList<>();
    FirebaseFirestore fireStoreDB;
    ChoosePlantTypeModel choosePlantTypeModel;
    FirebaseAuth fAuth;
    UserModel userModelData;
    private ProgressDialog lodingBar;
    boolean isChecked = false;
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_choose_my_plant);
        binding = ActivityChooseMyPlantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fireStoreDB = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        lodingBar = new ProgressDialog(ChooseMyPlantActivity.this);

        userModelData = UtilityApp.getUserData();
        binding.username.setText(userModelData.fullName);
//        Bundle bundle = getIntent().getExtras();
//        binding.username.setText(bundle.getString("name"));

        choosePlantTypeModel = new ChoosePlantTypeModel();

        choosePlantTypeModelArrayList = new ArrayList<>();
        choosePlantTypeModelArrayList.add(new ChoosePlantTypeModel(
                1, "Roses and ornamental", 2, 1, "https://firebasestorage.googleapis.com/v0/b/my-plant-6d13d.appspot.com/o/plantType%2Ftype1.png?alt=media&token=34ddf427-4484-4cb2-98ed-3df5b91d7c77"));
        choosePlantTypeModelArrayList.add(new ChoosePlantTypeModel(
                4, "Trees", 9, 3, "https://firebasestorage.googleapis.com/v0/b/my-plant-6d13d.appspot.com/o/plantType%2Ftype2.png?alt=media&token=d5635f5c-b3ed-40f0-95a1-7de4c8d0cf8b"));
        choosePlantTypeModelArrayList.add(new ChoosePlantTypeModel(
                5, "Cactus", 1, 9, "https://firebasestorage.googleapis.com/v0/b/my-plant-6d13d.appspot.com/o/plantType%2Ftype3.png?alt=media&token=09ed43ce-6149-418c-9a76-4aa1f763ae72"));
        choosePlantTypeModelArrayList.add(new ChoosePlantTypeModel(
                2, "climbing plants", 6, 7, "https://firebasestorage.googleapis.com/v0/b/my-plant-6d13d.appspot.com/o/plantType%2Ftype4.png?alt=media&token=352d2826-c73c-46af-b521-042ec9813469"));
        choosePlantTypeModelArrayList.add(new ChoosePlantTypeModel(
                3, "Aromatic plants", 3, 3, "https://firebasestorage.googleapis.com/v0/b/my-plant-6d13d.appspot.com/o/plantType%2Ftype5.png?alt=media&token=f3c0b688-337b-4976-8e96-c68124dcff8f"));
        choosePlantTypeModelArrayList.add(new ChoosePlantTypeModel(
                3, "poisonous plants", 7, 5, "https://firebasestorage.googleapis.com/v0/b/my-plant-6d13d.appspot.com/o/plantType%2Ftype6.png?alt=media&token=f94fb0b1-0536-470a-97a1-9ec47fc2960b"));

        binding.rv.setLayoutManager(new GridLayoutManager(ChooseMyPlantActivity.this, 3,
                GridLayoutManager.VERTICAL, false));
        binding.rv.setNestedScrollingEnabled(false);

        FlowLayoutManager flowLayoutManager = new FlowLayoutManager();
        flowLayoutManager.setAutoMeasureEnabled(true);
        binding.selectedRV.setLayoutManager(flowLayoutManager);
        binding.selectedRV.setNestedScrollingEnabled(false);
        binding.selectedRV.setVisibility(View.VISIBLE);
        initData();

        binding.startNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectedPlantType.isEmpty()) {
                    Toast.makeText(ChooseMyPlantActivity.this, R.string.please_select_one_least, Toast.LENGTH_SHORT).show();
                    return;
                }

                lodingBar.setTitle("Save selections");
                lodingBar.setMessage("Please wait while the selections are saved");
                lodingBar.setCanceledOnTouchOutside(false);
                lodingBar.show();

                for (ChoosePlantTypeModel choosePlantTypeModel : choosePlantTypeModelArrayList) {
                    sendPostToFirebase(choosePlantTypeModel);
                }
                startActivity(new Intent(ChooseMyPlantActivity.this, NavigationActivity.class));
                finish();
            }
        });

        binding.closePlantType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChooseMyPlantActivity.this, NavigationActivity.class));
                finish();
            }
        });

//        logoutBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
//                        new ResultCallback<Status>() {
//                            @Override
//                            public void onResult(Status status) {
//                                if (status.isSuccess()){
//                                    gotoMainActivity();
//                                }else{
//                                    Toast.makeText(getApplicationContext(),"Session not close", Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        });
//            }
//        });
    }

    private void initData() {

        ChoosePlantTypeAdapter choosePlantTypeAdapter = new ChoosePlantTypeAdapter(ChooseMyPlantActivity.this,
                choosePlantTypeModelArrayList, ChoosePlantTypeAdapter.SELECT, new DataCallBack() {
            @Override
            public void Result(Object obj, String type, Object otherData) {
                ChoosePlantTypeModel choosePlantTypeModel = (ChoosePlantTypeModel) obj;

                if (otherData.equals("isChecked")){
//                    isChecked = true;
                    choosePlantTypeModel.isChecked = true;
                }

                if (type.equals("remove")) {
                    selectedPlantType.remove(choosePlantTypeModel);
                } else {
                    selectedPlantType.add(choosePlantTypeModel);
                }
                initSelectedAdapter();
            }
        });
        binding.rv.setAdapter(choosePlantTypeAdapter);

    }

    private void initSelectedAdapter() {
        SelectedPlantAdapter adapter = new SelectedPlantAdapter(selectedPlantType);
        binding.selectedRV.setAdapter(adapter);
    }

    private void sendPostToFirebase(ChoosePlantTypeModel choosePlantTypeModel) {

        String plantTypeId = fireStoreDB.collection(Constants.TYPE).document().getId(); // this is auto genrat

        Map<String, Object> postModelMap = new HashMap<>();
        postModelMap.put("plantTypeId", plantTypeId);
        postModelMap.put("userPlantTypeId", userModelData.user_id);
        postModelMap.put("choosenPlantWateringDayes", choosePlantTypeModel.choosenPlantWateringDayes);
//        postModelMap.put("choosenPlantCurrentTime", choosePlantTypeModel.choosenPlantCurrentTime);
        postModelMap.put("plantWater", choosePlantTypeModel.plantWater);
        postModelMap.put("plantSun", choosePlantTypeModel.plantSun);
        postModelMap.put("choosenPlantName", choosePlantTypeModel.choosenPlantName);
        postModelMap.put("isChecked", choosePlantTypeModel.isChecked);
        postModelMap.put("choosenPlantPhoto", choosePlantTypeModel.choosenPlantPhoto);

        fireStoreDB.collection(Constants.USER).document(userModelData.user_id).collection(Constants.TYPE)
                .document(plantTypeId).set(postModelMap, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        binding.loadingLY.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.success_add_plant), Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.fail_add_post), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}