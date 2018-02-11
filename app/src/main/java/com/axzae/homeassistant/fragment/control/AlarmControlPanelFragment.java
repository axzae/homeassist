package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.util.CommonUtil;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AlarmControlPanelFragment extends BaseControlFragment implements View.OnClickListener {

    private EditText mInput;
    private MaterialDialog mDialog;

    public static AlarmControlPanelFragment newInstance(Entity entity) {
        AlarmControlPanelFragment fragment = new AlarmControlPanelFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        switch (mEntity.state) {
            case "armed_away":
            case "armed_home":
            case "pending": {

                MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                        .title(mEntity.getFriendlyName())
                        .autoDismiss(true)
                        .cancelable(true)
                        .alwaysCallInputCallback()
                        .negativeText(R.string.action_cancel)
                        .positiveText("Disarm")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                String input = CommonUtil.getDialogInput(dialog);
                                callService(mEntity.getDomain(), "alarm_disarm", new CallServiceRequest(mEntity.entityId).setCode(input));
                            }
                        });

                if (true) {
                    builder.input("Code (Optional)", "", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                            final String inputString = input.toString();
                            boolean isValid = true;
                            if (mEntity.attributes.codeFormat != null) {
                                isValid = inputString.matches(mEntity.attributes.codeFormat);
                                //Log.d("YouQi", "Pattern: " + mEntity.attributes.pattern + ", isValid? " + (isValid ? "true" : "false"));
                            }

                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(isValid);
                        }
                    });
                }
                mDialog = builder.build();
            }
            break;
            case "disarmed": {

                mDialog = new MaterialDialog.Builder(getActivity())
                        .title(mEntity.getFriendlyName())
                        .autoDismiss(true)
                        .cancelable(true)
                        .negativeText("Arm Away")
                        .positiveText("Arm Home")
                        .alwaysCallInputCallback()
                        .input("Code (Optional)", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                final String inputString = input.toString();
                                boolean isValid = true;
                                if (mEntity.attributes.codeFormat != null) {
                                    isValid = inputString.matches(mEntity.attributes.codeFormat);
                                }

                                dialog.getActionButton(DialogAction.NEGATIVE).setEnabled(isValid);
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(isValid);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                String input = CommonUtil.getDialogInput(dialog);
                                callService(mEntity.getDomain(), "alarm_arm_away", new CallServiceRequest(mEntity.entityId).setCode(input));
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                String input = CommonUtil.getDialogInput(dialog);
                                callService(mEntity.getDomain(), "alarm_arm_home", new CallServiceRequest(mEntity.entityId).setCode(input));
                            }
                        })
                        .build();
            }
            break;
        }


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
    }

    @Override
    public void onChange(Entity entity) {
        super.onChange(entity);
        refreshUi();
    }
}
