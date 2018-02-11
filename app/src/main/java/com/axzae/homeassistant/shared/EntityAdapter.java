package com.axzae.homeassistant.shared;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.MDIFont;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class EntityAdapter extends RecyclerView.Adapter<EntityAdapter.EntityTileViewHolder> {

    private ArrayList<Entity> items;
    private ArrayList<Entity> filteredItems;
    private Context context;
    private EntityInterface entityInterface;

    public EntityAdapter(Context context, EntityInterface entityInterface, ArrayList<Entity> items) {
        this.context = context;
        this.entityInterface = entityInterface;
        this.items = items == null ? new ArrayList<Entity>() : items;
        this.filteredItems = null;
    }

    public void updateList(ArrayList<Entity> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new EntityDiffUtilCallback(getDisplayItems(), newItems));
        this.items.clear();
        this.items.addAll(newItems);

        if (isFilterState()) {
            for (Entity item : items) {
                int filterpos = filteredItems.indexOf(item);
                if (filterpos != -1) {
                    filteredItems.set(filterpos, item);
                }
            }
        }
        diffResult.dispatchUpdatesTo(this);
    }

    public void updateDisplayList(ArrayList<Entity> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new EntityDiffUtilCallback(getDisplayItems(), newItems));

        if (isFilterState()) {
            this.filteredItems = new ArrayList<>();
            this.filteredItems.clear();
            this.filteredItems.addAll(newItems);
        } else {
            this.items.clear();
            this.items.addAll(newItems);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    public void clearFilter() {
        if (filteredItems != null) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new EntityDiffUtilCallback(getDisplayItems(), items));
            filteredItems = null;
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public void updateFilterList(ArrayList<Entity> newItems) {

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new EntityDiffUtilCallback(getDisplayItems(), newItems));

        if (filteredItems == null) {
            filteredItems = new ArrayList<>();
        }
        this.filteredItems.clear();
        this.filteredItems.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public EntityTileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_entity, parent, false);
        return new EntityTileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final EntityTileViewHolder viewHolder, final int position) {
        final Entity entity = getItem(position);
        viewHolder.setData(entity);
    }

    public ArrayList<Entity> getItems() {
        return items;
    }

    public ArrayList<Entity> getDisplayItems() {
        return !isFilterState() ? items : filteredItems;
    }

    @Override
    public int getItemCount() {

        if (!isFilterState()) {
            return items == null ? 0 : items.size();
        } else {
            return filteredItems.size();
        }
    }

    public Entity getItem(int position) {

        if (!isFilterState()) {
            //Log.d("YouQi", "getNormalItem: " + position);
            return items.get(position);
        } else {
            //Log.d("YouQi", "getFilterItem: " + position);
            return filteredItems.get(position);
        }
    }

    public void updateState(Entity entity) {
        if (entity != null) {
            int pos = items.indexOf(entity);
            if (pos != -1) {
                Entity oldEntity = items.get(pos);
                if (oldEntity.lastChanged.equals(entity.lastChanged)) {
                    //Log.d("YouQi", "No change for " + entity.getFriendlyName());
                    return;
                }

                entity.displayOrder = items.get(pos).displayOrder;
                items.set(pos, entity);
            }

            if (isFilterState()) {
                int filterpos = filteredItems.indexOf(entity);
                if (filterpos != -1) {
                    entity.displayOrder = filteredItems.get(filterpos).displayOrder;
                    filteredItems.set(filterpos, entity);
                    notifyItemChanged(filterpos);
                }
            } else {
                if (pos != -1) {
                    notifyItemChanged(pos);
                }
            }
        }
    }

    public boolean isFilterState() {
        return filteredItems != null;
    }

    public class EntityTileViewHolder extends RecyclerView.ViewHolder {
        public final View rootView;
        final View upperView;
        final View lowerView;
        final TextView mTextFriendlyName;
        final TextView mTextGroup;
        final TextView mTextState;
        final TextView mTextIcon;
        final MaterialProgressBar mProgressbar;

        //private final View rootView;
        CardView mCardView;
        ViewGroup mItemView;
        ImageView mImageView;
        View mDivider;
        View mIndicator;

        EntityTileViewHolder(View v) {
            super(v);
            rootView = v;
            mCardView = v.findViewById(R.id.card);
            mItemView = v.findViewById(R.id.item);
            mImageView = v.findViewById(R.id.icon);
            mIndicator = v.findViewById(R.id.indicator);
            mTextIcon = v.findViewById(R.id.text_mdi);
            mDivider = v.findViewById(R.id.divider);
            mProgressbar = v.findViewById(R.id.progressbar);

            upperView = v.findViewById(R.id.upper_container);
            lowerView = v.findViewById(R.id.lower_container);

            mTextFriendlyName = v.findViewById(R.id.text_friendly_name);
            mTextGroup = v.findViewById(R.id.text_group);
            mTextState = v.findViewById(R.id.text_state);

            mItemView.setSoundEffectsEnabled(false);
            mItemView.setOnTouchListener(getTouchListener());
        }

        View.OnTouchListener getTouchListener() {
            return new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(upperView, "scaleX", 0.9f);
                            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(upperView, "scaleY", 0.9f);
                            scaleDownX.setDuration(200);
                            scaleDownY.setDuration(200);

                            AnimatorSet scaleDown = new AnimatorSet();
                            scaleDown.play(scaleDownX).with(scaleDownY);
                            scaleDown.setInterpolator(new OvershootInterpolator());
                            scaleDown.start();
                            //mClickDown.start();
                            break;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(upperView, "scaleX", 1f);
                            ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(upperView, "scaleY", 1f);
                            scaleDownX2.setDuration(200);
                            scaleDownY2.setDuration(200);

                            AnimatorSet scaleDown2 = new AnimatorSet();
                            scaleDown2.play(scaleDownX2).with(scaleDownY2);
                            scaleDown2.setInterpolator(new OvershootInterpolator());
                            scaleDown2.start();

//                            if (event.getAction() == MotionEvent.ACTION_UP) {
//                                v.performClick();
//                            }
                            break;
                    }
                    return false;
                }
            };
        }

        void setActivate(boolean isActivated) {
            rootView.setActivated(isActivated);
            mTextState.setActivated(isActivated);
            mTextGroup.setActivated(isActivated);
            mTextFriendlyName.setActivated(isActivated);
            //mDivider.setActivated(isActivated);
        }

        public void setData(final Cursor cursor) {
            final Entity entity = Entity.getInstance(cursor);
            if (entity == null) {
                Crashlytics.logException(new RuntimeException("Entity is Null!"));
            } else {
                setData(entity);
            }
        }

        public void setColor(int resColorState) {
            mTextState.setTextColor(context.getResources().getColorStateList(resColorState));
            mTextGroup.setTextColor(context.getResources().getColorStateList(resColorState));
            mTextIcon.setTextColor(context.getResources().getColorStateList(resColorState));
            mTextFriendlyName.setTextColor(context.getResources().getColorStateList(resColorState));
        }

        public void setData(final Entity entity) {
            mProgressbar.setVisibility(View.GONE);
            mTextFriendlyName.setText(entity.getFriendlyName());
            mTextGroup.setText(entity.getGroupName());

            Typeface typeface = ResourcesCompat.getFont(context, entity.hasStateIcon() ? R.font.mdi : R.font.dincond);
            mTextState.setTypeface(typeface);

            mTextState.setText(entity.getIconState());
            mIndicator.setVisibility(entity.hasIndicator() ? View.VISIBLE : View.INVISIBLE);
            setActivate(entity.isActivated());

//            if (entity.hasMdiIcon() && entity.isSensor()) {
//                mTextIcon.setVisibility(View.VISIBLE);
//                mTextIcon.setText(MDIFont.getIcon(entity.attributes.icon));
//            } else {
                mTextIcon.setVisibility(View.GONE);
//            }

            if (entity.isScript() || entity.isInputSelect()) {
                mIndicator.setVisibility(View.INVISIBLE);
            }

            mImageView.setVisibility(View.GONE);
//            mImageView.setVisibility(entity.isScript() ? View.VISIBLE : View.GONE);
//            if (entity.isScript()) {
//                mTextState.setText("");
//                GlideApp.with(context).load(R.drawable.ic_code_braces_black_24dp).into(mImageView);
//            }


            if (entity.isSensor()) {
                setColor(R.color.color_sensor);

                if (entity.attributes.entityPicture != null) {

                    mImageView.setVisibility(View.VISIBLE);
                    mTextState.setText("");

                    mTextGroup.setText(entity.getFriendlyState());
                    mProgressbar.setVisibility(View.VISIBLE);

                    Log.d("YouQi", "LOADING...");
                    GlideApp.with(context)
                            .load(entity.attributes.getEntityPictureUri())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.d("YouQi", "Load Failed" + entity.attributes.getEntityPictureUri());
                                    mProgressbar.setVisibility(View.GONE);

//                                    mImageView.setVisibility(View.GONE);

//                                    mTextState.setTypeface(ResourcesCompat.getFont(context, R.font.mdi));
//                                    mTextState.setVisibility(View.VISIBLE);
//                                    mTextState.setText(MDIFont.getIcon("mdi:alert-decagram"));
//                                    mTextState.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.md_red_500, null));
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Log.d("YouQi", "Resource Ready");
                                    mProgressbar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            //.placeholder(R.drawable.loading_spinner)
                            .into(mImageView);
                }

                //} else if (entity.isBinarySensor()) {
                //    setColor(R.color.color_amber);
            } else if (entity.isDeviceTracker()) {
                setColor(R.color.color_devicetracker);
            } else {
                setColor(R.color.color_xiaomi);
            }


            if (entity.isCircle()) {
                //mCardView.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.md_blue_A200, null));
                mCardView.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.md_light_blue_400, null));
                setColor(R.color.color_sensor);
            } else {
                mCardView.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.md_white_1000, null));
            }


            mItemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return entityInterface.onEntityUpperViewLongClick(EntityTileViewHolder.this, entity);
                }
            });

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("YouQi", "mItemView.setOnClickListener");
                    entityInterface.onEntityUpperViewClick(EntityTileViewHolder.this, entity);
                }
            });

//            lowerView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    entityInterface.onEntityLowerViewClick(EntityTileViewHolder.this, entity);
//                }
//            });

//            Typeface typeface2 = ResourcesCompat.getFont(context, R.font.mdi);
//            FontIconDrawable drawable = new FontIconDrawable(context, MDIFont.getIcon("mdi:bell"), typeface2).colorRes(R.color.md_red_500);
//            mImageView.setVisibility(View.VISIBLE);
//            mImageView.setImageDrawable(drawable);

        }

        public boolean isActivated() {
            return rootView.isActivated();
        }
    }
}