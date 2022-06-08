package com.example.myplant.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myplant.Model.UserModel;
import com.example.myplant.R;
import com.example.myplant.SigninActivity;
import com.example.myplant.classes.Constants;
import com.example.myplant.classes.UtilityApp;
import com.example.myplant.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseFirestore fireStoreDB;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fireStoreDB = FirebaseFirestore.getInstance();
        getUserImage();

        binding.logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), SigninActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        binding.profileImag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return root;
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

}