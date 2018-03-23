package com.axzae.homeassistant.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.axzae.homeassistant.util.CommonUtil;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Entity {
    @SerializedName("entity_id")
    public String entityId;

    //on
    @SerializedName("state")
    public String state;

    //2017-08-14T15:50:46.810842+00:00
    @SerializedName("last_updated")
    public String lastUpdated;

    //2017-08-14T15:50:46.810842+00:00
    @SerializedName("last_changed")
    public String lastChanged;

    @SerializedName("attributes")
    public Attribute attributes;

    @SerializedName("checksum")
    public String checksum;

    @SerializedName("displayOrder")
    public Integer displayOrder;

    public int getDisplayOrder() {
        return (displayOrder == null) ? 1000 : displayOrder;
    }

    public static Entity getInstance(Cursor cursor) {
        Entity entity = null;
        try {
            entity = CommonUtil.inflate(cursor.getString(cursor.getColumnIndex("RAW_JSON")), Entity.class);
            entity.checksum = cursor.getString(cursor.getColumnIndex("CHECKSUM"));
            if (cursor.getColumnIndex("DISPLAY_ORDER") != -1) {
                entity.displayOrder = cursor.getInt(cursor.getColumnIndex("DISPLAY_ORDER"));
            }
        } catch (Exception e) {
            Log.d("YouQi", "Cursor: " + cursor.getString(cursor.getColumnIndex("RAW_JSON")));
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return entity;
    }

    public String getDomain() {
        return entityId.split("\\.")[0];
    }

    public int getDomainRanking() {
        if (isSun()) {
            return 0;
        } else if (isDeviceTracker()) {
            return 1;
        } else if (isSensor()) {
            return 2;
        } else if (isAnySensors()) {
            return 3;
        } else {
            return (int) getDomain().charAt(0);
        }
    }

    public String getFriendlyName() {
        return (attributes == null || attributes.friendlyName == null) ? "" : attributes.friendlyName;
    }


    public String getLastUpdated() {
        DateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");
        SimpleDateFormat widgetDateFormat = new SimpleDateFormat("HH:mm");
        if (lastUpdated == null) {
            return "";
        } else {
            try {
                Date parsed = iso8601DateFormat.parse(lastUpdated);

                return widgetDateFormat.format(parsed);
            } catch (java.text.ParseException e) {
                return "";
            }
        }
    }
    public boolean isHidden() {
        return attributes != null && attributes.hidden != null && attributes.hidden;
    }

    public boolean isSupported() {
        return getFriendlyState() != null && entityId != null && !getFriendlyName().equals("");
    }

    public boolean isDisplayTile() {
        return attributes.unitOfMeasurement != null;
    }

    public boolean isSwitch() {
        return entityId.startsWith("switch.");
    }

    public boolean isLight() {
        return entityId.startsWith("light.");
    }

    public boolean isFan() {
        return entityId.startsWith("fan.");
    }

    public boolean isCover() {
        return entityId.startsWith("cover.");
    }

    public boolean isVacuum() {
        return entityId.startsWith("vacuum.");
    }

    public boolean isMediaPlayer() {
        return entityId.startsWith("media_player.");
    }

    public boolean isDeviceTracker() {
        return entityId.startsWith("device_tracker.");
    }

    public boolean isZone() {
        return entityId.startsWith("zone.");
    }

    public boolean isSun() {
        return entityId.startsWith("sun.");
    }

    public boolean isSensor() {
        return entityId.startsWith("sensor.");
    }

    public boolean isClimate() {
        return entityId.startsWith("climate.");
    }

    public boolean isCamera() {
        return entityId.startsWith("camera.");
    }

    public boolean isAnySensors() {
        return entityId.contains("sensor.");
    }

    public boolean isGroup() {
        return entityId.startsWith("group.");
    }

    public boolean isAutomation() {
        return entityId.startsWith("automation.");
    }

    public boolean isScript() {
        return entityId.startsWith("script.");
    }

    public boolean isInputSelect() {
        return entityId.startsWith("input_select.");
    }

    public boolean isInputSlider() {
        return entityId.startsWith("input_slider.") || entityId.startsWith("input_number.");
    }

    public boolean isAlarmControlPanel() {
        return entityId.startsWith("alarm_control_panel.");
    }

    public boolean isScene() {
        return entityId.startsWith("scene.");
    }

    public boolean isInputBoolean() {
        return entityId.startsWith("input_boolean.");
    }

    public boolean isInputText() {
        return entityId.startsWith("input_text.");
    }

    public boolean isInputDateTime() {
        return entityId.startsWith("input_datetime.");
    }

    public boolean isPersistentNotification() {
        return entityId.startsWith("persistent_notification.");
    }

    public boolean isBinarySensor() {
        return entityId.startsWith("binary_sensor.");
    }

    public boolean hasIndicator() {
        //return !(entityId.startsWith("updater.") || entityId.startsWith("sun.") || isGroup() || isAnySensors());
        return isToggleable() && !isGroup();
    }

    public String getGroupName() {
        return (isSensor() && attributes.unitOfMeasurement != null) ? ((hasMdiIcon() ? (state + " ") : "") + attributes.unitOfMeasurement) : getFriendlyDomainName();
    }

    public boolean hasMdiIcon() {
        return attributes.icon != null && attributes.icon.startsWith("mdi:");
    }

    public String getFriendlyDomainName() {
        String domain = getDomain();
        if (isInputSelect()) {
            return "Input Select";
        }

        if (isInputSlider()) {
            return "Input Number";
        }

        if (isInputDateTime()) {
            return "Input DateTime";
        }

        if (isInputText()) {
            return "Input Text";
        }

        if (isInputBoolean()) {
            return "Input Boolean";
        }

        if (isMediaPlayer()) {
            return "Media Player";
        }

        if (isBinarySensor()) {
            return getFriendlyState();
        }

        if (isSun()) {
            return state.equals("above_horizon") ? "Above Horizon" : "Below Horizon";
        }

        if (isDeviceTracker()) {
            //return "Device Tracker";
            return state.equals("home") ? "Home" : "Away";
        }

        if (isAlarmControlPanel()) {
            return CommonUtil.getNameTitleCase(state.replace("_", " "));
        }

        if (isPersistentNotification()) {
            return "Notification";
        }

        return domain.substring(0, 1).toUpperCase() + domain.substring(1);
    }

    public boolean hasStateIcon() {
        if (isLight() || isSwitch() || isScript() || isAutomation() || isCamera() || isMediaPlayer() || isGroup() || isDeviceTracker() || isSun() || isFan() || isCover()) {
            return true;
        } else if (isBinarySensor() || isAlarmControlPanel() || isScene()) {
            return true;
        } else if (isInputBoolean() && hasMdiIcon()) {
            if (hasMdiIcon()) return true;
        } else if (hasMdiIcon() && !isAnySensors() && !isInputBoolean() && !isInputSelect() && !isInputSlider()) {
            return true;
        } else if (isSensor() && hasMdiIcon()) {
            return true;
        }
        return false;
    }

    public String getIconState() {

        if (hasMdiIcon() && !isInputBoolean() && !isInputSelect() && !isInputSlider()) {
            return MDIFont.getIcon(attributes.icon);
        }

        if (isAlarmControlPanel()) {
            switch (state) {
                case "armed_away":
                    return MDIFont.getIcon("mdi:pine-tree");
                case "disarmed":
                    return MDIFont.getIcon("mdi:bell-outline");
                case "armed_home":
                    return MDIFont.getIcon("mdi:home");
                case "pending":
                default:
                    return MDIFont.getIcon("mdi:alarm");
            }
        } else if (isScene()) {
            return MDIFont.getIcon("mdi:format-paint");
        } else if (isFan()) {
            return MDIFont.getIcon("mdi:fan");
        } else if (isCover()) {
            return MDIFont.getIcon("mdi:window-closed");
        } else if (isGroup()) {
            return MDIFont.getIcon("mdi:google-circles-communities");
        } else if (isLight()) {
            return MDIFont.getIcon(state.toUpperCase().equals("ON") ? "mdi:lightbulb" : "mdi:lightbulb-outline");
        } else if (isSun()) {
            return MDIFont.getIcon(state.toUpperCase().equals("ABOVE_HORIZON") ? "mdi:white-balance-sunny" : "mdi:brightness-3");
        } else if (isSwitch()) {
            return MDIFont.getIcon(state.toUpperCase().equals("ON") ? "mdi:toggle-switch" : "mdi:toggle-switch-off");
        } else if (isScript()) {
            return MDIFont.getIcon("mdi:code-braces");
        } else if (isCamera()) {
            return MDIFont.getIcon("mdi:camera");
        } else if (isMediaPlayer()) {
            return MDIFont.getIcon("mdi:cast");
        } else if (isDeviceTracker()) {
            return MDIFont.getIcon("mdi:radar");
        } else if (isAutomation()) {
            return MDIFont.getIcon("playlist-play");
        } else if (isBinarySensor()) {
            return hasMdiIcon() ? MDIFont.getIcon(attributes.icon) : MDIFont.getIcon("numeric-1-box-outline");
        } else if (isInputBoolean() && hasMdiIcon()) {
            return MDIFont.getIcon(attributes.icon);
        } else {
            return getFriendlyState();
        }
    }

    public String getMdiIcon() {
        if (hasMdiIcon()) {
            return MDIFont.getIcon(attributes.icon);
        } else if (isAlarmControlPanel()) {
            switch (state) {
                case "armed_away":
                    return MDIFont.getIcon("mdi:pine-tree");
                case "disarmed":
                    return MDIFont.getIcon("mdi:bell-outline");
                case "armed_home":
                    return MDIFont.getIcon("mdi:home");
                case "pending":
                default:
                    return MDIFont.getIcon("mdi:alarm");
            }
        } else if (isScene()) {
            return MDIFont.getIcon("mdi:format-paint");
        } else if (isFan()) {
            return MDIFont.getIcon("mdi:fan");
        } else if (isCover()) {
            return MDIFont.getIcon("mdi:window-closed");
        } else if (isGroup()) {
            return MDIFont.getIcon("mdi:google-circles-communities");
        } else if (isLight()) {
            return MDIFont.getIcon(state.toUpperCase().equals("ON") ? "mdi:lightbulb" : "mdi:lightbulb-outline");
        } else if (isSun()) {
            return MDIFont.getIcon(state.toUpperCase().equals("ABOVE_HORIZON") ? "mdi:white-balance-sunny" : "mdi:brightness-3");
        } else if (isSwitch()) {
            return MDIFont.getIcon(state.toUpperCase().equals("ON") ? "mdi:toggle-switch" : "mdi:toggle-switch-off");
        } else if (isScript()) {
            return MDIFont.getIcon("mdi:code-braces");
        } else if (isCamera()) {
            return MDIFont.getIcon("mdi:camera");
        } else if (isMediaPlayer()) {
            return MDIFont.getIcon("mdi:cast");
        } else if (isAutomation()) {
            return MDIFont.getIcon("playlist-play");
        } else if (isInputText()) {
            return MDIFont.getIcon("textbox");
        } else if (isInputDateTime()) {
            return MDIFont.getIcon("calendar-clock");
        } else if (isAnySensors()) {
            return MDIFont.getIcon("eye");
        } else if ("homeassistant".equals(getDomain())) {
            return MDIFont.getIcon("home");
        }
        return MDIFont.getIcon("mdi:information-outline");
    }

    public String getDeviceClassState() {
        String returnValue = null;
        if (attributes != null && attributes.deviceClass != null) {
            boolean isOff = !isCurrentStateActive();
            switch (attributes.deviceClass) {
                case "cold":
                    returnValue = isOff ? "Off" : "Cold";
                    break;
                case "connectivity":
                    returnValue = isOff ? "No Connection" : "Connection Present";
                    break;
                case "gas":
                    returnValue = isOff ? "Off" : "Gas Detected";
                    break;
                case "heat":
                    returnValue = isOff ? "Off" : "Hot";
                    break;
                case "light":
                    returnValue = isOff ? "Off" : "On";
                    break;
                case "moisture":
                    returnValue = isOff ? "Off" : "Wet";
                    break;
                case "motion":
                    returnValue = isOff ? "Clear" : "Detected";
                    break;
                case "moving":
                    returnValue = isOff ? "Stopped" : "Moving";
                    break;
                case "occupancy":
                    returnValue = isOff ? "Not Occupied" : "Occupied";
                    break;
                case "opening":
                    returnValue = isOff ? "Closed" : "Open";
                    break;
                case "plug":
                    returnValue = isOff ? "Off" : "On";
                    break;
                case "power":
                    returnValue = isOff ? "Off" : "On";
                    break;
                case "safety":
                    returnValue = isOff ? "Unsafe" : "Safe";
                    break;
                case "smoke":
                    returnValue = isOff ? "Off" : "Smoke Detected";
                    break;
                case "sound":
                    returnValue = isOff ? "No Sound" : "Sound Detected";
                    break;
                case "vibration":
                    returnValue = isOff ? "No Vibration" : "Vibration Detected";
                    break;
            }
        }

        return returnValue;
    }

    public String getFriendlyState() {

        String friendlyState = state;
        String deviceClass = getDeviceClassState();
        if (deviceClass != null) {
            friendlyState = deviceClass;
        } else if (isAlarmControlPanel()) {
            switch (state) {
                case "armed_away":
                    friendlyState = "Armed Away";
                    break;
                case "disarmed":
                    friendlyState = "Disarmed";
                    break;
                case "armed_home":
                    friendlyState = "Armed Home";
                    break;
                case "pending":
                default:
                    friendlyState = "Pending";
                    break;
            }
        } else if (isSwitch() || isLight() || isAutomation() || isBinarySensor() || isScript() || isInputBoolean() || isMediaPlayer() || isGroup()) {
            friendlyState = friendlyState.toUpperCase();
        }

        return friendlyState;
    }

    public boolean isCurrentStateActive() {
        return "ON".equals(state.toUpperCase());
    }

    public String getFriendlyStateRow() {
        String returnValue = getFriendlyState();

        if (isAnySensors()) {
            if (attributes != null && attributes.unitOfMeasurement != null) {
                return String.format(Locale.ENGLISH, "%s %s", state, attributes.unitOfMeasurement);
            }
        }
        return returnValue;
    }

    public boolean isActivated() {
        if (isMediaPlayer()) {
            return !state.toUpperCase().equals("OFF");
        } else if (isSun()) {
            return false; //state.toUpperCase().equals("ABOVE_HORIZON");
        } else if (isDeviceTracker()) {
            return state.toUpperCase().equals("HOME");
        }
        return state.toUpperCase().equals("ON");
    }

    public ContentValues getContentValues(boolean withId) {
        String rawJson = CommonUtil.deflate(this);
        ContentValues initialValues = new ContentValues();
        if (withId) initialValues.put("ENTITY_ID", entityId);
        initialValues.put("FRIENDLY_NAME", getFriendlyName());
        initialValues.put("DOMAIN", getDomain());
        initialValues.put("RAW_JSON", rawJson);
        initialValues.put("CHECKSUM", CommonUtil.md5(rawJson));
        return initialValues;
    }

    public String getNextState() {
        return "turn_" + (isCurrentStateActive() ? "off" : "on");
    }

    public ContentValues getContentValues() {
        return getContentValues(true);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (!(o instanceof Entity))
            return false;

        final Entity other = (Entity) o;
//        if (this.checksum == null || other.checksum == null) {
//            throw new RuntimeException("checksum is null");
//        }
        return this.entityId.equals(other.entityId);
    }

    public boolean isToggleable() {
        return isSwitch() || isLight() || isAutomation() || isScript() || isInputBoolean() || isGroup() || isFan() || isVacuum(); //|| isMediaPlayer()
    }

    public boolean isCircle() {
        return isSensor() || isSun() || isAnySensors() || isDeviceTracker() || isAlarmControlPanel();
    }

    public LatLng getLocation() {
        LatLng result = null;
        if (isDeviceTracker() || isZone())
            if (attributes != null && attributes.latitude != null && attributes.longitude != null) {
                result = new LatLng(attributes.latitude.floatValue(), attributes.longitude.floatValue());
            }
        return result;
    }
}