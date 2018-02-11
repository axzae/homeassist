package com.axzae.homeassistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.axzae.homeassistant.util.CommonUtil;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.styles.Github;

public class WikiActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_wiki, menu);

        CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_open_in_browser), R.color.md_white_1000);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.menu_faq);
        }

        Github css = new Github();
        css.addRule("body", "line-height: 1.6", "padding: 0px");

        final MarkdownView mMarkdownView = findViewById(R.id.markdown_view);
        mMarkdownView.addStyleSheet(css);
        //mMarkdownView.loadMarkdown("**MarkdownView**");
        //mMarkdownView.loadMarkdownFromAsset("markdown1.md");
        //mMarkdownView.loadMarkdownFromFile(new File());
        mMarkdownView.loadMarkdownFromUrl("https://raw.githubusercontent.com/axzae/homeassist-builder/master/wiki/MAIN.md");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //int id = item.getItemId();
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_open_in_browser:
                openInBrowser();
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

    private void openInBrowser() {
        String url = "https://github.com/axzae/homeassist-builder/blob/master/wiki/MAIN.md";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}