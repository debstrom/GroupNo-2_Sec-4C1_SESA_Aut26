package com.example.geoshieldparent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class ParentLoginActivity extends AppCompatActivity {
    // Emulator: 10.0.2.2 | Real phone: PC er LAN IP
    public static final String PREFS = "parent_prefs";

    private EditText etChildId, etChildEmail;
    private Button btnLogin;
    private ProgressBar progress;

    String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Age login kora thakle sorasori home e
        SharedPreferences sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (sp.getBoolean("logged_in", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "NOT GET", Toast.LENGTH_SHORT).show();
            }
            // Get new FCM token
            token = task.getResult();
             //  Toast.makeText(this, "token"+token, Toast.LENGTH_SHORT).show();
            Log.d("ridoy", token);

            // Log or use the token

        });

        setContentView(R.layout.activity_parent_login);

        etChildId = findViewById(R.id.etChildId);
        etChildEmail = findViewById(R.id.etChildEmail);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);

        btnLogin.setOnClickListener(v -> {
            if (validate()) fetchTokenThenLogin();
        });

    }

    private boolean validate() {
        if (TextUtils.isEmpty(etChildId.getText().toString().trim())) {
            etChildId.setError("Child er User ID dao");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(etChildEmail.getText().toString().trim()).matches()) {
            etChildEmail.setError("Valid email dao");
            return false;
        }

        return true;
    }

    // FCM token niye tarpor login (SOS notification pathanor jonno server e token lagbe)
    private void fetchTokenThenLogin() {
        setLoading(true);
        login(token);
    }

    private void login(String fcmToken) {
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", etChildId.getText().toString().trim());
            body.put("email", etChildEmail.getText().toString().trim());
            body.put("fcm_token", fcmToken);
            if (fcmToken != null) body.put("fcm_token", fcmToken);
        } catch (JSONException e) {
            setLoading(false);
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, getString(R.string.MAIN_URL)+"/parent_login", body,
                r -> {
                    setLoading(false);
                    JSONObject child = r.optJSONObject("child");
                    if (child == null) {
                        Toast.makeText(this, "Server response thik na", Toast.LENGTH_LONG).show();
                        return;
                    }
                    getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                            .putBoolean("logged_in", true)
                            .putString("child_id", child.optString("user_id"))
                            .putString("child_name", child.optString("username"))
                            .putString("child_contact", child.optString("user_contact"))
                            .apply();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                },
                e -> {
                    setLoading(false);
                    if (e.networkResponse != null && e.networkResponse.statusCode == 401) {
                        Toast.makeText(this, "ID, email ba code mile nai", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Network error, abar try koro", Toast.LENGTH_LONG).show();
                    }
                });

        Volley.newRequestQueue(this).add(req);
    }

    private void setLoading(boolean on) {
        progress.setVisibility(on ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!on);
    }

}