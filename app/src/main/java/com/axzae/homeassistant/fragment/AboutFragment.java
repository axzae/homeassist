package com.axzae.homeassistant.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.axzae.homeassistant.R;

import java.util.Calendar;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class AboutFragment extends Fragment {
    private static final int MAX_CLICK_DURATION = 200;

    public static AboutFragment getInstance() {
        AboutFragment fragment = new AboutFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        KonfettiView viewKonfetti = rootView.findViewById(R.id.viewKonfetti);
        viewKonfetti.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        viewKonfetti.build()
                .addColors(
                        ResourcesCompat.getColor(getResources(), R.color.md_grey_300, null),
                        ResourcesCompat.getColor(getResources(), R.color.md_grey_500, null),
                        ResourcesCompat.getColor(getResources(), R.color.primary, null),
                        ResourcesCompat.getColor(getResources(), R.color.primary_light, null)
                )
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 10f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(9, 5f))
                .setFadeOutEnabled(true)
                .setPosition(-50f, screenWidth + 50f, -50f, -100f)
                .stream(100, 10000L);


        final ImageView playStoreView = rootView.findViewById(R.id.playstore_badge);
        playStoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
                } else {
                    flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
                }
                goToMarket.addFlags(flags);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
                }
            }
        });

        playStoreView.setOnTouchListener(getTouchListener(playStoreView));

        final ImageView trelloView = (ImageView) rootView.findViewById(R.id.trello_badge);
        trelloView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://trello.com/b/C0YnMv3L/homeassist");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
        trelloView.setOnTouchListener(getTouchListener(trelloView));


        return rootView;
    }

    View.OnTouchListener getTouchListener(final View mView) {
        final long[] startClickTime = new long[1];
        return new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startClickTime[0] = Calendar.getInstance().getTimeInMillis();

                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mView, "scaleX", 0.95f);
                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mView, "scaleY", 0.95f);
                        scaleDownX.setDuration(200);
                        scaleDownY.setDuration(200);

                        AnimatorSet scaleDown = new AnimatorSet();
                        scaleDown.play(scaleDownX).with(scaleDownY);
                        scaleDown.setInterpolator(new OvershootInterpolator());
                        scaleDown.start();

                        // spinslot();
                        break;

                    case MotionEvent.ACTION_UP:

                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime[0];
                        if (clickDuration < MAX_CLICK_DURATION) {

                            ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(mView, "scaleX", 1f);
                            ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(mView, "scaleY", 1f);
                            scaleDownX2.setDuration(200);
                            scaleDownY2.setDuration(200);

                            AnimatorSet scaleDown2 = new AnimatorSet();
                            scaleDown2.play(scaleDownX2).with(scaleDownY2);
                            scaleDown2.setInterpolator(new OvershootInterpolator());
                            scaleDown2.start();

                            scaleDown2.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mView.callOnClick();
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                            return true;
                        }


                    case MotionEvent.ACTION_CANCEL:
                        ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(mView, "scaleX", 1f);
                        ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(mView, "scaleY", 1f);
                        scaleDownX2.setDuration(200);
                        scaleDownY2.setDuration(200);

                        AnimatorSet scaleDown2 = new AnimatorSet();
                        scaleDown2.play(scaleDownX2).with(scaleDownY2);
                        scaleDown2.setInterpolator(new OvershootInterpolator());
                        scaleDown2.start();
                        break;
                }
                return true;
            }
        };
    }

}
