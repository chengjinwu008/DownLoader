package com.cjq.DownLoader.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by android on 2015/5/12.
 */
public class DownloadDBHelper extends SQLiteOpenHelper {
    public static final String NAME = "downloader.db";
    public static final int VERSION =1;

    public DownloadDBHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table download(_id integer primary key autoincrement,thread_id integer,url varchar(200),progress integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
