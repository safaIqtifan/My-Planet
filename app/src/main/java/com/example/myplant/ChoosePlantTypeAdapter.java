package com.example.myplant;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplant.Model.ChoosePlantTypeModel;

import java.util.List;

public class ChoosePlantTypeAdapter extends RecyclerView.Adapter<ChoosePlantTypeAdapter.ViewHolder>{

    Activity activity;
    List<ChoosePlantTypeModel> list;
//    DataCallBack dataCallBack;

    public ChoosePlantTypeAdapter(Activity activity, List<ChoosePlantTypeModel> list) {
        this.activity = activity;
        this.list = list;
//        this.dataCallBack = callBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewHolder viewHolder = new ViewHolder(inflater.inflate(R.layout.item_my_plant, parent, false));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ChoosePlantTypeModel choosePlantTypeModel = list.get(position);
        Glide.with(activity).asBitmap().load(choosePlantTypeModel.choosenPlantPhoto).into(holder.plantImageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView plantImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            plantImageView = itemView.findViewById(R.id.plantImage);
        }
    }
}
