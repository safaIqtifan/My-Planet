package com.example.myplant.Model;

import java.io.Serializable;
import java.util.Date;

public class AddPlantModel implements Serializable {

    public String plant_id;
    public String userId;
    public String plantName;
    public String plantType;
    public int plantSun;
    public int plantWater;
    public int plantWateringDayes;
    public Date choosenPlantCurrentTime;
    public int plantTypePosition;
    public String plantPhoto;
    public String plantAge;
    public String plantDescription;
    public boolean plantIsFavourite = false;


}
