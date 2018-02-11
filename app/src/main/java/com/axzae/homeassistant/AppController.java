package com.axzae.homeassistant;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import io.fabric.sdk.android.Fabric;

public class AppController extends Application {
    private static AppController mInstance;
    private static SharedPreferences sharedPref;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        if (BuildConfig.DEBUG) {
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build();
            mFirebaseRemoteConfig.setConfigSettings(configSettings);
        }
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        fetchFirebaseConfig();

        //String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //Crashlytics.log(Log.DEBUG, "YouQi", "");

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

    }

    private void fetchFirebaseConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds.
        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from the server.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        Log.d("YouQi", "cacheExpiration: " + cacheExpiration);
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            Log.d("YouQi", "Fetch Failed");
                        }
                    }
                });
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public SharedPreferences getSharedPref() {
        if (sharedPref == null) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return sharedPref;
    }
}