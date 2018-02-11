package com.axzae.homeassistant.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.TypedValue;

/**
 * Embed an icon into a Drawable that can be used as TextView icons, or ActionBar icons.
 * <p>
 * new IconDrawable(context, IconValue.icon_star)
 * .colorRes(R.color.white)
 * .actionBarSize();
 * <p>
 * If you don't set the size of the drawable, it will use the size
 * that is given to him. Note that in an ActionBar, if you don't
 * set the size explicitly it uses 0, so please use actionBarSize().
 */
public class FontIconDrawable extends Drawable {

    public static int ANDROID_ACTIONBAR_ICON_SIZE_DP = 24;

    private final Context context;

    private final String icon;

    private TextPaint paint;

    private int size = -1;

    private int alpha = 255;

    /**
     * Create an IconDrawable.
     *
     * @param context Your activity or application context.
     * @param icon    The icon you want this drawable to display.
     */
    public FontIconDrawable(Context context, String icon, Typeface typeface) {
        this.context = context;
        this.icon = icon;
        paint = new TextPaint();
        paint.setTypeface(typeface);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setUnderlineText(false);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
    }

    /**
     * Set the size of this icon to the standard Android ActionBar.
     *
     * @return The current IconDrawable for chaining.
     */
    public FontIconDrawable actionBarSize() {
        return sizeDp(ANDROID_ACTIONBAR_ICON_SIZE_DP);
    }

    /**
     * Set the size of the drawable.
     *
     * @param dimenRes The dimension resource.
     * @return The current IconDrawable for chaining.
     */
    public FontIconDrawable sizeRes(int dimenRes) {
        return sizePx(context.getResources().getDimensionPixelSize(dimenRes));
    }

    /**
     * Set the size of the drawable.
     *
     * @param size The size in density-independent pixels (dp).
     * @return The current IconDrawable for chaining.
     */
    public FontIconDrawable sizeDp(int size) {
        return sizePx(dpToPx(context.getResources(), size));
    }

    /**
     * Dp to px.
     *
     * @param res the res
     * @param dp  the dp
     * @return the int
     */
    public static int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                res.getDisplayMetrics());
    }

    /**
     * Set the size of the drawable.
     *
     * @param size The size in pixels (px).
     * @return The current IconDrawable for chaining.
     */
    public FontIconDrawable sizePx(int size) {
        this.size = size;
        setBounds(0, 0, size, size);
        invalidateSelf();
        return this;
    }

    /**
     * Set the color of the drawable.
     *
     * @param color The color, usually from android.graphics.Color or 0xFF012345.
     * @return The current IconDrawable for chaining.
     */
    public FontIconDrawable color(int color) {
        paint.setColor(color);
        invalidateSelf();
        return this;
    }

    /**
     * Set the color of the drawable.
     *
     * @param colorRes The color resource, from your R file.
     * @return The current IconDrawable for chaining.
     */
    public FontIconDrawable colorRes(int colorRes) {
        paint.setColor(context.getResources().getColor(colorRes));
        invalidateSelf();
        return this;
    }

    /**
     * Set the alpha of this drawable.
     *
     * @param alpha The alpha, between 0 (transparent) and 255 (opaque).
     * @return The current IconDrawable for chaining.
     */
    public FontIconDrawable alpha(int alpha) {
        setAlpha(alpha);
        invalidateSelf();
        return this;
    }

    @Override
    public int getIntrinsicHeight() {
        return size;
    }

    @Override
    public int getIntrinsicWidth() {
        return size;
    }

    @Override
    public void draw(Canvas canvas) {
        paint.setTextSize(getBounds().height());
        Rect textBounds = new Rect();
        String textValue = icon;
        paint.getTextBounds(textValue, 0, 1, textBounds);
        float textBottom = (getBounds().height() - textBounds.height()) / 2f + textBounds.height() - textBounds.bottom;
        canvas.drawText(textValue, getBounds().width() / 2f, textBottom, paint);
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public boolean setState(int[] stateSet) {
        int oldValue = paint.getAlpha();
        int newValue = isEnabled(stateSet) ? alpha : alpha / 2;
        paint.setAlpha(newValue);
        return oldValue != newValue;
    }

    /**
     * Checks if is enabled.
     *
     * @param stateSet the state set
     * @return true, if is enabled
     */
    public static boolean isEnabled(int[] stateSet) {
        for (int state : stateSet)
            if (state == android.R.attr.state_enabled)
                return true;
        return false;
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public void clearColorFilter() {
        paint.setColorFilter(null);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    /**
     * Sets paint style.
     *
     * @param style to be applied
     */
    public void setStyle(Paint.Style style) {
        paint.setStyle(style);
    }
}