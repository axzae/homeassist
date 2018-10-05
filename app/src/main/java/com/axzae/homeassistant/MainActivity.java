package com.axzae.homeassistant;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.axzae.homeassistant.fragment.ConnectionFragment;
import com.axzae.homeassistant.fragment.EntityFragment;
import com.axzae.homeassistant.model.Changelog;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.ErrorMessage;
import com.axzae.homeassistant.model.Group;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.MDIFont;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.model.rest.RxPayload;
import com.axzae.homeassistant.provider.DatabaseManager;
import com.axzae.homeassistant.provider.DummyContentProvider;
import com.axzae.homeassistant.provider.EntityContentProvider;
import com.axzae.homeassistant.provider.ServiceProvider;
import com.axzae.homeassistant.service.DataSyncService;
import com.axzae.homeassistant.util.BottomNavigationViewHelper;
import com.axzae.homeassistant.shared.EntityProcessInterface;
import com.axzae.homeassistant.shared.EventEmitterInterface;
import com.axzae.homeassistant.util.CommonUtil;
import com.axzae.homeassistant.util.FaultUtil;
import com.axzae.homeassistant.view.ChangelogView;
import com.axzae.homeassistant.view.MultiSwipeRefreshLayout;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.jaeger.library.StatusBarUtil;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener, EntityProcessInterface, EventEmitterInterface {
    private Subject<RxPayload> mEventEmitter = PublishSubject.create();
    private Call<String> mCall2;
    private Spinner mServerSpinner;
    private int spinnerCheck = 0;
    private ArrayList<HomeAssistantServer> mServers;
    private ServerAdapter mServerAdapter;
    private AppBarLayout mAppBarLayout;

    @Override
    public Subject<RxPayload> getEventSubject() {
        return mEventEmitter;
    }

    //Cursor Loader
    private EntityChangeObserver mEntityChangeObserver;

    private SharedPreferences mSharedPref;

    private HomeAssistantServer mCurrentServer;
    private ProgressBar mProgressBar;
    private RefreshTask mRefreshTask;
    private Call<ArrayList<Entity>> mCall;
    private boolean doubleBackToExitPressedOnce;
    private MultiSwipeRefreshLayout mSwipeRefresh;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private Toast mToast;
    private boolean runonce = false;

    private BottomNavigationView mNavigation;
    private MaterialSearchView mSearchView;
    private MenuItem mMenuHoursand;

    private ViewPagerAdapter mViewPagerAdapter;
    private ViewPager mViewPager;

    //Data
    private ArrayList<Group> mGroups;

    //Bound Service (Experimental)
    private DataSyncService mService;
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


            Log.d("YouQi", "Service Bound");
            binder.getEventSubject()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<RxPayload>() {
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
                    });


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    private void showNetworkBusy() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void showNetworkIdle() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void showDatabaseBusy() {
        if (mMenuHoursand != null) mMenuHoursand.setVisible(true);
    }

    private void showDatabaseIdle() {
        if (mMenuHoursand != null) mMenuHoursand.setVisible(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_edit), R.color.md_white_1000);
        CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_refresh), R.color.md_white_1000);
        CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_sort), R.color.md_white_1000);
        CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_webui), R.color.md_white_1000);
        CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_hoursand), R.color.md_white_1000);

        Log.d("YouQi", "onCreateOptionsMenu");
        mMenuHoursand = menu.findItem(R.id.action_hoursand);
        //mSearchView.setMenuItem(mMenuSearch);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupContentObserver();
        mSharedPref = getAppController().getSharedPref();
        mServers = DatabaseManager.getInstance(this).getConnections();
        //mCurrentServer = HomeAssistantServer.newInstance(mSharedPref);
        mCurrentServer = mServers.get(mSharedPref.getInt("connectionIndex", 0));
        mProgressBar = findViewById(R.id.progressBar);

        Crashlytics.log(Log.DEBUG, "YouQi", mCurrentServer.getBaseUrl());
        Log.d("YouQi", "onCreate");

        setupToolbar();
        setupDrawer();
        setupViewPager();
        setupBottomNavigation();
        setupSearchView();
        setupWhatsNew();

        mProgressBar.setVisibility(View.GONE);

    }

    //https://stackoverflow.com/questions/21380914/contentobserver-onchange
    private void setupContentObserver() {
        // creates and starts a new thread set up as a looper
        HandlerThread thread = new HandlerThread("COHandlerThread");
        thread.start();

        // creates the handler using the passed looper
        Handler handler = new Handler(thread.getLooper());
        mEntityChangeObserver = new EntityChangeObserver(handler);
    }

    private void setupSearchView() {
        mSearchView = findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getCurrentEntityFragment().search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                Log.d("YouQi", "onQueryTextChange: " + newText);
                return false;
            }
        });

        mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
                Log.d("YouQi", "onSearchViewShown");
            }

            @Override
            public void onSearchViewClosed() {
                Log.d("YouQi", "onSearchViewClosed");
                //Do some magic
            }
        });
    }

    private void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        mAppBarLayout = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(getString(R.string.app_name));
            //getSupportActionBar().setSubtitle(mFullUri);
        }
    }

    private void setupDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        final TextView mVersionText = findViewById(R.id.version_text);
        final View mHeaderView = mNavigationView.getHeaderView(0);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String summary = String.format(Locale.ENGLISH, "v%s", packageInfo.versionName);
            mVersionText.setText(summary);
        } catch (Exception e) {
            mVersionText.setText("");
        }


        final View mProfileImage = mHeaderView.findViewById(R.id.profile_image);
        CommonUtil.setBouncyTouch(mProfileImage);

        final TextView websocketButton = mHeaderView.findViewById(R.id.text_websocket);
        websocketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.startWebSocket(mCurrentServer);
            }
        });


        //TextView mainText = mHeaderView.findViewById(R.id.main_text);
        //TextView subText = mHeaderView.findViewById(R.id.sub_text);
        //mainText.setText(getString(R.string.app_name));
        //subText.setText(mFullUri);

        mServerSpinner = mHeaderView.findViewById(R.id.spinner_server);
        //servers.add(new HomeAssistantServer(mCurrentServer.getBaseUrl(), mCurrentServer.getPassword()));
        mServerAdapter = new ServerAdapter(this, 0, mServers);
        mServerSpinner.setAdapter(mServerAdapter);
        mServerSpinner.setSelection(mSharedPref.getInt("connectionIndex", 0), false); //must
        mServerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                if (++spinnerCheck <= 1) return;

                Log.d("YouQi", "mServerSpinner selected: " + pos);
                mSharedPref.edit().putInt("connectionIndex", pos).apply();
                switchConnection(mServers.get(pos));
