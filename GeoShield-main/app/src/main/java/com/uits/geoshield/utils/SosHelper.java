package com.uits.geoshield.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.SmsManager;

import com.uits.geoshield.models.Contact;

import java.util.List;

public final class SosHelper {

    private SosHelper() {}

    public static String buildMessage(double lat, double lng) {
        return "SOS! Ami biposh e achi, please help. My location: https://maps.google.com/?q="
                + lat + "," + lng;
    }

    /** Shob contact ke SMS pathay (auto). Koyjon ke pathalo sheta return kore. */
    public static int sendSmsToAll(Context ctx, List<Contact> contacts, String message) {
        SmsManager sms = SimUtils.getSmsManager(ctx);
        int sent = 0;
        for (Contact c : contacts) {
            try {
                sms.sendMultipartTextMessage(c.getPhone(), null,
                        sms.divideMessage(message), null, null);
                sent++;
            } catch (Exception ignored) { }
        }
        return sent;
    }
}