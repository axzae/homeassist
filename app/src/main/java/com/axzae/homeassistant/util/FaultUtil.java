package com.axzae.homeassistant.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.axzae.homeassistant.R;
import com.google.gson.JsonSyntaxException;

import retrofit2.Response;

public class FaultUtil {

    public static boolean isRetrofitServerError(Response response) {
        if (response.code() == 200 && response.body() != null) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("SameParameterValue")
    public static void showError(final Context context, String title, String content) {
        new MaterialDialog.Builder(context)
                .cancelable(false)
                .title(title)
                .content(content)
                .positiveText(R.string.button_continue)
                .positiveColorRes(R.color.md_red_500)
                .buttonRippleColorRes(R.color.md_grey_200)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static String getPrintableMessage(Context context, Throwable t) {

        if (t instanceof java.net.ConnectException) {
            return context.getString(R.string.exception_connect);
        }

        if (t instanceof java.net.SocketTimeoutException) {
            return context.getString(R.string.exception_sockettimeout);
        }

        if (t instanceof java.net.UnknownHostException) {
            return context.getString(R.string.exception_unknownhost);
        }

        if (t instanceof java.net.SocketException) {
            return context.getString(R.string.exception_socket);
        }

        if (t instanceof JsonSyntaxException) {
            return context.getString(R.string.exception_jsonsyntax);
        }

        return t.getClass().getName(); //t.getMessage();

    }
}
