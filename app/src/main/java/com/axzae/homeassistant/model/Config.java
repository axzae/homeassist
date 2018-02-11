package com.axzae.homeassistant.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Config {
    @SerializedName("components")
    public ArrayList<String> components;

    @SerializedName("config_dir")
    public String config_dir;

    @SerializedName("location_name")
    public String location_name;

    @SerializedName("latitude")
    public Double latitude;

    @SerializedName("longitude")
    public Double longitude;

    @SerializedName("time_zone")
    public String time_zone;

    @SerializedName("unit_system")
    public UnitSystem unit_system;

    private class UnitSystem {
        @SerializedName("length")
        public String length;

        @SerializedName("mass")
        public String mass;

        @SerializedName("temperature")
        public String temperature;

        @SerializedName("volume")
        public String volume;
    }
}