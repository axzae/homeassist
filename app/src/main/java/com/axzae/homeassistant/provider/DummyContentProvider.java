package com.axzae.homeassistant.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.axzae.homeassistant.BuildConfig;

public class DummyContentProvider extends ContentProvider {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.DummyContentProvider";
    private static final String TABLE_NAME = "entities";

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "", 1);
    }

    public static Uri getUrl() {
        return getUrl("");
    }

    public static Uri getUrl(String append) {
        return Uri.parse("content://" + AUTHORITY + "/" + append);
        //return Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME + "/offset/" + limit);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    synchronized public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d("YouQi", "query uri: " + uri.toString());
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return BuildConfig.APPLICATION_ID + ".dummy";
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return getUrl();
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return -1;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return -1;
    }
}