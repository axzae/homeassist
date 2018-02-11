package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.MDIFont;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.util.CommonUtil;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Simple fragment with blur effect behind.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LightFragment extends BaseControlFragment implements View.OnClickListener {

    private ColorPickerView mColorPickerView;
    private DiscreteSeekBar mBrightnessSeekBar;
    private DiscreteSeekBar mColorTemperatureSeekBar;
    private TextView mPowerButton;
    private ViewGroup mColorTemperatureLayout;
    private ViewGroup mBrightnessLayout;
    private int mDefaultColor;

    public static LightFragment newInstance(Entity entity) {
        LightFragment fragment = new LightFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.control_light, null);
        builder.setView(view);
        builder.setTitle(mEntity.getFriendlyName());

        view.findViewById(R.id.button_reset).setOnClickListener(this);
        view.findViewById(R.id.button_close).setOnClickListener(this);

        mColorPickerView = view.findViewById(R.id.color_picker_view);
        if (mEntity.attributes.rgbColors != null) {
            mDefaultColor = Color.rgb(
                    mEntity.attributes.rgbColors.get(0).intValue(),
                    mEntity.attributes.rgbColors.get(1).intValue(),
                    mEntity.attributes.rgbColors.get(2).intValue());
        } else {
            view.findViewById(R.id.button_reset).setVisibility(View.GONE);
        }

        mColorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int selectedColor) {
                //Log.d("YouQi", "addOnColorChangedListener:" + Integer.toHexString(selectedColor));
                //int selectedColor = mColorPickerView.getSelectedColor();
                callService(mEntity.getDomain(), "turn_on", new CallServiceRequest(mEntity.entityId).setRGBColor(selectedColor));
            }
        });

        mPowerButton = view.findViewById(R.id.text_light);
        mPowerButton.setOnClickListener(this);

        mBrightnessLayout = view.findViewById(R.id.layout_brightness);
        mBrightnessSeekBar = view.findViewById(R.id.seekbar_brightness);
        mBrightnessSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if (seekBar.getProgress() == 0) {
                    callService("light", "turn_off", new CallServiceRequest(mEntity.entityId));
                } else {
                    callService("light", "turn_on", new CallServiceRequest(mEntity.entityId).setBrightness(seekBar.getProgress()));
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        mColorTemperatureLayout = view.findViewById(R.id.layout_temperature);
        mColorTemperatureSeekBar = view.findViewById(R.id.seekbar_temperature);
        mColorTemperatureSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                callService("light", "turn_on", new CallServiceRequest(mEntity.entityId).setColorTemperature(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        refreshUi();

        return builder.create();
    }

    public void refreshUi() {
        if (!mEntity.isActivated()) {
            //mPowerButton.setActivated(true);
            mPowerButton.setText(MDIFont.getIcon("mdi:lightbulb-outline"));
        } else {
            //mPowerButton.setActivated(false);
            mPowerButton.setText(MDIFont.getIcon("mdi:lightbulb"));
        }

        if (mEntity.attributes.brightness != null) {
            mBrightnessLayout.setVisibility(View.VISIBLE);
            mBrightnessSeekBar.setProgress(mEntity.attributes.brightness.intValue());
        } else {
            mBrightnessLayout.setVisibility(View.GONE);
        }

        if (mEntity.attributes.colorTemp != null) {
            mColorTemperatureLayout.setVisibility(View.VISIBLE);
            mColorTemperatureSeekBar.setProgress(mEntity.attributes.colorTemp.intValue());
        } else {
            mColorTemperatureLayout.setVisibility(View.GONE);
        }

        if (mEntity.attributes.rgbColors != null) {
            int color = Color.rgb(
                    mEntity.attributes.rgbColors.get(0).intValue(),
                    mEntity.attributes.rgbColors.get(1).intValue(),
                    mEntity.attributes.rgbColors.get(2).intValue());

            mColorPickerView.setColor(color, true);
        }


//        items.put("lightbulb", "\uF335");
//        items.put("lightbulb-on", "\uF6E7");
//        items.put("lightbulb-on-outline", "\uF6E8");
//        items.put("lightbulb-outline", "\uF336");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_light:
                callService(mEntity.getDomain(), mEntity.getNextState(), new CallServiceRequest(mEntity.entityId));
                break;

            case R.id.button_reset:
                mColorPickerView.setColor(mDefaultColor, true);
                break;
            case R.id.button_close:
                dismiss();
        }
    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        Log.d("YouQi", "Received LightBulb State Change!");
        refreshUi();
    }
}
