package com.cjq.DownLoader.utils;

import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CJQ on 2015/5/12.
 */
public class DownloadThread extends Thread{

    private static final int BUFFER_SIZE = 8192;
    private URL url;
    private Pair<Integer,Integer> start_end;
    private File file;
    private int threadID;
    private DownloadThreadListener listener;

    public DownloadThread(URL url, Pair<Integer, Integer> start_end, File file, int threadID, DownloadThreadListener listener) {
        this.url = url;
        this.start_end = start_end;
        this.file = file;
        this.threadID = threadID;
        this.listener = listener;
    }

    interface DownloadThreadListener{
        void downloading(int threadID,int downloadedLength);
        void downloadFinished(int threadID,int downloadedLength);
        boolean getDownloadOrder(int threadID);
    }

    @Override
    public void run() {
        //开启链接
        try {
            int downloadedLength = 0;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            ConnetionModifyUtil.setupConnection(connection);
            connection.setRequestProperty("Range", "bytes=" + start_end.first + "-"+ start_end.second);//设置获取实体数据的范围

            BufferedInputStream bin = new BufferedInputStream(connection.getInputStream());
            byte[] buff = new byte[BUFFER_SIZE];
            int len;
            //打开文件流
            RandomAccessFile raf = new RandomAccessFile(file,"rw");
            raf.seek(start_end.first);

            while (listener.getDownloadOrder(threadID) && (len =bin.read(buff,0,BUFFER_SIZE))!=-1){
                raf.write(buff,0,len);
                downloadedLength +=len;
                if(listener!=null)
                    listener.downloading(threadID, len);
            }
            connection.disconnect();
            bin.close();
            raf.close();
            if(listener!=null)
                listener.downloadFinished(threadID, downloadedLength);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
