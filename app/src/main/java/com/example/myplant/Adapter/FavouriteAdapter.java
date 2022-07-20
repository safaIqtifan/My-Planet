package com.example.myplant.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplant.Model.AddPlantModel;
import com.example.myplant.Model.ArticalModel;
import com.example.myplant.R;
import com.example.myplant.classes.DataCallBack;
import com.example.myplant.databinding.ItemMyarticalBinding;
import com.example.myplant.databinding.ItemMyplantBinding;

import java.util.ArrayList;

public class FavouriteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Object> favouriteArray;
    Activity context;
    DataCallBack callBack;

//    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_plant = 1;
    private final int VIEW_TYPE_artical = 2;

    public FavouriteAdapter(ArrayList<Object> favouriteArray, Activity context, DataCallBack callBack) {
        this.favouriteArray = favouriteArray;
        this.context = context;
        this.callBack = callBack;
    }

    @Override
    public int getItemViewType(int position) {

        if (favouriteArray.get(position) instanceof AddPlantModel) {
            return VIEW_TYPE_plant;
        } else if (favouriteArray.get(position) instanceof ArticalModel) {
            return VIEW_TYPE_artical;
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_plant) {
            ItemMyplantBinding binding = ItemMyplantBinding.inflate(LayoutInflater.from(context), parent,
                    false);
            return new FavouriteAdapter.MyPlantViewHolder(binding);
        } else {
            ItemMyarticalBinding binding = ItemMyarticalBinding.inflate(LayoutInflater.from(context), parent,
                    false);
            return new FavouriteAdapter.MyArticalViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == VIEW_TYPE_plant) {

            MyPlantViewHolder myPlantViewHolder = (MyPlantViewHolder) holder;
            AddPlantModel addPlantModel = (AddPlantModel) favouriteArray.get(position);

            myPlantViewHolder.binding.plantNameTV.setText(addPlantModel.plantName);
            myPlantViewHolder.binding.plantTypeTV.setText(addPlantModel.plantType);
            myPlantViewHolder.binding.plantAgeTv.setText(addPlantModel.plantAge);

            Glide.with(context).asBitmap().load(addPlantModel.plantPhoto)
                    .placeholder(R.drawable.camera).into(myPlantViewHolder.binding.plantImageIv);

        } else if (getItemViewType(position) == VIEW_TYPE_artical) {

            MyArticalViewHolder myarticalViewHolder = (MyArticalViewHolder) holder;
            ArticalModel articalModel = (ArticalModel) favouriteArray.get(position);

            myarticalViewHolder.binding.articalNameTV.setText(articalModel.articalName);
            myarticalViewHolder.binding.articalTypeTV.setText(articalModel.articalType);
            myarticalViewHolder.binding.articalCreatedAtTv.setText(articalModel.articalTime);

            Glide.with(context).asBitmap().load(articalModel.articalImage)
                    .placeholder(R.drawable.camera).into(myarticalViewHolder.binding.articalImageIv);

        }

    }

    @Override
    public int getItemCount() {
        return favouriteArray.size();
    }

    public class MyPlantViewHolder extends RecyclerView.ViewHolder {

        ItemMyplantBinding binding;

        public MyPlantViewHolder(@NonNull ItemMyplantBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AddPlantModel addPlantModel = (AddPlantModel) favouriteArray.get(getAdapterPosition());
                    callBack.Result(addPlantModel, "VIEW_TYPE_plant", getAdapterPosition());

                }
            });

        }
    }

    public class MyArticalViewHolder extends RecyclerView.ViewHolder {

        ItemMyarticalBinding binding;

        public MyArticalViewHolder(@NonNull ItemMyarticalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ArticalModel articalModel = (ArticalModel) favouriteArray.get(getAdapterPosition());
                    callBack.Result(articalModel, "VIEW_TYPE_artical", getAdapterPosition());

                }
            });
        }
    }


}
