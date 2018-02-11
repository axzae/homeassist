package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.util.CommonUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Simple fragment with blur effect behind.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SunFragment extends BaseControlFragment implements View.OnClickListener {

    private TextView mRisingLabel;
    private TextView mRising;
    private TextView mSettingLabel;
    private TextView mSetting;
    private TextView mNoonLabel;
    private TextView mNoon;

    public static SunFragment newInstance(Entity entity) {
        SunFragment fragment = new SunFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_sun, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        rootView.findViewById(R.id.button_close).setOnClickListener(this);
        mRisingLabel = rootView.findViewById(R.id.text_rising_label);
        mNoonLabel = rootView.findViewById(R.id.text_noon_label);
        mSettingLabel = rootView.findViewById(R.id.text_setting_label);
        mRising = rootView.findViewById(R.id.text_rising);
        mNoon = rootView.findViewById(R.id.text_noon);
        mSetting = rootView.findViewById(R.id.text_setting);

        refreshUi();

        return builder.create();
    }

    private void refreshUi() {

        Log.d("YouQi", "Sun: " + CommonUtil.deflate(mEntity));

        try {

            DateFormat df = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH));
            DateFormat dfDisplay = (new SimpleDateFormat("hh:mm a", Locale.ENGLISH));
            Date nextDawn = df.parse(mEntity.attributes.nextDawn);
            Date nextDusk = df.parse(mEntity.attributes.nextDusk);
            Date nextMidnight = df.parse(mEntity.attributes.nextMidnight);
            Date nextNoon = df.parse(mEntity.attributes.nextNoon);
            Date nextRising = df.parse(mEntity.attributes.nextRising);
            Date nextSetting = df.parse(mEntity.attributes.nextSetting);

            mRisingLabel.setText("Rising " + DateUtils.getRelativeTimeSpanString(nextRising.getTime()));
            mNoonLabel.setText("Noon " + DateUtils.getRelativeTimeSpanString(nextNoon.getTime()));
            mSettingLabel.setText("Setting " + DateUtils.getRelativeTimeSpanString(nextSetting.getTime()));
            mRising.setText(dfDisplay.format(nextRising));
            mNoon.setText(dfDisplay.format(nextNoon));
            mSetting.setText(dfDisplay.format(nextSetting));
            //Log.d("YouQi", "nextDawn: " + DateUtils.getRelativeTimeSpanString(nextDawn.getTime()));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_close:
                dismiss();
                break;
        }
    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        refreshUi();
    }

}
