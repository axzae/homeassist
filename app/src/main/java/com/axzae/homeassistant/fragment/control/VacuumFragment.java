package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.util.CommonUtil;

import java.util.ArrayList;

/**
 * Simple fragment with blur effect behind.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VacuumFragment extends BaseControlFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private TextView mButtonStartPause;
    private TextView mButtonStop;
    private TextView mButtonCleanSpot;
    private TextView mButtonLocate;
    private TextView mButtonHome;
    private Spinner mSpinnerSpeed;
    private ArrayList<String> mSpeeds;
    private SwitchCompat mSwitchToggle;

    public static VacuumFragment newInstance(Entity entity) {
        VacuumFragment fragment = new VacuumFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_vacuum, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        rootView.findViewById(R.id.button_close).setOnClickListener(this);
        mSwitchToggle = rootView.findViewById(R.id.switch_toggle);
        mButtonStartPause = rootView.findViewById(R.id.text_start_pause);
        mButtonStop = rootView.findViewById(R.id.text_stop);
        mButtonCleanSpot = rootView.findViewById(R.id.text_clean_spot);
        mButtonLocate = rootView.findViewById(R.id.text_locate);
        mButtonHome = rootView.findViewById(R.id.text_home);
        mSpinnerSpeed = rootView.findViewById(R.id.spinner_speed);

        mButtonStartPause.setOnClickListener(this);
        mButtonStop.setOnClickListener(this);
        mButtonCleanSpot.setOnClickListener(this);
        mButtonLocate.setOnClickListener(this);
        mButtonHome.setOnClickListener(this);

        mSpeeds = new ArrayList<>();
        for (String speed : mEntity.attributes.fanSpeedList) {
            mSpeeds.add(CommonUtil.getNameTitleCase(speed));
        }
        //SpeedAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_edittext_lookalike, speeds);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_edittext_lookalike, mSpeeds);
        mSpinnerSpeed.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSpeed.setOnItemSelectedListener(this);

        refreshUi();

        return builder.create();
    }

    private void refreshUi() {

        mSwitchToggle.setOnCheckedChangeListener(null);

        if ("ON".equals(mEntity.getFriendlyState())) {
            if (!mSwitchToggle.isChecked()) mSwitchToggle.setChecked(true);
        } else {
            if (mSwitchToggle.isChecked()) mSwitchToggle.setChecked(false);
        }

        mSwitchToggle.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_close:
                dismiss();
                break;
            case R.id.text_start_pause:
                callService(mEntity.getDomain(), "start_pause", new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_stop:
                callService(mEntity.getDomain(), "stop", new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_clean_spot:
                callService(mEntity.getDomain(), "clean_spot", new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_locate:
                callService(mEntity.getDomain(), "locate", new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_home:
                callService(mEntity.getDomain(), "return_to_base", new CallServiceRequest(mEntity.entityId));
                break;
        }
    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        refreshUi();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("YouQi", "position: " + mSpeeds.get(position));
        callService(mEntity.getDomain(), "set_fan_speed", new CallServiceRequest(mEntity.entityId).setFanSpeed(mSpeeds.get(position).toLowerCase()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (mEntity != null)
            callService("homeassistant", "turn_" + (b ? "on" : "off"), new CallServiceRequest(mEntity.entityId));
    }

    private class SpeedAdapter extends ArrayAdapter<String> {

        private ArrayList<String> allItems;

        SpeedAdapter(Context context, int resource, ArrayList<String> objects) {
            super(context, resource, objects);
            allItems = objects;
        }
    }


}
