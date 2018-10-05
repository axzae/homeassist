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
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.provider.ServiceProvider;
import com.axzae.homeassistant.util.CommonUtil;
import com.axzae.homeassistant.util.FaultUtil;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ClimateFragment extends BaseControlFragment implements View.OnClickListener {

    private Call<ArrayList<ArrayList<Entity>>> mCall;
    private View mProgressBar;
    private HomeAssistantServer mServer;
    private LineChartView mChart;
    private ViewGroup mEmptyView;
    private ViewGroup mConnErrorView;
    private TextView mTargetState;
    private TextView mCurrentState;

    public static ClimateFragment newInstance(Entity entity, HomeAssistantServer server) {
        ClimateFragment fragment = new ClimateFragment();
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_climate, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        mChart = rootView.findViewById(R.id.chart);
        mTargetState = rootView.findViewById(R.id.text_target_state);
        mCurrentState = rootView.findViewById(R.id.text_current_temperature);
        mEmptyView = rootView.findViewById(R.id.list_empty);
        mConnErrorView = rootView.findViewById(R.id.list_conn_error);

        rootView.findViewById(R.id.text_minus).setOnClickListener(this);
        rootView.findViewById(R.id.text_plus).setOnClickListener(this);
        mProgressBar = rootView.findViewById(R.id.progressbar);
        callService();
        refreshUi();

        return builder.create();
    }

    public void callService() {
        if (mCall == null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mCall = ServiceProvider.getApiService(mServer.getBaseUrl()).getHistory(mServer.getBearerHeader(), mEntity.entityId);
            mCall.enqueue(new Callback<ArrayList<ArrayList<Entity>>>() {
                @Override
                public void onResponse(@NonNull Call<ArrayList<ArrayList<Entity>>> call, @NonNull Response<ArrayList<ArrayList<Entity>>> response) {
                    mCall = null;
                    mProgressBar.setVisibility(View.GONE);

                    if (FaultUtil.isRetrofitServerError(response)) {
                        Log.d("YouQi", response.message());
//                        showError(response.message());
                        return;
                    }

                    ArrayList<ArrayList<Entity>> restResponse = response.body();
                    CommonUtil.logLargeString("YouQi", "HISTORY restResponse: " + CommonUtil.deflate(restResponse));

                    if (restResponse != null && restResponse.size() > 0) {
                        ArrayList<Entity> histories = restResponse.get(0);
                        if (histories.size() <= 1) {
                            mEmptyView.setVisibility(View.VISIBLE);
                        } else {
                            setupChart(histories);
                        }
                    } else {
                        mEmptyView.setVisibility(View.VISIBLE);
                    }


                }

                @Override
                public void onFailure(@NonNull Call<ArrayList<ArrayList<Entity>>> call, @NonNull Throwable t) {
                    mCall = null;
                    mProgressBar.setVisibility(View.GONE);

                    t.printStackTrace();
                    mConnErrorView.setVisibility(View.VISIBLE);
                    Log.d("YouQi", FaultUtil.getPrintableMessage(getActivity(), t));
//                    showError(FaultUtil.getPrintableMessage(t));
                }
            });
        }


        //ContentValues values = new ContentValues();
        //values.put(HabitTable.TIME); //whatever column you want to update, I dont know the name of it
        //getContentResolver().update(HabitTable.CONTENT_URI,values,HabitTable.ID+"=?",new String[] {String.valueOf(id)}); //id is the id of the row you wan to update
        //getContentResolver().update()
    }

    private class DataItem {
        public SimpleDateFormat df = new SimpleDateFormat("MMM-dd HH:mm", Locale.ENGLISH);
        public Date date;
        public Float value;

        DataItem(Date date, Float value) {
            this.date = date;
            this.value = value;
        }

        public String getLabel() {
            return df.format(date);
        }

        public long getXValue() {
            return date.getTime();
        }

        public Float getYValue() {
            return value;
        }
    }

    private void setupChart(ArrayList<Entity> histories) {

        DateFormat df = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ", Locale.ENGLISH));
        ArrayList<DataItem> mDataCurrent = new ArrayList<>();
        ArrayList<DataItem> mDataTarget = new ArrayList<>();
        for (Entity history : histories) {
            try {
                if (history.attributes.currentTemperature != null) {
                    mDataCurrent.add(new DataItem(df.parse(history.lastUpdated), history.attributes.currentTemperature.floatValue()));
                }

                mDataTarget.add(new DataItem(df.parse(history.lastUpdated), history.attributes.getTemperature().floatValue()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Line currentLine = getLine(mDataCurrent, "#3366cc");
        Line targetLine = getLine(mDataTarget, "#dc3912");

        List<PointValue> yValues = new ArrayList<>();
        List<AxisValue> axisValues = new ArrayList<>();
        for (int x = 0; x < mDataTarget.size(); ++x) {
            DataItem dataItem = mDataTarget.get(x);
            float yValue = dataItem.getYValue();
            yValues.add(new PointValue(dataItem.getXValue(), yValue));
        }

        //In most cased you can call data model methods in builder-pattern-like manner.
        List<Line> lines = new ArrayList<>();
        lines.add(currentLine);
        lines.add(targetLine);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axisY = new Axis().setHasLines(true);
        axisY.setName(mEntity.attributes.unitOfMeasurement);
        data.setAxisYLeft(axisY);

        Axis axisX;

        if (mDataCurrent.size() != 0) {
            axisX = getXAxis(mDataCurrent);
        } else if (mDataTarget.size() != 0) {
            axisX = getXAxis(mDataTarget);
        } else {
            axisX = new Axis().setName("Time (Last 24 Hours)");
        }

        data.setAxisXBottom(axisX);

        mChart.setLineChartData(data);
        mChart.setVisibility(View.VISIBLE);
    }

    private Axis getXAxis(ArrayList<DataItem> data) {
        long startTime = data.get(0).getXValue();
        long endTime = data.get(data.size() - 1).getXValue();
        long step = (endTime - startTime) / 10;
        List<AxisValue> xValues = new ArrayList<>();
        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

        for (long i = startTime; i < endTime; i += step) {
            xValues.add(new AxisValue(i).setLabel(df2.format(new Date(i))));
        }
        xValues.add(new AxisValue(endTime).setLabel(df2.format(new Date(endTime))));
        Axis axisX = new Axis(xValues);
        axisX.setName("Time (Last 24 Hours)");
        return axisX;
    }

    private Line getLine(ArrayList<DataItem> dataItems, String color) {

        List<PointValue> values = new ArrayList<>();
        for (int x = 0; x < dataItems.size(); ++x) {
            DataItem dataItemCurrent = dataItems.get(x);
            values.add(new PointValue(dataItemCurrent.getXValue(), dataItemCurrent.getYValue()));
        }

        return new Line(values).setColor(Color.parseColor(color)).setCubic(false).setHasLabels(true).setHasPoints(false).setCubic(false);
    }

    private void refreshUi() {

        if (mEntity.attributes.getTemperature() != null) {
            String result = String.format(Locale.ENGLISH, "%s %s", mEntity.attributes.getTemperature().setScale(1, BigDecimal.ROUND_UP).toString(), mEntity.attributes.unitOfMeasurement);
            mTargetState.setText(result);
            mTargetState.setVisibility(View.VISIBLE);
        } else {
            mTargetState.setVisibility(View.GONE);
        }

        if (mEntity.attributes.currentTemperature != null) {
            String currentState = String.format(Locale.ENGLISH, "%s %s", mEntity.attributes.currentTemperature.setScale(1, BigDecimal.ROUND_UP).toString(), mEntity.attributes.unitOfMeasurement);
            mCurrentState.setText(currentState);
            mCurrentState.setVisibility(View.VISIBLE);
        } else {
            mCurrentState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_cancel:
                dismiss();
                break;

            case R.id.button_set:
                dismiss();
                break;

            case R.id.text_minus:
                callService("climate", "set_temperature", new CallServiceRequest(mEntity.entityId).setTemperature(mEntity.attributes.getTemperature().subtract(new BigDecimal(0.5))));
                break;

            case R.id.text_plus:
                callService("climate", "set_temperature", new CallServiceRequest(mEntity.entityId).setTemperature(mEntity.attributes.getTemperature().add(new BigDecimal(0.5))));
                break;
        }
    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        refreshUi();
    }

}
