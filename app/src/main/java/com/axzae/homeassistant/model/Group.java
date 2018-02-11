package com.axzae.homeassistant.model;

import android.database.Cursor;

import com.axzae.homeassistant.util.CommonUtil;
import com.crashlytics.android.Crashlytics;
import com.google.gson.annotations.SerializedName;

public class Group extends Entity {

    @SerializedName("group_id")
    public Integer groupId;

    @SerializedName("sortKey")
    public Integer sortKey;

    @SerializedName("groupName")
    public String groupName;

    public static Group getInstance(Entity entity, int groupId) {
        Group group = CommonUtil.inflate(CommonUtil.deflate(entity), Group.class);
        group.groupId = groupId;
        return group;
    }

    public static Group getInstance(Cursor cursor) {
        Group entity = null;
        try {
            if ("".equals(cursor.getString(3))) {
                entity = new Group();
                entity.entityId = cursor.getString(1);
                entity.attributes = new Attribute();
                entity.attributes.friendlyName = cursor.getString(2);
                entity.groupId = cursor.getInt(0);
                entity.sortKey = cursor.getInt(cursor.getColumnIndex("SORT_KEY"));
            } else {
                entity = CommonUtil.inflate(cursor.getString(3), Group.class);
                entity.displayOrder = cursor.getInt(4);
                entity.groupId = cursor.getInt(0);
                entity.sortKey = cursor.getInt(cursor.getColumnIndex("SORT_KEY"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return entity;
    }

    @Override
    public String getFriendlyName() {
        return groupName != null ? groupName : super.getFriendlyName();
    }
}