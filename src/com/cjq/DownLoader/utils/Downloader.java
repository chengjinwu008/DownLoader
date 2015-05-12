package com.cjq.DownLoader.utils;

import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by android on 2015/5/12.
 */
public class Downloader {
    String urlString;
    DBHelper helper;

    public void download(){
        //��ȡ�����ļ��ĳ���
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    //��������
                    connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");

                } catch (MalformedURLException e) {
                    //url��ʽ������
                    e.printStackTrace();
                } catch (IOException e) {
                    //url���ӳ���
                    e.printStackTrace();
                }
            }
        }


    }
}
