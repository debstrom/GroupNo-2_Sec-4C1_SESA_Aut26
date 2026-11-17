package com.uits.geoshield.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.uits.geoshield.R;
import com.uits.geoshield.adapters.HelpAdapter;
import com.uits.geoshield.models.HelpPlace;
import com.uits.geoshield.utils.ContactRepository;
import com.uits.geoshield.utils.SimUtils;
import com.uits.geoshield.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.uits.geoshield.models.Contact;
import com.uits.geoshield.utils.SosHelper;
import java.util.ArrayDeque;
import java.util.Map;

import android.location.LocationListener;
import android.location.LocationManager;
public class SosActiveActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView tvTimer;
    private View rowLocation;
    private View rowContacts;
    private int seconds = 0;
    private int contactCount;


    private static final long NEXT_CALL_DELAY_MS = 5000;

    private boolean sosStarted = false;
    private String sosMessage = "";
    private final ArrayDeque<Contact> callQueue = new ArrayDeque<>();
    private boolean callRunning = false;
    private boolean callLaunched = false;

    private LocationManager locationManager;
    private LocationListener locListener;
    private boolean locationDelivered = false;
    private static final long LOCATION_TIMEOUT_MS = 10000;
    private SimUtils.Sim sosSim;

    public static String token = "";

