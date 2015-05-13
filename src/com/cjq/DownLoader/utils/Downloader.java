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
    private Thread[] threads;//线程管理数组
    private boolean[] threads_finish_status;//线程完成状态容器
    private boolean[] threads_going_status;//线程开关控制容器
    private String urlString;//请求文件下载的url地址
    private int fileLength;//下载文件的长度
    private String savePath;//文件下载目录
    private ProgressLogUtil progressLogUtil;//用于保存线程数据的dao
    private Map<Integer, Integer> logData;//用于记录线程现在进度的容器
    private int thread_count;//线程的数量
    private int downloaded_size = 0;//已经下载了的数据大小
    private int block;//每条线程需要下载的数量
    private File file;//下载的文件对象
    private boolean prepared;//准备标识
    private DownloaderListener listener;//监听器
    private boolean stoped = false;//停止下载标志
    private boolean paused = false;//暂停下载标志

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
        //重新下载，则应该重置所有的标志
        prepared = false;
        stoped = false;
        paused = false;

        //获取下载文件的长度
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

                    //构建要下载的file对象
                    file = new File(savePath, fileName);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }

                    //获取下载记录
                    logData = progressLogUtil.readProgress(urlString);
                    if (logData != null) {
                        if (logData.size() == thread_count) {
                            //如果记录数量和线程数量一致，则表示可以断点续传
                            //统计下载了的数据大小
                            for (Map.Entry<Integer, Integer> m : logData.entrySet()) {
                                downloaded_size += m.getValue();
                            }
                        }
                    } else {
                        logData = new HashMap<Integer, Integer>();
                    }

//                    计算下载的片段大小
                    block = fileLength % thread_count == 0 ? fileLength / thread_count : fileLength / thread_count + 1;

                    connection.disconnect();
                    //做好准备结束标识
                    prepared = true;
                    //执行准备完毕的监听器
                    if (listener != null) {
                        listener.onPrepareFinished(Downloader.this);
                    }
                } catch (MalformedURLException e) {
                    //url格式有问题
                    e.printStackTrace();
                } catch (IOException e) {
                    //url连接出错
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void download() throws IOException {
        //初始化线程容器
        threads_finish_status = new boolean[thread_count];
        threads_going_status = new boolean[thread_count];
        threads = new Thread[thread_count];

        if (!prepared) {
            throw new RuntimeException("not Prepared!");
        }

        //首先因为得到了长度，应该取得文件的randomAccess对象
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        if (fileLength > 0)//成功获取了文件长度
            raf.setLength(fileLength);
//        设置完长度就关闭文件流
        if (logData.size() != thread_count)//线程数量和记录数量不等，表示不能断点续传
        {
            logData.clear();
            //初始化下载长度
            for (int i = 0; i < thread_count; i++) {
                logData.put(i, 0);
            }
        }

        //准备完毕，开始下载
        //构建url
        URL url = new URL(urlString);
        int start;
        int end;
        for (int i = 0; i < thread_count; i++) {
            //计算每个线程要下载的开头和结尾
            start = i == 0 ? 0 : i * block + 1;
            start += logData.get(i);//把已经下载的数据也算上
            end = i != thread_count - 1 ? (i + 1) * block : fileLength;
            //判断是否已经下载完了
            if (start >= end) {
                //已经下载完了
                continue;
            }

            Pair<Integer, Integer> start_end = new Pair<>(start, end);
            //开启新线程下载

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
                    //每条线程下载完毕无论如何，保存下载长度
                    progressLogUtil.updateProgress(urlString, threadID, downloadedLength);
                    if (isFinished()) {
                        if (listener != null) {
                            listener.onDownloadFinished();
                        }
                        //判断下载是否真的完成了，完成则删除记录
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
                    //正则匹配
                    Matcher matcher = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                    if (matcher.find()) return matcher.group(1);
                }
            }
            fileName = UUID.randomUUID() + ".temp";//随机名
        }
        return fileName;
    }

    public void pause() {
        //暂停
        //暂停需要做的事情1：中止线程 2:记录下载长度
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
            //删除记录
            progressLogUtil.delete(urlString);
        }
    }

    public boolean isStoped() {
        return stoped;
    }
}
