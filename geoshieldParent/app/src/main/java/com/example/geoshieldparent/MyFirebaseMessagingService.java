package com.example.geoshieldparent;


import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = null;
        String body = null;

        // Data-only message (background/killed e eta-i kaj kore)
        Map<String, String> data = remoteMessage.getData();
        if (data != null) {
            title = data.get("title");
            body = data.get("body");
        }

        // App foreground e thakle notification block theke
        if (remoteMessage.getNotification() != null) {
            if (title == null) title = remoteMessage.getNotification().getTitle();
            if (body == null) body = remoteMessage.getNotification().getBody();
        }

        if (title == null && body == null) return;

        Bundle b = new Bundle();
        b.putString(AlertActivity.EXTRA_TITLE, title);
        b.putString(AlertActivity.EXTRA_MESSAGE, body);

        AlarmService.start(this, b);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        // token server e pathan (apnar ager code ja chilo ta rakhun)
    }
}
