package com.example.myplant.Model;

public class ChoosePlantTypeModel {

    public String plantTypeId;
    public String userPlantTypeId;
    public String choosenPlantPhoto;
    public boolean isChecked = false;
    public String choosenPlantName = "";

    public ChoosePlantTypeModel(){}

    public ChoosePlantTypeModel(String plantTypeId, String choosenPlantName, String choosenPlantPhoto) {
        this.plantTypeId = plantTypeId;
        this.choosenPlantPhoto = choosenPlantPhoto;
        this.choosenPlantName = choosenPlantName;
    }

    public String getPlantTypeId() {
        return plantTypeId;
    }

    public void setPlantTypeId(String plantTypeId) {
        this.plantTypeId = plantTypeId;
    }

    public String getChoosenPlantPhoto() {
        return choosenPlantPhoto;
    }

    public void setChoosenPlantPhoto(String choosenPlantPhoto) {
        this.choosenPlantPhoto = choosenPlantPhoto;
    }

    public String getChoosenPlantName() {
        return choosenPlantName;
    }

    public void setChoosenPlantName(String choosenPlantName) {
        this.choosenPlantName = choosenPlantName;
    }

}
