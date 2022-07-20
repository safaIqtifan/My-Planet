package com.example.myplant.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplant.Model.ChoosePlantTypeModel;
import com.example.myplant.R;
import com.example.myplant.classes.DataCallBack;

import java.util.List;

public class ChoosePlantTypeAdapter extends RecyclerView.Adapter<ChoosePlantTypeAdapter.ViewHolder> {

    Activity activity;
    List<ChoosePlantTypeModel> list;
    DataCallBack dataCallBack;
    int clickType;
    public static int NO_CLICK = 0, SELECT = 1, CLICK = 2;

    public ChoosePlantTypeAdapter(Activity activity, List<ChoosePlantTypeModel> list, int clickType, DataCallBack callBack) {
        this.activity = activity;
        this.list = list;
        this.dataCallBack = callBack;
        this.clickType = clickType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewHolder viewHolder = new ViewHolder(inflater.inflate(R.layout.item_choosen_plant, parent, false));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ChoosePlantTypeModel choosePlantTypeModel = list.get(position);
        Glide.with(activity).asBitmap().load(choosePlantTypeModel.getChoosenPlantPhoto()).placeholder(R.drawable.planttype).into(holder.plantImageView);

        if (choosePlantTypeModel.isChecked) {
//            choosePlantTypeModel.isChecked = false;
            holder.checkImage.setImageResource(R.drawable.checked);

        } else {
//            choosePlantTypeModel.isChecked = true;
            holder.checkImage.setImageResource(R.drawable.unchecked);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView plantImageView;
        ImageView checkImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            plantImageView = itemView.findViewById(R.id.plantImage);
            checkImage = itemView.findViewById(R.id.checkImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ChoosePlantTypeModel choosePlantTypeModel = list.get(getAdapterPosition());
                    if (clickType == SELECT) {
                        if (choosePlantTypeModel.isChecked) {
                            choosePlantTypeModel.isChecked = false;
                            dataCallBack.Result(choosePlantTypeModel, "remove", "");

                        } else {
                            choosePlantTypeModel.isChecked = true;
                            dataCallBack.Result(choosePlantTypeModel, "add", "isChecked");
//                            checkImage.setImageResource(R.drawable.unchecked);
                        }
                        notifyItemChanged(getAdapterPosition());
                    }
                }
            });
        }
    }
}
