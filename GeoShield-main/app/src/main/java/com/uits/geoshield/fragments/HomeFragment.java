package com.uits.geoshield.fragments;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.uits.geoshield.R;
import com.uits.geoshield.activities.FakeCallActivity;
import com.uits.geoshield.activities.SosActiveActivity;
import com.uits.geoshield.utils.ContactRepository;
import com.uits.geoshield.utils.Prefs;
import com.uits.geoshield.utils.ShareUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private Prefs prefs;
    private TextView tvGreeting;
    private TextView tvLang;
    private TextView tvContactsChip;
    String fcm_token;

    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new Prefs(requireContext());
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvLang = view.findViewById(R.id.btnLang);
        tvContactsChip = view.findViewById(R.id.tvContactsChip);
        sharedPreferences = getActivity().getSharedPreferences(""+R.string.app_name,getActivity().MODE_PRIVATE);

        tvLang.setOnClickListener(v -> {
            boolean bn = Prefs.LANG_BN.equals(prefs.getLanguage());
            prefs.setLanguage(bn ? Prefs.LANG_EN : Prefs.LANG_BN);
            refresh();
        });

        View sos = view.findViewById(R.id.sosButton);
        sos.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            SosActiveActivity.token = fcm_token;
            startActivity(new Intent(requireContext(), SosActiveActivity.class));

        });

        view.findViewById(R.id.cardFakeCall).setOnClickListener(v -> scheduleFakeCall());
        view.findViewById(R.id.cardShare).setOnClickListener(v -> ShareUtils.shareLiveLink(requireContext()));
        view.findViewById(R.id.cardThana).setOnClickListener(v -> openMapsSearch("police station"));
        view.findViewById(R.id.cardHospital).setOnClickListener(v -> openMapsSearch("hospital"));


        fetchEmergencyContacts(sharedPreferences.getInt("user_id",0));
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        boolean bn = Prefs.LANG_BN.equals(prefs.getLanguage());
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greetRes;
        if (hour < 12) greetRes = bn ? R.string.greet_morning_bn : R.string.greet_morning_en;
        else if (hour < 17) greetRes = bn ? R.string.greet_afternoon_bn : R.string.greet_afternoon_en;
        else greetRes = bn ? R.string.greet_evening_bn : R.string.greet_evening_en;
        tvGreeting.setText(getString(greetRes) + ", " + prefs.getUserName());

        // Highlight the active language in the toggle, dim the other.
        String label = getString(R.string.lang_toggle);
        SpannableStringBuilder sb = new SpannableStringBuilder(label);
        int dim = ContextCompat.getColor(requireContext(), R.color.ink_muted);
        int bar = label.indexOf('|');
        if (bn) {
            sb.setSpan(new ForegroundColorSpan(dim), bar + 1, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            sb.setSpan(new ForegroundColorSpan(dim), 0, bar, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvLang.setText(sb);

        int n = ContactRepository.get().size();
        tvContactsChip.setText(getResources().getQuantityString(R.plurals.contacts_set, n, n));
    }

    private void scheduleFakeCall() {
        int delay = prefs.getCallDelaySeconds();
        Toast.makeText(requireContext(), getString(R.string.fake_call_scheduled, delay), Toast.LENGTH_SHORT).show();
        final Context app = requireContext().getApplicationContext();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent i = new Intent(app, FakeCallActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(i);
        }, delay * 1000L);
    }

    private void openMapsSearch(String query) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query)));
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), R.string.no_maps_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchEmergencyContacts(int userId) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        // URL-er sathe query parameter add kora (jemon: ?user_id=706367)
        String url = getString(R.string.MAIN_URL) + "/emergency_contact?user_id=" + userId;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {

                        progressDialog.dismiss();
                        // Success message ba JSON object parse kora
                        String message = response.getString("message");
                        JSONArray contactsArray = response.getJSONArray("contacts");

                        // Loop chaliye contacts gula pete paro
                        for (int i = 0; i < contactsArray.length(); i++) {
                            JSONObject contact = contactsArray.getJSONObject(i);
                            String contactName = contact.getString("contact_name");
                            String contactPhone = contact.getString("contact_phone");
                            fcm_token = contact.getString("fcm_token");
                            Toast.makeText(getActivity(), "token"+fcm_token, Toast.LENGTH_SHORT).show();
                            Log.d("ridoytoken",fcm_token);
                            // Ekhaner data guli UI-te ba list-e show korte parbe
                        }

                        Toast.makeText(getActivity(), "Contacts fetched successfully", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Parsing error", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        Toast.makeText(getActivity(), "No emergency contacts found", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Network error, try again", Toast.LENGTH_LONG).show();
                    }
                });

        // Request queue-te add kora
        Volley.newRequestQueue(getActivity()).add(req);
    }
}
