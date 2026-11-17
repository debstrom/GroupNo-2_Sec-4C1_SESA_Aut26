package com.uits.geoshield.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public final class SimUtils {

    private static final String PREFS = "geoshield_prefs";
    private static final String KEY_SUB_ID = "sos_sim_sub_id";

    private SimUtils() {}

    public static class Sim {
        public int subId;
        public int slot;
        public String iccId;
        public String label;
    }

    public interface OnSimSelected { void onSelected(Sim sim); }

    private static boolean hasPhoneState(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Phone e ja ja active SIM ache. */
    public static List<Sim> getActiveSims(Context ctx) {
        List<Sim> out = new ArrayList<>();
        if (!hasPhoneState(ctx)) return out;
        try {
            SubscriptionManager sm = ctx.getSystemService(SubscriptionManager.class);
            List<SubscriptionInfo> list = sm.getActiveSubscriptionInfoList();
            if (list == null) return out;
            for (SubscriptionInfo info : list) {
                Sim s = new Sim();
                s.subId = info.getSubscriptionId();
                s.slot = info.getSimSlotIndex();
                s.iccId = info.getIccId();
                CharSequence carrier = info.getCarrierName();
                s.label = "SIM " + (s.slot + 1) + " - " + (carrier != null ? carrier : "Unknown");
                out.add(s);
            }
        } catch (SecurityException ignored) { }
        return out;
    }

    /** User jeta select korse (ekhono active thakle). Na thakle null. */
    public static Sim getSelected(Context ctx) {
        int saved = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_SUB_ID, -1);
        if (saved == -1) return null;
        for (Sim s : getActiveSims(ctx)) {
            if (s.subId == saved) return s;
        }
        return null;
    }

    /** Selected thakle seta, na hole prothom active SIM. */
    public static Sim resolve(Context ctx) {
        Sim s = getSelected(ctx);
        if (s != null) return s;
        List<Sim> all = getActiveSims(ctx);
        return all.isEmpty() ? null : all.get(0);
    }

    public static void save(Context ctx, Sim sim) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_SUB_ID, sim.subId).apply();
    }

    /** SIM select korar dialog. */
    public static void showPicker(Activity act, OnSimSelected cb) {
        List<Sim> sims = getActiveSims(act);
        if (sims.isEmpty()) {
            Toast.makeText(act, "Kono active SIM pawa jayni (phone permission dao)",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[sims.size()];
        Sim cur = getSelected(act);
        int checked = 0;
        for (int i = 0; i < sims.size(); i++) {
            labels[i] = sims.get(i).label;
            if (cur != null && cur.subId == sims.get(i).subId) checked = i;
        }
        final int[] choice = {checked};
        new MaterialAlertDialogBuilder(act)
                .setTitle("SOS er jonno SIM select koro")
                .setSingleChoiceItems(labels, checked, (d, which) -> choice[0] = which)
                .setPositiveButton("Save", (d, w) -> {
                    Sim s = sims.get(choice[0]);
                    save(act, s);
                    if (cb != null) cb.onSelected(s);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------- SMS ----------
    public static SmsManager getSmsManager(Context ctx) {
        Sim sim = resolve(ctx);
        if (sim != null) return SmsManager.getSmsManagerForSubscriptionId(sim.subId);
        return Build.VERSION.SDK_INT >= 31
                ? ctx.getSystemService(SmsManager.class)
                : SmsManager.getDefault();
    }

    // ---------- Call ----------
    private static PhoneAccountHandle findHandle(Context ctx, Sim sim) {
        try {
            TelecomManager tm = (TelecomManager) ctx.getSystemService(Context.TELECOM_SERVICE);
            List<PhoneAccountHandle> handles = tm.getCallCapablePhoneAccounts();
            if (handles == null || handles.isEmpty()) return null;

            for (PhoneAccountHandle h : handles) {
                String id = h.getId();
                if (id == null) continue;
                if (id.equals(String.valueOf(sim.subId))) return h;
                if (sim.iccId != null && (id.contains(sim.iccId) || sim.iccId.contains(id))) return h;
            }
            // fallback: slot index diye
            if (sim.slot >= 0 && sim.slot < handles.size()) return handles.get(sim.slot);
        } catch (SecurityException ignored) { }
        return null;
    }

    /** Selected SIM diye shorashori call (dialog chara). */
    public static void placeCall(Context ctx, String number, Sim sim) {
        Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)));
        if (sim != null) {
            PhoneAccountHandle handle = findHandle(ctx, sim);
            if (handle != null) {
                i.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle);
            }
            // kichu phone er jonno extra (best-effort)
            i.putExtra("com.android.phone.force.slot", true);
            i.putExtra("com.android.phone.extra.slot", sim.slot);
            i.putExtra("slot", sim.slot);
            i.putExtra("simSlot", sim.slot);
        }
        ctx.startActivity(i);
    }
}