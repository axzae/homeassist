package com.axzae.homeassistant.shared.async;

import android.os.AsyncTask;
import android.util.Log;

import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.shared.interfaces.EntityAsyncCallback;
import com.axzae.homeassistant.util.CommonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortEntityAsyncTask extends AsyncTask<Integer, Void, ArrayList<Entity>> {
    private final ArrayList<Entity> mEntities;
    private final EntityAsyncCallback mEntityAsyncCallback;

    public SortEntityAsyncTask(ArrayList<Entity> entities, EntityAsyncCallback entityAsyncCallback) {
        this.mEntities = entities;
        this.mEntityAsyncCallback = entityAsyncCallback;
    }

    static final ArrayList<Comparator<Entity>> comparators;

    static {
        comparators = new ArrayList<>();
        comparators.add(new Comparator<Entity>() {
            @Override
            public int compare(Entity lhs, Entity rhs) {
                return lhs.getDisplayOrder() - rhs.getDisplayOrder();
            }
        });

        comparators.add(new Comparator<Entity>() {
            @Override
            public int compare(Entity lhs, Entity rhs) {
                return lhs.getFriendlyName().compareTo(rhs.getFriendlyName());
            }
        });

        comparators.add(new Comparator<Entity>() {
            @Override
            public int compare(Entity lhs, Entity rhs) {
                int domainRanking = lhs.getDomainRanking() - rhs.getDomainRanking(); //ascending order
                if (domainRanking == 0) {
                    return lhs.getFriendlyName().compareTo(rhs.getFriendlyName()); //ascending order
                }
                return domainRanking;
            }
        });
    }

    @Override
    protected ArrayList<Entity> doInBackground(Integer... integers) {
        final Integer sortInt = integers[0];

        Collections.sort(mEntities, comparators.get(sortInt));
        for (Entity entity : mEntities) {
            Log.d("YouQi", entity.getFriendlyName() + ": " + entity.displayOrder);
        }

        return mEntities;
    }

    @Override
    protected void onPostExecute(ArrayList<Entity> entities) {
        if (mEntityAsyncCallback != null) mEntityAsyncCallback.onFinished(entities);

    }
}