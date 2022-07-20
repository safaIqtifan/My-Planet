package com.example.myplant.Model;

import java.io.Serializable;

public class UserModel implements Serializable {

    public String user_id;
    public String fullName;
    public String password;
    public String email;
    public String gender;
    public String userImage = "";

    public UserModel() {
    }

    public UserModel(String name, String email, String gender) {
        this.fullName = name;
        this.email = email;
        this.gender = gender;
    }

}
