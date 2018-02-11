package com.axzae.homeassistant.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ResultResponse {

    @SerializedName("id")
    public Integer id;

    @SerializedName("result")
    public ArrayList<Entity> result;

    @SerializedName("success")
    public Boolean success;

    @SerializedName("type")
    public String type;
}