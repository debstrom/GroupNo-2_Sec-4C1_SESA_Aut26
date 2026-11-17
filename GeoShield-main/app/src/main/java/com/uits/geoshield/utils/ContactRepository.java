package com.uits.geoshield.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.uits.geoshield.models.Contact;

import java.util.ArrayList;
import java.util.List;

/** Trusted contacts store, phone er SQLite database e save hoy. */
public final class ContactRepository {
    public static final int MAX_CONTACTS = 5;

    private static ContactRepository instance;

    private final ContactDbHelper dbHelper;
    private final List<Contact> contacts = new ArrayList<>(); // cache

    private ContactRepository(Context context) {
        dbHelper = new ContactDbHelper(context.getApplicationContext());
        loadFromDb();
    }

    /** Application.onCreate() e ekbar call korte hobe. */
    public static synchronized void init(Context context) {
        if (instance == null) instance = new ContactRepository(context);
    }

    public static ContactRepository get() {
        if (instance == null) {
            throw new IllegalStateException("ContactRepository.init(context) call kora hoyni");
        }
        return instance;
    }

    private void loadFromDb() {
        contacts.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(ContactDbHelper.TABLE, null, null, null, null, null,
                ContactDbHelper.COL_ID + " ASC")) {
            while (c.moveToNext()) {
                contacts.add(new Contact(
                        c.getString(c.getColumnIndexOrThrow(ContactDbHelper.COL_NAME)),
                        c.getString(c.getColumnIndexOrThrow(ContactDbHelper.COL_RELATION)),
                        c.getString(c.getColumnIndexOrThrow(ContactDbHelper.COL_CHANNELS)),
                        c.getString(c.getColumnIndexOrThrow(ContactDbHelper.COL_PHONE)),
                        c.getInt(c.getColumnIndexOrThrow(ContactDbHelper.COL_FLAG)) == 1
                ));
            }
        }
    }

    public List<Contact> getContacts() { return contacts; }
    public int size() { return contacts.size(); }
    public boolean isFull() { return contacts.size() >= MAX_CONTACTS; }

    public void add(Contact c) {
        ContentValues v = new ContentValues();
        v.put(ContactDbHelper.COL_NAME, c.getName());
        v.put(ContactDbHelper.COL_RELATION, c.getRelation());
        v.put(ContactDbHelper.COL_CHANNELS, c.getChannels());
        v.put(ContactDbHelper.COL_PHONE, c.getPhone());
       // v.put(ContactDbHelper.COL_FLAG, c.isEnabled() ? 1 : 0);
        dbHelper.getWritableDatabase().insert(ContactDbHelper.TABLE, null, v);
        contacts.add(c);
    }

    public int remove(Contact c) {
        int index = contacts.indexOf(c);
        if (index >= 0) {
            contacts.remove(index);
            dbHelper.getWritableDatabase().delete(
                    ContactDbHelper.TABLE,
                    ContactDbHelper.COL_NAME + "=? AND " + ContactDbHelper.COL_PHONE + "=?",
                    new String[]{c.getName(), c.getPhone()});
        }
        return index;
    }
}