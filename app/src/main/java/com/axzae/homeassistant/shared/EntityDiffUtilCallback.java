package com.axzae.homeassistant.shared;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.util.CommonUtil;

import java.util.List;

public class EntityDiffUtilCallback extends DiffUtil.Callback {

    private List<Entity> oldItems;
    private List<Entity> newItems;

    public EntityDiffUtilCallback(List<Entity> oldItems, List<Entity> newItems) {
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
        return oldItems.get(oldItemPosition).entityId.equals(newItems.get(newItemPosition).entityId);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        if (oldItems.get(oldItemPosition).checksum != null && newItems.get(newItemPosition).checksum != null) {
            return oldItems.get(oldItemPosition).checksum.equals(newItems.get(newItemPosition).checksum);
        } else if (oldItems.get(oldItemPosition).lastUpdated != null && newItems.get(newItemPosition).lastUpdated != null) {
            return oldItems.get(oldItemPosition).lastUpdated.equals(newItems.get(newItemPosition).lastUpdated);
        }

        return false;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}