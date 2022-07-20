package com.example.myplant.classes;

import com.example.myplant.Model.UserModel;
import com.google.gson.Gson;

public class UtilityApp {

    public static void setUserData(UserModel user) {
        String userData = new Gson().toJson(user);
        RootApplication.getInstance().getSharedPManger().SetData(Constants.KEY_USER_MEMBER, userData);
    }

    public static UserModel getUserData() {
        String userJsonData = RootApplication.getInstance().getSharedPManger().getDataString(Constants.KEY_USER_MEMBER);
        return new Gson().fromJson(userJsonData, UserModel.class);
    }

//    public static void setFavouriteData(AddPlantModel favourite) {
//        String favouriteData = new Gson().toJson(favourite);
//        RootApplication.getInstance().getSharedPManger().SetData(Constants.KEY_favourite, favouriteData);
//    }

//    public static AddPlantModel getFavouriteData() {
//        String favouriteJsonData = RootApplication.getInstance().getSharedPManger().getDataString(Constants.KEY_favourite);
//        return new Gson().fromJson(favouriteJsonData, AddPlantModel.class);
//    }
//
    public static void setIsNotificationEnabled(boolean isEnabled) {
        RootApplication.getInstance().getSharedPManger().SetData(Constants.KEY_is_notification_enabled, isEnabled);
    }
    public static boolean getNotificationEnabled() {
        return RootApplication.getInstance().getSharedPManger().getDataBool(Constants.KEY_is_notification_enabled, true);
    }
//
//    public static AddPlantModel getFavouriteData() {
//        String favouriteJsonData = RootApplication.getInstance().getSharedPManger().getDataString(Constants.KEY_favourite);
//        return new Gson().fromJson(favouriteJsonData, AddPlantModel.class);
//    }

//    public static void setUserData(UserModel user) {
//        String userData = new Gson().toJson(user);
//        RootApplication.getInstance().getSharedPManger().SetData(Constants.KEY_MEMBER, userData);
//    }
//
//    public static UserModel getUserData() {
//        String userJsonData = RootApplication.getInstance().getSharedPManger().getDataString(Constants.KEY_MEMBER);
//        return new Gson().fromJson(userJsonData, UserModel.class);
//    }

}
