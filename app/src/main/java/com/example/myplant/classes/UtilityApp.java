package com.example.myplant.classes;

public class UtilityApp {

    public static void setUserData(String user) {
//        String userData = new Gson().toJson(user);
        RootApplication.getInstance().getSharedPManger().SetData(Constants.KEY_USER_NAME, user);
    }

    public static String getUserData() {
        String userName = RootApplication.getInstance().getSharedPManger().getDataString(Constants.KEY_USER_NAME);
        return userName;
    }

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
