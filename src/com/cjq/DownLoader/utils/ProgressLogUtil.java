package com.cjq.DownLoader.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by android on 2015/5/12.
 */
public class ProgressLogUtil {

    private DownloadDBHelper helper;

    public ProgressLogUtil(Context context) {
        helper = new DownloadDBHelper(context);
    }

    public Map<Integer,Integer> readProgress(String url){
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select thread_id,progress from download where url=?", new String[]{url});
        if(cursor.getCount()<=0){
            return null;
        }
        cursor.moveToFirst();
        Map<Integer,Integer> res = new HashMap<>();
        res.put(cursor.getInt(0), cursor.getInt(1));

        while (cursor.moveToNext()){
            res.put(cursor.getInt(0), cursor.getInt(1));
        }
        cursor.close();
        database.close();
        return res;
    }

    public void updateProgress(String url,int thread_id,int progress){
        SQLiteDatabase database = helper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("progress",progress);
        database.update("download",values," url =? and thread_id=? ",new String[]{url, String.valueOf(thread_id)});
        database.close();
    }

    public void delete(String url){
        SQLiteDatabase database = helper.getReadableDatabase();
        database.delete("download"," url = ? ",new String[]{url});
        database.close();
    }
}
