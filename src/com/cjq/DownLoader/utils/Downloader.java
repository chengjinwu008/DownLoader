package com.cjq.DownLoader.utils;

import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by android on 2015/5/12.
 */
public class Downloader {
    private Thread[] threads;//�̹߳�������
    private boolean[] threads_finish_status;//�߳����״̬����
    private boolean[] threads_going_status;//�߳̿��ؿ�������
    private String urlString;//�����ļ����ص�url��ַ
    private int fileLength;//�����ļ��ĳ���
    private String savePath;//�ļ�����Ŀ¼
    private ProgressLogUtil progressLogUtil;//���ڱ����߳����ݵ�dao
    private Map<Integer, Integer> logData;//���ڼ�¼�߳����ڽ��ȵ�����
    private int thread_count;//�̵߳�����
    private int downloaded_size = 0;//�Ѿ������˵����ݴ�С
    private int block;//ÿ���߳���Ҫ���ص�����
    private File file;//���ص��ļ�����
    private boolean prepared;//׼����ʶ
    private DownloaderListener listener;//������
    private boolean stoped = false;//ֹͣ���ر�־
    private boolean paused = false;//��ͣ���ر�־

    public boolean isPaused() {
        return paused;
    }

    public File getFile() {
        return file;
    }

    public Downloader(String urlString, String savePath, ProgressLogUtil progressLogUtil, int thread_count, DownloaderListener listener) {
        this.urlString = urlString;
        this.savePath = savePath;
        this.progressLogUtil = progressLogUtil;
        this.thread_count = thread_count;
        this.listener = listener;
    }

    public int getFileLength() {
        if (prepared)
            return fileLength;
        else
            throw new RuntimeException("not Prepared!");
    }

    public interface DownloaderListener {
        void onPrepareFinished(Downloader downloader);

        void onDownloading(int downloaded_size);

        void onDownloadFinished();
    }

    public void downloadPrepare() {
        //�������أ���Ӧ���������еı�־
        prepared = false;
        stoped = false;
        paused = false;

        //��ȡ�����ļ��ĳ���
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    ConnetionModifyUtil.setupConnection(connection);
                    connection.connect();
                    if (connection.getResponseCode() == 200)
                        fileLength = connection.getContentLength();
                    if (fileLength < 0) throw new RuntimeException("unknown file size " + fileLength);
                    Log.i("fileLength", "" + fileLength);
                    String fileName = getFileName(connection);

                    //����Ҫ���ص�file����
                    file = new File(savePath, fileName);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }

                    //��ȡ���ؼ�¼
                    logData = progressLogUtil.readProgress(urlString);
                    if (logData != null) {
                        if (logData.size() == thread_count) {
                            //�����¼�������߳�����һ�£����ʾ���Զϵ�����
                            //ͳ�������˵����ݴ�С
                            for (Map.Entry<Integer, Integer> m : logData.entrySet()) {
                                downloaded_size += m.getValue();
                            }
                        }
                    } else {
                        logData = new HashMap<Integer, Integer>();
                    }

//                    �������ص�Ƭ�δ�С
                    block = fileLength % thread_count == 0 ? fileLength / thread_count : fileLength / thread_count + 1;

