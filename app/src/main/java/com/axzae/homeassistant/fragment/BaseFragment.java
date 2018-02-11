package com.axzae.homeassistant.fragment;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.axzae.homeassistant.AppController;
import com.axzae.homeassistant.MainActivity;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.crashlytics.android.Crashlytics;

public class BaseFragment extends Fragment {

    public void setSubtitle(String title) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(title);
            }
        }
    }

    public AppController getAppController() {
        Application app = getActivity().getApplication();
        if (app instanceof AppController) {
            return (AppController) app;
        }
        throw new RuntimeException("Unsupported Activity");
    }

    public void callService(final String domain, final String service, CallServiceRequest serviceRequest) {
        Activity app = getActivity();
        if (app instanceof MainActivity) {
            ((MainActivity) app).callService(domain, service, serviceRequest);
            return;
        }
        throw new RuntimeException("Unsupported Activity");
    }

    public boolean isActiveFragment() {
        Activity app = getActivity();
        if (app instanceof MainActivity) {
            return ((MainActivity) app).getCurrentEntityFragment() == this;
        }

        if (app == null) {
            Crashlytics.logException(new RuntimeException("Null Activity"));
        }
        return false;
        //throw new RuntimeException("Unsupported Activity");
    }

    public void showToast(String message) {
        Activity app = getActivity();
        if (app instanceof MainActivity) {
            ((MainActivity) app).showToast(message);
            return;
        }
        throw new RuntimeException("Unsupported Activity");
    }

    public HomeAssistantServer getServer() {
        Activity app = getActivity();
        if (app instanceof MainActivity) {
            return ((MainActivity) app).getServer();
        }
        throw new RuntimeException("Unsupported Activity");
    }

}

