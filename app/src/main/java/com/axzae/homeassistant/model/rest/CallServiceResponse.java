package com.axzae.homeassistant.model.rest;

import com.axzae.homeassistant.model.Entity;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CallServiceResponse {

    @SerializedName("states")
    public ArrayList<Entity> states;

    public String toString() {
        return (new Gson()).toJson(this);
    }
}
