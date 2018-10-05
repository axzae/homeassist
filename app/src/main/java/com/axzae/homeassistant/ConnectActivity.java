package com.axzae.homeassistant;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.ErrorMessage;
import com.axzae.homeassistant.model.Group;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.provider.DatabaseManager;
import com.axzae.homeassistant.provider.EntityWidgetProvider;
import com.axzae.homeassistant.provider.ServiceProvider;
import com.axzae.homeassistant.util.CommonUtil;
import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Response;

/**
 * A login screen that offers login via username/password.
 */
public class ConnectActivity extends BaseActivity {
    public static final String EXTRA_IPADDRESS = "ip_address";
    public static final String EXTRA_FULL_URI = "full_uri";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_LAST_REQUEST = "last_request";
    private SharedPreferences mSharedPref;
    private int settingCountDown = 5;

    // UI references.
    private EditText mIpAddressView;
    private EditText mPasswordView;
    private TextView mTextProgress;
    private ProgressBar mProgressBar;
    private Snackbar mSnackbar;
    private Button mConnectButton;
    private UserLoginTask mAuthTask;
    private LinearLayout mLayoutMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mLayoutMain = findViewById(R.id.main_layout);
        mLayoutMain.setVisibility(View.GONE);
        //Send a Google Analytics screen view.
        //Tracker tracker = getAppController().getDefaultTracker();
        //tracker.send(new HitBuilders.ScreenViewBuilder().build());

