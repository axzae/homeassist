package com.axzae.homeassistant.model;

import com.google.gson.annotations.SerializedName;

public class ErrorMessage {

    public static final int ERRORTYPE_DIALOG = 1;
    public static final int ERRORTYPE_TOAST = 2;
    public static final int ERRORTYPE_SNACKBAR = 3;
    public static final int ERRORTYPE_TEXT_INPUT = 4;

    public ErrorMessage(String title, Throwable throwable) {
        this(title, throwable.getMessage(), ERRORTYPE_DIALOG);
        this.throwable = throwable;
    }

    public ErrorMessage(String title, String message) {
        this(title, message, ERRORTYPE_DIALOG);
    }

    public ErrorMessage(String title, String message, int type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.throwable = null;
    }

    @SerializedName("throwable")
    public Throwable throwable;

    @SerializedName("type")
    public int type;

    @SerializedName("title")
    public String title;

    @SerializedName("message")
    public String message;

    public String getFirebaseMessage() {
        String response = "";
        if (title != null) {
            response = title.substring(0, title.length() < 100 ? title.length() : 100);
        }
        return response;
    }
}
