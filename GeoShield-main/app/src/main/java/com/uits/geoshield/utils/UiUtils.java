package com.uits.geoshield.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.android.material.textfield.TextInputLayout;

public final class UiUtils {
    private UiUtils() {}

    public static String text(TextInputLayout layout) {
        if (layout.getEditText() == null || layout.getEditText().getText() == null) return "";
        return layout.getEditText().getText().toString().trim();
    }

    public static void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /** Paints status and navigation bars for dark full-screen screens. */
    public static void setDarkSystemBars(Activity activity, @ColorRes int colorRes) {
        int color = ContextCompat.getColor(activity, colorRes);
        activity.getWindow().setStatusBarColor(color);
        activity.getWindow().setNavigationBarColor(color);
        WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);
    }

    public static boolean isValidBdPhone(String phone) {
        return phone.replace(" ", "").replace("-", "").matches("^(?:\\+?88)?01[3-9]\\d{8}$");
    }
}
