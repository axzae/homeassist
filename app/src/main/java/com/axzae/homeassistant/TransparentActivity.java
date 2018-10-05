package com.axzae.homeassistant;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.Widget;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.model.rest.RxPayload;
import com.axzae.homeassistant.provider.DatabaseManager;
import com.axzae.homeassistant.provider.EntityWidgetProvider;
import com.axzae.homeassistant.provider.ServiceProvider;
import com.axzae.homeassistant.service.DataSyncService;
import com.axzae.homeassistant.shared.EntityProcessInterface;
import com.axzae.homeassistant.shared.EventEmitterInterface;
import com.axzae.homeassistant.util.CommonUtil;
import com.axzae.homeassistant.util.EntityHandlerHelper;
import com.axzae.homeassistant.util.FaultUtil;

import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.SafeObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransparentActivity extends BaseActivity implements DialogInterface.OnDismissListener, EntityProcessInterface, Observer<RxPayload>, EventEmitterInterface {
    private SharedPreferences mSharedPref;
    private Entity mEntity;
    private Call<ArrayList<Entity>> mCall;
    private int mWidgetId;
    private HomeAssistantServer mCurrentServer;
    private ArrayList<HomeAssistantServer> mServers;
    private Toast mToast;
    private boolean isJobComplete = false;

    //Bound Service (Experimental)
    private Subject<RxPayload> mEventEmitter = PublishSubject.create();
    private DataSyncService mService;
    private SafeObserver<RxPayload> mSafeObserver;
    private boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DataSyncService.LocalBinder binder = (DataSyncService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            if (mSharedPref.getBoolean("websocket_mode", true)) {
                mService.startWebSocket(mCurrentServer, true);
            }

            mSafeObserver = new SafeObserver<>(TransparentActivity.this);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPref = getAppController().getSharedPref();
        mServers = DatabaseManager.getInstance(this).getConnections();
        mCurrentServer = mServers.get(mSharedPref.getInt("connectionIndex", 0));
        mWidgetId = getIntent().getIntExtra("appWidgetId", 0);

        String json = getIntent().getExtras().getString("entity");
        mEntity = CommonUtil.inflate(json, Entity.class);
        final Entity entity = mEntity;

        if (entity != null) {
            if (entity.isSwitch() || entity.isLight() || entity.isFan() || entity.isScript() || entity.isInputBoolean()) {
                callService("homeassistant", entity.getNextState(), new CallServiceRequest(entity.entityId));
                isJobComplete = true;
            } else {
                EntityHandlerHelper.onEntityLongClick(this, entity);
            }

            Call<Entity> mCall = ServiceProvider.getApiService(mCurrentServer.getBaseUrl()).getState(mCurrentServer.getBearerHeader(), mEntity.entityId);
            mCall.enqueue(new Callback<Entity>() {
                @Override
                public void onResponse(@NonNull Call<Entity> call, @NonNull Response<Entity> response) {
                    if (FaultUtil.isRetrofitServerError(response)) {
                        return;
                    }

                    Entity restResponse = response.body();
                    if (restResponse != null) {
                        getContentResolver().update(Uri.parse("content://com.axzae.homeassistant.provider.EntityContentProvider/"), restResponse.getContentValues(), "ENTITY_ID='" + restResponse.entityId + "'", null);
                        Widget newWidget = DatabaseManager.getInstance(TransparentActivity.this).getWidgetById(mWidgetId);
                        EntityWidgetProvider.updateEntityWidget(TransparentActivity.this, newWidget);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Entity> call, @NonNull Throwable t) {
                    showToast("Widget Refresh Failed");
                }
            });

        }
    }

    public HomeAssistantServer getServer() {
        return mCurrentServer;
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mEntity == null) {
            finish();
        }
    }

    @Override
    public void callService(final String domain, final String service, CallServiceRequest serviceRequest) {
        Log.d("YouQi", String.format(Locale.ENGLISH, "callService(%s, %s) in TransparentActivity: %s", domain, service, CommonUtil.deflate(serviceRequest)));
        if (mCall == null) {
            //showNetworkBusy();
            mCall = ServiceProvider.getApiService(mCurrentServer.getBaseUrl()).callService(mCurrentServer.getBearerHeader(), domain, service, serviceRequest);
            mCall.enqueue(new Callback<ArrayList<Entity>>() {
                @Override
                public void onResponse(@NonNull Call<ArrayList<Entity>> call, @NonNull Response<ArrayList<Entity>> response) {
                    mCall = null;
                    //showNetworkIdle();

                    if (FaultUtil.isRetrofitServerError(response)) {
                        //showError(response.message());
                        return;
                    }

                    ArrayList<Entity> restResponse = response.body();
                    //CommonUtil.logLargeString("YouQi", "service restResponse: " + CommonUtil.deflate(restResponse));

                    if ("script".equals(domain) || ("automation".equals(domain) || "scene".equals(domain) || "trigger".equals(service))) {
                        showToast(getString(R.string.toast_triggered));
                    }

                    if (restResponse != null) {
                        for (Entity entity : restResponse) {
                            getContentResolver().update(Uri.parse("content://com.axzae.homeassistant.provider.EntityContentProvider/"), entity.getContentValues(), "ENTITY_ID='" + entity.entityId + "'", null);

                            if (entity.equals(mEntity)) {
                                Widget widget = Widget.getInstance(entity, mWidgetId);
                                EntityWidgetProvider.updateEntityWidget(TransparentActivity.this, widget);
                            }
                        }
                    }
                    Log.d("YouQi", "callService JobCompleted");
                    if (isJobComplete) finish();
                }

                @Override
                public void onFailure(@NonNull Call<ArrayList<Entity>> call, @NonNull Throwable t) {
                    mCall = null;

                    showToast(FaultUtil.getPrintableMessage(TransparentActivity.this, t));
                    if (isJobComplete) finish();
                    //showNetworkIdle();
                    //showError(FaultUtil.getPrintableMessage(MainActivity.this, t));
                }
            });
        }
    }

    @Override
    public void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

//        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
//            Log.d("YouQi", "Loading...");
//            mInterstitialAd.show();
//        } else {
//            Log.d("YouQi", "The interstitial wasn't loaded yet.");
//        }
        finish();
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
        Log.d("YouQi", "onDestroy");

        if (mSafeObserver != null) mSafeObserver.dispose();
        if (mBound) {
            //mService.stopWebSocket();
            getApplicationContext().unbindService(mConnection);
            mBound = false;
        }

        //mEventEmitter.onComplete();
    }

    public Activity getActivity() {
        return this;
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onNext(RxPayload rxPayload) {
        mEventEmitter.onNext(rxPayload);
    }

    @Override
    public void onError(Throwable e) {
    }

    @Override
    public void onComplete() {
    }

    @Override
    public Subject<RxPayload> getEventSubject() {
        return mEventEmitter;
    }
}