package com.axzae.homeassistant;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.axzae.homeassistant.helper.ItemTouchHelperAdapter;
import com.axzae.homeassistant.helper.SimpleItemTouchHelperCallback;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.ErrorMessage;
import com.axzae.homeassistant.model.Group;
import com.axzae.homeassistant.model.MDIFont;
import com.axzae.homeassistant.provider.DatabaseManager;
import com.axzae.homeassistant.shared.GlideApp;
import com.axzae.homeassistant.util.CommonUtil;
import com.axzae.homeassistant.util.FaultUtil;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class EditActivity extends BaseActivity {

    private SharedPreferences mSharedPref;

    private RecyclerView mRecyclerView;
    private EntityAdapter mAdapter;
    protected ArrayList<Entity> mItems = new ArrayList<>();
    private MaterialDialog mProgressDialog;
    private SaveTask mSaveTask;
    private boolean mEdited = false;
    private ArrayList<Entity> mEntities = null;
    private Group mGroup;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);
        CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_help), R.color.md_white_1000);
        //CommonUtil.setMenuDrawableColor(this, menu.findItem(R.id.action_add), R.color.md_white_1000);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Crashlytics.log("group: " + bundle.getString("group", "empty"));
            mGroup = CommonUtil.inflate(bundle.getString("group"), Group.class);
            if (mGroup == null) {
                discardAndFinish();
                return;
            }
        } else {
            discardAndFinish();
            return;
        }

        mSharedPref = getAppController().getSharedPref();
        mProgressDialog = CommonUtil.getProgressDialog(this);
        mProgressDialog.setContent(getString(R.string.progress_saving));

        //Setup Toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_edit));
        }

        TabLayout mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setSelectedTabIndicatorHeight(CommonUtil.pxFromDp(this, 4f));
        mTabLayout.addTab(mTabLayout.newTab().setText(mGroup.getFriendlyName()));
        if (mGroup.hasMdiIcon()) {
            int tabIndex = mTabLayout.getTabCount() - 1;
            TabLayout.Tab currentTab = mTabLayout.getTabAt(tabIndex);
            if (currentTab != null) {
                View tab = LayoutInflater.from(this).inflate(R.layout.custom_tab, mTabLayout, false);

                TextView mdiText = tab.findViewById(R.id.text_mdi);
                TextView nameText = tab.findViewById(R.id.text_name);
                mdiText.setText(MDIFont.getIcon(mGroup.attributes.icon));
                nameText.setText(mGroup.getFriendlyName());
                nameText.setVisibility(View.VISIBLE);
                currentTab.setCustomView(tab);
            }
        }

        //mDatabaseManager = DatabaseManager.getInstance(this);
        //getSupportLoaderManager().initLoader(1, null, this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view);

        mItems.clear();
        mItems.addAll(DatabaseManager.getInstance(this).getDashboard(mGroup.groupId));

        mAdapter = new EntityAdapter(mItems);
        mRecyclerView.setAdapter(mAdapter);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        updateRecyclerViewLayout();
    }

    private void updateRecyclerViewLayout() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;

        //final int spanCount = getResources().getInteger(R.integer.grid_columns);
        int spanCount = (int) Math.floor(dpWidth / 90.0d);
        final int prefCount = Integer.parseInt(mSharedPref.getString("num_columns", "0"));
        if (prefCount != 0) {
            spanCount = prefCount;
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_help:
                showHelp();
                return true;

            case R.id.action_save:
                saveChanges();
                return true;
            case R.id.action_remove_all:
                mAdapter.clearAll();
                return true;

            case R.id.action_add_default:
                addDefault();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {
        if (mSaveTask == null) {
            mSaveTask = new SaveTask();
            mSaveTask.execute((Void) null);
        }
    }

    private void showHelp() {

        View targetRecyclerView = mRecyclerView;
        if (mAdapter.getItemCount() > 0) {
            if (mAdapter.getItemCount() >= 8) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForLayoutPosition(5);
                if (viewHolder != null) {
                    targetRecyclerView = viewHolder.itemView;
                }
            } else if (mAdapter.getItemCount() >= 4) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForLayoutPosition(1);
                if (viewHolder != null) {
                    targetRecyclerView = viewHolder.itemView;
                }
            } else {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForLayoutPosition(0);
                if (viewHolder != null) {
                    targetRecyclerView = viewHolder.itemView;
                }
            }
        }

        new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(findViewById(R.id.fab), getString(R.string.tutorial_title_add), getString(R.string.tutorial_desc_add))
                                .cancelable(false)
                                .transparentTarget(true)
                                .outerCircleColor(R.color.primary)
                                .tintTarget(false),
                        TapTarget.forView(targetRecyclerView, getString(R.string.tutorial_title_sort), getString(R.string.tutorial_desc_sort))
                                .outerCircleColor(R.color.primary)
                                .transparentTarget(true)
                                .cancelable(false),
                        TapTarget.forView(targetRecyclerView, getString(R.string.tutorial_title_remove), getString(R.string.tutorial_desc_remove))
                                .outerCircleColor(R.color.primary)
                                .transparentTarget(true)
                                .cancelable(false),
                        TapTarget.forView(findViewById(R.id.action_save), getString(R.string.tutorial_title_save), getString(R.string.tutorial_desc_save))
                                //.outerCircleColor(R.color.primary)
                                //.tintTarget(false)
                                .cancelable(false)
                )
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                    }
                }).start();
    }

    private void showAddDialog() {
        if (mEntities == null) mEntities = DatabaseManager.getInstance(this).getEntities();

        Collections.sort(mEntities, new Comparator<Entity>() {
            @Override
            public int compare(Entity lhs, Entity rhs) {
                return lhs.getFriendlyName().compareTo(rhs.getFriendlyName()); //descending order
            }
        });

        final ArrayAdapter<Entity> adapter = new ArrayAdapter<Entity>(this, android.R.layout.simple_list_item_1, mEntities) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.item_widget_select, null);
                }

                Entity entity = mEntities.get(position);

                ViewGroup mItemView = convertView.findViewById(R.id.item);
                TextView mIconView = convertView.findViewById(R.id.text_mdi);
                TextView mMainText = convertView.findViewById(R.id.main_text);
                TextView mLabelText = convertView.findViewById(R.id.sub_text);

                ProgressBar mProgressBar = convertView.findViewById(R.id.progressbar);

                mItemView.setClickable(false);
                mItemView.setFocusable(false);
                mIconView.setText(entity.getMdiIcon());
                mMainText.setText(entity.attributes.friendlyName);
                mLabelText.setText(entity.getDomain());
                mProgressBar.setVisibility(View.GONE);
                return convertView;
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_additem);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mEdited = true;
                Entity selectedEntity = mEntities.get(which);
                mItems.add(selectedEntity);
                mAdapter.notifyItemInserted(mItems.size() - 1);

            }
        });
        builder.show();
    }

    private void addDefault() {
        mItems.clear();
        ArrayList<Entity> entities = DatabaseManager.getInstance(this).getEntities();
        for (Entity entity : entities) {
            if (entity.isSupported()) mItems.add(entity);
        }
        mEdited = true;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (mEdited) {
            new MaterialDialog.Builder(this)
                    .content(R.string.title_save_changes)
                    .autoDismiss(true)
                    .cancelable(true)
                    .negativeText(getString(R.string.action_discard))
                    .positiveText(getString(R.string.action_save))
                    .positiveColorRes(R.color.md_blue_500)
                    .negativeColorRes(R.color.md_blue_500)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            discardAndFinish();
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            if (mSaveTask == null) {
                                mSaveTask = new SaveTask();
                                mSaveTask.execute((Void) null);
                            }

                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void discardAndFinish() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(R.anim.stay_still, R.anim.fade_out);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2000: {
                updateRecyclerViewLayout();
                break;
            }
        }
    }

    private class EntityTileViewHolder extends RecyclerView.ViewHolder {
        private final View rootView;
        private final View upperView;
        private final TextView mTextFriendlyName;
        private final TextView mTextGroup;
        private final TextView mTextState;
        private final TextView mTextIcon;
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

            upperView = v.findViewById(R.id.upper_container);
            mTextFriendlyName = v.findViewById(R.id.text_friendly_name);
            mTextGroup = v.findViewById(R.id.text_group);
            mTextState = v.findViewById(R.id.text_state);

            mItemView.setSoundEffectsEnabled(false);
            mItemView.setOnTouchListener(getTouchListener());

        }

        View.OnTouchListener getTouchListener() {
            return new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
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

        public void setColor(int resColorState) {
            mTextState.setTextColor(getResources().getColorStateList(resColorState));
            mTextGroup.setTextColor(getResources().getColorStateList(resColorState));
            mTextIcon.setTextColor(getResources().getColorStateList(resColorState));
            mTextFriendlyName.setTextColor(getResources().getColorStateList(resColorState));
        }

        public void setData(final Entity entity) {
            mTextFriendlyName.setText(entity.getFriendlyName());
            mTextGroup.setText(entity.getGroupName());

            Typeface typeface = ResourcesCompat.getFont(EditActivity.this, entity.hasStateIcon() ? R.font.mdi : R.font.dincond);
            mTextState.setTypeface(typeface);

            mTextState.setText(entity.getIconState());
            mIndicator.setVisibility(entity.hasIndicator() ? View.VISIBLE : View.INVISIBLE);
            setActivate(entity.isActivated());

            if (entity.hasMdiIcon() && entity.isSensor()) {
                mTextIcon.setVisibility(View.VISIBLE);
                mTextIcon.setText(MDIFont.getIcon(entity.attributes.icon));
            } else {
                mTextIcon.setVisibility(View.GONE);
            }

            if (entity.isScript() || entity.isInputSelect()) {
                mIndicator.setVisibility(View.INVISIBLE);
            }

            mImageView.setVisibility(View.GONE);

            if (entity.isSensor()) {
                setColor(R.color.color_sensor);

                if (entity.attributes.entityPicture != null) {

                    mImageView.setVisibility(View.VISIBLE);
                    mTextState.setText("");

                    mTextGroup.setText(entity.getFriendlyState());

                    Uri uri = Uri.parse("https://etc.com/im;png");

                    GlideApp.with(EditActivity.this)
                            .load(entity.attributes.getEntityPictureUri())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.d("YouQi", "Load Failed" + entity.attributes.getEntityPictureUri());

//                                    mImageView.setVisibility(View.GONE);

//                                    mTextState.setTypeface(ResourcesCompat.getFont(context, R.font.mdi));
//                                    mTextState.setVisibility(View.VISIBLE);
//                                    mTextState.setText(MDIFont.getIcon("mdi:alert-decagram"));
//                                    mTextState.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.md_red_500, null));
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Log.d("YouQi", "Resouce Ready");
                                    return false;
                                }
                            })
                            //.placeholder(R.drawable.loading_spinner)
                            .into(mImageView);
                }

                //} else if (entity.isBinarySensor()) {
                //    setColor(R.color.color_amber);
            } else {
                setColor(R.color.color_xiaomi);
            }

            if (entity.isCircle()) {
                //mCardView.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.md_blue_A200, null));
                mCardView.setCardBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_light_blue_400, null));
                setColor(R.color.color_sensor);
            } else {
                mCardView.setCardBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_white_1000, null));
            }

        }
    }

    //https://stackoverflow.com/questions/39825125/android-recyclerview-cursorloader-contentprovider-load-more
    private class EntityAdapter extends RecyclerView.Adapter<EntityTileViewHolder> implements ItemTouchHelperAdapter {
        private ArrayList<Entity> items;

        EntityAdapter(ArrayList<Entity> items) {
            this.items = items;
        }

        @Override
        public EntityTileViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tile_entity, viewGroup, false);
            return new EntityTileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final EntityTileViewHolder viewHolder, final int position) {
            final Entity item = items.get(position);

            viewHolder.setData(item);
            viewHolder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(EditActivity.this)
                            .cancelable(true)
                            .autoDismiss(true)
                            .content(getString(R.string.message_remove, item.getFriendlyName()))
                            .negativeText(R.string.action_cancel)
                            .positiveText(R.string.action_remove)
                            .buttonRippleColorRes(R.color.md_grey_500)
                            .positiveColorRes(R.color.md_blue_500)
                            .negativeColorRes(R.color.md_blue_500)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    //viewHolder.getAdapterPosition()
                                    int currentPosition = viewHolder.getAdapterPosition();
                                    Log.d("YouQi", "currentPosition: " + currentPosition);
                                    items.remove(currentPosition);
                                    notifyItemRemoved(currentPosition);
                                }
                            })
                            .show();

                }
            });


        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        void clearAll() {
            mEdited = true;
            items.clear();
            notifyDataSetChanged();
        }

        ArrayList<Entity> getItems() {
            return items;
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            mEdited = true;

            Entity str = mItems.get(fromPosition);
            mItems.remove(fromPosition);
            mItems.add(toPosition, str);
            //Collections.swap(items, fromPosition, toPosition);

            notifyItemMoved(fromPosition, toPosition);
            return false;
        }

        @Override
        public void onItemDismiss(int position) {
            mEdited = true;
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    private Paint getDividerPaint() {
        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.colorDivider, null));
        paint.setAntiAlias(true);
        paint.setPathEffect(new DashPathEffect(new float[]{25.0f, 25.0f}, 0));
        return paint;
    }

    private class SaveTask extends AsyncTask<Void, String, ErrorMessage> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected ErrorMessage doInBackground(Void... params) {
            try {
                DatabaseManager.getInstance(EditActivity.this).updateDashboard(mGroup.groupId, mAdapter.getItems());
                DatabaseManager.getInstance(EditActivity.this).updateSortKeyForGroup(0, mGroup.groupId);
            } catch (Exception e) {
                e.printStackTrace();
                return new ErrorMessage("System Exception", e.toString());
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //setStatus(values[0]);
        }

        @Override
        protected void onPostExecute(final ErrorMessage errorMessage) {
            mSaveTask = null;
            mProgressDialog.dismiss();

            if (errorMessage != null) {
                FaultUtil.showError(EditActivity.this, getString(R.string.title_saving_error), errorMessage.message);
            } else {
                setResult(Activity.RESULT_OK, getIntent());
                finish();
                Toast.makeText(EditActivity.this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mSaveTask = null;
            mProgressDialog.dismiss();
        }
    }
}