//                HomeAssistantServer mServer = (HomeAssistantServer) mServerAdapter.getSelectedItem();
//                if (mServer == null) {
//                    logOut();
//                    return;
//                }
                //setTitle(CommonUtil.getPrintableMSISDN(mSubscriber.msisdn));
                //getSupportActionBar().setSubtitle(mSubscriber.primaryOfferName);
                //getSupportActionBar().setSubtitle(CommonUtil.getPrintableMSISDN(mSubscriber.msisdn));
                mDrawerLayout.closeDrawers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //Hack in place to make blurdialogfragment statusbar height calculation works
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }


        //StatusBar Scrim
        StatusBarUtil.setColorForDrawerLayout(this, mDrawerLayout, ResourcesCompat.getColor(getResources(), R.color.md_red_500, null), 128);

        ViewGroup contentView = findViewById(android.R.id.content);
        View fakeTranslucentView = contentView.findViewById(com.jaeger.library.R.id.statusbarutil_translucent_view);
        if (fakeTranslucentView != null) {
            if (fakeTranslucentView.getVisibility() == View.GONE) {
                fakeTranslucentView.setVisibility(View.VISIBLE);
            }
            String colorString = "#" + Integer.toHexString(128) + Integer.toHexString(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDarkBeforeAlpha50, null)).substring(2);
            Log.d("YouQi", "colorString: " + colorString);
            fakeTranslucentView.setBackgroundColor(Color.parseColor(colorString));
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, (Toolbar) findViewById(R.id.toolbar), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean isSelected = false;
                switch (item.getItemId()) {
                    case R.id.nav_webui:
                        showWebUI();
                        break;
                    case R.id.nav_ads:
                        watchAds();
                        break;
                    case R.id.nav_logbook:
                        showLogbook();
                        break;
                    case R.id.nav_map:
                        showMap();
                        break;
                    case R.id.nav_help:
                        showWiki();
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.nav_settings:
                        showSettings();
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.nav_share:
                        shareApp();
                        break;
                    case R.id.nav_bug_report:
                        sendBugReport();
                        mDrawerLayout.closeDrawers();
                        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        //overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
                        //isSelected = true;
                        break;

                    case R.id.nav_logout:
                        showSwitch();
                        mDrawerLayout.closeDrawers();
                        break;

                    default:
                        isSelected = true;
                }
                return isSelected;
            }
        });

    }

    private void switchConnection(HomeAssistantServer homeAssistantServer) {
        mCurrentServer = homeAssistantServer;
        if (mSharedPref.getBoolean("websocket_mode", true)) {
            mService.stopWebSocket();
            mService.startWebSocket(mCurrentServer);
        }
    }

    private void showLogbook() {
        Intent intent = new Intent(MainActivity.this, LogbookActivity.class);
        intent.putExtra("server", CommonUtil.deflate(mCurrentServer));
        startActivity(intent);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    private void showMap() {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        intent.putExtra("server", CommonUtil.deflate(mCurrentServer));
        startActivity(intent);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    private void shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message_recommendation, "https://goo.gl/5rkPnP  #homeassistant #android"));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void watchAds() {
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));
        AdView mAdView = findViewById(R.id.adView);
        mAdView.loadAd(CommonUtil.getAdRequest());
    }

    private void setupViewPager() {
        TabLayout mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.viewpager);

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        mGroups = DatabaseManager.getInstance(this).getGroups();
        for (Group group : mGroups) {
            if (group.groupId == 1) {
                group.groupName = getString(R.string.title_home);
            }
            mViewPagerAdapter.addFragment(EntityFragment.getInstance(group), group.getFriendlyName());
        }
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(20);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setSelectedTabIndicatorHeight(CommonUtil.pxFromDp(this, 4f));
        for (int i = 0; i < mGroups.size(); ++i) {
            Group group = mGroups.get(i);
            if (group.hasMdiIcon()) {
                TabLayout.Tab currentTab = mTabLayout.getTabAt(i);
                if (currentTab != null) {
                    View tab = LayoutInflater.from(this).inflate(R.layout.custom_tab, mTabLayout, false);
                    TextView mdiText = tab.findViewById(R.id.text_mdi);
                    TextView nameText = tab.findViewById(R.id.text_name);
                    mdiText.setText(MDIFont.getIcon(group.attributes.icon));
                    nameText.setText(group.getFriendlyName());
                    currentTab.setCustomView(tab);
                }
            }
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(tab.getPosition() == 0 ? getString(R.string.app_name) : tab.getText());
                }

                showBottomNavigation();
                //numTab = tab.getPosition();
                //prefs.edit().putInt("numTab", numTab).apply();
            }
        });
    }

    public void showBottomNavigation() {
        mNavigation.clearAnimation();
        mNavigation.animate().translationY(0).setDuration(200);
    }

    private void setupBottomNavigation() {
        mSwipeRefresh = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshApi();
            }
        });
        mSwipeRefresh.setSwipeableChildren(mViewPager);

        mNavigation = findViewById(R.id.navigation);
        mNavigation.setOnNavigationItemSelectedListener(this);
        mNavigation.setSelectedItemId(-1);
        mNavigation.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Log.d("YouQi", "navigation: " + mNavigation.getMeasuredHeight());
        //CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);
        //params.setMargins(0, 0, 0, mNavigation.getMeasuredHeight());  // left, top, right, bottom

        //CoordinatorLayout.LayoutParams params2 = (CoordinatorLayout.LayoutParams) mSwipeRefresh.getLayoutParams();
        //Log.d("YouQi", "Margin: " + params2.leftMargin + ", " + params2.topMargin + ", " + params2.rightMargin + ", " + mNavigation.getMeasuredHeight());
        //params2.setMargins(params2.leftMargin, params2.topMargin, params2.rightMargin, mNavigation.getMeasuredHeight());
        //mSwipeRefresh.setLayoutParams(params2);
        BottomNavigationViewHelper.disableShiftMode(mNavigation);
        //Log.d("YouQi", "FULL URI: " + mSharedPref.getString(ConnectActivity.EXTRA_FULL_URI, null));

        //CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mNavigation.getLayoutParams();
        //layoutParams.setBehavior(new BottomNavigationViewBehavior());
    }

    private void setupWhatsNew() {
        final String prefKey = "whatsnew" + BuildConfig.VERSION_CODE;

        Changelog changelog = Changelog.getLatest();
        ChangelogView changelogView = new ChangelogView(this);
        changelogView.loadLogs(changelog.logs);
        new MaterialDialog.Builder(this)
                .title("Community Edition")
                .customView(changelogView, true)
                .positiveText(getString(R.string.action_gotit))
                .positiveColorRes(R.color.md_blue_500)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, DataSyncService.class);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("YouQi", "Destroying MainActivity");

        if (mBound) {
            getApplicationContext().unbindService(mConnection);
            mBound = false;
        }

        mEventEmitter.onComplete();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_refresh:
                refreshApi();
                return true;

            case R.id.action_edit:
                showEdit(null);
                return true;

            case R.id.action_sort:
                showSortOptions();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_settings:
                showSettings();
                return true;

            case R.id.action_clear_search:
                clearSearch();
                return true;
            case R.id.action_webui:
                showWebUI();
                return true;

            case R.id.action_switch:
                showSwitch();
                return true;

            //case R.id.action_logout:
            //    logOut();
            //    return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearSearch() {
        getCurrentEntityFragment().clearSearch();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2000: {
                if (resultCode == Activity.RESULT_OK) {
                    mEventEmitter.onNext(RxPayload.getInstance("SETTINGS"));
                }
                break;
            }

            case 2001: {
                if (resultCode == Activity.RESULT_OK) {
                    clearSearch();

                    Group group = CommonUtil.inflate(data.getStringExtra("group"), Group.class);
                    Log.d("YouQi", "Received Group:" + group.groupId);

                    RxPayload payload = RxPayload.getInstance("EDIT");
                    payload.group = group;
                    mEventEmitter.onNext(payload);
                    //Toast.makeText(this, "OK!", Toast.LENGTH_SHORT).show();
                    //getContentResolver().notifyChange(EntityContentProvider.getUrl(), null);
                }
                break;
            }
        }
    }

    private void showSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(i, 2000);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    private void showWiki() {
        Intent i = new Intent(this, WikiActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    private void sendBugReport() {

        new MaterialDialog.Builder(this)
                .title(R.string.pref_bug_report)
                .content(R.string.message_attach_bootstrap)
                .positiveText(getString(R.string.action_yes))
                .negativeText(getString(R.string.action_no))
                .negativeColorRes(R.color.md_blue_500)
                .positiveColorRes(R.color.md_blue_500)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        sendBugReport(null);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        if (mCall2 == null) {
                            showNetworkBusy();
                            mCall2 = ServiceProvider.getRawApiService(mCurrentServer.getBaseUrl()).rawStates(mCurrentServer.getBearerHeader());
                            mCall2.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                    mCall2 = null;
                                    showNetworkIdle();

                                    if (FaultUtil.isRetrofitServerError(response)) {
                                        showError(response.message());
                                        return;
                                    }

                                    File bootstrapFile = CommonUtil.writeToExternalCache(MainActivity.this, "states.json", response.body());
                                    Uri uri = Uri.fromFile(bootstrapFile);
                                    sendBugReport(uri);
                                }

                                @Override
                                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                    mCall2 = null;
                                    showNetworkIdle();
                                    showError(FaultUtil.getPrintableMessage(MainActivity.this, t));
                                }
                            });
                        }


                    }
                })
                .show();


    }

    private void sendBugReport(Uri uri) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@axzae.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "HomeAssist Bug Report");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\nAndroid Version: " + Build.VERSION.RELEASE + "\nHomeAssist Version: " + BuildConfig.VERSION_NAME);
        if (uri != null) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        startActivity(Intent.createChooser(emailIntent, getString(R.string.title_send_email)));
    }

    private void showSwitch() {
        new MaterialDialog.Builder(this)
                .content(getString(R.string.message_signout, mCurrentServer.getBaseUrl()))
                .positiveText(getString(R.string.action_logout))
                .negativeText(getString(R.string.action_cancel))
                .negativeColorRes(R.color.md_blue_500)
                .positiveColorRes(R.color.md_blue_500)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        logOut();
                    }
                })
                .show();
    }

    private void showWebUI() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        //builder.setStartAnimations(mActivity, R.anim.right_in, R.anim.left_out);

        builder.setStartAnimations(this, R.anim.activity_open_translate, R.anim.activity_close_scale);
        builder.setExitAnimations(this, R.anim.activity_open_scale, R.anim.activity_close_translate);
        builder.setToolbarColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_500, null));
