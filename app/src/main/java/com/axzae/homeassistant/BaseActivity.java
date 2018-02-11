package com.axzae.homeassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.axzae.homeassistant.provider.DatabaseManager;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public AppController getAppController() {
        return ((AppController) getApplication());
    }

    public void logOut() {

        SharedPreferences.Editor editor = getAppController().getSharedPref().edit();
        //editor.remove(ConnectActivity.EXTRA_FULL_URI);
        editor.remove(ConnectActivity.EXTRA_IPADDRESS);
        editor.remove(ConnectActivity.EXTRA_PASSWORD);
        //editor.remove(ConnectActivity.EXTRA_LAST_REQUEST);
        editor.apply();

        //Clear All Data in both tables.
        DatabaseManager.getInstance(this).clear();

        Intent i = new Intent(this, ConnectActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
}
