package com.example.myplant.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplant.Model.ArticalModel;
import com.example.myplant.R;
import com.example.myplant.classes.DataCallBack;
import com.example.myplant.databinding.ItemArticalBinding;

import java.util.ArrayList;

public class ArticalAdapter extends RecyclerView.Adapter<ArticalAdapter.MyViewHolder> {

    public ArrayList<ArticalModel> myArray;
    Activity context;
    DataCallBack callBack;
    String type;

    public ArticalAdapter(Activity context, ArrayList<ArticalModel> myArray, String type, DataCallBack callBack) {
        this.myArray = myArray;
        this.context = context;
        this.callBack = callBack;
        this.type = type;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemArticalBinding binding = ItemArticalBinding.inflate(LayoutInflater.from(context), parent,
                false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ArticalModel articalModel = myArray.get(position);

        int articalColor = Color.parseColor(articalModel.articalColor);
        int articalTextColor = Color.parseColor(articalModel.articalTextColor);

        holder.binding.articalName.setText(articalModel.articalName);
        holder.binding.articalType.setText(articalModel.articalType);
        holder.binding.articalBackground.setBackgroundColor(articalColor);
        holder.binding.articalType.setTextColor(articalTextColor);

        Glide.with(context).asBitmap().load(articalModel.articalImage)
                .placeholder(R.drawable.camera).into(holder.binding.articalImage);
    }

    @Override
    public int getItemCount() {
        return myArray.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ItemArticalBinding binding;

        public MyViewHolder(@NonNull ItemArticalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ArticalModel articalModel = myArray.get(getAdapterPosition());
                    callBack.Result(articalModel, "", getAdapterPosition());

                }
            });


//            Random rnd = new Random();
//            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//            binding.articalBackground.setBackgroundColor(color);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });

        }
    }
}
