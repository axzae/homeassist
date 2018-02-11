package com.axzae.homeassistant.shared;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.axzae.homeassistant.R;

//https://stackoverflow.com/questions/39825125/android-recyclerview-cursorloader-contentprovider-load-more
public class EntityCursorAdapter { //extends CursorRecyclerViewAdapter {

//    EntityCursorAdapter(Context context, Cursor cursor) {
//        super(context, cursor);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return super.getItemId(position);
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Log.d("YouQi", "createViewHolder Called");
//
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_entity, parent, false);
//        return new EntityTileViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
//
//        Log.d("YouQi", "bindView Called");
//        EntityTileViewHolder holder = (EntityTileViewHolder) viewHolder;
//        cursor.moveToPosition(cursor.getPosition());
//        holder.setData(cursor);
//    }
//
//    @Override
//    public int getItemCount() {
//        return super.getItemCount();
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return 0;
//    }
//
//    @Override
//    public String getIdColName() {
//        return "ENTITY_ID";
//    }
}