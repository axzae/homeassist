package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
public class FanFragment extends BaseControlFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private TextView mButtonOff;
    private TextView mButtonOn;
    private Spinner mSpinnerSpeed;
    private ArrayList<String> mSpeeds;

    public static FanFragment newInstance(Entity entity) {
        FanFragment fragment = new FanFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_fan, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        rootView.findViewById(R.id.button_close).setOnClickListener(this);
        mButtonOff = rootView.findViewById(R.id.text_off);
        mButtonOn = rootView.findViewById(R.id.text_on);
        mSpinnerSpeed = rootView.findViewById(R.id.spinner_speed);

        mButtonOff.setOnClickListener(this);
        mButtonOn.setOnClickListener(this);


        mSpeeds = new ArrayList<>();
        for (String speed : mEntity.attributes.speedList) {
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
        if (mEntity.isCurrentStateActive()) {
            mButtonOn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
            mButtonOff.setTextColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_500, null));
        } else {
            mButtonOff.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
            mButtonOn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_500, null));
        }

        mSpinnerSpeed.setOnItemSelectedListener(null);
        mSpinnerSpeed.setSelection(mSpeeds.indexOf(mEntity.state));
        mSpinnerSpeed.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_close:
                dismiss();
                break;
            case R.id.text_off:
                callService("homeassistant", "turn_off", new CallServiceRequest(mEntity.entityId));
                break;
            case R.id.text_on:
                callService("homeassistant", "turn_on", new CallServiceRequest(mEntity.entityId));
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
        callService(mEntity.getDomain(), "turn_on", new CallServiceRequest(mEntity.entityId).setSpeed(mSpeeds.get(position).toLowerCase()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class SpeedAdapter extends ArrayAdapter<String> {

        private ArrayList<String> allItems;

        SpeedAdapter(Context context, int resource, ArrayList<String> objects) {
            super(context, resource, objects);
            allItems = objects;
        }

//        @Override
//        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
//            if (convertView == null) {
//                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                convertView = vi.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
//            }
//            ((TextView) convertView).setText(allItems.get(position).colour);
//            return convertView;
//        }

//        @Override
//        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
//            if (convertView == null) {
//                convertView = View.inflate(getContext(), R.layout.spinner_edittext_lookalike, null);
//            }
//
//            ((TextView) convertView).setText(allItems.get(position));
//            return convertView;
//        }
    }


}
