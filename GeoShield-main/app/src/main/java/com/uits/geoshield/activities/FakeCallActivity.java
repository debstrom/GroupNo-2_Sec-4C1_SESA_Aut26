package com.uits.geoshield.activities;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.uits.geoshield.R;
import com.uits.geoshield.utils.Prefs;
import com.uits.geoshield.utils.UiUtils;

public class FakeCallActivity extends AppCompatActivity {

    private Ringtone ringtone;
    private View rowRinging;
    private View rowInCall;
    private TextView tvIncoming;
    private TextView tvStatus;
    private TextView tvFooter;
    private Chronometer chrono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);
        UiUtils.setDarkSystemBars(this, R.color.call_bottom);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        Prefs prefs = new Prefs(this);
        String caller = prefs.getCallerName();
        ((TextView) findViewById(R.id.tvCaller)).setText(caller);
        ((TextView) findViewById(R.id.tvAvatar)).setText(
                caller.isEmpty() ? "?" : caller.substring(0, 1).toUpperCase());

        rowRinging = findViewById(R.id.rowRinging);
        rowInCall = findViewById(R.id.rowInCall);
        tvIncoming = findViewById(R.id.tvIncoming);
        tvStatus = findViewById(R.id.tvStatus);
        tvFooter = findViewById(R.id.tvFooter);
        chrono = findViewById(R.id.chrono);

        findViewById(R.id.btnDecline).setOnClickListener(v -> endCall());
        findViewById(R.id.btnEnd).setOnClickListener(v -> endCall());
        findViewById(R.id.btnAccept).setOnClickListener(v -> answer());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                endCall();
            }
        });

        if (prefs.isRingtoneEnabled()) startRinging();
    }

    private void startRinging() {
        ringtone = RingtoneManager.getRingtone(this,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        if (ringtone != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone.setLooping(true);
            ringtone.play();
        }
    }

    private void stopRinging() {
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
    }

    private void answer() {
        stopRinging();
        rowRinging.setVisibility(View.GONE);
        tvFooter.setVisibility(View.GONE);
        tvIncoming.setVisibility(View.INVISIBLE);
        tvStatus.setVisibility(View.GONE);
        rowInCall.setVisibility(View.VISIBLE);
        chrono.setVisibility(View.VISIBLE);
        chrono.setBase(SystemClock.elapsedRealtime());
        chrono.start();
    }

    private void endCall() {
        stopRinging();
        chrono.stop();
        finish();
    }

    @Override
    protected void onDestroy() {
        stopRinging();
        super.onDestroy();
    }
}
