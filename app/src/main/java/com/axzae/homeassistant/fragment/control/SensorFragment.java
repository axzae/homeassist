package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.provider.ServiceProvider;
import com.axzae.homeassistant.util.CommonUtil;
import com.axzae.homeassistant.util.FaultUtil;

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
public class SensorFragment extends BaseControlFragment implements View.OnClickListener {

    private Call<ArrayList<ArrayList<Entity>>> mCall;
    private View mProgressBar;
    private LineChartView mChart;
    private ViewGroup mEmptyView;
    private ViewGroup mConnErrorView;
    private HomeAssistantServer mServer;

    public static SensorFragment newInstance(Entity entity, HomeAssistantServer server) {
        SensorFragment fragment = new SensorFragment();
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
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_sensor, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        mChart = rootView.findViewById(R.id.chart);
        mEmptyView = rootView.findViewById(R.id.list_empty);
        mConnErrorView = rootView.findViewById(R.id.list_conn_error);

        //rootView.findViewById(R.id.button_cancel).setOnClickListener(this);
        //rootView.findViewById(R.id.button_set).setOnClickListener(this);
        mProgressBar = rootView.findViewById(R.id.progressbar);
        callService();

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
                    //CommonUtil.logLargeString("YouQi", "HISTORY restResponse: " + CommonUtil.deflate(restResponse));
                    if (restResponse != null && restResponse.size() > 0) {
                        ArrayList<Entity> histories = restResponse.get(0);
                        if (histories.size() <= 1) {
                            mEmptyView.setVisibility(View.VISIBLE);
                        } else {
                            setupChart(restResponse.get(0));
                        }
                    } else {
                        mEmptyView.setVisibility(View.VISIBLE);
                    }


                }

                @Override
                public void onFailure(@NonNull Call<ArrayList<ArrayList<Entity>>> call, @NonNull Throwable t) {
                    mCall = null;
                    mProgressBar.setVisibility(View.GONE);

                    mConnErrorView.setVisibility(View.VISIBLE);

                    Activity activity = getActivity();
                    if (activity != null && !activity.isFinishing()) {
                        Log.d("YouQi", FaultUtil.getPrintableMessage(getActivity(), t));
                    }
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
        DateFormat dfShort = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH));
        ArrayList<DataItem> mData = new ArrayList<>();
        for (Entity history : histories) {
            try {
                if (history.lastUpdated.length() == 25) {
                    mData.add(new DataItem(dfShort.parse(history.lastUpdated), Float.parseFloat(history.state)));
                } else {
                    mData.add(new DataItem(df.parse(history.lastUpdated), Float.parseFloat(history.state)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<PointValue> yValues = new ArrayList<>();
        List<AxisValue> axisValues = new ArrayList<>();
        for (int x = 0; x < mData.size(); ++x) {
            DataItem dataItem = mData.get(x);
            float yValue = dataItem.getYValue();
            yValues.add(new PointValue(dataItem.getXValue(), yValue));

//            if (x == 0 || x == mData.size() - 1) {
//                AxisValue axisValue = new AxisValue(dataItem.getXValue());
//                axisValue.setLabel(dataItem.getLabel());
//                axisValues.add(axisValue);
//            }
        }


        List<PointValue> values = new ArrayList<>();
        for (int x = 0; x < mData.size(); ++x) {
            DataItem dataItem = mData.get(x);

            values.add(new PointValue(dataItem.getXValue(), dataItem.getYValue()));
        }

        //In most cased you can call data model methods in builder-pattern-like manner.
        Line line = new Line(values).setColor(Color.parseColor("#3366cc")).setCubic(false).setHasLabels(true).setHasPoints(false).setCubic(false);
        List<Line> lines = new ArrayList<>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        data.setLines(lines);


        Axis axisY = new Axis().setHasLines(true);
        axisY.setName(mEntity.attributes.unitOfMeasurement);
        data.setAxisYLeft(axisY);

        Axis axisX;

        if (mData.size() != 0) {
            long startTime = mData.get(0).getXValue();
            long endTime = mData.get(mData.size() - 1).getXValue();
            long step = (endTime - startTime) / 10;
            List<AxisValue> xValues = new ArrayList<>();
            SimpleDateFormat df2 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

            for (long i = startTime; i < endTime; i += step) {
                xValues.add(new AxisValue(i).setLabel(df2.format(new Date(i))));
            }
            xValues.add(new AxisValue(endTime).setLabel(df2.format(new Date(endTime))));
            axisX = new Axis(xValues);
            axisX.setName("Time (Last 24 Hours)");
        } else {
            axisX = new Axis().setName("Time (Last 24 Hours)");
        }

        data.setAxisXBottom(axisX);

        mChart.setLineChartData(data);
        mChart.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_cancel:
                dismiss();
                break;
            case R.id.button_set:
                dismiss();
        }
    }
}
