package com.axzae.homeassistant.model.rest;

import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.Group;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RxPayload {

    public static RxPayload getInstance(String event) {
        RxPayload payload = new RxPayload();
        payload.event = event;
        return payload;
    }

    @SerializedName("event")
    public String event;

    @SerializedName("group")
    public Group group;

    @SerializedName("entity")
    public Entity entity;

    @SerializedName("entities")
    public ArrayList<Entity> entities;

    public String toString() {
        return (new Gson()).toJson(this);
    }
}
