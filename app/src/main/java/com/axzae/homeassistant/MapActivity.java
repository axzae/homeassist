package com.axzae.homeassistant;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.rest.RxPayload;
import com.axzae.homeassistant.provider.DatabaseManager;
import com.axzae.homeassistant.service.DataSyncService;
import com.axzae.homeassistant.util.CommonUtil;
import com.axzae.homeassistant.util.IconGenerator;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.SafeObserver;
import io.reactivex.schedulers.Schedulers;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, Observer<RxPayload> {
    private InterstitialAd mInterstitialAd;
    private Toast mToast;
    private FusedLocationProviderClient mFusedLocationClient;

    private GoogleMap mMap;
    private IconGenerator mifZoneIcon;
    private IconGenerator mifZoneText;
    private IconGenerator mifMarkerIcon;
    private IconGenerator mifMarkerText;

    private HashMap<String, Marker> markers = new HashMap<>();

    //Bound Service (Experimental)
    private DataSyncService mService;
    private SafeObserver<RxPayload> mSafeObserver;
    private boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DataSyncService.LocalBinder binder = (DataSyncService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            mSafeObserver = new SafeObserver<>(MapActivity.this);
            binder.getEventSubject()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mSafeObserver);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);

        CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_locate), R.color.md_white_1000);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, DataSyncService.class);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("YouQi", "Destroying MapActivity");
        if (mSafeObserver != null) mSafeObserver.dispose();

        if (mBound) {
            Log.d("YouQi", "Super Destroying MapActivity");

            getApplicationContext().unbindService(mConnection);
            mBound = false;
        }

        //mEventEmitter.onComplete();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_locate:
                refreshLocation();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) {
            overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
        }
    }

    private void setupAdSense() {
        Log.d("YouQi", "Map Adsense: " + ((FirebaseRemoteConfig.getInstance().getBoolean("adsense_map")) ? "Yes" : "No"));
        if (FirebaseRemoteConfig.getInstance().getBoolean("adsense_map")) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_unit_id_interstitial));
            mInterstitialAd.loadAd(CommonUtil.getAdRequest());
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    mInterstitialAd.show();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.menu_map));
        }

        setupIconGenerator();
        setupAdSense();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setupIconGenerator() {
        mifZoneIcon = new IconGenerator(this);
        mifZoneIcon.setRotation(0);
        mifZoneIcon.setContentRotation(0);
        mifZoneIcon.setStyle(IconGenerator.STYLE_ORANGE);
        mifZoneIcon.setColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_grey_500, null));

        mifZoneText = new IconGenerator(this);
        mifZoneText.setRotation(0);
        mifZoneText.setContentRotation(0);
        mifZoneText.setStyle(IconGenerator.STYLE_ORANGE);
        mifZoneText.setColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_grey_500, null));

        mifMarkerIcon = new IconGenerator(this);
        mifMarkerIcon.setRotation(0);
        mifMarkerIcon.setContentRotation(0);
        mifMarkerIcon.setStyle(IconGenerator.STYLE_ORANGE);
        mifMarkerIcon.setColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_500, null));

        mifMarkerText = new IconGenerator(this);
        mifMarkerText.setRotation(0);
        mifMarkerText.setContentRotation(0);
        mifMarkerText.setStyle(IconGenerator.STYLE_ORANGE);
        mifMarkerText.setColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_500, null));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);

        DatabaseManager databaseManager = DatabaseManager.getInstance(this);
        ArrayList<Entity> devices = databaseManager.getDeviceLocations();


        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        int zoneCount = 0;
        int deviceCount = 0;

        for (Entity device : devices) {
            Log.d("YouQi", "Device: " + CommonUtil.deflate(device));

            LatLng latLng = device.getLocation();
            if (latLng == null) continue;
            builder.include(latLng);

            if (device.isZone()) {
                zoneCount += 1;
                Log.d("YouQi", "Zone!!");
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(latLng)
//                        .strokeColor(Color.RED)
                        .strokeColor(Color.parseColor("#FF5722"))
                        .fillColor(Color.parseColor("#33FFAB91")));

                if (device.attributes.radius != null) {
                    circle.setRadius(device.attributes.radius.floatValue());
                }

                if (device.hasMdiIcon()) {
                    Log.d("YouQi", "hasMdiIcon");
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(mifZoneIcon.makeMaterialIcon(device.getMdiIcon())))
                            .position(latLng)
                            .zIndex(1.0f)
                            .anchor(mifZoneIcon.getAnchorU(), mifZoneIcon.getAnchorV()));
                } else {
                    Log.d("YouQi", "nope");
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(mifZoneText.makeIcon(device.getFriendlyName())))
                            .position(latLng)
                            .zIndex(1.0f)
                            .anchor(mifZoneText.getAnchorU(), mifZoneText.getAnchorV()));
                }

            } else if (device.isDeviceTracker()) {
                deviceCount += 1;
                Marker marker = createMarker(device);
                markers.put(device.entityId, marker);
            }
        }

        if (deviceCount == 0 && zoneCount == 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.3333395, -30.3274332), 2));

            new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .title(R.string.title_nozone)
                    .content(R.string.content_nozone)
                    .positiveText(R.string.button_continue)
                    .positiveColorRes(R.color.md_red_500)
                    .buttonRippleColorRes(R.color.md_grey_200)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.20); // offset from edges of the map 10% of screen
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 10));
            mMap.animateCamera(cu);
        }
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private Marker createMarker(Entity device) {
        Marker marker;
        LatLng latLng = device.getLocation();
        if (device.hasMdiIcon()) {
            marker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(mifMarkerIcon.makeMaterialIcon(device.getMdiIcon())))
                    .position(latLng)
                    .zIndex(2.0f)
                    .anchor(mifMarkerIcon.getAnchorU(), mifMarkerIcon.getAnchorV()));
        } else {
            marker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(mifMarkerText.makeIcon(device.getFriendlyName())))
                    .position(latLng)
                    .zIndex(2.0f)
                    .anchor(mifMarkerText.getAnchorU(), mifMarkerText.getAnchorV()));
        }

        return marker;
    }

    protected GoogleMap getMap() {
        return mMap;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("YouQi", "hello");
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(RxPayload payload) {
        Log.d("YouQi", "MAP RECEIVED: " + payload.toString());

        switch (payload.event) {
            case "UPDATE": {
                Entity entity = payload.entity;
                if (entity.isDeviceTracker()) {
                    Marker marker = markers.get(entity.entityId);
                    if (marker != null) {
                        Log.d("YouQi", "FOUND 1");
                        marker.remove();
                    }
                    marker = createMarker(entity);
                    markers.put(entity.entityId, marker);
                }
            }
            break;

            case "UPDATE_ALL": {
                for (Entity entity : payload.entities) {
                    if (entity.isDeviceTracker()) {
                        Marker marker = markers.get(entity.entityId);
                        if (marker != null) {
                            Log.d("YouQi", "FOUND 2");
                            marker.remove();
                        }
                        marker = createMarker(entity);
                        markers.put(entity.entityId, marker);
                    }
                }
            }
            break;
        }

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }

    public void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showToast("Permission denied");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            showToast("Permission Granted");
        }
    }

    private void refreshLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("YouQi", "No Location Permission");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.d("YouQi", "shouldShowRequestPermissionRationale");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title("Request Permission")
                        .content("HomeAssist needs your location to update device tracker")
                        .negativeText("Cancel")
                        .positiveText("Allow")
                        .cancelable(false)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ActivityCompat.requestPermissions(MapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                            }
                        })
                        .show();

            } else {
                Log.d("YouQi", "else statement");
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }


            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d("YouQi", "getLastLocation");

        if (mFusedLocationClient == null)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("YouQi", "Location: " + CommonUtil.deflate(location));
                    }
                });
    }
}
