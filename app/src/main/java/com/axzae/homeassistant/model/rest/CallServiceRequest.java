package com.axzae.homeassistant.model.rest;

import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class CallServiceRequest {

    public CallServiceRequest(String entityId) {
        this.entityId = entityId;
    }

    @SerializedName("entity_id")
    public String entityId;

    @SerializedName("option")
    public String option;

    @SerializedName("value")
    public String value;

    @SerializedName("code")
    public String code;

    @SerializedName("rgb_color")
    public Integer[] rgbColor;

    @SerializedName("volume_level")
    public BigDecimal volumeLevel;

    @SerializedName("brightness")
    public Integer brightness;

    @SerializedName("color_temp")
    public Integer colorTemp;

    @SerializedName("temperature")
    public BigDecimal temperature;

    @SerializedName("is_volume_muted")
    public Boolean isVolumeMuted;

    @SerializedName("date")
    public String date;

    @SerializedName("speed")
    public String speed;

    @SerializedName("fan_speed")
    public String fanSpeed;

    @SerializedName("time")
    public String time;

    @SerializedName("message")
    public String message;

    public CallServiceRequest setOption(String option) {
        this.option = option;
        return this;
    }

    public CallServiceRequest setSpeed(String speed) {
        this.speed = speed;
        return this;
    }

    public CallServiceRequest setFanSpeed(String fanSpeed) {
        this.fanSpeed = fanSpeed;
        return this;
    }

    public CallServiceRequest setValue(String value) {
        this.value = value;
        return this;
    }

    public CallServiceRequest setCode(String code) {
        this.code = code;
        return this;
    }

    public CallServiceRequest setDate(String date) {
        this.date = date;
        return this;
    }

    public CallServiceRequest setTime(String time) {
        this.time = time;
        return this;
    }

    public CallServiceRequest setVolumeMute(Boolean value) {
        this.isVolumeMuted = value;
        return this;
    }

    public CallServiceRequest setVolumeLevel(BigDecimal value) {
        this.volumeLevel = value;
        return this;
    }

    public CallServiceRequest setTemperature(BigDecimal value) {
        this.temperature = value;
        return this;
    }

    public CallServiceRequest setMessage(String message) {
        this.message = message;
        return this;
    }

    public CallServiceRequest setColorTemperature(Integer value) {
        this.colorTemp = value;
        return this;
    }

    public CallServiceRequest setBrightness(Integer value) {
        this.brightness = value;
        return this;
    }


    public CallServiceRequest setRGBColor(int rgbColor) {
        this.rgbColor = new Integer[]{Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor)};
        return this;
    }

    public String toString() {
        return (new Gson()).toJson(this);
    }
}
