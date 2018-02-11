package com.axzae.homeassistant.shared.async;

import android.database.Cursor;
import android.os.AsyncTask;

import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.shared.interfaces.EntityAsyncCallback;

import java.util.ArrayList;

public class Cursor2EntityAsyncTask extends AsyncTask<Void, Void, ArrayList<Entity>> {
    private final EntityAsyncCallback mEntityAsyncCallback;
    private final Cursor mCursor;

    public Cursor2EntityAsyncTask(Cursor cursor, EntityAsyncCallback entityAsyncCallback) {
        this.mCursor = cursor;
        this.mEntityAsyncCallback = entityAsyncCallback;
    }

    @Override
    protected ArrayList<Entity> doInBackground(Void... voids) {
        //https://stackoverflow.com/questions/18326313/is-onloadfinished-asynchronous-background-thread
        ArrayList<Entity> results = new ArrayList<>();
        if (!mCursor.isClosed()) {
            if (mCursor.moveToFirst()) {
                do {
                    Entity entity = Entity.getInstance(mCursor);
                    if (entity != null) {
                        results.add(entity);
                    }

                    if (isCancelled()) {
                        return null;
                    }
                } while (mCursor.moveToNext());
            }
        }
        return results;
    }

    @Override
    protected void onPostExecute(ArrayList<Entity> entities) {
        if (mEntityAsyncCallback != null) mEntityAsyncCallback.onFinished(entities);

    }

//    @Override
//    protected void onPostExecute(ArrayList<Entity> entities) {
//        if (entities != null) {
//
//            int oriCount = mAdapter.getItemCount();
//
//            Parcelable recyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
//            mAdapter.updateList(entities);
//            mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
//
//            if (oriCount == 0) {
//                refreshPreferenceConfigs();
//            }
//
//            mProgressBarCircle.setVisibility(View.GONE);
//            mProgressBar.setVisibility(View.GONE);
//        }
//
//        if (!runonce) {
//            runonce = true;
//            refreshApi();
//        }
//        Log.d("YouQi", "onPostExecute: " + entities.size());
//        mCursorConverterTask = null;
//    }
}