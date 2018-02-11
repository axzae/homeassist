package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.util.CommonUtil;

/**
 * Simple fragment with blur effect behind.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CoverFragment extends BaseControlFragment implements View.OnClickListener {

    private TextView mButtonUp;
    private TextView mButtonDown;
    private TextView mButtonStop;

    public static CoverFragment newInstance(Entity entity) {
        CoverFragment fragment = new CoverFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_cover, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        rootView.findViewById(R.id.button_close).setOnClickListener(this);
        mButtonUp = rootView.findViewById(R.id.text_up);
        mButtonDown = rootView.findViewById(R.id.text_down);
        mButtonStop = rootView.findViewById(R.id.text_stop);

        mButtonUp.setOnClickListener(this);
        mButtonDown.setOnClickListener(this);
        mButtonStop.setOnClickListener(this);

        refreshUi();

        return builder.create();
    }

    private void refreshUi() {
        mButtonStop.setTextColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_800, null));

        if ("open".equals(mEntity.state)) {
            mButtonUp.setEnabled(false);
            mButtonDown.setEnabled(true);
            mButtonUp.setTextColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_500, null));
            mButtonDown.setTextColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_800, null));
        }

        if ("closed".equals(mEntity.state)) {
            mButtonUp.setEnabled(true);
            mButtonDown.setEnabled(false);
            mButtonUp.setTextColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_800, null));
            mButtonDown.setTextColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_500, null));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_close:
                dismiss();
                break;
            case R.id.text_up:
                callService(mEntity.getDomain(), "open_cover", new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_down:
                callService(mEntity.getDomain(), "close_cover", new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_stop:
                callService(mEntity.getDomain(), "stop_cover", new CallServiceRequest(mEntity.entityId));
                break;
        }
    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        refreshUi();
    }

}
