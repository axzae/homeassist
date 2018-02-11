package com.axzae.homeassistant.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.axzae.homeassistant.AboutActivity;
import com.axzae.homeassistant.BuildConfig;
import com.axzae.homeassistant.ChangelogActivity;
import com.axzae.homeassistant.LibraryActivity;
import com.axzae.homeassistant.R;
import com.axzae.homeassistant.SettingsActivity;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    int countDown = 5;
    SettingsActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mActivity = (SettingsActivity) getActivity();
        addPreferencesFromResource(R.xml.preferences);

        Preference pref = findPreference("version");
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String summary = String.format(Locale.ENGLISH, "v%s (%s)", packageInfo.versionName, packageInfo.versionCode);
            pref.setSummary(summary);
        } catch (Exception e) {
            pref.setSummary("Failed to determine app version");
        }

        Preference buildPref = findPreference("build");
        try {
            buildPref.setSummary(BuildConfig.BUILD_TYPE + " \u2022 " + BuildConfig.buildTime);

            buildPref.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(android.support.v7.preference.Preference preference) {
                    if (--countDown <= 0) {
                        preference.setSummary(BuildConfig.FLAVOR + " \u2022 " + BuildConfig.BUILD_TYPE + " \u2022 " + BuildConfig.buildTime);
                    }
                    return false;
                }
            });


        } catch (Exception e) {
            buildPref.setSummary("Failed to determine build_date");
        }

        Preference aboutPref = findPreference("about");
        aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), AboutActivity.class);
                startActivity(new Intent(getActivity(), AboutActivity.class));
                getActivity().overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
                return false;
            }
        });

        Preference changelogPref = findPreference("changelog");
        changelogPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), ChangelogActivity.class);
                startActivity(new Intent(getActivity(), ChangelogActivity.class));
                getActivity().overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
                return false;
            }
        });

        Preference recommendPref = findPreference("recommend");
        recommendPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey, check out HomeAssist android app at: https://goo.gl/5rkPnP #homeassistant #android");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return false;
            }
        });

        Preference opensourcePref = findPreference("open_source");
        opensourcePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), LibraryActivity.class);
                startActivity(new Intent(getActivity(), LibraryActivity.class));
                getActivity().overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
                return false;
            }
        });

        Preference bugReportPref = findPreference("bug_report");
        if (bugReportPref != null) {
            bugReportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:"));
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@axzae.com"});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "HomeAssist Bug Report");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\nAndroid Version: " + Build.VERSION.RELEASE + "\nHomeAssist Version: " + BuildConfig.VERSION_NAME);
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.title_send_email)));

                    return false;
                }
            });
        }

//        Preference testPushNotificationPref = findPreference("test_push_notification");
//        testPushNotificationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                PendingIntent resultPendingIntent=PendingIntent.getBroadcast(getActivity(), 0, new Intent("DoNothing"), 0);
//
//                NotificationManager mNotifyManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
//                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity());
//
//                mBuilder.setSmallIcon(R.drawable.ic_notification_24dp)
//                        .setContentTitle("Push Notification")
//                        .setContentText("Hello from HomeAssist")
//                        //.setStyle(new NotificationCompat.BigTextStyle().bigText("Test"))
//                        .setContentIntent(resultPendingIntent)
//                        .setAutoCancel(true);
//                mNotifyManager.notify(1, mBuilder.build());
//                return false;
//            }
//        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //unregister the preferenceChange listener
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("YouQi", "onSharedPreferenceChanged: " + key);
        if ("num_columns".equals(key)) {
            ((SettingsActivity) getActivity()).setActivityResult(Activity.RESULT_OK);
        }
//        Preference preference = findPreference(key);
//        // Log.d(TAG, "Pref Value: " + sharedPreferences.getBoolean(getString(R.string.pref_key_reminder), false));
//        // Log.d(TAG, "Pref Value: " + sharedPreferences.getString(getString(R.string.pref_key_alarm), ""));
//        switch (preference.getKey()) {
//            case "key_reminder":
//            case "key_alarm":
//                Intent intent = new Intent("com.google.developer.bugmaster.SETTING_CHANGED");
//                getActivity().sendBroadcast(intent);
//                break;
//        }
    }
}
