package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.util.CommonUtil;

import java.util.Locale;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class InputTextFragment extends BaseControlFragment implements View.OnClickListener {

    private EditText mInput;
    private MaterialDialog mDialog;

    public static InputTextFragment newInstance(Entity entity) {
        InputTextFragment fragment = new InputTextFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new MaterialDialog.Builder(getActivity())
                .title(mEntity.getFriendlyName())
                .autoDismiss(true)
                .cancelable(true)
                .inputRangeRes(mEntity.attributes.min.intValue(), mEntity.attributes.max.intValue(), R.color.md_red_500)
                .alwaysCallInputCallback()
                .input("Value", mEntity.state, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                        final String inputString = input.toString();
                        boolean isValid = true;
                        if (mEntity.attributes.pattern != null) {
                            isValid = inputString.matches(mEntity.attributes.pattern);
                            //Log.d("YouQi", "Pattern: " + mEntity.attributes.pattern + ", isValid? " + (isValid ? "true" : "false"));
                        }

                        if (isValid) {
                            isValid = inputString.length() >= mEntity.attributes.min.intValue() && inputString.length() <= mEntity.attributes.max.intValue();
                        }

                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(isValid);
                    }
                })
                .positiveText(R.string.button_set)
                .negativeText(R.string.button_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String input = CommonUtil.getDialogInput(dialog);
                        callService(mEntity.getDomain(), "set_value", new CallServiceRequest(mEntity.entityId).setValue(input));
                    }
                })
                .build();
        mInput = mDialog.getInputEditText();
        CommonUtil.fixDialogKeyboard(mDialog);
        return mDialog;
    }

    private void refreshUi() {
        mInput.setText("");
        mInput.append(mEntity.state);
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.button_cancel:
//                dismiss();
//                break;
//            case R.id.button_set:
//                callService(mEntity.getDomain(), "set_value", new CallServiceRequest(mEntity.entityId).setValue(mInput.getText().toString().trim()));
//                dismiss();
//                break;
//        }
    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        refreshUi();
    }
}
