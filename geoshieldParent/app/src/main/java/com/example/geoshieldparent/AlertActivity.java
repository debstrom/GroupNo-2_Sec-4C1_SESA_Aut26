package com.example.geoshieldparent;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlertActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_FROM_TAP = "from_tap";

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");
    private static final Pattern COORD_PATTERN =
            Pattern.compile("(-?\\d{1,3}\\.\\d+)\\s*,\\s*(-?\\d{1,3}\\.\\d+)");

    private TextView tvChildName, tvSubtitle, tvLocation, btnSilence;
    private String mapUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alert);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        tvChildName = findViewById(R.id.tvChildName);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvLocation = findViewById(R.id.tvLocation);
        btnSilence = findViewById(R.id.btnSilence);

        findViewById(R.id.btnGetLocation).setOnClickListener(v -> openMaps());
        btnSilence.setOnClickListener(v -> silence());
        findViewById(R.id.btnDismiss).setOnClickListener(v -> {
            AlarmService.stop(this);
            finish();
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void showOverLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        String title = intent.getStringExtra(EXTRA_TITLE);
        String body = intent.getStringExtra(EXTRA_MESSAGE);

        if (isEmpty(title)) title = "GeoShield";

        mapUrl = extractMapUrl(body);

        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        tvChildName.setText(title);
        tvSubtitle.setText(time);

        if (!isEmpty(mapUrl)) {
            tvLocation.setText(mapUrl);
        } else if (!isEmpty(body)) {
            tvLocation.setText(body);
        } else {
            tvLocation.setText("Location pawa jayni");
        }

        // Notification e tap kore ashle alarm bondho hobe
        if (intent.getBooleanExtra(EXTRA_FROM_TAP, false)) {
            AlarmService.stop(this);
            btnSilence.setVisibility(View.GONE);
        } else {
            btnSilence.setVisibility(View.VISIBLE);
        }
    }

    // Body theke maps URL ber kore. URL na thakle coordinates theke URL banay.
    private String extractMapUrl(String body) {
        if (isEmpty(body)) return null;

        Matcher u = URL_PATTERN.matcher(body);
        if (u.find()) return u.group();

        Matcher c = COORD_PATTERN.matcher(body);
        if (c.find()) {
            return "https://www.google.com/maps?q=" + c.group(1) + "," + c.group(2);
        }
        return null;
    }

    private void silence() {
        AlarmService.stop(this);
        btnSilence.setVisibility(View.GONE);
    }

    private void openMaps() {
        AlarmService.stop(this);
        btnSilence.setVisibility(View.GONE);

        if (isEmpty(mapUrl)) {
            Toast.makeText(this, "Location pawa jayni", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(mapUrl);
        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapsIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapsIntent);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, "Maps open kora jayni", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}