package com.example.myplant.Model;

public class ChoosePlantTypeModel {

    public String plantTypeId;
    public String userPlantTypeId;
    public String choosenPlantPhoto;
    public boolean isChecked = false;
    public String choosenPlantName = "";
    public int choosenPlantWateringDayes;
    public int plantSun;
    public int plantWater;

    public ChoosePlantTypeModel() {
    }

    public ChoosePlantTypeModel(int choosenPlantWateringDayes, String choosenPlantName, int plantWater, int plantSun, String choosenPlantPhoto) {

        this.choosenPlantWateringDayes = choosenPlantWateringDayes;
        this.choosenPlantPhoto = choosenPlantPhoto;
        this.choosenPlantName = choosenPlantName;
        this.plantWater = plantWater;
        this.plantSun = plantSun;
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