                    connection.disconnect();
                    //����׼��������ʶ
                    prepared = true;
                    //ִ��׼����ϵļ�����
                    if (listener != null) {
                        listener.onPrepareFinished(Downloader.this);
                    }
                } catch (MalformedURLException e) {
                    //url��ʽ������
                    e.printStackTrace();
                } catch (IOException e) {
                    //url���ӳ���
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void download() throws IOException {
        //��ʼ���߳�����
        threads_finish_status = new boolean[thread_count];
        threads_going_status = new boolean[thread_count];
        threads = new Thread[thread_count];

        if (!prepared) {
            throw new RuntimeException("not Prepared!");
        }

        //������Ϊ�õ��˳��ȣ�Ӧ��ȡ���ļ���randomAccess����
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        if (fileLength > 0)//�ɹ���ȡ���ļ�����
            raf.setLength(fileLength);
//        �����곤�Ⱦ͹ر��ļ���
        if (logData.size() != thread_count)//�߳������ͼ�¼�������ȣ���ʾ���ܶϵ�����
        {
            logData.clear();
            //��ʼ�����س���
            for (int i = 0; i < thread_count; i++) {
                logData.put(i, 0);
            }
        }

        //׼����ϣ���ʼ����
        //����url
        URL url = new URL(urlString);
        int start;
        int end;
        for (int i = 0; i < thread_count; i++) {
            //����ÿ���߳�Ҫ���صĿ�ͷ�ͽ�β
            start = i == 0 ? 0 : i * block + 1;
            start += logData.get(i);//���Ѿ����ص�����Ҳ����
            end = i != thread_count - 1 ? (i + 1) * block : fileLength;
            //�ж��Ƿ��Ѿ���������
            if (start >= end) {
                //�Ѿ���������
                continue;
            }

            Pair<Integer, Integer> start_end = new Pair<>(start, end);
            //�������߳�����

            Thread thread = new DownloadThread(url, start_end, file, i, new DownloadThread.DownloadThreadListener() {
                @Override
                public void downloading(int threadID, int downloadedLength) {
                    logDataAppend(threadID, downloadedLength);
                    if (listener != null)
                        listener.onDownloading(downloaded_size);
                }

                @Override
                public void downloadFinished(int threadID, int downloadedLength) {
                    logDataSet(threadID, downloadedLength);
                    threads_finish_status[threadID] = true;
                    //ÿ���߳��������������Σ��������س���
                    progressLogUtil.updateProgress(urlString, threadID, downloadedLength);
                    if (isFinished()) {
                        if (listener != null) {
                            listener.onDownloadFinished();
                        }
                        //�ж������Ƿ��������ˣ������ɾ����¼
                        if (downloaded_size >= fileLength || stoped) {
                            progressLogUtil.delete(urlString);
                        }
                    }
                }

                @Override
                public boolean getDownloadOrder(int threadID) {
                    return threads_going_status[threadID];
                }
            });
            threads_finish_status[i] = false;
            threads_going_status[i] = true;
            threads[i] = thread;
            thread.start();
        }
    }

    private boolean isFinished() {
        for (boolean b : threads_finish_status) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    private synchronized void logDataAppend(int threadID, int downloadedLength) {
        logData.put(threadID, logData.get(threadID) + downloadedLength);
        downloaded_size += downloadedLength;
    }

    private synchronized void logDataSet(int threadID, int downloadedLength) {
        logData.put(threadID, downloadedLength);
    }

    public String getFileName(HttpURLConnection connection) {
        String fileName = urlString.substring(urlString.lastIndexOf("/") + 1);
        if ("".equals(fileName.trim())) {

            for (int i = 0; ; i++) {
                String mine = connection.getHeaderField(i);
                if (mine == null) break;
                if ("content-disposition".equals(connection.getHeaderFieldKey(i).toLowerCase())) {
                    //����ƥ��
                    Matcher matcher = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                    if (matcher.find()) return matcher.group(1);
                }
            }
            fileName = UUID.randomUUID() + ".temp";//�����
        }
        return fileName;
    }

    public void pause() {
        //��ͣ
        //��ͣ��Ҫ��������1����ֹ�߳� 2:��¼���س���
        if (!paused)
            for (int i = 0; i < threads_going_status.length; i++)
                threads_going_status[i] = false;
        paused = true;
    }

    public void stop() {
        if (!paused) {
            for (int i = 0; i < threads_going_status.length; i++)
                threads_going_status[i] = false;
            stoped = true;
        } else {
            stoped = true;
            if (listener != null) {
                listener.onDownloadFinished();
            }
            //ɾ����¼
            progressLogUtil.delete(urlString);
        }
    }

    public boolean isStoped() {
        return stoped;
    }
}
