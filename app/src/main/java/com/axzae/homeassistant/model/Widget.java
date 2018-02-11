package com.axzae.homeassistant.model;

import android.database.Cursor;

import com.axzae.homeassistant.util.CommonUtil;
import com.crashlytics.android.Crashlytics;
import com.google.gson.annotations.SerializedName;

public class Widget extends Entity {

    @SerializedName("appWidgetId")
    public Integer appWidgetId;

    public static Widget getInstance(Entity entity, int appWidgetId) {
        Widget widget = CommonUtil.inflate(CommonUtil.deflate(entity), Widget.class);
        widget.appWidgetId = appWidgetId;
        return widget;
    }

    public static Widget getInstance(Cursor cursor) {
        Widget widget = CommonUtil.inflate(cursor.getString(cursor.getColumnIndex("RAW_JSON")), Widget.class);
        return widget;
    }

}