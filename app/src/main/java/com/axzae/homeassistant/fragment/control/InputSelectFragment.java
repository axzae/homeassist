package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.util.CommonUtil;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

/**
 * Simple fragment with blur effect behind.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class InputSelectFragment extends BaseControlFragment {

    public static InputSelectFragment newInstance(Entity entity) {
        InputSelectFragment fragment = new InputSelectFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mEntity.getFriendlyName());
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.item_list_item, mEntity.attributes.options);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String result = mEntity.attributes.options.get(which);
                callService(mEntity.getDomain(), "select_option", new CallServiceRequest(mEntity.entityId).setOption(result));
            }
        });
        return builder.create();
    }

}
