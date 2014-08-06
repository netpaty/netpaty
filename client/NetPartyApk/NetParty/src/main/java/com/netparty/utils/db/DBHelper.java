package com.netparty.utils.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.netparty.data.Names;

public class DBHelper extends SQLiteOpenHelper {



    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("tag", "--- onCreate database ---");

        db.execSQL("create table " + DataBaseAdapter.ACCOUNTS_TABLE + " ("
                + Names.DB_FIELD_ID + " integer primary key autoincrement,"
                + Names.DB_FIELD_NETWORK_ID + " text,"
                + Names.DB_FIELD_NETWORK_TYPE + " text,"
                + Names.DB_FIELD_USER_NAME + " text,"
                + Names.DB_FIELD_USER_ID + " text);");


        db.execSQL("create table " + DataBaseAdapter.USERS_TABLE + " ("
                + Names.DB_FIELD_USER_ID + " integer primary key autoincrement,"
                + Names.DB_FIELD_NOTIFICATION_FLAG + " integer" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
