package com.axzae.homeassistant.model;

import android.net.Uri;

import com.axzae.homeassistant.helper.AlwaysListTypeAdapterFactory;
import com.crashlytics.android.Crashlytics;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Attribute {
    @JsonAdapter(AlwaysListTypeAdapterFactory.class)
    @SerializedName("entity_id")
    public ArrayList<String> entityIds;
//    public String entityId;

    @SerializedName("next_dawn")
    public String nextDawn;

    @SerializedName("next_dusk")
    public String nextDusk;

    @SerializedName("next_midnight")
    public String nextMidnight;

    @SerializedName("next_noon")
    public String nextNoon;

    @SerializedName("next_rising")
    public String nextRising;

    @SerializedName("next_setting")
    public String nextSetting;

    @SerializedName("speed_list")
    public ArrayList<String> speedList;

    @SerializedName("fan_speed_list")
    public ArrayList<String> fanSpeedList;

    @SerializedName("assumed_state")
    public String assumedState;

    @SerializedName("friendly_name")
    public String friendlyName;

    @SerializedName("app_name")
    public String appName;

    @SerializedName("is_volume_muted")
    public Boolean isVolumeMuted;

    @SerializedName("volume_level")
    public BigDecimal volumeLevel;

    @SerializedName("entity_picture")
    public String entityPicture;

    @SerializedName("icon")
    public String icon;

    @SerializedName("title")
    public String title;


    @SerializedName("year")
    public Integer year;

    @SerializedName("month")
    public Integer month;

    @SerializedName("day")
    public Integer day;

    @SerializedName("hour")
    public Integer hour;

    @SerializedName("minute")
    public Integer minute;

    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName("has_date")
    public Boolean hasDate;

    @SerializedName("has_time")
    public Boolean hasTime;

    @SerializedName("device_class")
    public String deviceClass;

    @SerializedName("brightness")
    public BigDecimal brightness;

    @SerializedName("color_temp")
    public BigDecimal colorTemp;

    @SerializedName("rgb_color")
    public ArrayList<BigDecimal> rgbColors;

    @SerializedName("options")
    public ArrayList<String> options;

    @SerializedName("order")
    public Integer order;

    @SerializedName("auto")
    public Boolean auto;

    @SerializedName("hidden")
    public Boolean hidden;

    @SerializedName("view")
    public Boolean view;

    @SerializedName("name")
    public String name;

    @SerializedName("initial")
    public Integer initial;

    @SerializedName("code_format")
    public String codeFormat;

    @SerializedName("pattern")
    public String pattern;

    @SerializedName("max")
    public BigDecimal max;

    @SerializedName("min")
    public BigDecimal min;

    @SerializedName("step")
    public BigDecimal step;

    @SerializedName("current_temperature")
    public BigDecimal currentTemperature;

    @SerializedName("max_temp")
    public BigDecimal maxTemp;

    @SerializedName("min_temp")
    public BigDecimal minTemp;

    @SerializedName("temperature")
    public String temperature;

    @SerializedName("operation_mode")
    public String operationMode;


    @SerializedName("activity")
    public String activity;

    @SerializedName("provider")
    public String provider;

    @SerializedName("source_type")
    public String sourceType;

    //Can be "High"(String) or Number
    @SerializedName("battery")
    public String battery;

    @SerializedName("gps_accuracy")
    public BigDecimal gpsAccuracy;

    @SerializedName("altitude")
    public BigDecimal altitude;

    @SerializedName("latitude")
    public BigDecimal latitude;

    @SerializedName("longitude")
    public BigDecimal longitude;

    @SerializedName("radius")
    public BigDecimal radius;

    //https://home-assistant.io/blog/2017/08/12/release-51/#release-0512---august-14
    @SerializedName("release_notes")
    public Boolean releaseNotes;

    @SerializedName("unit_of_measurement")
    public String unitOfMeasurement;

    public int getNumberOfDecimalPlaces() {
        String string = step.stripTrailingZeros().toPlainString();
        int index = string.indexOf(".");
        return index < 0 ? 0 : string.length() - index - 1;
    }

    public Uri getEntityPictureUri() {
        return Uri.parse(entityPicture.startsWith("//") ? ("http:" + entityPicture) : entityPicture);
    }

    public Long getTimestampForInputDateTime() {
        return timestamp == null ? null : (new BigDecimal(timestamp).longValue());
    }

    public BigDecimal getTemperature() {
        Crashlytics.log("temperature: " + temperature);
        if (temperature == null) {
            return null;
        }
        return new BigDecimal(temperature);
    }

    public boolean isView() {
        return view != null && view;
    }

}