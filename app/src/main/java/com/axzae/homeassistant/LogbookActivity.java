package com.axzae.homeassistant;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.LogSheet;
import com.axzae.homeassistant.model.MDIFont;
import com.axzae.homeassistant.provider.DatabaseManager;
import com.axzae.homeassistant.provider.ServiceProvider;
import com.axzae.homeassistant.shared.LogSheetDiffUtilCallback;
import com.axzae.homeassistant.util.CommonUtil;
import com.axzae.homeassistant.util.FaultUtil;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogbookActivity extends AppCompatActivity {

    HomeAssistantServer mCurrentServer;
    private Call<ArrayList<LogSheet>> mCall;
    private ProgressBar mProgressBar;
    private View mEmptyList;
    private View mConnError;
    private RecyclerView mRecyclerView;
    private LogsheetAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private HashMap<String, Entity> mEntities = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logbook);
        mProgressBar = findViewById(R.id.progressbar);
        mEmptyList = findViewById(R.id.list_empty);
        mConnError = findViewById(R.id.list_conn_error);

        Bundle params = new Bundle();
        params.putString("name", this.getClass().getName());
        FirebaseAnalytics.getInstance(this).logEvent("open_activity", params);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mCurrentServer = CommonUtil.inflate(bundle.getString("server", ""), HomeAssistantServer.class);
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.menu_logbook));
        }

        setupRecyclerView();
        refreshApi();
    }

    public void setupRecyclerView() {
        mSwipeRefresh = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshApi();
            }
        });


        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new LogsheetAdapter(null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //int id = item.getItemId();
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) {
            overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
        }
    }

    public void refreshApi() {
        DateFormat df = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH));
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date currentDate = Calendar.getInstance().getTime();
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        Log.d("YouQi", "Date: " + df.format(now.getTime()));

        if (mCall == null) {
            //showNetworkBusy();
            mCall = ServiceProvider.getApiService(mCurrentServer.getBaseUrl()).getLogbook(mCurrentServer.getBearerHeader(), df.format(now.getTime()));
            mCall.enqueue(new Callback<ArrayList<LogSheet>>() {
                @Override
                public void onResponse(@NonNull Call<ArrayList<LogSheet>> call, @NonNull Response<ArrayList<LogSheet>> response) {
                    mCall = null;
                    showNetworkIdle();
                    mConnError.setVisibility(View.GONE);

                    if (FaultUtil.isRetrofitServerError(response)) {
                        showError(response.message());
                        return;
                    }

                    ArrayList<LogSheet> restResponse = response.body();
                    CommonUtil.logLargeString("YouQi", "service restResponse: " + CommonUtil.deflate(restResponse));

                    if (restResponse != null) {
                        if (restResponse.size() == 0) {
                            mEmptyList.setVisibility(View.VISIBLE);
                        } else {
                            mEmptyList.setVisibility(View.GONE);

                            Log.d("YouQi", "restResponse.size: " + restResponse.size());


                            Collections.sort(restResponse, new Comparator<LogSheet>() {
                                @Override
                                public int compare(LogSheet lhs, LogSheet rhs) {
                                    return (int) (rhs.when.getTime() - lhs.when.getTime()); //descending order
                                }
                            });

                            mAdapter.setItems(restResponse);

                            //for (LogSheet logsheet : restResponse) {
                            //    Log.d("YouQi", String.format(Locale.ENGLISH, "%s, %s", df2.format(logsheet.when.getTime()), DateUtils.getRelativeTimeSpanString(logsheet.when.getTime())));
                            //    //getContentResolver().update(Uri.parse("content://com.axzae.homeassistant.provider.EntityContentProvider/"), entity.getContentValues(), "ENTITY_ID='" + entity.entityId + "'", null);
                            //}
                        }
                    }

                }

                @Override
                public void onFailure(@NonNull Call<ArrayList<LogSheet>> call, @NonNull Throwable t) {
                    mCall = null;
                    showNetworkIdle();
                    mConnError.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void showError(String message) {
        mConnError.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showNetworkBusy() {
        mSwipeRefresh.setRefreshing(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void showNetworkIdle() {
        mSwipeRefresh.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
    }


    class LogbookViewHolder extends RecyclerView.ViewHolder {
        //private final View rootView;
        ViewGroup mItemView;
        TextView mIconView;
        TextView mNameView;
        TextView mSubText;
        TextView mStateText;

        LogbookViewHolder(View v) {
            super(v);
            //rootView = v;
            mIconView = v.findViewById(R.id.text_mdi);
            mItemView = v.findViewById(R.id.item);
            mNameView = v.findViewById(R.id.main_text);
            mSubText = v.findViewById(R.id.sub_text);
            mStateText = v.findViewById(R.id.state_text);
        }
    }

    private Entity getEntity(String entityId) {
        Entity result = mEntities.get(entityId);

        if (result == null) {
            DatabaseManager databaseManager = DatabaseManager.getInstance(LogbookActivity.this);
            Entity entity = databaseManager.getEntityById(entityId);
            mEntities.put(entityId, entity);
            result = entity;
        }

        return result;
    }

    private class LogsheetAdapter extends RecyclerView.Adapter<LogbookViewHolder> {
        private List<LogSheet> items;
        private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss aa", Locale.ENGLISH);

        LogsheetAdapter(List<LogSheet> items) {
            if (items == null) items = new ArrayList<>();
            this.items = items;
        }

        public void setItems(List<LogSheet> newItems) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new LogSheetDiffUtilCallback(items, newItems));
            this.items.clear();
            this.items.addAll(newItems);
            diffResult.dispatchUpdatesTo(this);
            //this.items = items;
            //notifyDataSetChanged();
        }

        @Override
        public LogbookViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            Log.d("YouQi", "Created ViewHolder Library");
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_logbook, viewGroup, false);
            return new LogbookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final LogbookViewHolder viewHolder, final int position) {
            final LogSheet logSheet = items.get(position);
            Log.d("YouQi", "rendering: " + position + "logSheet.message: " + logSheet.message);
            //viewHolder.mNameView.setText(logSheet.name);
            viewHolder.mStateText.setText(TextUtils.concat(dateFormat.format(logSheet.when.getTime()), "\n", CommonUtil.getSpanText(LogbookActivity.this, DateUtils.getRelativeTimeSpanString(logSheet.when.getTime()).toString(), null, 0.9f)));
            viewHolder.mIconView.setText(MDIFont.getIcon("mdi:information-outline"));

            Entity entity = getEntity(logSheet.entityId);
            if (entity != null) {
                viewHolder.mIconView.setText(entity.getMdiIcon());
            }


            Spannable wordtoSpan1 = new SpannableString(logSheet.name);
            wordtoSpan1.setSpan(new RelativeSizeSpan(1.0f), 0, wordtoSpan1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            wordtoSpan1.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, wordtoSpan1.length(), 0);

            Spannable wordtoSpan2 = new SpannableString(logSheet.message);
            wordtoSpan2.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.md_grey_500, null)), 0, wordtoSpan2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            wordtoSpan2.setSpan(new RelativeSizeSpan(0.95f), 0, wordtoSpan2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            //viewHolder.mNameView.setText(TextUtils.concat(wordtoSpan1, " ", wordtoSpan2));
            viewHolder.mNameView.setText(logSheet.name);
            viewHolder.mSubText.setText(logSheet.message);

        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }
    }


}
