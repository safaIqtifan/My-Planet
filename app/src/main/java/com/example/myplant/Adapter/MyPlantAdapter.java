package com.example.myplant.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplant.Model.AddPlantModel;
import com.example.myplant.R;
import com.example.myplant.classes.DataCallBack;
import com.example.myplant.databinding.ItemMyplantBinding;

import java.util.ArrayList;

public class MyPlantAdapter extends RecyclerView.Adapter<MyPlantAdapter.MyViewHolder> {

    public ArrayList<AddPlantModel> myArray;
    Activity context;
    DataCallBack callBack;

    public MyPlantAdapter(Activity context, ArrayList<AddPlantModel> myArray, DataCallBack callBack) {
        this.myArray = myArray;
        this.context = context;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        Context context = parent.getContext();
//        LayoutInflater inflater = LayoutInflater.from(context);
//
//        View chosenView = inflater.inflate(R.layout.item_myplant, parent, false);
//        MyViewHolder viewHolder = new MyViewHolder(chosenView);
        ItemMyplantBinding binding = ItemMyplantBinding.inflate(LayoutInflater.from(context), parent,
                false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        AddPlantModel addPlantModel = myArray.get(position);

        holder.binding.plantNameTV.setText(addPlantModel.plantName);
        holder.binding.plantTypeTV.setText(addPlantModel.plantType);
        holder.binding.plantAgeTv.setText(addPlantModel.plantAge);

        Glide.with(context).asBitmap().load(addPlantModel.plantPhoto).placeholder(R.drawable.camera).into(holder.binding.plantImageIv);
    }

    @Override
    public int getItemCount() {
        return myArray.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ItemMyplantBinding binding;
//        ImageView plantImageIv;
//        TextView plantNameEd;
//        TextView plantTypeTV;
//        TextView plantAgeTv;

        public MyViewHolder(@NonNull ItemMyplantBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

//            plantImageIv = itemView.findViewById(R.id.plantImageIv);
//            plantNameEd = itemView.findViewById(R.id.plantNameTV);
//            plantTypeTV = itemView.findViewById(R.id.plantTypeTV);
//            plantAgeTv = itemView.findViewById(R.id.plantAgeTv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AddPlantModel addPlantModel = myArray.get(getAdapterPosition());
                    callBack.Result(addPlantModel, "", getAdapterPosition());

                }
            });

        }
    }
}
