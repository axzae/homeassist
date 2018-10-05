package com.axzae.homeassistant.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.axzae.homeassistant.MainActivity;
import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.provider.DatabaseManager;
import com.axzae.homeassistant.provider.ServiceProvider;
import com.axzae.homeassistant.util.CommonUtil;
import com.axzae.homeassistant.util.FaultUtil;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogEngine;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ConnectionFragment extends DialogFragment implements View.OnClickListener {

    private TextView mName;
    private TextView mBaseUrl;
    private TextView mPassword;
    private BlurDialogEngine mBlurEngine;
    private HomeAssistantServer mConnection;

    public static ConnectionFragment newInstance(HomeAssistantServer connection) {
        ConnectionFragment fragment = new ConnectionFragment();
        Bundle args = new Bundle();
        args.putString("connection", CommonUtil.deflate(connection));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mConnection = CommonUtil.inflate(args.getString("connection"), HomeAssistantServer.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_connection, null);
        builder.setView(rootView);
        builder.setTitle(R.string.title_add_connection);

        mName = rootView.findViewById(R.id.text_name);
        mBaseUrl = rootView.findViewById(R.id.text_ipaddress);
        mPassword = rootView.findViewById(R.id.text_password);

        rootView.findViewById(R.id.button_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.button_add).setOnClickListener(this);

        //refreshUi();

        return builder.create();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBlurEngine = new BlurDialogEngine(getActivity());
        mBlurEngine.setBlurRadius(5);
        mBlurEngine.setDownScaleFactor(6f);
        mBlurEngine.debug(false);
        mBlurEngine.setBlurActionBar(false);
        mBlurEngine.setUseRenderScript(true);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mBlurEngine != null) mBlurEngine.onDismiss();
        //https://stackoverflow.com/questions/23786033/dialogfragment-and-ondismiss
        final Activity activity = getActivity();

        Log.d("YouQi", "onDismiss ConnectionFragment");
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).refreshConnections();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBlurEngine != null) mBlurEngine.onResume(getRetainInstance());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBlurEngine != null) mBlurEngine.onDetach();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_cancel:
                dismiss();
                break;
            case R.id.button_add:
                addConnection();
                break;
        }
    }

    private void addConnection() {
        attemptLogin();
    }

    private void attemptLogin() {

        // Reset errors.
        mName.setError(null);
        mBaseUrl.setError(null);
        mPassword.setError(null);

        // Store values at the time of the login attempt.
        final String name = mName.getText().toString().trim();
        final String baseURL = mBaseUrl.getText().toString().trim();
        final String password = mPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            mName.setError(getString(R.string.error_field_required));
            focusView = mName;
            cancel = true;
        }

        if (TextUtils.isEmpty(baseURL)) {
            mBaseUrl.setError(getString(R.string.error_field_required));
            focusView = mBaseUrl;
            cancel = true;
        } else if (!(baseURL.startsWith("http://") || baseURL.startsWith("https://"))) {
            mBaseUrl.setError(getString(R.string.error_invalid_baseurl));
            focusView = mBaseUrl;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            HomeAssistantServer connection = new HomeAssistantServer(baseURL, password);
            connection.name = name;
            testConnection(connection);
        }
    }

    private void testConnection(final HomeAssistantServer connection) {
        ServiceProvider.getRawApiService(connection.getBaseUrl()).rawStates(connection.getBearerHeader())
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (FaultUtil.isRetrofitServerError(response)) {
                            showError(response.message());
                            return;
                        }

                        Activity activity = getActivity();
                        if (activity != null && !activity.isFinishing()) {
                            DatabaseManager.getInstance(activity).addConnection(connection);
                            dismiss();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Activity activity = getActivity();
                        if (activity != null && !activity.isFinishing()) {
                            showError(FaultUtil.getPrintableMessage(activity, t));
                        }
                    }
                });
    }

    public void showError(final String status) {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
        }
    }

}
