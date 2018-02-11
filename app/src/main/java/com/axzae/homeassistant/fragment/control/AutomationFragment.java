package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.util.CommonUtil;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AutomationFragment extends BaseControlFragment implements View.OnClickListener {

    private SwitchCompat mSwitchToggle;
    private CompoundButton.OnCheckedChangeListener mCheckdChangelistener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (mEntity != null)
                callService(mEntity.getDomain(), "turn_" + (b ? "on" : "off"), new CallServiceRequest(mEntity.entityId));
        }
    };

    public static AutomationFragment newInstance(Entity entity) {
        AutomationFragment fragment = new AutomationFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_automation, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        mSwitchToggle = rootView.findViewById(R.id.switch_toggle);
        rootView.findViewById(R.id.button_close).setOnClickListener(this);
        rootView.findViewById(R.id.button_trigger).setOnClickListener(this);

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

        mSwitchToggle.setOnCheckedChangeListener(mCheckdChangelistener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_close:
                dismiss();
                break;
            case R.id.button_trigger:
                callService(mEntity.getDomain(), "trigger", new CallServiceRequest(mEntity.entityId));
                break;
        }
    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        refreshUi();
    }
}
