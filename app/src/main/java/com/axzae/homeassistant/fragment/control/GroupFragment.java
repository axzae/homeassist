package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.HomeAssistantServer;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.model.rest.RxPayload;
import com.axzae.homeassistant.provider.DatabaseManager;
import com.axzae.homeassistant.shared.EntityProcessInterface;
import com.axzae.homeassistant.util.CommonUtil;
import com.axzae.homeassistant.util.EntityHandlerHelper;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GroupFragment extends BaseControlFragment implements View.OnClickListener {

    private Call<ArrayList<ArrayList<Entity>>> mCall;
    private View mProgressBar;
    private HomeAssistantServer mServer;
    private RecyclerView mRecyclerView;
    private EntityRowAdapter mAdapter;
    private ArrayList<Entity> mItems = new ArrayList<>();

    public static GroupFragment newInstance(Entity entity, HomeAssistantServer server) {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        args.putString("server", CommonUtil.deflate(server));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServer = CommonUtil.inflate(getArguments().getString("server"), HomeAssistantServer.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_group, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        //mConnErrorView = rootView.findViewById(R.id.list_conn_error);


        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mAdapter = new EntityRowAdapter(mItems);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

        rootView.findViewById(R.id.button_close).setOnClickListener(this);
        //callService();
        refreshUi();

        return builder.create();
    }


    private void refreshUi() {
        ArrayList<String> entityIds = mEntity.attributes.entityIds;
        DatabaseManager databaseManager = DatabaseManager.getInstance(getActivity());
        mItems.clear();
        for (String entityId : entityIds) {
//            Log.d("YouQi", "entityId: " + entityId);
            Entity entity = databaseManager.getEntityById(entityId);
            if (entity != null) {
                if (entity.isGroup() || entity.isMediaPlayer()) continue;
                mItems.add(entity);
            } else {
//                Log.d("YouQi", "entityId: " + entityId + " is null");
            }
        }
        mAdapter.notifyDataSetChanged();
        Log.d("YouQi", "itemsize: " + mAdapter.getItemCount());
        //String result = String.format(Locale.ENGLISH, "%s %s", mEntity.attributes.getTemperature().setScale(1, BigDecimal.ROUND_UP).toString(), mEntity.attributes.unitOfMeasurement);
    }

    @Override
    public void onNext(RxPayload payload) {
        switch (payload.event) {
            case "UPDATE":
                if (payload.entity.equals(mEntity)) onChange(payload.entity);
                if (mItems.contains(payload.entity)) onSubChange(payload.entity);
                break;

            case "UPDATE_ALL":
                for (Entity entity : payload.entities) {
                    if (payload.entity.equals(mEntity)) onChange(payload.entity);
                    if (mItems.contains(payload.entity)) onSubChange(payload.entity);
                }
                //mAdapter.updateList(payload.entities);
                break;
        }
    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        //refreshUi();
    }

    private void onSubChange(Entity entity) {
        int pos = mItems.indexOf(entity);
        if (pos != -1) {
            Entity oldEntity = mItems.get(pos);
            if (oldEntity.lastChanged.equals(entity.lastChanged)) {
                return;
            }

            entity.displayOrder = mItems.get(pos).displayOrder;
            mItems.set(pos, entity);
            mAdapter.notifyItemChanged(pos);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_close:
                dismiss();
                break;
        }
    }

    class EntityRowViewHolder extends RecyclerView.ViewHolder {
        ViewGroup mItemView;
        TextView mIconView;

        TextView mMainText;
        TextView mControlText;
        Switch mControlSwitch;
        Button mControlButton;

        ViewGroup mSwitchLayout;
        ViewGroup mCoverLayout;
        TextView mSwitchOff;
        TextView mSwitchOn;
        TextView mSwitchUp;
        TextView mSwitchStop;
        TextView mSwitchDown;

        EntityRowViewHolder(View v) {
            super(v);
            mItemView = v.findViewById(R.id.item);

            mIconView = v.findViewById(R.id.text_mdi);
            mMainText = v.findViewById(R.id.main_text);
            mControlText = v.findViewById(R.id.control_text);
            mControlSwitch = v.findViewById(R.id.control_switch);
            mControlButton = v.findViewById(R.id.control_button);

            mSwitchLayout = v.findViewById(R.id.layout_switch);
            mCoverLayout = v.findViewById(R.id.layout_cover);
            mSwitchOff = v.findViewById(R.id.text_off);
            mSwitchOn = v.findViewById(R.id.text_on);

            mSwitchUp = v.findViewById(R.id.text_up);
            mSwitchStop = v.findViewById(R.id.text_stop);
            mSwitchDown = v.findViewById(R.id.text_down);
        }

        public void showText(String text) {
            mControlText.setVisibility(View.VISIBLE);
            mControlSwitch.setVisibility(View.GONE);
            mControlButton.setVisibility(View.GONE);
            mSwitchLayout.setVisibility(View.GONE);
            mCoverLayout.setVisibility(View.GONE);
            mControlText.setText(text);
        }

        public void showToggle() {
            mControlText.setVisibility(View.GONE);
            mControlSwitch.setVisibility(View.VISIBLE);
            mControlButton.setVisibility(View.GONE);
            mSwitchLayout.setVisibility(View.GONE);
            mCoverLayout.setVisibility(View.GONE);
            mControlSwitch.setOnCheckedChangeListener(null);
        }

        public void showButton(String buttonText) {
            mControlText.setVisibility(View.GONE);
            mControlSwitch.setVisibility(View.GONE);
            mControlButton.setVisibility(View.VISIBLE);
            mSwitchLayout.setVisibility(View.GONE);
            mCoverLayout.setVisibility(View.GONE);

            mControlButton.setText(buttonText);
            mControlButton.setOnClickListener(null);
        }

        public void showSwitch() {
            mControlText.setVisibility(View.GONE);
            mControlSwitch.setVisibility(View.GONE);
            mControlButton.setVisibility(View.GONE);
            mSwitchLayout.setVisibility(View.VISIBLE);
            mCoverLayout.setVisibility(View.GONE);

            mSwitchOff.setOnClickListener(null);
            mSwitchOn.setOnClickListener(null);
        }

        public void showCover() {
            mControlText.setVisibility(View.GONE);
            mControlSwitch.setVisibility(View.GONE);
            mControlButton.setVisibility(View.GONE);
            mSwitchLayout.setVisibility(View.GONE);
            mCoverLayout.setVisibility(View.VISIBLE);

            mSwitchOff.setOnClickListener(null);
            mSwitchOn.setOnClickListener(null);
        }
    }

    private class EntityRowAdapter extends RecyclerView.Adapter<EntityRowViewHolder> {
        private List<Entity> items;

        EntityRowAdapter(List<Entity> items) {
            this.items = items;
        }

        @Override
        public EntityRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_entity_row, viewGroup, false);
            return new EntityRowViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final EntityRowViewHolder viewHolder, final int position) {
            final Entity item = items.get(position);
            final EntityProcessInterface epi = (EntityProcessInterface) getActivity();

            //Log.d("YouQi", "bindview entity: " + CommonUtil.deflate(mEntity));
            viewHolder.mIconView.setText(item.getMdiIcon());
            viewHolder.mMainText.setText(item.getFriendlyName());
            //viewHolder.mSubText.setText(item.getFriendlyStateRow());

            if (item.isScript() || item.isScene()) {
                viewHolder.showButton("Activate");
                viewHolder.mControlButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        triggerAnswer(item, "single");
                        EntityHandlerHelper.onEntityClick((EntityProcessInterface) getActivity(), item);
                    }
                });
            } else if (item.isInputDateTime()) {
                viewHolder.showText(item.state);
            } else if (item.isSwitch()) {
                viewHolder.showSwitch();
                viewHolder.mSwitchOff.setActivated(!item.isActivated());
                viewHolder.mSwitchOff.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        triggerAnswer(item, "single");
                        epi.callService("homeassistant", "turn_off", new CallServiceRequest(item.entityId));
                    }
                });

                viewHolder.mSwitchOn.setActivated(item.isActivated());
                viewHolder.mSwitchOn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        triggerAnswer(item, "single");
                        epi.callService("homeassistant", "turn_on", new CallServiceRequest(item.entityId));
                    }
                });
            } else if (item.isCover()) {
                viewHolder.showCover();

                viewHolder.mSwitchUp.setEnabled(!"open".equals(item.state));
                viewHolder.mSwitchUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        triggerAnswer(item, "single");
                        epi.callService(mEntity.getDomain(), "open_cover", new CallServiceRequest(item.entityId));
                    }
                });
                viewHolder.mSwitchStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        triggerAnswer(item, "single");
                        epi.callService(mEntity.getDomain(), "stop_cover", new CallServiceRequest(item.entityId));
                    }
                });
                viewHolder.mSwitchDown.setEnabled(!"closed".equals(item.state));
                viewHolder.mSwitchDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        triggerAnswer(item, "single");
                        epi.callService(mEntity.getDomain(), "close_cover", new CallServiceRequest(item.entityId));
                    }
                });

            } else if (item.isToggleable()) {
                viewHolder.showToggle();
                viewHolder.mControlSwitch.setChecked(item.isActivated());
                viewHolder.mControlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        triggerAnswer(item, "single");
                        epi.callService("homeassistant", "turn_" + (b ? "on" : "off"), new CallServiceRequest(item.entityId));
                    }
                });
            } else {
                viewHolder.showText(item.getFriendlyStateRow());
            }

            viewHolder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    triggerAnswer(item, "long");
                    boolean isConsumed = false;

                    if (item.isScript() || item.isScene()) {
                        //do nothing
                    } else {
                        isConsumed = EntityHandlerHelper.onEntityLongClick((EntityProcessInterface) getActivity(), item);
                    }

                    if (!isConsumed) {
                        epi.showToast(getString(R.string.toast_noaction));
                    }
                }
            });

            //viewHolder.mDateView.setText(item.date);
            //viewHolder.mChangelogView.loadLogs(item.logs);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private void triggerAnswer(Entity entity, String clickType) {
        Log.d("YouQi", "Stub!");
    }


}
