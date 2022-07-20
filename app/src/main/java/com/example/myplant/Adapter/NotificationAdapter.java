package com.example.myplant.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplant.Model.NotificationModel;
import com.example.myplant.R;
import com.example.myplant.databinding.ItemNotificationBinding;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    public ArrayList<NotificationModel> myArray;
    Activity context;

    public NotificationAdapter(Activity context, ArrayList<NotificationModel> myArray) {
        this.myArray = myArray;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(context), parent,
                false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        NotificationModel notificationModel = myArray.get(position);

        holder.binding.notificationTitleTV.setText(notificationModel.NotificationTitle);
        holder.binding.notificationBodyTV.setText(notificationModel.NotificationBody);

        Glide.with(context).asBitmap().load(notificationModel.NotificationIcon)
                .placeholder(R.drawable.camera).into(holder.binding.notificationImageIv);
    }

    @Override
    public int getItemCount() {
        return myArray.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

         ItemNotificationBinding binding;

        public MyViewHolder(@NonNull ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
