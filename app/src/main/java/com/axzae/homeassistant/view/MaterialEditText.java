package com.axzae.homeassistant.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

public class MaterialEditText extends com.rengwuxian.materialedittext.MaterialEditText {

    public MaterialEditText(Context context) {
        super(context);
    }

    public MaterialEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialEditText(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        setFocusFraction(1f);
        //setFocusFraction(getFloatingLabelFraction());
        super.onDraw(canvas);
    }
}
