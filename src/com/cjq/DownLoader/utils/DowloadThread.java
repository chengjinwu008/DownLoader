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
public class DowloadThread extends Thread{

    private static final int BUFFER_SIZE = 8192;
    private URL url;
    private Pair<Integer,Integer> start_end;
    private File file;
    private int threadID;
    private DownloadThreadListener listener;

    public DowloadThread(URL url, Pair<Integer, Integer> start_end, File file, int threadID, DownloadThreadListener listener) {
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
        //��������
        try {
            int downloadedLength = 0;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");
//��������
            connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            //��������
            connection.setRequestProperty("Accept-Language", "zh-CN");
            //��ת�����ӣ�ʲôҳ���������ģ���ֹ�������Ĳ������أ�
            connection.setRequestProperty("Referer", url.toString());
            //�����ʽ
            connection.setRequestProperty("Charset", "UTF-8");
            //ģ�������
//                    connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            //���ֳ�����
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Range", "bytes=" + start_end.first + "-"+ start_end.second);//���û�ȡʵ�����ݵķ�Χ

            BufferedInputStream bin = new BufferedInputStream(connection.getInputStream());
            byte[] buff = new byte[BUFFER_SIZE];
            int len;
            //���ļ���
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
