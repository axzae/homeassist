package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.util.CommonUtil;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

/**
 * Simple fragment with blur effect behind.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralFragment extends BlurDialogFragment implements View.OnClickListener {

    private List<Item> items = new ArrayList<>();
    private Entity mEntity;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;
    private HorizontalDividerItemDecoration mDivider;

    public static GeneralFragment newInstance(Entity entity) {
        GeneralFragment fragment = new GeneralFragment();
        Bundle args = new Bundle();
        Log.d("YouQi", "newInstance");
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation2;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        Log.d("YouQi", "onCreate: " + args.getString("entity"));
        mEntity = CommonUtil.inflate(args.getString("entity"), Entity.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_general, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        rootView.findViewById(R.id.button_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.button_set).setOnClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        initViews();

        items.add(new Item("String", "1234"));
        items.add(new Item("String", "1234"));
        items.add(new Item("String", "1234"));
        items.add(new Item("String", "1234"));

        return builder.create();
    }

    private void initViews() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.colorDivider, null));
        paint.setAntiAlias(true);
        paint.setPathEffect(new DashPathEffect(new float[]{25.0f, 25.0f}, 0));

        mDivider = new HorizontalDividerItemDecoration.Builder(getActivity()).showLastDivider().paint(paint).build();
        mAdapter = new ItemAdapter(items);
        //mRecyclerView.addItemDecoration(mDivider); //.marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

//            mRetryView = new RetryView(findViewById(android.R.id.content));
//            mRetryView.showError();

        //items.add(new Item(getString(R.string.login_id), transaction.dealerId));


    }

    private void refresh() {
    }

    @Override
    protected boolean isDebugEnable() {
        return false;
    }

    @Override
    protected boolean isDimmingEnable() {
        return true;
    }

    @Override
    protected boolean isActionBarBlurred() {
        return false;
    }

    @Override
    protected float getDownScaleFactor() {
        return 6;
    }

    @Override
    protected int getBlurRadius() {
        return 5;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_cancel:
            case R.id.button_set:
                dismiss();
        }
    }


    private class Item {
        public String key;
        public CharSequence value;

        Item(String key, String value) {
            this.key = key;
            this.value = value;
        }

        Item(String key, CharSequence value) {
            this.key = key;
            this.value = value;
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        ViewGroup mItemView;
        TextView mMainText;
        TextView mLabelText;
        ImageView mIconView;

        ItemViewHolder(View v) {
            super(v);
            mItemView = (ViewGroup) v.findViewById(R.id.item);
            mMainText = (TextView) v.findViewById(R.id.main_text);
            mLabelText = (TextView) v.findViewById(R.id.label_text);

            mIconView = (ImageView) v.findViewById(R.id.main_icon);
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> implements View.OnClickListener {
        private List<Item> items;

        private ItemAdapter(List<Item> transactions) {
            this.items = transactions;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.widget_row_item, viewGroup, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder viewHolder, final int position) {
            final int adapterPosition = viewHolder.getAdapterPosition();
            final Item item = items.get(adapterPosition);

            viewHolder.mMainText.setText(item.key);
            viewHolder.mLabelText.setText(item.value);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public void onClick(View view) {

        }
    }
}
