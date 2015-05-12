package com.cjq.DownLoader.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by android on 2015/5/12.
 */
public class ProgressLogUtil {

    private DBHelper helper;

    public Map<Integer,Double> readProgress(String url){
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select thread_id,progress from download where url=?", new String[]{url});
        if(cursor.getCount()<=0){
            return null;
        }
        cursor.moveToFirst();
        Map<Integer,Double> res = new HashMap<>();
        res.put(cursor.getInt(0), cursor.getDouble(1));

        while (cursor.moveToNext()){
            res.put(cursor.getInt(0), cursor.getDouble(1));
        }
        return res;
    }

    public void updateProgress(String url,int thread_id,double progress){
        SQLiteDatabase database = helper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("progress",progress);
        database.update("download",values," url =? and thread_id=? ",new String[]{url, String.valueOf(thread_id)});
    }

    public void delete(String url){
        SQLiteDatabase database = helper.getReadableDatabase();
        database.delete("download"," url = ? ",new String[]{url});
    }
}
