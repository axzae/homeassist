package com.axzae.homeassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.provider.EntityContentProvider;
import com.axzae.homeassistant.util.CommonUtil;

public class EntityActivity extends BaseActivity {
    private TextView mTextGroup;
    private TextView mTextState;
    private Entity mEntity;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu_main, menu);
        //CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_edit), R.color.md_white_1000);
        //CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_refresh), R.color.md_white_1000);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity);

        mTextState = findViewById(R.id.text_state);
        mTextGroup = findViewById(R.id.text_group);

        String json = getIntent().getExtras().getString("entity");
        mEntity = CommonUtil.inflate(json, Entity.class);

        //Setup Toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(mEntity.getFriendlyName());
            getSupportActionBar().setSubtitle(mEntity.getFriendlyDomainName());
        }

        mTextState.setText(mEntity.getFriendlyState());
        mTextGroup.setText(mEntity.getGroupName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Postpone the transition until the window's decor view has
            // finished its layout.
            postponeEnterTransition();

            final View decor = getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startPostponedEnterTransition();
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_webui:
                return true;

            case R.id.action_switch:
                return true;

            //case R.id.action_logout:
            //    logOut();
            //    return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2000: {
                break;
            }

            case 2001: {
                if (resultCode == Activity.RESULT_OK) {
                    //Toast.makeText(this, "OK!", Toast.LENGTH_SHORT).show();
                    getContentResolver().notifyChange(EntityContentProvider.getUrl(), null);
                }
                break;
            }
        }
    }
}
