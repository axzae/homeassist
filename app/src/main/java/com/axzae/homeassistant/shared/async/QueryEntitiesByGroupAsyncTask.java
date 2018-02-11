package com.axzae.homeassistant.shared.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.Group;
import com.axzae.homeassistant.provider.DatabaseManager;
import com.axzae.homeassistant.shared.interfaces.EntityAsyncCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class QueryEntitiesByGroupAsyncTask extends AsyncTask<Void, Void, ArrayList<Entity>> {
    @SuppressLint("StaticFieldLeak") private final Context mContext;
    private final Group mGroup;
    private final EntityAsyncCallback mEntityAsyncCallback;

    public QueryEntitiesByGroupAsyncTask(Context context, Group group, EntityAsyncCallback entityAsyncCallback) {
        this.mContext = context;
        this.mGroup = group;
        this.mEntityAsyncCallback = entityAsyncCallback;
    }

    @Override
    protected ArrayList<Entity> doInBackground(Void... voids) {

//        try {
//            Thread.sleep(6000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        ArrayList<Entity> entities = DatabaseManager.getInstance(mContext).getEntitiesByGroup(mGroup.groupId);
        Comparator<Entity> comparator = SortEntityAsyncTask.comparators.get(mGroup.sortKey);
        Collections.sort(entities, comparator);
        return entities;
    }

    @Override
    protected void onPostExecute(ArrayList<Entity> entities) {
        if (mEntityAsyncCallback != null) mEntityAsyncCallback.onFinished(entities);
    }
}