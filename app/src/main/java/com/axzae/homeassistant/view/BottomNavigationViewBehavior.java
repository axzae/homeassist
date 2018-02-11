package com.axzae.homeassistant.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

@SuppressWarnings("unused")
public class BottomNavigationViewBehavior extends CoordinatorLayout.Behavior<BottomNavigationView> {
    private boolean visible = true;
    private boolean inStartPosition = true;
    private float oldY;
    private DisplayMetrics metrics;

    public BottomNavigationViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        metrics = Resources.getSystem().getDisplayMetrics();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, BottomNavigationView fab, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, BottomNavigationView child, View dependency) {
        if (dependency instanceof AppBarLayout) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();

            float dy = oldY - dependency.getY();
            moveDown(child, dy);
            oldY = dependency.getY();
        }
        return true;
    }

    private void moveDown(View child, float dy) {
        float translationY = child.getTranslationY() + dy;
        if (translationY < 0) {
            translationY = 0f;
        }

        if (translationY > child.getHeight()) {
            translationY = child.getHeight();
        }

        child.setTranslationY(translationY);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull BottomNavigationView child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull BottomNavigationView child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        moveDown(child, dyConsumed);
    }
}