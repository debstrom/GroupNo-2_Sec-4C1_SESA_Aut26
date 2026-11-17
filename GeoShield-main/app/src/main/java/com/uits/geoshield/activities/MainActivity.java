package com.uits.geoshield.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.uits.geoshield.R;
import com.uits.geoshield.fragments.ContactsFragment;
import com.uits.geoshield.fragments.HomeFragment;
import com.uits.geoshield.fragments.MapFragment;
import com.uits.geoshield.fragments.ProfileFragment;
import com.uits.geoshield.utils.SimUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {


    private final ActivityResultLauncher<String> phoneStatePerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) askSimIfNeeded();
            });

    private void askSimIfNeeded() {
        if (SimUtils.getActiveSims(this).size() > 1 && SimUtils.getSelected(this) == null) {
            SimUtils.showPicker(this, null);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            show(createFragment(item.getItemId()));
            return true;
        });
        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_home);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            askSimIfNeeded();
        } else {
            phoneStatePerm.launch(Manifest.permission.READ_PHONE_STATE);
        }
    }

    private Fragment createFragment(int id) {
        if (id == R.id.nav_map) return new MapFragment();
        if (id == R.id.nav_contacts) return new ContactsFragment();
        if (id == R.id.nav_profile) return new ProfileFragment();
        return new HomeFragment();
    }

    private void show(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


}
