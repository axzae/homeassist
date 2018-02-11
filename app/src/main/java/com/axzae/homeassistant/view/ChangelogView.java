package com.axzae.homeassistant.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.axzae.homeassistant.R;

import java.util.List;

public class ChangelogView extends LinearLayout {

    public ChangelogView(Context context) {
        super(context);
        init(context);
    }

    public ChangelogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        //Setup default parameters
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(VERTICAL);
    }

    public void loadLogs(List<String> answers) {
        Log.d("YouQi", "loadLogs called");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        removeAllViews();
        for (String answer : answers) {
            View row = (View) inflater.inflate(R.layout.item_changelog_row, this, false);
            TextView rowView = (TextView) row.findViewById(R.id.text_log);
            rowView.setText(answer);
            addView(row);
        }
    }

}
