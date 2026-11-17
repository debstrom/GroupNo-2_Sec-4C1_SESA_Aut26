package com.uits.geoshield.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.uits.geoshield.R;
import com.uits.geoshield.models.Zone;
import com.uits.geoshield.utils.UiUtils;
import com.uits.geoshield.utils.ZoneRepository;
import com.uits.geoshield.views.HeatMapView;

import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment {

    private List<Zone> zones;
    private HeatMapView heatMap;
    private TextView tvTitle;
    private TextView tvDesc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        heatMap = view.findViewById(R.id.heatMap);
        tvTitle = view.findViewById(R.id.tvZoneTitle);
        tvDesc = view.findViewById(R.id.tvZoneDesc);
        EditText etSearch = view.findViewById(R.id.etSearch);

        zones = ZoneRepository.getZones();
        heatMap.setZones(zones);
        heatMap.setOnZoneSelectedListener(this::showZone);
        heatMap.selectZone(zones.get(2)); // Mohammadpur · Risky, as in the design

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(etSearch.getText().toString().trim());
                UiUtils.hideKeyboard(v);
                return true;
            }
            return false;
        });

        view.findViewById(R.id.btnReport).setOnClickListener(v -> showReportDialog());
    }

    private void search(String query) {
        if (query.isEmpty()) return;
        String q = query.toLowerCase(Locale.ROOT);
        for (Zone z : zones) {
            if (z.getName().toLowerCase(Locale.ROOT).contains(q)) {
                heatMap.selectZone(z);
                return;
            }
        }
        Toast.makeText(requireContext(), R.string.area_not_found, Toast.LENGTH_SHORT).show();
    }

    private void showZone(Zone zone) {
        tvTitle.setText(getString(R.string.zone_title_format, zone.getName(),
                getString(zone.getLevel().labelRes)));
        tvDesc.setText(zone.getDescription());
    }

    private void showReportDialog() {
        View content = getLayoutInflater().inflate(R.layout.dialog_report_incident, null);
        ChipGroup group = content.findViewById(R.id.chipGroup);
        TextInputLayout tilDetails = content.findViewById(R.id.tilDetails);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.report_title)
                .setView(content)
                .setPositiveButton(R.string.report_submit, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
            int checked = group.getCheckedChipId();
            if (checked == View.NO_ID) {
                Toast.makeText(requireContext(), R.string.report_pick_category, Toast.LENGTH_SHORT).show();
                return;
            }
            if (UiUtils.text(tilDetails).length() < 5) {
                tilDetails.setError(getString(R.string.report_details_short));
                return;
            }
            tilDetails.setError(null);
            dialog.dismiss();
            Toast.makeText(requireContext(), R.string.report_thanks, Toast.LENGTH_LONG).show();
        }));
        dialog.show();
    }
}
