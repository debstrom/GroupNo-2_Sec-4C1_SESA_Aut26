package com.uits.geoshield.utils;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "geoshield.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "contacts";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_RELATION = "relation";
    public static final String COL_CHANNELS = "channels";
    public static final String COL_PHONE = "phone";
    public static final String COL_FLAG = "flag";

    public ContactDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT, "
                + COL_RELATION + " TEXT, "
                + COL_CHANNELS + " TEXT, "
                + COL_PHONE + " TEXT, "
                + COL_FLAG + " INTEGER)");


    }

    private void insertSeed(SQLiteDatabase db, String n, String r, String c, String p) {
        ContentValues v = new ContentValues();
        v.put(COL_NAME, n);
        v.put(COL_RELATION, r);
        v.put(COL_CHANNELS, c);
        v.put(COL_PHONE, p);
        v.put(COL_FLAG, 1);
        db.insert(TABLE, null, v);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}