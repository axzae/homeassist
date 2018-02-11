package com.axzae.homeassistant.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Changelog;
import com.axzae.homeassistant.view.ChangelogView;

import java.util.List;

public class ChangelogFragment extends Fragment {

    protected RecyclerView mRecyclerView;
    protected ChangelogAdapter mAdapter;

    public static ChangelogFragment getInstance() {
        ChangelogFragment fragment = new ChangelogFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_library, container, false);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);

        mAdapter = new ChangelogAdapter(Changelog.getItems());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        //mRecyclerView.setNestedScrollingEnabled(false);
        return rootView;
    }

    class LibraryViewHolder extends RecyclerView.ViewHolder {
        ViewGroup mItemView;
        TextView mVersionView;
        TextView mDateView;
        TextView mCodeView;
        ChangelogView mChangelogView;

        LibraryViewHolder(View v) {
            super(v);
            mItemView = v.findViewById(R.id.item);
            mVersionView = v.findViewById(R.id.text_version);
            mDateView = v.findViewById(R.id.text_date);
            mCodeView = v.findViewById(R.id.sub_text);
            mChangelogView = v.findViewById(R.id.view_changelog);
        }
    }

    private class ChangelogAdapter extends RecyclerView.Adapter<LibraryViewHolder> {
        private List<Changelog> items;

        ChangelogAdapter(List<Changelog> items) {
            this.items = items;
        }

        @Override
        public LibraryViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_changelog, viewGroup, false);
            return new LibraryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final LibraryViewHolder viewHolder, final int position) {
            final Changelog item = items.get(position);

            viewHolder.mVersionView.setText(item.version);
            viewHolder.mDateView.setText(item.date);
            viewHolder.mChangelogView.loadLogs(item.logs);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

}
