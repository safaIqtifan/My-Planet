package com.example.myplant.Model;

import java.io.Serializable;

public class UserModel implements Serializable {

    public String user_id;
    public String username;
    public String email;
    public String gender;

    public UserModel() {
    }

    public UserModel(String name, String email, String gender) {
        this.username = name;
        this.email = email;
        this.gender = gender;
    }

}
