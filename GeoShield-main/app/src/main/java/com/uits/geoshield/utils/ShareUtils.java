package com.uits.geoshield.utils;

import android.content.Context;
import android.content.Intent;

import com.uits.geoshield.R;

import java.security.SecureRandom;

public final class ShareUtils {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static String tripLink;

    private ShareUtils() {}

    /** One link per app session; a real implementation would request it from the backend. */
    public static synchronized String getTripLink() {
        if (tripLink == null) {
            SecureRandom random = new SecureRandom();
            StringBuilder sb = new StringBuilder("https://geoshield.app/track/");
            for (int i = 0; i < 8; i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            tripLink = sb.toString();
        }
        return tripLink;
    }

    public static void shareLiveLink(Context context) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message, getTripLink()));
        context.startActivity(Intent.createChooser(send, context.getString(R.string.share_chooser)));
    }
}
