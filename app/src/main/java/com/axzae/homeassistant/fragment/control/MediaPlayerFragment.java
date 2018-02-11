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
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView
;
import com.afollestad.materialdialogs.MaterialDialog;
import com.axzae.homeassistant.AppController;
import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.MDIFont;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.shared.GlideApp;
import com.axzae.homeassistant.util.CommonUtil;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MediaPlayerFragment extends BaseControlFragment implements View.OnClickListener {

    private Call<ArrayList<ArrayList<Entity>>> mCall;
    private View mProgressBar;
    private HomeAssistantServer mServer;
    private SharedPreferences mSharedPref;
    private String mFullUri;
    private String mPassword;
    private ViewGroup mEmptyView;
    private ViewGroup mConnErrorView;
    private ImageView mImageView;
    private TextView mTextAppName;
    private TextView mTextState;
    private DiscreteSeekBar mSeekBar;

    private ViewGroup mLayoutVolume;
    private ViewGroup mLayoutTrackControl;
    private TextView mPowerButton;
    private TextView mSpeechButton;
    private TextView mPrevButton;
    private TextView mNextButton;
    private TextView mPlayButton;
    private TextView mPauseButton;
    private TextView mVolumeButton;


    final BigDecimal min = new BigDecimal("0.0");
    final BigDecimal max = new BigDecimal("1.0");
    final BigDecimal step = new BigDecimal("0.05");

    public static MediaPlayerFragment newInstance(Entity entity, HomeAssistantServer server) {
        MediaPlayerFragment fragment = new MediaPlayerFragment();
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
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_mediaplayer, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        Log.d("YouQi", "You are watching: " + CommonUtil.deflate(mEntity));


        //rootView.findViewById(R.id.button_refresh).setOnClickListener(this);
        rootView.findViewById(R.id.button_close).setOnClickListener(this);

        mLayoutTrackControl = rootView.findViewById(R.id.layout_track_control);
        mLayoutVolume = rootView.findViewById(R.id.layout_volume);
        mPowerButton = rootView.findViewById(R.id.text_power);
        mPowerButton.setOnClickListener(this);
        mSpeechButton = rootView.findViewById(R.id.text_speech);
        mSpeechButton.setOnClickListener(this);
        mPrevButton = rootView.findViewById(R.id.text_prev);
        mPrevButton.setOnClickListener(this);
        mNextButton = rootView.findViewById(R.id.text_next);
        mNextButton.setOnClickListener(this);
        mPlayButton = rootView.findViewById(R.id.text_play);
        mPlayButton.setOnClickListener(this);
        mPauseButton = rootView.findViewById(R.id.text_pause);
        mPauseButton.setOnClickListener(this);
        mVolumeButton = rootView.findViewById(R.id.text_volume);
        mVolumeButton.setOnClickListener(this);
        mSeekBar = rootView.findViewById(R.id.discrete_data);


        mTextAppName = rootView.findViewById(R.id.text_appname);
        mTextState = rootView.findViewById(R.id.text_state);


        mImageView = rootView.findViewById(R.id.image_view);
        mEmptyView = rootView.findViewById(R.id.list_empty);
        mConnErrorView = rootView.findViewById(R.id.list_conn_error);

        //rootView.findViewById(R.id.button_cancel).setOnClickListener(this);
        //rootView.findViewById(R.id.button_set).setOnClickListener(this);
        mProgressBar = rootView.findViewById(R.id.progressbar);

        setupSeekbar();
        refreshUi();


        return builder.create();
    }

    private void refreshCamera() {
        mProgressBar.setVisibility(View.VISIBLE);
        //mImageView.setVisibility(View.INVISIBLE);
        GlideApp.with(getActivity())
                .load(mServer.getBaseUrl() + mEntity.attributes.entityPicture)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mConnErrorView.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        mImageView.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                //.transition(DrawableTransitionOptions.withCrossFade())
                .into(mImageView);
    }

    public void refreshUi() {
        //mPowerButton.setOnClickListener(this);
        //mSpeechButton.setOnClickListener(this);
        mTextAppName.setText(mEntity.attributes.appName == null ? "" : mEntity.attributes.appName);
        mTextState.setText(mEntity.attributes.appName == null ? "" : mEntity.getFriendlyState());

        if (mEntity.attributes.isVolumeMuted != null) {
            mVolumeButton.setText(MDIFont.getIcon(mEntity.attributes.isVolumeMuted ? "mdi:volume-off" : "mdi:volume-high"));
            //mSeekBar.setVisibility(mEntity.attributes.isVolumeMuted ? View.INVISIBLE : View.VISIBLE);
            mSeekBar.setEnabled(!mEntity.attributes.isVolumeMuted);
        } else {
            mVolumeButton.setText(MDIFont.getIcon("mdi:volume-high"));
            mSeekBar.setEnabled(false);
            //mSeekBar.setVisibility(View.INVISIBLE);
        }

        if (mEntity.getFriendlyState().equals("PLAYING")) {
            mPauseButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.GONE);
        } else {
            mPauseButton.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.VISIBLE);
        }

        if (mEntity.getFriendlyState().equals("OFF")) {
            mPowerButton.setActivated(true);
            mSpeechButton.setVisibility(View.GONE);
            mLayoutVolume.setVisibility(View.INVISIBLE);
            mLayoutTrackControl.setVisibility(View.INVISIBLE);

            mPrevButton.setEnabled(false);
            mNextButton.setEnabled(false);
            mPlayButton.setEnabled(false);
            mPauseButton.setEnabled(false);
        } else {
            mPowerButton.setActivated(false);
            mSpeechButton.setVisibility(View.VISIBLE);
            mLayoutVolume.setVisibility(View.VISIBLE);
            mLayoutTrackControl.setVisibility(View.VISIBLE);

            mPrevButton.setEnabled(true);
            mNextButton.setEnabled(true);
            mPlayButton.setEnabled(true);
            mPauseButton.setEnabled(true);
        }

        if (mEntity.attributes.volumeLevel != null) {
            mSeekBar.setProgress(mEntity.attributes.volumeLevel.subtract(min).divide(step, RoundingMode.HALF_UP).intValue());
        } else {
            mSeekBar.setProgress(0);
        }

        refreshPicture();

    }

    private void refreshPicture() {
        if(mEntity.attributes.entityPicture != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            //mImageView.setVisibility(View.INVISIBLE);
            GlideApp.with(getActivity())
                    .load(mServer.getBaseUrl() + mEntity.attributes.entityPicture)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            mConnErrorView.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            mProgressBar.setVisibility(View.GONE);
                            mImageView.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    //.transition(DrawableTransitionOptions.withCrossFade())
                    .into(mImageView);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_power:
                callService("media_player", mEntity.getNextState(), new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_speech:
                showTextToSpeech();
                break;
            case R.id.text_prev:
                callService("media_player", "media_previous_track", new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_next:
                callService("media_player", "media_next_track", new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_play:
            case R.id.text_pause:
                callService("media_player", "media_play_pause", new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_volume:
                callService("media_player", "volume_mute", new CallServiceRequest(mEntity.entityId).setVolumeMute(!mEntity.attributes.isVolumeMuted));
                break;
            case R.id.button_close:
                dismiss();
        }
    }

    private void showTextToSpeech() {

        new MaterialDialog.Builder(getActivity())
                .title("Text to Speech")
                .content("Your message")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.button_speak)
                .input("Hello World", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        callService("tts", "google_say", new CallServiceRequest(mEntity.entityId).setMessage(input.toString()));
                    }
                }).show();

    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        refreshUi();
    }

    private void setupSeekbar() {
        DiscreteSeekBar.NumericTransformer multiplyTransformer =
                new DiscreteSeekBar.NumericTransformer() {
                    @Override
                    public int transform(int value) {
                        return value; // value * mEntity.attributes.step;
                    }

                    @Override
                    public String transformToString(int value) {
                        BigDecimal finalValue = new BigDecimal(value).multiply(step).add(min);
                        return String.format(Locale.ENGLISH, "%.2f", finalValue);
                    }

                    @Override
                    public boolean useStringTransform() {
                        return true;
                    }
                };


        mSeekBar.setNumericTransformer(multiplyTransformer);
        mSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                BigDecimal finalValue = new BigDecimal(seekBar.getProgress()).multiply(step).add(min);
                Log.d("YouQi", String.format(Locale.ENGLISH, "%.2f", finalValue));

                callService("media_player", "volume_set", new CallServiceRequest(mEntity.entityId).setVolumeLevel(finalValue));

                //valueView.setText();
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        mSeekBar.setMin(0);
        mSeekBar.setMax(max.subtract(min).divide(step, RoundingMode.HALF_UP).intValue());

    }

}
