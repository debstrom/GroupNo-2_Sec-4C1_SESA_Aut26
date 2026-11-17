package com.uits.geoshield.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.uits.geoshield.R;
import com.uits.geoshield.models.Contact;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.VH> {

    public interface Listener {
        void onClick(Contact contact);
        void onLongClick(Contact contact);
    }

    private final List<Contact> items;
    private final Listener listener;

    public ContactAdapter(List<Contact> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Contact c = items.get(position);
        h.initial.setText(c.getInitial());
        h.name.setText(c.getName());
        h.meta.setText(c.getRelation() + " · " + c.getChannels());
        if (c.isVerified()) {
            h.status.setText(R.string.verified);
            h.status.setBackgroundResource(R.drawable.bg_chip_teal);
            h.status.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.teal));
        } else {
            h.status.setText(R.string.pending);
            h.status.setBackgroundResource(R.drawable.bg_pending);
            h.status.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.pending_text));
        }
        h.itemView.setOnClickListener(v -> listener.onClick(c));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(c);
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView initial;
        final TextView name;
        final TextView meta;
        final TextView status;

        VH(@NonNull View itemView) {
            super(itemView);
            initial = itemView.findViewById(R.id.tvInitial);
            name = itemView.findViewById(R.id.tvName);
            meta = itemView.findViewById(R.id.tvMeta);
            status = itemView.findViewById(R.id.tvStatus);
        }
    }
}
