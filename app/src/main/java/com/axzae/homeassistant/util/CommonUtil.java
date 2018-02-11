package com.axzae.homeassistant.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.axzae.homeassistant.R;
import com.google.android.gms.ads.AdRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CommonUtil {
    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    @SuppressWarnings("SameParameterValue")
    public static int pxFromDp(final Context context, final float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static CharSequence getSpanText(Context context, String text, Integer colorResId, Float size) {
        Spannable result = new SpannableString(text);
        if (colorResId != null) {
            result.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(context.getResources(), colorResId, null)), 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (size != null) {
            result.setSpan(new RelativeSizeSpan(size), 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return result;
    }

    public static Gson getGson() {
        return new GsonBuilder()
                //.registerTypeAdapter(Subscriber.SubscriberList.class, new Subscriber.SubscriberListTypeAdapter())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ")
                .create();
    }

    public static String deflate(Object object) {
        return new Gson().toJson(object);
    }

    public static <T> T inflate(String json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }

    public static <T> T inflate(String json, Type typeOfT) {
        return new Gson().fromJson(json, typeOfT);
    }

    public static String getCertificateSHA1Fingerprint(Context mContext) throws Exception {
        PackageManager pm = mContext.getPackageManager();
        String packageName = mContext.getPackageName();
        int flags = PackageManager.GET_SIGNATURES;
        PackageInfo packageInfo = null;
        packageInfo = pm.getPackageInfo(packageName, flags);
        Signature[] signatures = packageInfo.signatures;
        byte[] cert = signatures[0].toByteArray();
        InputStream input = new ByteArrayInputStream(cert);
        CertificateFactory cf = null;
        cf = CertificateFactory.getInstance("X509");
        X509Certificate c = null;
        c = (X509Certificate) cf.generateCertificate(input);
        String hexString = null;
        MessageDigest md = MessageDigest.getInstance("SHA1");
        byte[] publicKey = md.digest(c.getEncoded());
        hexString = byte2HexFormatted(publicKey);
        return hexString;
    }

    public static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = "0" + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1)) str.append(':');
        }
        return str.toString();
    }

    public static void logLargeString(String TAG, String str) {
        if (str.length() > 3000) {
            Log.d(TAG, str.substring(0, 3000));
            logLargeString(TAG, str.substring(3000));
        } else {
            Log.d(TAG, str); // continuation
        }
    }

    public static boolean isUiThread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? Looper.getMainLooper().isCurrentThread() : Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    public static String readFromAssets(Context context, String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

        // do reading, usually loop until end of file reading
        StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine).append("\n"); // process line
            mLine = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }

    public static File writeToExternalCache(Context context, String filename, String content) {
        File rootDir = context.getExternalCacheDir();
        if (rootDir != null) {
            if (!rootDir.exists()) {
                boolean isSuccess = rootDir.mkdirs();
                if (!isSuccess) {
                    Log.d("YouQi", "failed to create" + rootDir.getAbsolutePath());
                }
            }

            File bootstrapFile = new File(rootDir, filename);
            if (bootstrapFile.exists()) {
                boolean isSuccess = bootstrapFile.delete();
            }
            Log.d("YouQi", "External file system root: " + bootstrapFile.getAbsolutePath());

            try {
                FileOutputStream f = new FileOutputStream(bootstrapFile);
                PrintWriter pw = new PrintWriter(f);
                pw.println(content);
                pw.flush();
                pw.close();
                f.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bootstrapFile;
        }

        return null;
    }

    public static String getDialogInput(final MaterialDialog dialog) {
        EditText editText = dialog.getInputEditText();
        if (editText != null) {
            return dialog.getInputEditText().getText().toString().trim();
        } else {
            throw new IllegalStateException("EditText not found");
        }
    }

    public static void fixDialogKeyboard(final MaterialDialog dialog) {
        //https://github.com/afollestad/material-dialogs/issues/1105
        EditText inputEditText = dialog.getInputEditText();
        if (inputEditText != null) {
            inputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_SEARCH)) {
                        View positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
                        if (dialog.getActionButton(DialogAction.POSITIVE).isEnabled()) {
                            positiveButton.callOnClick();
                        } else {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    public static AdRequest getAdRequest() {
        return new AdRequest.Builder()
                .addTestDevice("CE1828AEF0F43C2D5DF5834F3F309B02")
                .build();
    }

    public static void setMenuDrawableColor(Context context, MenuItem menuItem, int resColor) {
        Drawable drawable = menuItem.getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, resColor));
        menuItem.setIcon(drawable);
    }


    public static MaterialDialog getProgressDialog(Context context) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                //.title("progress_dialog")
                .content(context.getString(R.string.progress_wait))
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .build();
        dialog.setCancelable(false); //BackButton
        dialog.setCanceledOnTouchOutside(false); //Outside Touch
        return dialog;
        //dialog.show();
    }

    public static String getLocale(Context context) {
        Locale locale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            locale = context.getResources().getConfiguration().locale;
        }

        return String.format(Locale.ENGLISH, "%s_%s", locale.getLanguage(), locale.getCountry());
    }

    public static String getNameTitleCase(String name) {
        final String ACTIONABLE_DELIMITERS = " '-/";
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.isEmpty()) {
            boolean capitaliseNext = true;
            for (char c : name.toCharArray()) {
                c = (capitaliseNext) ? Character.toUpperCase(c) : Character.toLowerCase(c);
                sb.append(c);
                capitaliseNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0);
            }
            name = sb.toString();
            if (name.startsWith("Mc") && name.length() > 2) {
                char c = name.charAt(2);
                if (ACTIONABLE_DELIMITERS.indexOf((int) c) < 0) {
                    sb = new StringBuilder();
                    sb.append(name.substring(0, 2));
                    sb.append(name.substring(2, 3).toUpperCase());
                    sb.append(name.substring(3));
                    name = sb.toString();
                }
            } else if (name.startsWith("Mac") && name.length() > 3) {
                char c = name.charAt(3);
                if (ACTIONABLE_DELIMITERS.indexOf((int) c) < 0) {
                    sb = new StringBuilder();
                    sb.append(name.substring(0, 3));
                    sb.append(name.substring(3, 4).toUpperCase());
                    sb.append(name.substring(4));
                    name = sb.toString();
                }
            }
        }
        return name;
    }

    public static void setBouncyTouch(final View mView) {
        final int MAX_CLICK_DURATION = 200;
        final long[] startClickTime = new long[1];

        View.OnTouchListener touchListener = new View.OnTouchListener() {
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

                        //mImageView.setEnabled(false);
                        break;
                }
                return true;
            }
        };

        mView.setOnTouchListener(touchListener);
    }

}
