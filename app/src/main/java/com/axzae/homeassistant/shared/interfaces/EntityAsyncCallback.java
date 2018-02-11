package com.axzae.homeassistant.shared.interfaces;

import com.axzae.homeassistant.model.Entity;

import java.util.ArrayList;

public interface EntityAsyncCallback {
    void onFinished(ArrayList<Entity> entities);
}
