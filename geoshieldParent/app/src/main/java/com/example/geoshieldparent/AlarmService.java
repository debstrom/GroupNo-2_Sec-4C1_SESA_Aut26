package com.example.geoshieldparent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class AlarmService extends Service {

    private static final String CHANNEL_ID = "sos_alarm_channel";
    private static final int NOTIF_ID = 1001;

    private MediaPlayer player;
    private Vibrator vibrator;
    private AudioManager audioManager;
    private int oldAlarmVolume = -1;

    public static void start(Context context, Bundle extras) {
        Intent i = new Intent(context, AlarmService.class);
        if (extras != null) i.putExtras(extras);
        ContextCompat.startForegroundService(context, i);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, AlarmService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createChannel();
        Notification n = buildNotification(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIF_ID, n);
        }

        startAlarm();
        return START_NOT_STICKY;
    }

    private Notification buildNotification(Intent src) {
        Bundle extras = src != null && src.getExtras() != null ? src.getExtras() : new Bundle();
        String title = extras.getString(AlertActivity.EXTRA_TITLE, "GeoShield");
        String msg = extras.getString(AlertActivity.EXTRA_MESSAGE, "SOS alert");

        // Lock screen e full-screen intent: alarm cholte thakbe
        Intent fullIntent = new Intent(this, AlertActivity.class);
        fullIntent.putExtras(extras);
        fullIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent fullPi = PendingIntent.getActivity(this, 1, fullIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Notification e tap korle: alarm bondho hobe
        Intent tapIntent = new Intent(this, AlertActivity.class);
        tapIntent.putExtras(extras);
        tapIntent.putExtra(AlertActivity.EXTRA_FROM_TAP, true);
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tapPi = PendingIntent.getActivity(this, 2, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(title)
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(0xFFA32D2D)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(tapPi)
                .setFullScreenIntent(fullPi, true)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return;

        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "SOS alerts", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Child SOS alarm");
        ch.setSound(null, null); // sound amra nijei bajai
        ch.enableVibration(false);
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        ch.setBypassDnd(true);
        nm.createNotificationChannel(ch);
    }

    private void startAlarm() {
        stopAlarm();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            oldAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
        }

        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            player = new MediaPlayer();
            player.setDataSource(this, uri);
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 800, 600};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }
    }

    private void stopAlarm() {
        if (player != null) {
            try {
                if (player.isPlaying()) player.stop();
            } catch (IllegalStateException ignored) {
            }
            player.release();
            player = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        if (audioManager != null && oldAlarmVolume >= 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldAlarmVolume, 0);
            oldAlarmVolume = -1;
        }
    }

    @Override
    public void onDestroy() {
        stopAlarm();
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
