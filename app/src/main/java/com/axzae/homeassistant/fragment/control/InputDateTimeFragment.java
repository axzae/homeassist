package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.util.CommonUtil;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class InputDateTimeFragment extends BaseControlFragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private EditText mInput;
    private TextView mDateView;
    private TextView mTimeView;
    private View mDateLayout;
    private View mTimeLayout;

    public static InputDateTimeFragment newInstance(Entity entity) {
        InputDateTimeFragment fragment = new InputDateTimeFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_input_datetime, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        mDateLayout = rootView.findViewById(R.id.layout_date);
        mTimeLayout = rootView.findViewById(R.id.layout_time);
        mDateView = rootView.findViewById(R.id.text_date_value);
        mTimeView = rootView.findViewById(R.id.text_time_value);

        mDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar now = Calendar.getInstance();
                Integer year = mEntity.attributes.year == null ? now.get(Calendar.YEAR) : mEntity.attributes.year;
                Integer month = mEntity.attributes.month == null ? now.get(Calendar.MONTH) : (mEntity.attributes.month - 1);
                Integer day = mEntity.attributes.day == null ? now.get(Calendar.DAY_OF_MONTH) : mEntity.attributes.day;
                DatePickerDialog dpd = DatePickerDialog.newInstance(InputDateTimeFragment.this, year, month, day);
                dpd.setVersion(DatePickerDialog.Version.VERSION_2);
                dpd.show(getFragmentManager(), "Datepickerdialog");

            }
        });

        mTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Integer hour = mEntity.attributes.hour == null ? 0 : mEntity.attributes.hour;
                Integer minute = mEntity.attributes.minute == null ? 0 : mEntity.attributes.minute;
                TimePickerDialog tpd = TimePickerDialog.newInstance(InputDateTimeFragment.this, hour, minute, 0, true);
                tpd.setVersion(TimePickerDialog.Version.VERSION_2);
                tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        });

        rootView.findViewById(R.id.button_close).setOnClickListener(this);
        refreshUi();

        return builder.create();
    }

    private void refreshUi() {

//        int year = 2017, month = 9, day = 28, hour = 22, minute = 45;
//        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);
//        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
//        System.out.println(dateTime.format(formatter));

        if (mEntity.attributes.hasDate) {
            String strDate = "Unknown";
            if (mEntity.attributes.getTimestampForInputDateTime() != null) {
                Date date = new Date(mEntity.attributes.getTimestampForInputDateTime() * 1000);
                DateFormat.getMediumDateFormat(getActivity()).format(date);
                strDate = DateFormat.getMediumDateFormat(getActivity()).format(date);
            }
            mDateLayout.setVisibility(View.VISIBLE);
            mDateView.setText(strDate);
        } else {
            mDateLayout.setVisibility(View.GONE);
        }

        if (mEntity.attributes.hasTime) {
            String strTime = "Unknown";
            if (mEntity.attributes.hour != null) {
                strTime = String.format(Locale.ENGLISH, "%02d:%02d %s", mEntity.attributes.hour, mEntity.attributes.minute, mEntity.attributes.hour < 12 ? "AM" : "PM");
            }
            mTimeLayout.setVisibility(View.VISIBLE);
            mTimeView.setText(strTime);
        } else {
            mTimeLayout.setVisibility(View.GONE);
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

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        if (mEntity.attributes.hasTime) {
            callService(mEntity.getDomain(), "set_datetime", new CallServiceRequest(mEntity.entityId).setDate(String.format(Locale.ENGLISH, "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)).setTime(mEntity.attributes.hour + ":" + mEntity.attributes.minute));
        } else {
            callService(mEntity.getDomain(), "set_datetime", new CallServiceRequest(mEntity.entityId).setDate(String.format(Locale.ENGLISH, "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)));
        }
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

        if (mEntity.attributes.hasDate) {
            callService(mEntity.getDomain(), "set_datetime", new CallServiceRequest(mEntity.entityId).setDate(String.format(Locale.ENGLISH, "%d-%02d-%02d", mEntity.attributes.year, mEntity.attributes.month, mEntity.attributes.day)).setTime(hourOfDay + ":" + minute));
        } else {
            callService(mEntity.getDomain(), "set_datetime", new CallServiceRequest(mEntity.entityId).setTime(hourOfDay + ":" + minute));
        }
    }
}
