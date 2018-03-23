package com.axzae.homeassistant.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.axzae.homeassistant.BuildConfig;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.Widget;

import java.util.ArrayList;

public class EntityContentProvider extends ContentProvider {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.EntityContentProvider";
    private static final String TABLE_NAME = "entities";

    private DatabaseManager mSqliteOpenHelper;
    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //sUriMatcher.addURI(AUTHORITY, TABLE_NAME + "/offset/" + "#", TABLE_ITEMS);
        sUriMatcher.addURI(AUTHORITY, "entities", 1);
        sUriMatcher.addURI(AUTHORITY, "dashboard", 2);
    }

    public static Uri getUrl() {
        return Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
        //return "content://com.axzae.homeassistant.provider.EntityContentProvider/";
        //return Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME + "/offset/" + limit);
    }

    @Override
    public boolean onCreate() {
        mSqliteOpenHelper = DatabaseManager.getInstance(getContext());
        return true;
    }

    @Override
    synchronized public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d("YouQi", "query uri: " + uri.toString());

        SQLiteDatabase db = mSqliteOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
        Cursor c = null;
        String offset;

        Log.d("YouQi", "query URI Matcher: " + sUriMatcher.match(uri));
//        switch (sUriMatcher.match(uri)) {
//            case TABLE_ITEMS: {
//                sqb.setTables(TABLE_NAME);
//                offset = uri.getLastPathSegment();
//                break;
//            }
//
//            default:
//                throw new IllegalArgumentException("uri not recognized!");
//        }
//
//        int intOffset = Integer.parseInt(offset);
//
//        String limitArg = intOffset + ", " + 30;
//        Log.d(TAG, "query: " + limitArg);
//        c = sqb.query(db, projection, selection, selectionArgs, null, null, sortOrder, limitArg);

        //https://stackoverflow.com/questions/4957009/how-do-i-join-two-sqlite-tables-in-my-android-application
        String query = "SELECT *, -1 AS DISPLAY_ORDER FROM entities";
        c = db.rawQuery(query, null);
        //Log.d("YouQi", "sort: " + sortOrder);

        //c = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        //TODO: REFERENCE
        //SQLiteDatabase db = this.getReadableDatabase();
        //String[] columnsToReturn = {"ISO_CODE", "COUNTRY_NAME", "API_KEY"};
        //String selection = "UPPER(ISO_CODE) = ?";
        //String[] selectionArgs = {isoCode.trim()}; // matched to "?" in selection
        //Cursor cursor = db.query("nationalities", columnsToReturn, selection, selectionArgs, null, null, null);

        return c;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return BuildConfig.APPLICATION_ID + ".item";
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        String table = "";
//        switch (sUriMatcher.match(uri)) {
//            case TABLE_ITEMS: {
//                table = TABLE_NAME; //TableItems.NAME;
//                break;
//            }
//        }

        long result = mSqliteOpenHelper.getWritableDatabase().insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        if (result == -1) {
            throw new SQLException("insert with conflict!");
        }

        Uri retUri = ContentUris.withAppendedId(uri, result);
        return retUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        Log.d("YouQi", "Bulk Insert Called");
        SQLiteDatabase db = mSqliteOpenHelper.getWritableDatabase();
        db.beginTransaction();

        int inserted = 0;

        try {
            db.delete(TABLE_NAME, null, null);

            for (ContentValues value : values) {
                db.insert(TABLE_NAME, null, value);
                ++inserted;
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            //db.close();
        }
        //getContext().getContentResolver().notifyChange(Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME + "/" + "input_slider.harmonyvolume"), null);
        Log.d("YouQi", "Inserted: " + inserted);
        //getContext().getContentResolver().notifyChange(getUrl(), null);

        Uri callbackUri = DummyContentProvider.getUrl("ALL");
        Context context = getContext();
        if (context != null) context.getContentResolver().notifyChange(callbackUri, null);

        return inserted;
        //return super.bulkInsert(uri, values);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return -1;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        Log.d("YouQi", "update uri: " + uri.toString());
        //Log.d("YouQi", "selection: " + selection);

        SQLiteDatabase db = mSqliteOpenHelper.getWritableDatabase();
        db.update(TABLE_NAME, values, selection, selectionArgs);
        //db.close();

        //Update if there are widgets found.
        final String entityId = values.getAsString("ENTITY_ID");
        Entity entity = mSqliteOpenHelper.getEntityById(entityId);
        ArrayList<Integer> widgetIds = mSqliteOpenHelper.getEntityWidgetIdsByEntityId(entityId);
        for (int widgetId : widgetIds) {
            Log.d("YouQi", "Updating Widget: " + widgetId);
            Widget widget = Widget.getInstance(entity, widgetId);
            EntityWidgetProvider.updateEntityWidget(getContext(), widget);
        }
        ArrayList<Integer> sensorWidgetIds = mSqliteOpenHelper.getSensorWidgetIdsByEntityId(entityId);
        for (int sensorId : sensorWidgetIds) {
            Log.d("YouQi", "Updating Sensor Widget: " + sensorId);
            Widget widget = Widget.getInstance(entity, sensorId);
            SensorWidgetProvider.updateEntityWidget(getContext(), widget);
        }
        Uri newUri = DummyContentProvider.getUrl(entityId);
        Log.d("YouQi", "inform URI: " + newUri);

        getContext().getContentResolver().notifyChange(newUri, null);
        //getContext().getContentResolver().notifyChange(Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME + "/"), null);
////        String limitArg = intOffset + ", " + 30;
////        Log.d(TAG, "query: " + limitArg);
////        c = sqb.query(db, projection, selection, selectionArgs, null, null, sortOrder, limitArg);
//


        return -1;
    }
}