package com.example.geoshieldparent;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    // Parent app er MainActivity.onCreate() e ekbar call korun
    public static void ensureAlertPermissions(Activity a) {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(a, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(a,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        if (Build.VERSION.SDK_INT >= 34) {
            NotificationManager nm = a.getSystemService(NotificationManager.class);
            if (!nm.canUseFullScreenIntent()) {
                Intent i = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        Uri.parse("package:" + a.getPackageName()));
                a.startActivity(i);
            }
        }
    }
}