        try {
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);
            Log.d("YouQi", "Date1: " + df2.parse("2017-10-01T23:00:12+00:00").getTime());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("YouQi", "Date2: " + e.getMessage());
        }

        findViewById(R.id.splash_logo).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", 0.95f);
                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 0.95f);
                        scaleDownX.setDuration(200);
                        scaleDownY.setDuration(200);

                        AnimatorSet scaleDown = new AnimatorSet();
                        scaleDown.play(scaleDownX).with(scaleDownY);
                        scaleDown.setInterpolator(new OvershootInterpolator());
                        scaleDown.start();

                        if (--settingCountDown <= 0) {
                            startSettingActivity();
                            settingCountDown = 5;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(v, "scaleX", 1f);
                        ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(v, "scaleY", 1f);
                        scaleDownX2.setDuration(200);
                        scaleDownY2.setDuration(200);

                        AnimatorSet scaleDown2 = new AnimatorSet();
                        scaleDown2.play(scaleDownX2).with(scaleDownY2);
                        scaleDown2.setInterpolator(new OvershootInterpolator());
                        scaleDown2.start();
                        break;
                }
                return true;
            }
        });
        mProgressBar = findViewById(R.id.progressBar);
        mIpAddressView = findViewById(R.id.text_ipaddress);
        mPasswordView = findViewById(R.id.text_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.button_connect || id == EditorInfo.IME_NULL || id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                }
                return false;
            }
        });

        mConnectButton = findViewById(R.id.button_connect);
        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mTextProgress = findViewById(R.id.text_progress);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSharedPref == null) {
            new SharedPreferenceLoadingTask().execute();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mSnackbar != null) {
            mSnackbar.dismiss();
        }

        if (mSharedPref == null) {
            Toast.makeText(this, "Please wait awhile before retrying", Toast.LENGTH_SHORT).show();
            return;
        }

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // Reset errors.
        mIpAddressView.setError(null);
        mPasswordView.setError(null);


        // Store values at the time of the login attempt.
        String baseURL = mIpAddressView.getText().toString().trim();
        final String password = mPasswordView.getText().toString();

        if (baseURL.endsWith("/")) {
            baseURL = baseURL.substring(0, baseURL.length() - 1);
            mIpAddressView.setText(baseURL);
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(baseURL)) {
            mIpAddressView.setError(getString(R.string.error_field_required));
            focusView = mIpAddressView;
            cancel = true;
        } else if (!(baseURL.startsWith("http://") || baseURL.startsWith("https://"))) {
            mIpAddressView.setError(getString(R.string.error_invalid_baseurl));
            focusView = mIpAddressView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true, getString(R.string.progress_connecting));
            String host = Uri.parse(baseURL).getHost();
            Log.d("YouQi", "baseURL: " + baseURL);
            Log.d("YouQi", "host: " + host);

            if (mAuthTask == null) {
                mAuthTask = new UserLoginTask(baseURL, host, password);
                mAuthTask.execute((Void) null);
            }
        }
    }

    private class UserLoginTask extends AsyncTask<Void, String, ErrorMessage> {

        private final String mUri;
        private final String mIpAddress;
        private final String mPassword;
        private final String mBearerHeader;
        private String mBoostrapData;

        UserLoginTask(String uri, String ipAddress, String password) {
            mUri = uri;
            mIpAddress = ipAddress;
            mPassword = password;
            mBearerHeader = "Bearer " + password;

            mIpAddressView.setEnabled(false);
            mPasswordView.setEnabled(false);
            mConnectButton.setEnabled(false);
            mProgressBar.setVisibility(View.VISIBLE);
            mTextProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected ErrorMessage doInBackground(Void... params) {
            try {
                publishProgress(getString(R.string.progress_connecting));

                //Response<BootstrapResponse> response = ServiceProvider.getApiService(mUri).bootstrap(mBearerHeader).execute();
                Response<String> response = ServiceProvider.getRawApiService(mUri).rawStates(mBearerHeader).execute();

                if (response.code() != 200) {
                    if (response.code() == 401) {
                        return new ErrorMessage("Error 401", getString(R.string.error_invalid_password));
                    }

                    if (response.code() == 404) {
                        return new ErrorMessage("Error 404", getString(R.string.error_invalid_ha_server));
                    }

                    //OAuthToken token = new Gson().fromJson(response.errorBody().string(), OAuthToken.class);
                    return new ErrorMessage("Error" + response.code(), response.message());
                }

                mBoostrapData = response.body();
                final ArrayList<Entity> bootstrapResponse = CommonUtil.inflate(mBoostrapData, new TypeToken<ArrayList<Entity>>() {
                }.getType());
                //final BootstrapResponse bootstrapResponse = CommonUtil.inflate(CommonUtil.readFromAssets(ConnectActivity.this, "bootstrap.txt"), BootstrapResponse.class);
                //final BootstrapResponse bootstrapResponse = response.body();
                CommonUtil.logLargeString("YouQi", "bootstrapResponse: " + bootstrapResponse);
                publishProgress(getString(R.string.progress_bootstrapping));

                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString(EXTRA_FULL_URI, mUri);
                editor.putString(EXTRA_IPADDRESS, mIpAddress);
                editor.putString(EXTRA_PASSWORD, mPassword);
                editor.putInt("connectionIndex", 0);
                editor.putLong(EXTRA_LAST_REQUEST, System.currentTimeMillis()).apply();
                editor.apply();


                DatabaseManager databaseManager = DatabaseManager.getInstance(ConnectActivity.this);
                databaseManager.updateTables(bootstrapResponse);
                databaseManager.addConnection(new HomeAssistantServer(mUri, mPassword));
//                ArrayList<Entity> entities = databaseManager.getEntities();
//                for (Entity entity : entities) {
//                    Log.d("YouQi", "Entity: " + entity.entityId);
//                }

                //Crashlytics.setUserIdentifier(settings.bootstrapResponse.profile.loginId);

            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                return new ErrorMessage("JsonSyntaxException", e);
            } catch (Exception e) {
                Log.d("YouQi", "ERROR!");
                e.printStackTrace();
                Crashlytics.logException(e);
                return new ErrorMessage(e.getMessage(), e.toString());
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mConnectButton.setText(values[0]);
        }

        @Override
        protected void onPostExecute(final ErrorMessage errorMessage) {
            mAuthTask = null;


            if (errorMessage == null) {
                mConnectButton.setText(R.string.progress_starting);
                startMainActivity();
            } else {
                mIpAddressView.setEnabled(true);
                mPasswordView.setEnabled(true);
                mConnectButton.setEnabled(true);
                mConnectButton.setText(R.string.button_connect);
                mProgressBar.setVisibility(View.GONE);
                mTextProgress.setVisibility(View.GONE);

                mPasswordView.requestFocus();
                showError(errorMessage.message);

                if (errorMessage.throwable != null) {
                    sendEmail(mBoostrapData, errorMessage.throwable);
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //showProgress(false);
        }
    }

    private void showError(String message) {
        Drawable warningIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_warning_white_18dp, null);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(message);
        mSnackbar = Snackbar.make(findViewById(android.R.id.content), builder, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.action_retry), new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                });
        TextView textView = mSnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setCompoundDrawablesWithIntrinsicBounds(warningIcon, null, null, null);
        textView.setCompoundDrawablePadding(getResources().getDimensionPixelOffset(R.dimen.icon_8dp));
        mSnackbar.getView().setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_red_A200, null));
        mSnackbar.show();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    private void showProgress(final boolean show, final String message) {

        ConnectActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextProgress.setVisibility(View.GONE);
                if (show) {
                    mIpAddressView.setEnabled(false);
                    mPasswordView.setEnabled(false);
                    mConnectButton.setEnabled(false);
                    mConnectButton.setText(message);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mTextProgress.setVisibility(View.VISIBLE);
                } else {
                    mIpAddressView.setEnabled(true);
                    mPasswordView.setEnabled(true);
                    mConnectButton.setEnabled(true);
                    mConnectButton.setText(R.string.button_connect);
                    mProgressBar.setVisibility(View.GONE);
                    mTextProgress.setVisibility(View.GONE);
                }
            }
        });
    }

    private void startMainActivity() {
        Intent i = new Intent(ConnectActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(new Intent(ConnectActivity.this, MainActivity.class));
        overridePendingTransition(R.anim.stay_still, R.anim.fade_out);
        finish();
    }

    private void startSettingActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivityForResult(i, 2000);
    }

    private class SharedPreferenceLoadingTask extends AsyncTask<Void, Void, ErrorMessage> {

        SharedPreferenceLoadingTask() {
            showProgress(true, getString(R.string.progress_initializing));
        }

        @Override
        protected ErrorMessage doInBackground(Void... param) {
//            if (!CommonUtil.checkSignature(ConnectActivity.this)) {
//                return new ErrorMessage("Error", getString(R.string.error_corrupted));
//            }
//
//            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
//                return new ErrorMessage("Error", getString(R.string.error_jellybean));
//            }
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
            mSharedPref = getAppController().getSharedPref();


            int ids[] = AppWidgetManager.getInstance(ConnectActivity.this).getAppWidgetIds(new ComponentName(ConnectActivity.this, EntityWidgetProvider.class));
            if (ids.length > 0) {
                ArrayList<String> appWidgetIds = new ArrayList<>();
                for (int id : ids) {
                    appWidgetIds.add(Integer.toString(id));
                }

                DatabaseManager databaseManager = DatabaseManager.getInstance(ConnectActivity.this);
                databaseManager.forceCreate();
                databaseManager.housekeepWidgets(appWidgetIds);

            }
            //mBundle = getIntent().getExtras();
            return null;
        }

        @Override
        protected void onPostExecute(ErrorMessage errorMessage) {
            if (errorMessage == null) {

                if (mSharedPref.getString(EXTRA_IPADDRESS, null) != null) {

                    DatabaseManager databaseManager = DatabaseManager.getInstance(ConnectActivity.this);

                    ArrayList<Group> groups = databaseManager.getGroups();
                    ArrayList<HomeAssistantServer> connections = databaseManager.getConnections();
                    int dashboardCount = databaseManager.getDashboardCount();
                    Log.d("YouQi", "dashboardCount: " + dashboardCount);
                    if (groups.size() != 0 && connections.size() != 0 && dashboardCount > 0) {
                        startMainActivity();
                        return;
                    }
                }

                mLayoutMain.setVisibility(View.VISIBLE);

                mIpAddressView.setText(mSharedPref.getString(EXTRA_FULL_URI, ""));
                if (mIpAddressView.getText().toString().trim().length() != 0) {
                    mPasswordView.requestFocus();
                } else {
                    mIpAddressView.requestFocus();
                }
                showProgress(false, null);
            } else {
                mProgressBar.setVisibility(View.GONE);
                mConnectButton.setVisibility(View.GONE);
                mTextProgress.setText(errorMessage.message);
            }

            super.onPostExecute(errorMessage);
        }
    }

    private void sendEmail(final String content, final Throwable throwable) {
        final File bootstrapFile = writeToSDFile(content);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        final String sStackTrace = sw.toString(); // stack trace as a string
        //System.out.println(sStackTrace);

        new MaterialDialog.Builder(this)
                .title(R.string.title_send_crash_report)
                .content(R.string.message_crash_report)
                .negativeText(getString(R.string.action_dont_send_report))
                .positiveText(getString(R.string.action_send_report))
                .negativeColorRes(R.color.md_blue_500)
                .stackingBehavior(StackingBehavior.ADAPTIVE)
                .positiveColorRes(R.color.md_red_500)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@axzae.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "HomeAssist Bootstrap Crash Report");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, sStackTrace);

                        Uri uri = Uri.fromFile(bootstrapFile);
                        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        startActivity(Intent.createChooser(emailIntent, getString(R.string.title_send_email)));
                    }
                })
                .show();
    }

    private File writeToSDFile(String content) {
        File rootDir = getExternalCacheDir();
        if (rootDir != null) {
            if (!rootDir.exists()) {
                boolean isSuccess = rootDir.mkdirs();
                if (!isSuccess) {
                    Log.d("YouQi", "failed to create" + rootDir.getAbsolutePath());
                }
            }

            File bootstrapFile = new File(rootDir, "states.json");
            if (bootstrapFile.exists()) {
                boolean isSuccess = bootstrapFile.delete();
            }
            Log.d("YouQi", "External file system root: " + bootstrapFile.getAbsolutePath());

            try {
                FileOutputStream f = new FileOutputStream(bootstrapFile);
                PrintWriter pw = new PrintWriter(f);
                pw.println(content);
                pw.flush();
                pw.close();
                f.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bootstrapFile;
        }

        return null;
    }

}

