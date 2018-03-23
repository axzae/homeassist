package com.axzae.homeassistant;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.Widget;
import com.axzae.homeassistant.provider.DatabaseManager;
import com.axzae.homeassistant.provider.SensorWidgetProvider;

import java.util.ArrayList;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class SensorWidgetConfigActivity extends BaseActivity {

    private ListView mListView;
    private EntityListAdapter mAdapter;
    private DatabaseManager mDatabaseManager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_widget_config, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);
        setResult(RESULT_CANCELED);
        mDatabaseManager = DatabaseManager.getInstance(this).forceCreate();

        setupToolbar();
        initListViews();
    }

    private void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Select Widget Control");
            //getSupportActionBar().setSubtitle("Add New Widget");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                performDone();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return true;
    }

    public void initListViews() {
        final ArrayList<Entity> mEntities = mDatabaseManager.getSensors();
        mAdapter = new EntityListAdapter(mEntities);

        mListView = findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);
    }

    private void performDone() {

        int pos = mListView.getCheckedItemPosition();
        if (pos == -1) {
            Toast.makeText(this, "Please select a control", Toast.LENGTH_SHORT).show();
            return;
        }
        Entity selectedItem = (Entity) mAdapter.getItem(pos);

        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);

            mDatabaseManager.insertWidget(appWidgetId, selectedItem, "SENSOR");
            Widget widget = Widget.getInstance(selectedItem, appWidgetId);
            SensorWidgetProvider.updateEntityWidget(this, widget);

            setResult(RESULT_OK);
            finish();
        }
        if (appWidgetId == INVALID_APPWIDGET_ID) {
            Log.i("I am invalid", "I am invalid");
            finish();
        }
    }


    private class EntityListAdapter extends BaseAdapter {
        private ArrayList<Entity> items = new ArrayList<>();

        EntityListAdapter(ArrayList<Entity> items) {
            this.items = items;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.item_widget_select, null);
            }

            Entity entity = items.get(position);

            ViewGroup mItemView = convertView.findViewById(R.id.item);
            TextView mIconView = convertView.findViewById(R.id.text_mdi);
            TextView mMainText = convertView.findViewById(R.id.main_text);
            TextView mLabelText = convertView.findViewById(R.id.sub_text);

            mIconView.setText(entity.getMdiIcon());

            mItemView.setClickable(false);
            mItemView.setFocusable(false);
            mMainText.setText(entity.attributes.friendlyName);
            mLabelText.setText(entity.getDomain());

            //((TextView) convertView).setText(allItems.get(position).colour);
            return convertView;
        }

        public Context getContext() {
            return SensorWidgetConfigActivity.this;
        }
    }

}
