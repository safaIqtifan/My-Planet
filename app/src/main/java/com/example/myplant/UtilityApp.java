//package com.example.myplant;
//
//import com.example.myplant.Model.UserModel;
//import com.example.myplant.classes.Constants;
//import com.example.myplant.classes.RootApplication;
//
//public class UtilityApp {
//
//    public static void setUserData(UserModel user) {
//        String userData = new Gson().toJson(user);
//        RootApplication.getInstance().getSharedPManger().SetData(Constants.KEY_MEMBER, userData);
//    }
//
//    public static UserModel getUserData() {
//        String userJsonData = RootApplication.getInstance().getSharedPManger().getDataString(Constants.KEY_MEMBER);
//        return new Gson().fromJson(userJsonData, UserModel.class);
//    }
//
//}
