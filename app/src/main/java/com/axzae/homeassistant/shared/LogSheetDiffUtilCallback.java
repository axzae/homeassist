package com.axzae.homeassistant.shared;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.axzae.homeassistant.model.LogSheet;

import java.util.List;

public class LogSheetDiffUtilCallback extends DiffUtil.Callback {

    private List<LogSheet> oldItems;
    private List<LogSheet> newItems;

    public LogSheetDiffUtilCallback(List<LogSheet> oldItems, List<LogSheet> newItems) {
        this.oldItems = oldItems;
        this.newItems = newItems;
    }

    @Override
    public int getOldListSize() {
        return oldItems.size();
    }

    @Override
    public int getNewListSize() {
        return newItems.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldItems.get(oldItemPosition).equals(newItems.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldItems.get(oldItemPosition).equals(newItems.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}