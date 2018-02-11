package com.axzae.homeassistant.util;

import android.app.FragmentManager;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.axzae.homeassistant.R;
import com.axzae.homeassistant.fragment.control.AlarmControlPanelFragment;
import com.axzae.homeassistant.fragment.control.AutomationFragment;
import com.axzae.homeassistant.fragment.control.CameraFragment;
import com.axzae.homeassistant.fragment.control.ClimateFragment;
import com.axzae.homeassistant.fragment.control.CoverFragment;
import com.axzae.homeassistant.fragment.control.FanFragment;
import com.axzae.homeassistant.fragment.control.GroupFragment;
import com.axzae.homeassistant.fragment.control.InputDateTimeFragment;
import com.axzae.homeassistant.fragment.control.InputSelectFragment;
import com.axzae.homeassistant.fragment.control.InputSliderFragment;
import com.axzae.homeassistant.fragment.control.InputTextFragment;
import com.axzae.homeassistant.fragment.control.LightFragment;
import com.axzae.homeassistant.fragment.control.MediaPlayerFragment;
import com.axzae.homeassistant.fragment.control.SensorFragment;
import com.axzae.homeassistant.fragment.control.SunFragment;
import com.axzae.homeassistant.fragment.control.SwitchFragment;
import com.axzae.homeassistant.fragment.control.VacuumFragment;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.shared.EntityProcessInterface;

public class EntityHandlerHelper {

    public static boolean onEntityClick(final EntityProcessInterface epi, final Entity entity) {
        boolean isConsumed = true;
        FragmentManager fragmentManager = epi.getFragmentManager();
        HomeAssistantServer server = epi.getServer();

        if (entity.isGroup()) {
            GroupFragment fragment = GroupFragment.newInstance(entity, server);
            fragment.show(fragmentManager, null);
        } else if (entity.isMediaPlayer()) {
            MediaPlayerFragment fragment = MediaPlayerFragment.newInstance(entity, server);
            fragment.show(fragmentManager, null);
        } else if (entity.isScript()) {
            epi.callService("homeassistant", "turn_on", new CallServiceRequest(entity.entityId));
        } else if (entity.isToggleable()) {
            epi.callService("homeassistant", entity.getNextState(), new CallServiceRequest(entity.entityId));
        } else if (entity.isScene()) {
            epi.callService(entity.getDomain(), "turn_on", new CallServiceRequest(entity.entityId));
        } else if (entity.isAlarmControlPanel()) {
            AlarmControlPanelFragment fragment = AlarmControlPanelFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isInputSelect()) {
            InputSelectFragment fragment = InputSelectFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isInputDateTime()) {
            InputDateTimeFragment fragment = InputDateTimeFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isInputText()) {
            InputTextFragment fragment = InputTextFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isInputSlider()) {
            InputSliderFragment fragment = InputSliderFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isSensor()) {
            SensorFragment fragment = SensorFragment.newInstance(entity, server);
            fragment.show(fragmentManager, null);
        } else if (entity.isSun()) {
            SunFragment fragment = SunFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isClimate()) {
            ClimateFragment fragment = ClimateFragment.newInstance(entity, server);
            fragment.show(fragmentManager, null);
        } else if (entity.isCamera()) {
            CameraFragment fragment = CameraFragment.newInstance(entity, server);
            fragment.show(fragmentManager, null);
        } else if (entity.isCover()) {
            CoverFragment fragment = CoverFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isVacuum()) {
            VacuumFragment fragment = VacuumFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isPersistentNotification()) {
            //Log.d("YouQi", "data.getString(2): " + cursor.getString(2));
            new MaterialDialog.Builder(epi.getActivityContext())
                    .cancelable(true)
                    .autoDismiss(true)
                    .content(entity.state)
                    .positiveText(R.string.action_ok)
                    .neutralText(R.string.action_dismiss)
                    .buttonRippleColorRes(R.color.md_grey_500)
                    .positiveColorRes(R.color.md_blue_500)
                    .neutralColorRes(R.color.md_blue_500)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            epi.callService(entity.getDomain(), "dismiss", new CallServiceRequest(entity.entityId));
                        }
                    })
                    .show();
        } else {
            isConsumed = false;
        }

        return isConsumed;
    }

    public static boolean onEntityLongClick(final EntityProcessInterface epi, final Entity entity) {
        boolean isConsumed = true;
        FragmentManager fragmentManager = epi.getFragmentManager();
        HomeAssistantServer server = epi.getServer();

        if (entity.isGroup()) {
            GroupFragment fragment = GroupFragment.newInstance(entity, server);
            fragment.show(fragmentManager, null);
        } else if (entity.isMediaPlayer()) {
            MediaPlayerFragment fragment = MediaPlayerFragment.newInstance(entity, server);
            fragment.show(fragmentManager, null);
        } else if (entity.isAutomation()) {
            AutomationFragment fragment = AutomationFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isSwitch()) {
            SwitchFragment fragment = SwitchFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isLight()) {
            LightFragment fragment = LightFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isScene()) {
            epi.callService(entity.getDomain(), "turn_on", new CallServiceRequest(entity.entityId));
        } else if (entity.isAlarmControlPanel()) {
            AlarmControlPanelFragment fragment = AlarmControlPanelFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isInputSelect()) {
            InputSelectFragment fragment = InputSelectFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isInputDateTime()) {
            InputDateTimeFragment fragment = InputDateTimeFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isInputText()) {
            InputTextFragment fragment = InputTextFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isInputSlider()) {
            InputSliderFragment fragment = InputSliderFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isSensor()) {
            SensorFragment fragment = SensorFragment.newInstance(entity, server);
            fragment.show(fragmentManager, null);
        } else if (entity.isSun()) {
            SunFragment fragment = SunFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isClimate()) {
            ClimateFragment fragment = ClimateFragment.newInstance(entity, server);
            fragment.show(fragmentManager, null);
        } else if (entity.isCamera()) {
            CameraFragment fragment = CameraFragment.newInstance(entity, server);
            fragment.show(fragmentManager, null);
        } else if (entity.isFan()) {
            FanFragment fragment = FanFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isCover()) {
            CoverFragment fragment = CoverFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else if (entity.isVacuum()) {
            VacuumFragment fragment = VacuumFragment.newInstance(entity);
            fragment.show(fragmentManager, null);
        } else {
            isConsumed = false;
        }

        return isConsumed;
    }
}