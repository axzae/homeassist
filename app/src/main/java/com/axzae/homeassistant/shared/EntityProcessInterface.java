package com.axzae.homeassistant.shared;

import android.app.FragmentManager;
import android.content.Context;

import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.rest.CallServiceRequest;

public interface EntityProcessInterface {
    void callService(final String domain, final String service, CallServiceRequest serviceRequest);

    FragmentManager getFragmentManager();

    HomeAssistantServer getServer();

    Context getActivityContext();

    void showToast(String message);
}