//        builder.setSecondaryToolbarColor(ResourcesCompat.getColor(getResources(), R.color.md_white_1000, null));
        CustomTabsIntent customTabsIntent = builder.build();

        try {
            customTabsIntent.launchUrl(this, mCurrentServer.getBaseUri());
        } catch (ActivityNotFoundException e) {
            showToast(getString(R.string.exception_no_chrome));
        }
    }

    private void addConnection() {
        //mDrawerLayout.closeDrawers();
        ConnectionFragment fragment = ConnectionFragment.newInstance(null);
        fragment.show(getFragmentManager(), null);
        //Toast.makeText(mService, "add connection", Toast.LENGTH_SHORT).show();
    }

    public void refreshConnections() {
        Log.d("YouQi", "refreshConnections");
        mServers = DatabaseManager.getInstance(this).getConnections();
        mServerAdapter.setItems(mServers);
        mServerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawers();
        } else if (mSearchView.isSearchOpen()) {
            mSearchView.closeSearch();
        } else if (getCurrentEntityFragment().isFilterState()) {
            getCurrentEntityFragment().clearSearch();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    public void showError(final String status) {
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
    }

    public void refreshApi() {
        if (mRefreshTask == null) {
            mRefreshTask = new RefreshTask();
            mRefreshTask.execute((Void) null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mEntityChangeObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(DummyContentProvider.getUrl(), true, mEntityChangeObserver);
        if (!runonce || (mService != null && !mService.isWebSocketRunning())) {
            runonce = true;
            refreshApi();
        }
    }

    @Override
    public void callService(final String domain, final String service, CallServiceRequest serviceRequest) {
        if (mService != null && mService.isWebSocketRunning()) {
            Log.d("YouQi", "Using WebSocket");
            mService.callService(domain, service, serviceRequest);
        } else if (mCall == null) {
            Log.d("YouQi", "Using HTTP");
            showNetworkBusy();
            Crashlytics.log("baseUrl: " + mCurrentServer.getBaseUrl());
            mCall = ServiceProvider.getApiService(mCurrentServer.getBaseUrl()).callService(mCurrentServer.getBearerHeader(), domain, service, serviceRequest);
            mCall.enqueue(new Callback<ArrayList<Entity>>() {
                @Override
                public void onResponse(@NonNull Call<ArrayList<Entity>> call, @NonNull Response<ArrayList<Entity>> response) {
                    mCall = null;
                    showNetworkIdle();

                    if (FaultUtil.isRetrofitServerError(response)) {
                        showError(response.message());
                        return;
                    }

                    ArrayList<Entity> restResponse = response.body();
                    CommonUtil.logLargeString("YouQi", "service restResponse: " + CommonUtil.deflate(restResponse));

                    if ("script".equals(domain) || ("automation".equals(domain) || "scene".equals(domain) || "trigger".equals(service))) {
                        showToast(getString(R.string.toast_triggered));
                    }

                    if (restResponse != null) {
                        for (Entity entity : restResponse) {
                            getContentResolver().update(Uri.parse("content://com.axzae.homeassistant.provider.EntityContentProvider/"), entity.getContentValues(), "ENTITY_ID='" + entity.entityId + "'", null);
                        }

                        //RxPayload payload = RxPayload.getInstance("UPDATE_ALL");
                        //payload.entities = restResponse;
                        //mEventEmitter.onNext(payload);
                    }

                }

                @Override
                public void onFailure(@NonNull Call<ArrayList<Entity>> call, @NonNull Throwable t) {
                    mCall = null;
                    showNetworkIdle();
                    showError(FaultUtil.getPrintableMessage(MainActivity.this, t));
                }
            });
        }

        //ContentValues values = new ContentValues();
        //values.put(HabitTable.TIME); //whatever column you want to update, I dont know the name of it
        //getContentResolver().update(HabitTable.CONTENT_URI,values,HabitTable.ID+"=?",new String[] {String.valueOf(id)}); //id is the id of the row you wan to update
        //getContentResolver().update()
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        showBottomNavigation();
        mAppBarLayout.setExpanded(true);

        switch (item.getItemId()) {

            case R.id.action_search:
                showSearch();
                //doTest();
                break;

            case R.id.action_refresh:
                if (mRefreshTask == null) {
                    mSwipeRefresh.setRefreshing(true);
                }
                refreshApi();
                break;

            case R.id.action_edit:
                showEdit(null);
                break;

            case R.id.action_sort:
                showSortOptions();
                break;

            case R.id.action_switch:
                showSwitch();
                break;
        }

        return false;
    }

    private void showSearch() {
        mSearchView.showSearch(false);
    }

    @Override
    public HomeAssistantServer getServer() {
        return mCurrentServer;
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    private class RefreshTask extends AsyncTask<Void, String, ErrorMessage> {
        RefreshTask() {
            showNetworkBusy();
            mNavigation.getMenu().findItem(R.id.action_refresh).setEnabled(false);
        }

        @Override
        protected ErrorMessage doInBackground(Void... params) {
            try {
                publishProgress("Connecting");
                Response<ArrayList<Entity>> response = ServiceProvider.getApiService(mCurrentServer.getBaseUrl()).getStates(mCurrentServer.getBearerHeader()).execute();
                if (response.code() != 200) {
                    //OAuthToken token = new Gson().fromJson(response.errorBody().string(), OAuthToken.class);
                    return new ErrorMessage("Error", response.message());
                }

                final ArrayList<Entity> statesResponse = response.body();

                if (statesResponse == null) {
                    throw new RuntimeException("No Data");
                }

                Log.d("YouQi", "bootstrapResponse: " + statesResponse);
                publishProgress("Refreshing");

                ArrayList<ContentValues> values = new ArrayList<>();
                for (Entity entity : statesResponse) {
                    values.add(entity.getContentValues());
                }

                getContentResolver().bulkInsert(EntityContentProvider.getUrl(), values.toArray(new ContentValues[values.size()]));
                publishProgress((String) null);
                //Crashlytics.setUserIdentifier(settings.bootstrapResponse.profile.loginId);

            } catch (Exception e) {
                e.printStackTrace();
                return new ErrorMessage("System Exception", FaultUtil.getPrintableMessage(MainActivity.this, e));
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //setStatus(values[0]);
        }

        @Override
        protected void onPostExecute(final ErrorMessage errorMessage) {
            showNetworkIdle();
            mRefreshTask = null;
            mNavigation.getMenu().findItem(R.id.action_refresh).setEnabled(true);
            mSwipeRefresh.setRefreshing(false);

            if (errorMessage != null) {
                showError(errorMessage.message);
            }
        }

        @Override
        protected void onCancelled() {
            showNetworkIdle();
        }
    }

    public void showSortOptions() {
        PopupMenu popup = new PopupMenu(this, ((BottomNavigationMenuView) mNavigation.getChildAt(0)).getChildAt(2));
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.menu_sort, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                EntityFragment currentFragment = getCurrentEntityFragment();
                Group group = currentFragment.getGroup();
                if (group != null) {
                    currentFragment.sortEntity(item.getOrder());
                }
                return true;
            }
        });

        popup.show();
    }

    public EntityFragment getCurrentEntityFragment() {
        return (EntityFragment) mViewPagerAdapter.getItem(mViewPager.getCurrentItem());
    }

    @Override
    public void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void showEdit(View v) {
        Bundle bundle = new Bundle();
        bundle.putString("group", CommonUtil.deflate(getCurrentEntityFragment().getGroup()));
        Intent i = new Intent(this, EditActivity.class);
        i.putExtras(bundle);
        startActivityForResult(i, 2001);
        overridePendingTransition(R.anim.stay_still, R.anim.fade_out);
    }

    private class ServerAdapter extends ArrayAdapter<HomeAssistantServer> {
        private List<HomeAssistantServer> items;

        ServerAdapter(Context context, int resource, List<HomeAssistantServer> objects) {
            super(context, resource, objects);
            items = objects;
        }

        public void setItems(List<HomeAssistantServer> objects) {
            items = objects;
        }

        @Override
        public int getCount() {
            return 1 + items.size();
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //convertView = vi.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
                convertView = vi.inflate(R.layout.item_server_dropdown, parent, false);
            }

            TextView mainText = convertView.findViewById(R.id.main_text);
            TextView subText = convertView.findViewById(R.id.sub_text);

            if (position == items.size()) {
                mainText.setText(TextUtils.concat(
                        CommonUtil.getSpanText(MainActivity.this, " \n", null, 0.2f),
                        CommonUtil.getSpanText(MainActivity.this, "Add Connection…", null, 0.9f),
                        CommonUtil.getSpanText(MainActivity.this, "\n ", null, 0.2f)
                ));
                subText.setVisibility(View.GONE);

                convertView.findViewById(R.id.parent).setClickable(true);
                convertView.findViewById(R.id.parent).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(MainActivity.this, "Hello", Toast.LENGTH_SHORT).show();
                        addConnection();
                        View root = parent.getRootView();
                        root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                        root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                    }
                });
            } else {
                mainText.setText(items.get(position).getName());
                subText.setVisibility(View.VISIBLE);
                subText.setText(items.get(position).getBaseUrl());
                convertView.findViewById(R.id.parent).setClickable(false);
            }

            return convertView;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), android.R.layout.simple_list_item_1, null);
            }

            ((TextView) convertView.findViewById(android.R.id.text1)).setText(items.get(position).getLine(getContext()));
            ((TextView) convertView.findViewById(android.R.id.text1)).setTextColor(ResourcesCompat.getColor(getResources(), R.color.md_white_1000, null));
            return convertView;
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public class EntityChangeObserver extends ContentObserver {

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        EntityChangeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d("YouQi", "Observer onChange (UIThread? " + (CommonUtil.isUiThread() ? "Yes" : "No") + ") URI: " + (uri == null ? "null" : uri.toString()));
            if (uri == null) return;

            DatabaseManager databaseManager = DatabaseManager.getInstance(MainActivity.this);
            if ("ALL".equals(uri.getLastPathSegment())) {
                RxPayload payload = RxPayload.getInstance("UPDATE_ALL");
                payload.entities = databaseManager.getEntities();
                mEventEmitter.onNext(payload);
            } else {
                Entity entity = databaseManager.getEntityById(uri.getLastPathSegment());

                if (entity != null) {
                    RxPayload payload = RxPayload.getInstance("UPDATE");
                    payload.entity = entity;
                    mEventEmitter.onNext(payload);
                }
            }
        }

    }

}
