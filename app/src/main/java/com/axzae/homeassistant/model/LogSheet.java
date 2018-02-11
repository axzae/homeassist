package com.axzae.homeassistant.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class LogSheet {

    @SerializedName("domain")
    public String domain;

    @SerializedName("entity_id")
    public String entityId;

    @SerializedName("message")
    public String message;

    @SerializedName("name")
    public String name;

    @SerializedName("when")
    public Date when;

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (!(o instanceof LogSheet))
            return false;

        final LogSheet other = (LogSheet) o;
        return (this.entityId + this.when.getTime()).equals(other.entityId + other.when.getTime());
    }
}