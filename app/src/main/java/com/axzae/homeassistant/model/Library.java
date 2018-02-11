package com.axzae.homeassistant.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Library {

    public Library(String name, String author, String license, String website) {
        this.name = name;
        this.author = author;
        this.license = license;
        this.website = website;
    }

    @SerializedName("Name")
    public String name;

    @SerializedName("Author")
    public String author;

    @SerializedName("License")
    public String license;

    @SerializedName("Website")
    public String website;

    @Override
    public String toString() {
        return (new Gson()).toJson(this);
    }
}