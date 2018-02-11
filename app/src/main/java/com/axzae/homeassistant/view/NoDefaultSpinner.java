package com.axzae.homeassistant.view;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NoDefaultSpinner extends AppCompatSpinner {

    private SpinnerAdapter originalAdapter;

    public NoDefaultSpinner(Context context) {
        super(context);
    }

    public NoDefaultSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoDefaultSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SpinnerAdapter getOriginalAdapter() {
        return originalAdapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        try {
            int selectedPosition = getSelectedItemPosition();
            if (selectedPosition == -1) {
                View view = getAdapter().getView(-1, null, this);
                view.measure(getMeasuredWidth(), getMeasuredHeight());
                int height = Math.max(getMeasuredHeight(), view.getMeasuredHeight() + getPaddingBottom() + getPaddingTop());
                setMeasuredDimension(getMeasuredWidth(), height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAdapter(SpinnerAdapter orig) {
        originalAdapter = orig;
        final SpinnerAdapter adapter = newProxy(orig);

        super.setAdapter(adapter);

        try {
            final Method m = AdapterView.class.getDeclaredMethod("setNextSelectedPositionInt", int.class);
            m.setAccessible(true);
            m.invoke(this, -1);

            final Method n = AdapterView.class.getDeclaredMethod("setSelectedPositionInt", int.class);
            n.setAccessible(true);
            n.invoke(this, -1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SpinnerAdapter newProxy(SpinnerAdapter obj) {
        return (SpinnerAdapter) java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                new Class[]{SpinnerAdapter.class},
                new SpinnerAdapterProxy(obj));
    }


    /**
     * Intercepts getView() to display the prompt if position < 0
     */
    protected class SpinnerAdapterProxy implements InvocationHandler {

        protected SpinnerAdapter obj;
        protected Method getView;

        protected SpinnerAdapterProxy(SpinnerAdapter obj) {
            this.obj = obj;
            try {
                this.getView = SpinnerAdapter.class.getMethod(
                        "getView", int.class, View.class, ViewGroup.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
            try {
                return m.equals(getView) &&
                        (Integer) (args[0]) < 0 ?
                        getView((Integer) args[0], (View) args[1], (ViewGroup) args[2]) :
                        m.invoke(obj, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected View getView(int position, View convertView, ViewGroup parent)
                throws IllegalAccessException {

            if (position < 0) {
                Log.d("YouQi", "Negative Position");

                //int resIdTextView = android.R.layout.simple_spinner_item;
                //int resIdTextView = R.layout.spinner_edittext_lookalike;
                //final TextView v =  (TextView) ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(resIdTextView, parent, false);
                final TextView v = (TextView) obj.getView(0, convertView, parent);
                v.setTextColor(v.getCurrentHintTextColor()); //ContextCompat.getColor(getContext(), R.color.md_grey_300));
                v.setTextSize(18);
                v.setText(getPrompt());
                //v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                return v;
            }
            return obj.getView(position, convertView, parent);
        }
    }
}