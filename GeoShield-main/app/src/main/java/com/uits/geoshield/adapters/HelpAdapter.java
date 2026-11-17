package com.uits.geoshield.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uits.geoshield.R;
import com.uits.geoshield.models.HelpPlace;

import java.util.List;

public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.VH> {

    public interface OnCallListener {
        void onCall(HelpPlace place);
    }

    private final List<HelpPlace> items;
    private final OnCallListener listener;

    public HelpAdapter(List<HelpPlace> items, OnCallListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_help_place, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HelpPlace p = items.get(position);
        h.name.setText(p.getName());
        h.meta.setText(h.itemView.getContext().getString(R.string.help_meta, p.getDistanceKm()));
        View.OnClickListener click = v -> listener.onCall(p);
        h.itemView.setOnClickListener(click);
        h.call.setOnClickListener(click);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;
        final View call;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvPlaceName);
            meta = itemView.findViewById(R.id.tvPlaceMeta);
            call = itemView.findViewById(R.id.btnCall);
        }
    }
}
