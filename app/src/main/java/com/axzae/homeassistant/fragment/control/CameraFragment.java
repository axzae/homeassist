package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.axzae.homeassistant.AppController;
import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.shared.GlideApp;
import com.axzae.homeassistant.util.CommonUtil;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import retrofit2.Call;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CameraFragment extends BaseControlFragment implements View.OnClickListener {

    private Call<ArrayList<ArrayList<Entity>>> mCall;
    private View mProgressBar;
    private HomeAssistantServer mServer;
    private SharedPreferences mSharedPref;
    private String mFullUri;
    private String mPassword;
    private ViewGroup mEmptyView;
    private ViewGroup mConnErrorView;
    private ImageView mImageView;
    private TextView mAlertView;
    private TextView mErrorTextView;

    public static CameraFragment newInstance(Entity entity, HomeAssistantServer server) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        args.putString("server", CommonUtil.deflate(server));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServer = CommonUtil.inflate(getArguments().getString("server"), HomeAssistantServer.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mSharedPref = ((AppController) getActivity().getApplication()).getSharedPref();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_camera, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());


        rootView.findViewById(R.id.button_refresh).setOnClickListener(this);
        rootView.findViewById(R.id.button_close).setOnClickListener(this);


        mImageView = rootView.findViewById(R.id.image_view);
        mEmptyView = rootView.findViewById(R.id.list_empty);
        mConnErrorView = rootView.findViewById(R.id.list_conn_error);
        mErrorTextView = rootView.findViewById(R.id.text_error);
        mAlertView = rootView.findViewById(R.id.text_alert);

        mAlertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File bootstrapFile = CommonUtil.writeToExternalCache(getActivity(), "camera.stacktrace.txt", (String) mAlertView.getTag());
                Toast.makeText(getActivity(), getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            }
        });
        //rootView.findViewById(R.id.button_cancel).setOnClickListener(this);
        //rootView.findViewById(R.id.button_set).setOnClickListener(this);
        mProgressBar = rootView.findViewById(R.id.progressbar);
        refreshCamera();
        return builder.create();
    }

    private void refreshCamera() {
        mProgressBar.setVisibility(View.VISIBLE);
        //mImageView.setVisibility(View.INVISIBLE);

        mImageView.setImageResource(android.R.color.transparent);
        Crashlytics.log("camera url: " + mServer.getBaseUrl() + mEntity.attributes.entityPicture);

        GlideApp.with(getActivity())
                .load(mServer.getBaseUrl() + mEntity.attributes.entityPicture)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mConnErrorView.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        Crashlytics.logException(e);
                        e.printStackTrace();

                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        mAlertView.setTag(mServer.getBaseUrl() + mEntity.attributes.entityPicture + "\n\n" + sw.toString());

                        //mErrorTextView.setText(e.getMessage());
                        mAlertView.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mConnErrorView.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);
                        mImageView.setVisibility(View.VISIBLE);
                        mAlertView.setVisibility(View.GONE);
                        return false;
                    }
                })
                //.transition(DrawableTransitionOptions.withCrossFade())
                .into(mImageView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_refresh:
                refreshCamera();
                break;
            case R.id.button_close:
                dismiss();
        }
    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        refreshCamera();
    }

}
