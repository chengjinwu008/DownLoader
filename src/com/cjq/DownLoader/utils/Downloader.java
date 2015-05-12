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
        //获取下载文件的长度
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    //接收类型
                    connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");

                } catch (MalformedURLException e) {
                    //url格式有问题
                    e.printStackTrace();
                } catch (IOException e) {
                    //url连接出错
                    e.printStackTrace();
                }
            }
        }


    }
}
