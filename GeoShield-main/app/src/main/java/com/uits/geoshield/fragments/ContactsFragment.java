package com.uits.geoshield.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.uits.geoshield.R;
import com.uits.geoshield.activities.MainActivity;
import com.uits.geoshield.activities.SignupActivity;
import com.uits.geoshield.adapters.ContactAdapter;
import com.uits.geoshield.models.Contact;
import com.uits.geoshield.utils.ContactRepository;
import com.uits.geoshield.utils.ShareUtils;
import com.uits.geoshield.utils.UiUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ContactsFragment extends Fragment {

    private final ContactRepository repo = ContactRepository.get();
    private ContactAdapter adapter;
    private TextView tvCount;


    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences("" + R.string.app_name, getActivity().MODE_PRIVATE);
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCount = view.findViewById(R.id.tvCount);

        adapter = new ContactAdapter(repo.getContacts(), new ContactAdapter.Listener() {
            @Override
            public void onClick(Contact contact) {
                if (!contact.isVerified()) {
                    Toast.makeText(requireContext(), R.string.pending_hint, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLongClick(Contact contact) {
                confirmRemove(contact);
            }
        });
        RecyclerView rv = view.findViewById(R.id.rvContacts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        view.findViewById(R.id.btnAddContact).setOnClickListener(v -> {
            if (repo.isFull()) {
                Toast.makeText(requireContext(),
                        getString(R.string.max_contacts, ContactRepository.MAX_CONTACTS), Toast.LENGTH_SHORT).show();
            } else {
                showAddDialog();
            }
        });
        view.findViewById(R.id.btnSendLink).setOnClickListener(v -> ShareUtils.shareLiveLink(requireContext()));
        updateCount();
    }

    private void updateCount() {
        tvCount.setText(getString(R.string.contacts_count, repo.size(), ContactRepository.MAX_CONTACTS));
    }

    private void confirmRemove(Contact contact) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_contact_title)
                .setMessage(getString(R.string.delete_contact_msg, contact.getName()))
                .setPositiveButton(R.string.remove, (d, w) -> {
                    int index = repo.remove(contact);
                    if (index >= 0) adapter.notifyItemRemoved(index);
                    updateCount();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showAddDialog() {
        View content = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        TextInputLayout tilName = content.findViewById(R.id.tilName);
        TextInputLayout tilRelation = content.findViewById(R.id.tilRelation);
        TextInputLayout tilPhone = content.findViewById(R.id.tilPhone);
        CheckBox cbSms = content.findViewById(R.id.cbSms);
        CheckBox cbWhatsapp = content.findViewById(R.id.cbWhatsapp);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_contact_title)
                .setView(content)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
            String name = UiUtils.text(tilName);
            String relation = UiUtils.text(tilRelation);
            String phone = UiUtils.text(tilPhone);

            tilName.setError(name.isEmpty() ? getString(R.string.err_name) : null);
            tilRelation.setError(relation.isEmpty() ? getString(R.string.err_relation) : null);
            tilPhone.setError(UiUtils.isValidBdPhone(phone) ? null : getString(R.string.err_phone));
            boolean channelOk = cbSms.isChecked() || cbWhatsapp.isChecked();
            if (!channelOk) {
                Toast.makeText(requireContext(), R.string.err_channel, Toast.LENGTH_SHORT).show();
            }
            if (name.isEmpty() || relation.isEmpty() || !UiUtils.isValidBdPhone(phone) || !channelOk)
                return;

            String channels = cbSms.isChecked() && cbWhatsapp.isChecked() ? "SMS + WhatsApp"
                    : cbSms.isChecked() ? "SMS" : "WhatsApp";
            signup(name, relation, channels, phone);

            dialog.dismiss();
        }));
        dialog.show();
    }

    private void signup(String name, String relation, String channels, String phone) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading..");
        progressDialog.show();

        JSONObject body = new JSONObject();
        try {
            body.put("user_id", sharedPreferences.getInt("user_id", 0));
            body.put("contact_name", name);
            body.put("contact_number", phone);
        } catch (JSONException e) {
            progressDialog.dismiss();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getString(R.string.MAIN_URL) + "/emergency_contact", body,
                response -> {
                    // 201 mane signup hoyeche
                   progressDialog.dismiss();
                    Toast.makeText(getActivity(), response.optString("message", "Contact added"),
                            Toast.LENGTH_LONG).show();

                    repo.add(new Contact(name, relation, channels, phone, true));
                    adapter.notifyItemInserted(repo.size() - 1);
                    updateCount();
                },
                error -> {
                     progressDialog.dismiss();
                    String msg = "Network error, abar try koro";
                    if (error.networkResponse != null) {
                        // server er 500 error: user_id/email already thakle ei ta ashbe
                        msg = "Signup hoy nai (user ID ba email already ache?)";
                    }
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                });

        Volley.newRequestQueue(getActivity()).add(request);
    }

}