;

    private final ActivityResultLauncher<String[]> permLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> startSos());

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            seconds++;
            tvTimer.setText(String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60));
            handler.postDelayed(this, 1000);
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_active);
        UiUtils.setDarkSystemBars(this, R.color.sos_dark);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        contactCount = ContactRepository.get().size();
        tvTimer = findViewById(R.id.tvTimer);
        rowLocation = findViewById(R.id.rowLocation);
        rowContacts = findViewById(R.id.rowContacts);
        View rowRecording = findViewById(R.id.rowRecording);

        bindRow(rowLocation, false, getString(R.string.status_location_title),
                getString(R.string.status_location_wait));
        bindRow(rowContacts, false,
                getResources().getQuantityString(R.plurals.status_contacts_title, contactCount, contactCount),
                getString(R.string.status_contacts_wait));
        bindRecordingRow(rowRecording);


        permLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE
        });

        Log.d("ridoytokenn",token);
        // Simulated progress; replace with real location / messaging callbacks.
        handler.postDelayed(() -> bindRow(rowLocation, true, getString(R.string.status_location_title),
                getString(R.string.status_location_sub)), 1000);
        handler.postDelayed(() -> bindRow(rowContacts, true,
                getResources().getQuantityString(R.plurals.status_contacts_title, contactCount, contactCount),
                getString(R.string.status_contacts_sub)), 2000);

        List<HelpPlace> places = new ArrayList<>();
        places.add(new HelpPlace("Dhanmondi Thana", 1.2, "999"));
        places.add(new HelpPlace("Square Hospital ER", 2.1, "10616"));
        RecyclerView rv = findViewById(R.id.rvHelp);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new HelpAdapter(places, this::dial));

        findViewById(R.id.btnSafe).setOnClickListener(v -> confirmSafe());
        TextView falseAlarm = findViewById(R.id.btnFalseAlarm);
        falseAlarm.setPaintFlags(falseAlarm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        falseAlarm.setOnClickListener(v -> confirmFalseAlarm());




        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmSafe();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, 1000);
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(tick);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void sendFirebaseData(String title, String body, String token) {
        try {

            // URL-er sathe query parameters append kora
            String url = "https://clientproject.fun/geoshield/firebase.php?title=" + title
                    + "&body=" + body
                    + "&token=" + token;

            // Request.Method.POST er bodle ekhane Request.Method.GET use kora hoyeche
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);
                            Log.d("ridoytokenOrg", "sfddfsd   :"+token);
                            Toast.makeText(getApplicationContext(), "Successfully sent via GET!"+response, Toast.LENGTH_SHORT).show();
                            Log.d("ridoytokennnn",response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VolleyError", "Error: ", error);
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

            // Request queue-te add kora
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Encoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void bindRow(View row, boolean done, String title, String sub) {
        ImageView icon = row.findViewById(R.id.statusIcon);
        ((TextView) row.findViewById(R.id.statusTitle)).setText(title);
        ((TextView) row.findViewById(R.id.statusSub)).setText(sub);
        if (done) {
            icon.setBackgroundResource(R.drawable.bg_status_done);
            icon.setImageResource(R.drawable.ic_check);
            icon.setImageTintList(android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(this, R.color.mint_ink)));
        } else {
            icon.setBackgroundResource(R.drawable.bg_status_pending);
            icon.setImageDrawable(null);
        }
    }

    private void bindRecordingRow(View row) {
        ImageView icon = row.findViewById(R.id.statusIcon);
        icon.setBackgroundResource(R.drawable.bg_status_rec);
        icon.setImageResource(R.drawable.ic_record);
        ((TextView) row.findViewById(R.id.statusTitle)).setText(R.string.status_recording_title);
        ((TextView) row.findViewById(R.id.statusSub)).setText(R.string.status_recording_sub);
        ObjectAnimator pulse = ObjectAnimator.ofFloat(icon, View.ALPHA, 1f, 0.45f);
        pulse.setDuration(800);
        pulse.setRepeatCount(ObjectAnimator.INFINITE);
        pulse.setRepeatMode(ObjectAnimator.REVERSE);
        pulse.start();
        icon.setTag(pulse);
    }

    private void dial(HelpPlace place) {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + place.getPhone())));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_dialer, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmSafe() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_safe_title)
                .setMessage(R.string.confirm_safe_msg)
                .setPositiveButton(R.string.confirm_safe_yes, (d, w) -> finish())
                .setNegativeButton(R.string.confirm_keep, null)
                .show();
    }

    private void confirmFalseAlarm() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_false_title)
                .setMessage(R.string.confirm_false_msg)
                .setPositiveButton(R.string.confirm_false_yes, (d, w) -> {
                    Toast.makeText(this, R.string.false_alarm_sent, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.confirm_keep, null)
                .show();
    }


    private boolean has(String perm) {
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;
    }

    private void startSos() {
        if (sosStarted) return;
        sosStarted = true;

        if (!has(Manifest.permission.ACCESS_FINE_LOCATION)) {
            deliverLocation(null);
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Khub notun last-known thakle shathe shathe nao
        Location last = bestLastKnown();
        if (last != null && System.currentTimeMillis() - last.getTime() < 2 * 60 * 1000) {
            deliverLocation(last);
            return;
        }

        // Na hole notun fix er jonno opekkha koro
        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                deliverLocation(loc);
            }

            @Override public void onProviderEnabled(String provider) { }
            @Override public void onProviderDisabled(String provider) { }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
        };

        try {
            for (String p : new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}) {
                if (locationManager.isProviderEnabled(p)) {
                    locationManager.requestLocationUpdates(p, 0, 0, locListener, Looper.getMainLooper());
                }
            }
        } catch (SecurityException ignored) { }

        // 10 sec er moddhe na pele last-known (purono hole-o) diye cholbe
        handler.postDelayed(() -> deliverLocation(bestLastKnown()), LOCATION_TIMEOUT_MS);
    }

    private Location bestLastKnown() {
        if (locationManager == null || !has(Manifest.permission.ACCESS_FINE_LOCATION)) return null;
        Location best = null;
        for (String p : new String[]{LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER}) {
            try {
                Location l = locationManager.getLastKnownLocation(p);
                if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
            } catch (SecurityException | IllegalArgumentException ignored) { }
        }
        return best;
    }

    private void deliverLocation(Location loc) {
        if (locationDelivered) return; // ekbar-i
        locationDelivered = true;
        stopLocationUpdates();
        onLocationReady(loc);
    }

    private void stopLocationUpdates() {
        if (locationManager != null && locListener != null) {
            locationManager.removeUpdates(locListener);
        }
    }
    private void onLocationReady(Location loc) {
        String googleMapsUrl ="" ;
        if (loc != null) {
            double latitude = loc.getLatitude();
            double longitude = loc.getLongitude();

            // 1. Google Maps link banano
             googleMapsUrl = "https://www.google.com/maps?q=" + latitude + "," + longitude;

            // 2. sosMessage-er sathe map link ta set kora
            sosMessage = "SOS! আমি বিপদে আছি. Location: " + googleMapsUrl;

            bindRow(rowLocation, true, getString(R.string.status_location_title),
                    getString(R.string.status_location_sub));
        } else {
            sosMessage = "SOS! আমি বিপদে আছি!, please help. (Location pawa jayni)";
        }

        // 3. Ekhane Firebase-e Notification/Data pathanor function-ti call kore dite paro
        sendFirebaseData("হেল্প চাই", googleMapsUrl, token);



        sosSim = SimUtils.resolve(this);
        List<Contact> contacts = ContactRepository.get().getContacts();

        // 1) Shobar kache ekshathe SMS
        if (has(Manifest.permission.SEND_SMS)) {
            SosHelper.sendSmsToAll(this, contacts, sosMessage);
        }
        bindRow(rowContacts, true,
                getResources().getQuantityString(R.plurals.status_contacts_title, contactCount, contactCount),
                getString(R.string.status_contacts_sub));

        // 2) Ek ek kore call
      /*  if (has(Manifest.permission.CALL_PHONE)) {
            callQueue.clear();
            callQueue.addAll(contacts);
            callRunning = !callQueue.isEmpty();
            callLaunched = false;
            callNext();
        }*/
    }
    private void callNext() {
        Contact c = callQueue.poll();
        if (c == null) {
            callRunning = false;
            return;
        }
        callLaunched = true;
        try {
            SimUtils.placeCall(this, c.getPhone(), sosSim);
        } catch (Exception e) {
            callLaunched = false;
            callNext();
        }
    }

    private final Runnable nextCall = this::callNext;

    private void stopCalling() {
        callRunning = false;
        callLaunched = false;
        callQueue.clear();
        handler.removeCallbacks(nextCall);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Call shesh hoye app e ferot ashle, kichukkhon por porer jon ke call
        if (callRunning && callLaunched) {
            callLaunched = false;
            if (callQueue.isEmpty()) {
                callRunning = false;
            } else {
                Toast.makeText(this, "Porer jon ke call hocche... thamate Safe e tap koro",
                        Toast.LENGTH_SHORT).show();
                handler.postDelayed(nextCall, NEXT_CALL_DELAY_MS);
            }
        }
    }

}
