package com.sagur.pcshortcuts.appessible;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ran on 13/04/2017.
 */

public class MySqlHelper extends SQLiteOpenHelper {

    public MySqlHelper(Context context) {
        super(context, "places.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlCmd = "CREATE TABLE "+DBConstants.dbTable+" (_id INTEGER PRIMARY KEY AUTOINCREMENT,"+DBConstants.nameColumn+" TEXT,"+DBConstants.addressColumn+" TEXT)";
        db.execSQL(sqlCmd);

        String sqlCmd2 = "CREATE TABLE "+DBConstants.dbTableSaved+" (_id INTEGER PRIMARY KEY AUTOINCREMENT,"+DBConstants.nameColumn+" TEXT,"+DBConstants.addressColumn+" TEXT,"+DBConstants.latColumn+" TEXT,"+DBConstants.lngColumn + " Text)";
        db.execSQL(sqlCmd2);



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
