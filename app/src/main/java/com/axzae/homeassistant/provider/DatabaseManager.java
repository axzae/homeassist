package com.axzae.homeassistant.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.axzae.homeassistant.model.DatabaseException;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.Group;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.Widget;
import com.axzae.homeassistant.util.CommonUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 9;
    private static final String DATABASE_NAME = "HOMEASSISTANT";
    private static DatabaseManager sInstance;

    private static final String TABLE_CONNECTION = "connections";
    private static final String TABLE_DASHBOARD = "dashboards";
    private static final String TABLE_ENTITY = "entities";
    private static final String TABLE_GROUP = "groups";
    private static final String TABLE_WIDGET = "widgets";

    public static synchronized DatabaseManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseManager(context.getApplicationContext());
        }

        return sInstance;
    }

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        onCreateVer1(database);
        onCreateVer2(database);
        onCreateVer5(database);
        onCreateVer6(database);
        onCreateVer7(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.d("YouQi", "Upgrading database from version " + oldVersion + " to " + newVersion);
        switch (oldVersion) {
            case 1: //Upgrade from 1
            case 2: //Upgrade from 2
            case 3:
            case 4: //v2.0
                onCreateVer1(database);
                onCreateVer5(database);
            case 5: //v2.1
                onCreateVer6(database);
            case 6:
                onCreateVer7(database);
            case 7:
                onCreateVer2(database);
        }
        if (oldVersion>5 && oldVersion <9) {
            database.execSQL("ALTER TABLE " + TABLE_WIDGET + " ADD COLUMN WIDGET_TYPE VARCHAR");
            database.execSQL("UPDATE " + TABLE_WIDGET + " SET WIDGET_TYPE='ENTITY'");
        }
    }

    private void onCreateVer1(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTITY);

        String sql = "";
        sql += "\n" + "CREATE TABLE " + TABLE_ENTITY;
        sql += "\n" + "(";
        sql += "\n" + "	ENTITY_ID VARCHAR,";
        sql += "\n" + "	FRIENDLY_NAME VARCHAR,";
        sql += "\n" + "	DOMAIN VARCHAR,";
        sql += "\n" + "	RAW_JSON VARCHAR,";
        sql += "\n" + "	CHECKSUM VARCHAR,";
        sql += "\n" + "	UNIQUE (ENTITY_ID) ON CONFLICT REPLACE";
        sql += "\n" + ");";
        database.execSQL(sql);
    }

    private void onCreateVer2(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_DASHBOARD);

        String sql = "";
        sql += "\n" + "CREATE TABLE " + TABLE_DASHBOARD;
        sql += "\n" + "(";
        sql += "\n" + "	ENTITY_ID VARCHAR,";
        sql += "\n" + "	DASHBOARD_ID INTEGER,";
        sql += "\n" + "	CUSTOM_NAME VARCHAR,";
        sql += "\n" + "	DISPLAY_ORDER INTEGER,";
        sql += "\n" + "	UNIQUE (ENTITY_ID, DASHBOARD_ID) ON CONFLICT REPLACE";
        sql += "\n" + ");";
        database.execSQL(sql);

        Log.d("YouQi", "onCreateVer2");
    }

    private void onCreateVer5(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);

        String sql = "";
        sql += "\n" + "CREATE TABLE " + TABLE_GROUP;
        sql += "\n" + "(";
        sql += "\n" + "	GROUP_ID INTEGER,";
        sql += "\n" + "	GROUP_ENTITY_ID VARCHAR,";
        sql += "\n" + "	GROUP_NAME VARCHAR,";
        sql += "\n" + "	RAW_JSON VARCHAR,";
        sql += "\n" + "	GROUP_DISPLAY_ORDER INTEGER,";
        sql += "\n" + "	SORT_KEY INTEGER,";
        sql += "\n" + "	UNIQUE (GROUP_ID) ON CONFLICT REPLACE";
        sql += "\n" + ");";
        database.execSQL(sql);
    }

    private void onCreateVer6(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_WIDGET);

        String sql = "";
        sql += "\n" + "CREATE TABLE " + TABLE_WIDGET;
        sql += "\n" + "(";
        sql += "\n" + "	WIDGET_ID INTEGER,";
        sql += "\n" + " WIDGET_TYPE VARCHAR,"; //Added in db version 9
        sql += "\n" + "	ENTITY_ID VARCHAR,";
        sql += "\n" + "	FRIENDLY_STATE VARCHAR,";
        sql += "\n" + "	FRIENDLY_NAME VARCHAR,";
        sql += "\n" + "	LAST_UPDATED INTEGER,";
        sql += "\n" + "	UNIQUE (WIDGET_ID) ON CONFLICT REPLACE";
        sql += "\n" + ");";
        database.execSQL(sql);
    }

    private void onCreateVer7(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CONNECTION);

        String sql = "";
        sql += "\n" + "CREATE TABLE " + TABLE_CONNECTION;
        sql += "\n" + "(";
        sql += "\n" + "	CONNECTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,";
        sql += "\n" + "	CONNECTION_NAME VARCHAR,";
        sql += "\n" + "	BASE_URL VARCHAR,";
        sql += "\n" + "	PASSWORD VARCHAR,";
        sql += "\n" + "	UNIQUE (CONNECTION_ID) ON CONFLICT REPLACE";
        sql += "\n" + ");";
        database.execSQL(sql);
    }

    public DatabaseManager forceCreate() {
        SQLiteDatabase db = this.getWritableDatabase();
        return this;
    }

    public void clear() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_ENTITY, null, null);
            db.delete(TABLE_DASHBOARD, null, null);
            db.delete(TABLE_GROUP, null, null);
            db.delete(TABLE_CONNECTION, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void updateDashboard(int groupId, ArrayList<Entity> entities) throws DatabaseException {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(TABLE_DASHBOARD, "DASHBOARD_ID=?", new String[]{Integer.toString(groupId)});
            if (entities != null && entities.size() > 0) {
                //db.delete("entities", null, null);
                for (int i = 0; i < entities.size(); ++i) {
                    Entity entityItem = entities.get(i);
                    ContentValues initialValues = new ContentValues();
                    initialValues.put("ENTITY_ID", entityItem.entityId);
                    initialValues.put("CUSTOM_NAME", entityItem.getFriendlyName());
                    initialValues.put("DASHBOARD_ID", groupId);
                    initialValues.put("DISPLAY_ORDER", i);
                    db.insert(TABLE_DASHBOARD, null, initialValues);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException(e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public void updateTables(ArrayList<Entity> entities) throws DatabaseException {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(TABLE_ENTITY, null, null);
            if (entities != null && entities.size() > 0) {
                //db.delete("entities", null, null);

                Collections.sort(entities, new Comparator<Entity>() {
                    @Override
                    public int compare(Entity lhs, Entity rhs) {
                        int a = lhs.getDomainRanking() - rhs.getDomainRanking(); //ascending order
                        if (a == 0) {
                            return lhs.getFriendlyName().compareTo(rhs.getFriendlyName()); //ascending order
                        }
                        return a;
                    }
                });

                for (Entity entity : entities) {
                    if ("".equals(entity.getFriendlyName())) {
                        continue;
                    }

                    db.insert(TABLE_ENTITY, null, entity.getContentValues());
                    //task.addProgress(1);
                }

                for (int i = 0; i < entities.size(); ++i) {
                    Entity entity = entities.get(i);
                    if ("".equals(entity.getFriendlyName()) || entity.isHidden()) {
                        continue;
                    }

                    ContentValues initialValues = new ContentValues();
                    initialValues.put("ENTITY_ID", entity.entityId);
                    initialValues.put("CUSTOM_NAME", entity.getFriendlyName());
                    initialValues.put("DASHBOARD_ID", 1);
                    initialValues.put("DISPLAY_ORDER", i);
                    db.insert(TABLE_DASHBOARD, null, initialValues);
                }
            }


            ArrayList<Group> groups = new ArrayList<>();

            db.delete(TABLE_GROUP, null, null);
            if (entities != null && entities.size() > 0) {

                int count = 0;
                {
                    ++count;
                    ContentValues initialValues = new ContentValues();
                    initialValues.put("GROUP_ID", count);
                    initialValues.put("GROUP_ENTITY_ID", "");
                    initialValues.put("GROUP_NAME", "HOME");
                    initialValues.put("RAW_JSON", "");
                    initialValues.put("GROUP_DISPLAY_ORDER", -10);
                    initialValues.put("SORT_KEY", 0);
                    db.insert(TABLE_GROUP, null, initialValues);
                }

                for (int i = 0; i < entities.size(); ++i) {
                    Entity entity = entities.get(i);
                    if (entity.isGroup() && entity.attributes.isView()) {
                        ++count;
                        Group group = Group.getInstance(entity, count);
                        //Override for default_view
                        if (entity.entityId.equals("group.default_view")) {
                            group.groupId = 1;
                            db.delete(TABLE_GROUP, "GROUP_ID=?", new String[]{Integer.toString(-10)});
                            db.delete(TABLE_DASHBOARD, "DASHBOARD_ID=?", new String[]{Integer.toString(1)});
                        }

                        ContentValues initialValues = new ContentValues();
                        initialValues.put("GROUP_ID", group.groupId);
                        initialValues.put("GROUP_ENTITY_ID", group.entityId);
                        initialValues.put("GROUP_NAME", group.getFriendlyName());
                        initialValues.put("RAW_JSON", CommonUtil.deflate(entity));
                        initialValues.put("GROUP_DISPLAY_ORDER", group.attributes.order);
                        initialValues.put("SORT_KEY", 0);

                        groups.add(group);
                        db.insert(TABLE_GROUP, null, initialValues);
                    }
                }
            }

            for (Group group : groups) {
                if (group.attributes.entityIds != null && group.attributes.entityIds.size() > 0) {
                    for (int i = 0; i < group.attributes.entityIds.size(); ++i) {
                        String entityId = group.attributes.entityIds.get(i);
                        ContentValues initialValues = new ContentValues();
                        initialValues.put("ENTITY_ID", entityId);
                        initialValues.put("CUSTOM_NAME", "custom");
                        initialValues.put("DASHBOARD_ID", group.groupId);
                        initialValues.put("DISPLAY_ORDER", i);
                        db.insert(TABLE_DASHBOARD, null, initialValues);
                    }
                }
            }
            //db.execSQL("DELETE FROM " + TABLE_DASHBOARD + " WHERE ENTITY_NAME NO ");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException(e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<Entity> getEntities() {
        ArrayList<Entity> results = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * from " + TABLE_ENTITY + " ORDER BY FRIENDLY_NAME ASC, DOMAIN ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                results.add(Entity.getInstance(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return results;
    }
    public ArrayList<Entity> getSensors() {
        ArrayList<Entity> results = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * from " + TABLE_ENTITY + " WHERE ENTITY_ID LIKE 'sensor.%' ORDER BY FRIENDLY_NAME ASC, DOMAIN ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                results.add(Entity.getInstance(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return results;
    }

    public int updateSortKeyForGroup(int sortKey, int groupId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put("SORT_KEY", sortKey);
        int result = db.update(TABLE_GROUP, initialValues, "GROUP_ID = ?", new String[]{Integer.toString(groupId)});
        return result;
    }

    public long addConnection(HomeAssistantServer server) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues initialValues = new ContentValues();
        if (server.connectionId != null) {
            initialValues.put("CONNECTION_ID", server.connectionId);
        }

        initialValues.put("CONNECTION_NAME", server.getName());
        initialValues.put("BASE_URL", server.baseurl);
        initialValues.put("PASSWORD", server.password);
        long result = db.insert(TABLE_CONNECTION, null, initialValues);

        return result;
    }

    public ArrayList<Entity> getDeviceLocations() {
        ArrayList<Entity> results = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * from entities WHERE DOMAIN IN ('zone', 'device_tracker') ORDER BY DOMAIN ASC, ENTITY_ID ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Gson gson = new Gson();

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                results.add(Entity.getInstance(cursor));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        return results;
    }

    public ArrayList<HomeAssistantServer> getConnections() {
        ArrayList<HomeAssistantServer> results = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * from " + TABLE_CONNECTION + " ORDER BY CONNECTION_ID ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Gson gson = new Gson();

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                results.add(HomeAssistantServer.getInstance(cursor));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        return results;
    }


    public ArrayList<Entity> getEntitiesByGroup(int groupId) {
        ArrayList<Entity> results = new ArrayList<>();
        String selectQuery = "SELECT b.*, a.DISPLAY_ORDER FROM dashboards a LEFT JOIN entities b ON a.ENTITY_ID=b.ENTITY_ID WHERE a.DASHBOARD_ID=" + groupId + " AND b.ENTITY_ID IS NOT NULL ORDER BY a.DISPLAY_ORDER ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //Log.d("YouQi", "query: " + selectQuery);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Entity entity = Entity.getInstance(cursor);
                if (entity != null) results.add(entity);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        return results;
    }

    public ArrayList<Group> getGroups() {
        ArrayList<Group> results = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * from " + TABLE_GROUP + " ORDER BY GROUP_DISPLAY_ORDER ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                results.add(Group.getInstance(cursor));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        return results;
    }

    public int getDashboardCount() {
        String selectQuery = "SELECT COUNT(*) AS TOTAL from " + TABLE_DASHBOARD;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int result = cursor.getInt(cursor.getColumnIndex("TOTAL"));
        cursor.close();

        return result;
    }


    public void housekeepWidgets(ArrayList<String> appWidgetIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();


        String idList = appWidgetIds.toString();
        String csv = idList.substring(1, idList.length() - 1);
        final String sql = "DELETE FROM " + TABLE_WIDGET + " WHERE WIDGET_ID NOT IN(" + csv + " )";

        Log.d("YouQi", "Housekeep: " + "DELETE FROM " + TABLE_WIDGET + " WHERE WIDGET_ID NOT IN(" + csv + " )");
        try {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void insertWidget(int widgetId, Entity entity, String widgetType) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("WIDGET_ID", widgetId);
            initialValues.put("WIDGET_TYPE", widgetType);
            initialValues.put("ENTITY_ID", entity.entityId);
            initialValues.put("FRIENDLY_STATE", entity.getFriendlyName());
            initialValues.put("FRIENDLY_NAME", entity.getFriendlyName());
            initialValues.put("LAST_UPDATED", 1);
            db.insert(TABLE_WIDGET, null, initialValues);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public Entity getEntityById(String entityId) {
        //String selectQuery = "SELECT * from " + TABLE_ENTITY + " WHERE ENTITY_ID='" + entityId + "'";
        String selectQuery = "SELECT * FROM entities WHERE ENTITY_ID='" + entityId + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Entity entity = null;
        if (cursor.moveToFirst()) {
            entity = Entity.getInstance(cursor);
        }

        cursor.close();
        return entity;
    }

    public Widget getWidgetById(int appWidgetId) {
        //String selectQuery = "SELECT * from " + TABLE_ENTITY + " WHERE ENTITY_ID='" + entityId + "'";
        String selectQuery = "SELECT a.*, b.RAW_JSON FROM widgets a LEFT JOIN entities b ON a.ENTITY_ID=b.ENTITY_ID WHERE a.WIDGET_ID='" + appWidgetId + "' AND b.ENTITY_ID IS NOT NULL";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Widget widget = null;
        if (cursor.moveToFirst()) {
            widget = Widget.getInstance(cursor);
            widget.appWidgetId = appWidgetId;
        }

        cursor.close();
        return widget;
    }

    public ArrayList<Integer> getEntityWidgetIdsByEntityId(String entityId) {
        //String selectQuery = "SELECT * from " + TABLE_ENTITY + " WHERE ENTITY_ID='" + entityId + "'";
        String selectQuery = "SELECT WIDGET_ID FROM widgets WHERE WIDGET_TYPE<>'SENSOR' and ENTITY_ID='" + entityId + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<Integer> results = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int widgetId = cursor.getInt(cursor.getColumnIndex("WIDGET_ID"));
            results.add(widgetId);
        }
        cursor.close();
        return results;
    }

    public ArrayList<Integer> getSensorWidgetIdsByEntityId(String entityId) {
        //String selectQuery = "SELECT * from " + TABLE_ENTITY + " WHERE ENTITY_ID='" + entityId + "'";
        String selectQuery = "SELECT WIDGET_ID FROM widgets WHERE WIDGET_TYPE='SENSOR' and ENTITY_ID='" + entityId + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<Integer> results = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int widgetId = cursor.getInt(cursor.getColumnIndex("WIDGET_ID"));
            results.add(widgetId);
        }
        cursor.close();
        return results;
    }

    public ArrayList<Widget> getWidgets() {
        //String selectQuery = "SELECT * from " + TABLE_ENTITY + " WHERE ENTITY_ID='" + entityId + "'";
        String selectQuery = "SELECT a.*, b.RAW_JSON FROM widgets a LEFT JOIN entities b ON a.ENTITY_ID=b.ENTITY_ID WHERE b.ENTITY_ID IS NOT NULL";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        ArrayList<Widget> widgets = null;
        if (cursor.moveToFirst()) {
            Widget widget = Widget.getInstance(cursor);
            if (widget != null) widgets.add(widget);
        }

        cursor.close();
        return widgets;
    }

    public ArrayList<Entity> getDashboard(int groupId) {
        ArrayList<Entity> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT b.*, a.DISPLAY_ORDER FROM dashboards a LEFT JOIN entities b ON a.ENTITY_ID=b.ENTITY_ID WHERE a.DASHBOARD_ID=" + groupId + " AND b.ENTITY_ID IS NOT NULL ORDER BY a.DISPLAY_ORDER ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        Gson gson = new Gson();

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Entity entity = Entity.getInstance(cursor);
                if (entity != null) results.add(entity);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return results;
    }

}