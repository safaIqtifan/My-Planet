package com.example.myplant.Model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ArticalModel {

    public String articalId;
    public Map<String, Boolean> userId = new HashMap<>();
    public String articalName;
    public String articalType;
    public String articalImage;
    public String articalColor;
    public String articalTextColor;
    public String articalDescription;
    public String articalTime;
    public Date createdAt;
    public boolean articalIsFavourite = false;

}
