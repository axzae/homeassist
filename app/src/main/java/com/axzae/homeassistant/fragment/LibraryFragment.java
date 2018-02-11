package com.axzae.homeassistant.fragment;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.Library;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class LibraryFragment extends Fragment {

    protected RecyclerView mRecyclerView;
    protected LibraryAdapter mAdapter;

    public static LibraryFragment getInstance() {
        LibraryFragment fragment = new LibraryFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_library, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        ArrayList<Library> libraries = new ArrayList<>();
        libraries.add(new Library("Retrofit", "Square", "Apache v2.0", "https://square.github.io"));
        libraries.add(new Library("OkHttp", "Square", "Apache v2.0", "https://square.github.io"));
        libraries.add(new Library("Material Dialog", "Aidan Follestad", "Apache v2.0", "https://github.com/afollestad"));
        libraries.add(new Library("Glide", "Bump Technologies", "Apache v2.0", "https://github.com/bumptech"));
        libraries.add(new Library("Gson", "Google", "Apache v2.0", "https://github.com/google/gson"));
        libraries.add(new Library("Android Support Library", "Google", "Apache v2.0", "https://developer.android.com/topic/libraries/support-library/"));
        libraries.add(new Library("RecyclerView-FlexibleDivider", "Yoshihito Ikeda", "Apache v2.0", "https://github.com/yqritc"));
        libraries.add(new Library("DiscreteSeekBar", "Gustavo Claramunt", "Apache v2.0", "https://github.com/AnderWeb"));
        libraries.add(new Library("MaterialEditText", "Kai Zhu (rengwuxian)", "Apache v2.0", "https://github.com/rengwuxian"));
        libraries.add(new Library("MaterialProgressBar", "Zhang Hai", "Apache v2.0", "https://github.com/DreaminginCodeZH"));
        libraries.add(new Library("TapTargetView", "Keepsafe Software Inc", "Apache v2.0", "https://github.com/KeepSafe/"));
        libraries.add(new Library("Konfetti", "Dion Segijn", "ISC License", "https://github.com/DanielMartinus/"));
        libraries.add(new Library("BlurDialogFragment", "tvbarthel", "Apache 2.0", "https://github.com/tvbarthel/"));
        libraries.add(new Library("Color Picker", "QuadFlask", "Apache 2.0", "https://github.com/QuadFlask/"));
        libraries.add(new Library("HelloCharts", "Leszek Wach", "Apache 2.0", "https://github.com/lecho/"));
        libraries.add(new Library("MaterialSearchView", "Miguel Catalan Bañuls", "Apache 2.0", "https://github.com/MiguelCatalan/"));
        libraries.add(new Library("Material Design Icons", "Templarian", "WTFPL", "https://github.com/templarian/"));
        libraries.add(new Library("RxAndroid", "The RxAndroid authors", "Apache 2.0", "https://github.com/ReactiveX/"));
        libraries.add(new Library("RxJava", "RxJava Contributors", "Apache 2.0", "https://github.com/ReactiveX/"));
        libraries.add(new Library("StatusBarUtil", "Jaeger Chen", "Apache 2.0", "https://github.com/laobie/"));
        libraries.add(new Library("MarkdownView", "tiagohm", "Apache 2.0", "https://github.com/tiagohm/"));
        libraries.add(new Library("CircleImageView", "Henning Dodenhof", "Apache 2.0", "https://github.com/hdodenhof/"));
        libraries.add(new Library("Material DateTime Picker", "Wouter Dullaert", "Apache 2.0", "https://github.com/wdullaer/"));
        //libraries.add(new Library("Android Debug Database", "Amit Shekhar", "Apache v2.0", "https://github.com/amitshekhariitbhu/"));
        //libraries.add(new Library("Calligraphy", "Christopher Jenkins", "Apache v2.0", "https://github.com/chrisjenx"));
        //libraries.add(new Library("Home Assistant Assets", "Jeremy Geltman", "Creative Commons by-nc-sa v4.0", "https://home-assistant.io/blog/2015/03/08/new-logo/"));
        //libraries.add(new Library("PhotoView", "Chris Banes", "Apache v2.0", "https://github.com/chrisbanes/PhotoView"));
        //libraries.add(new Library("Product Tour", "Donghua Xun", "Apache v2.0", "https://github.com/matrixxun/ProductTour"));
        //libraries.add(new Library("Showcase View", "Alex Curran", "Apache v2.0", "https://github.com/amlcurran/ShowcaseView"));

        Collections.sort(libraries, new Comparator<Library>() {
            @Override
            public int compare(Library lhs, Library rhs) {
                return lhs.name.compareTo(rhs.name); //descending order
            }
        });

        mAdapter = new LibraryAdapter(libraries);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.divider, null));
        paint.setAntiAlias(true);
        paint.setPathEffect(new DashPathEffect(new float[]{25.0f, 25.0f}, 0));
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).showLastDivider().paint(paint).build()); //.marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
        mRecyclerView.addOnScrollListener(new SnapScrollListener());
        mRecyclerView.setNestedScrollingEnabled(false);

        return rootView;
    }

    class LibraryViewHolder extends RecyclerView.ViewHolder {
        //private final View rootView;
        ViewGroup mItemView;
        TextView mNameView;
        TextView mCodeView;

        LibraryViewHolder(View v) {
            super(v);
            //rootView = v;
            mItemView = (ViewGroup) v.findViewById(R.id.item);
            mNameView = (TextView) v.findViewById(R.id.main_text);
            mCodeView = (TextView) v.findViewById(R.id.sub_text);
        }
    }

    private class LibraryAdapter extends RecyclerView.Adapter<LibraryViewHolder> {
        private List<Library> libraries;

        //public LibraryAdapter() {
        //    this.libraries = new ArrayList<>();
        //}

        LibraryAdapter(List<Library> libraries) {
            this.libraries = libraries;
        }

        @Override
        public LibraryViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            Log.d("YouQi", "Created ViewHolder Library");
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_library, viewGroup, false);
            return new LibraryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final LibraryViewHolder viewHolder, final int position) {
            final Library library = libraries.get(position);

            viewHolder.mNameView.setText(library.name);

            String subText = library.author;
            if (library.license != null) {
                subText += ", " + library.license;
            }
            viewHolder.mCodeView.setText(subText);

            if (library.website != null) {
                viewHolder.mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("YouQi", "clicked");
                        String url = library.website;
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        //builder.setStartAnimations(this, R.anim.right_in, R.anim.left_out);
                        //builder.setExitAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        builder.setToolbarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
                        getActivity().overridePendingTransition(R.anim.stay_still, R.anim.fade_out);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return libraries.size();
        }
    }

    private class SnapScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            Log.d("YouQi", "onScrollStateChanged. newState: " + newState);
            if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                final int scrollDistance = getScrollDistanceOfColumnClosestToLeft(mRecyclerView);
                Log.d("YouQi", "scrollDistance" + scrollDistance);
                if (scrollDistance != 0) {
                    mRecyclerView.smoothScrollBy(scrollDistance, 0);
                }
            }
        }

    }

    private int getScrollDistanceOfColumnClosestToLeft(final RecyclerView recyclerView) {
        final LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        final RecyclerView.ViewHolder firstVisibleColumnViewHolder = recyclerView.findViewHolderForAdapterPosition(manager.findFirstVisibleItemPosition());
        if (firstVisibleColumnViewHolder == null) {
            return 0;
        }

        Log.d("YouQi", "firstVisibleColumnViewHolder: " + firstVisibleColumnViewHolder.getAdapterPosition());

        final int columnWidth = firstVisibleColumnViewHolder.itemView.getMeasuredWidth();
        final int left = firstVisibleColumnViewHolder.itemView.getLeft();
        final int absoluteLeft = Math.abs(left);

        final int columnHeight = firstVisibleColumnViewHolder.itemView.getMeasuredHeight();
        final int top = firstVisibleColumnViewHolder.itemView.getTop();
        final int absoluteTop = Math.abs(top);

        Log.d("YouQi", "columnWidth: " + columnWidth);
        Log.d("YouQi", "left: " + left);
        Log.d("YouQi", "absoluteLeft: " + absoluteLeft);

        Log.d("YouQi", "columnHeight: " + columnHeight);
        Log.d("YouQi", "top: " + top);
        Log.d("YouQi", "absoluteTop: " + absoluteTop);

        //return absoluteLeft <= (columnWidth / 2) ? left : columnWidth - absoluteLeft;
        return absoluteTop <= (columnHeight / 2) ? top : columnHeight - absoluteTop;
    }
}
