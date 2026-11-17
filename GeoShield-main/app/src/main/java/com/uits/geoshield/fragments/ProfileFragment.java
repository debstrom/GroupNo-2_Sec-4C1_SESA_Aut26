package com.uits.geoshield.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.uits.geoshield.R;
import com.uits.geoshield.utils.Prefs;
import com.uits.geoshield.utils.SimUtils;
import com.uits.geoshield.utils.UiUtils;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Prefs prefs = new Prefs(requireContext());

        TextView tvAvatar = view.findViewById(R.id.tvAvatar);
        TextInputLayout tilName = view.findViewById(R.id.tilName);
        TextInputLayout tilPhone = view.findViewById(R.id.tilPhone);
        TextInputLayout tilCaller = view.findViewById(R.id.tilCaller);
        TextView tvDelay = view.findViewById(R.id.tvDelay);
        Slider slider = view.findViewById(R.id.sliderDelay);
        MaterialSwitch ring = view.findViewById(R.id.switchRingtone);

        ((TextInputEditText) view.findViewById(R.id.etName)).setText(prefs.getUserName());
        ((TextInputEditText) view.findViewById(R.id.etPhone)).setText(prefs.getPhone());
        ((TextInputEditText) view.findViewById(R.id.etCaller)).setText(prefs.getCallerName());
        slider.setValue(Math.max(5, Math.min(30, prefs.getCallDelaySeconds())));
        ring.setChecked(prefs.isRingtoneEnabled());
        tvDelay.setText(getString(R.string.ring_delay, (int) slider.getValue()));
        setAvatar(tvAvatar, prefs.getUserName());

        slider.addOnChangeListener((s, value, fromUser) ->
                tvDelay.setText(getString(R.string.ring_delay, (int) value)));

        TextView tvSim = view.findViewById(R.id.tvSim);


        view.findViewById(R.id.simSel).setOnClickListener(view1 -> {
            SimUtils.showPicker(getActivity(), sim ->tvSim.setText(sim.label));
        });

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = UiUtils.text(tilName);
            String phone = UiUtils.text(tilPhone);
            String caller = UiUtils.text(tilCaller);

            tilName.setError(name.isEmpty() ? getString(R.string.err_name) : null);
            tilCaller.setError(caller.isEmpty() ? getString(R.string.err_caller) : null);
            boolean phoneOk = phone.isEmpty() || UiUtils.isValidBdPhone(phone);
            tilPhone.setError(phoneOk ? null : getString(R.string.err_own_phone));
            if (name.isEmpty() || caller.isEmpty() || !phoneOk) return;

            prefs.setUserName(name);
            prefs.setPhone(phone);
            prefs.setCallerName(caller);
            prefs.setCallDelaySeconds((int) slider.getValue());
            prefs.setRingtoneEnabled(ring.isChecked());
            setAvatar(tvAvatar, name);
            UiUtils.hideKeyboard(v);
            Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show();
        });
    }

    private void setAvatar(TextView tv, String name) {
        tv.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());
    }
}
