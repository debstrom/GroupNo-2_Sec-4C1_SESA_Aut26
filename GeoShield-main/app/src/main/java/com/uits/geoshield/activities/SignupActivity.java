package com.uits.geoshield.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.uits.geoshield.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private static final String SIGNUP_URL = "/signup";

    private EditText etUserId, etUsername, etEmail, etPass, etContact;
    private Button btnSignup;
    private ProgressBar progress;
    private TextView tvLogin;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("" + R.string.app_name, MODE_PRIVATE);
        editor = sharedPreferences.edit();



        if (sharedPreferences.getString("username","").length()>0){
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        }


        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        etContact = findViewById(R.id.etContact);
        btnSignup = findViewById(R.id.btnSignup);
        progress = findViewById(R.id.progress);
        tvLogin = findViewById(R.id.tvLogin);

        btnSignup.setOnClickListener(v -> {
            if (validate()) signup(100000 + new java.security.SecureRandom().nextInt(900000));
        });

        // Login page thakle ekhane open koro
        tvLogin.setOnClickListener(v -> finish());

    }

    private boolean validate() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString();
        String contact = etContact.getText().toString().trim();


        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Name dao");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email dao");
            return false;
        }
        if (pass.length() < 6) {
            etPass.setError("Minimum 6 character");
            return false;
        }
        if (!contact.matches("^01[3-9]\\d{8}$")) {
            etContact.setError("Valid BD number dao (01XXXXXXXXX)");
            return false;
        }
        return true;
    }

    private void signup(int user_id) {
        setLoading(true);

        JSONObject body = new JSONObject();
        try {
            body.put("user_id", user_id);
            body.put("username", etUsername.getText().toString().trim());
            body.put("email", etEmail.getText().toString().trim());
            body.put("user_pass", etPass.getText().toString());
            body.put("user_contact", etContact.getText().toString().trim());
        } catch (JSONException e) {
            setLoading(false);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getString(R.string.MAIN_URL) + SIGNUP_URL, body,
                response -> {
                    // 201 mane signup hoyeche
                    setLoading(false);
                    Toast.makeText(this, response.optString("message", "Signup completed"),
                            Toast.LENGTH_LONG).show();
                    editor.putString("username", etUsername.getText().toString().trim());
                    editor.putString("email", etEmail.getText().toString().trim());
                    editor.putInt("user_id",user_id);
                    editor.commit();
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    finish(); // login e fire jabe
                },
                error -> {
                    setLoading(false);
                    String msg = "Network error, abar try koro";
                    if (error.networkResponse != null) {
                        // server er 500 error: user_id/email already thakle ei ta ashbe
                        msg = "Signup hoy nai (user ID ba email already ache?)";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignup.setEnabled(!loading);
    }

}