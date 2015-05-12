package com.cjq.DownLoader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.cjq.DownLoader.utils.Downloader;
import com.cjq.DownLoader.utils.ProgressLogUtil;

import java.io.IOException;
import java.net.URL;

public class MyActivity extends Activity {
    private ProgressBar progressBar;
    private EditText text;
    private Handler mHandler = new Handler();
    private Downloader downloader;
    private boolean paused;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        text = (EditText) findViewById(R.id.text);
    }

    public void download(View view) {
        if(downloader==null){
            String url_string = text.getText().toString();
            downloader = new Downloader(url_string,"/sdcard/",new ProgressLogUtil(this),3,new Downloader.DownloaderListener(){

                @Override
                public void onPrepareFinished(Downloader downloader) {
                    try {
                        downloader.download();
                        progressBar.setMax(downloader.getFileLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDownloading(int downloaded_size) {
                    progressBar.setProgress(downloaded_size);
                    Log.i("downloaded_size",""+downloaded_size);
                }

                @Override
                public void onDownloadFinished() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
//                            text.setText("œ¬‘ÿÕÍ±œ");
                        }
                    });
                }
            });
            downloader.downloadPrepare();
            paused = false;
        }else{
            if(paused){
                downloader.downloadPrepare();
                paused=false;
            }else{
                downloader.pause();
                paused=true;
            }
        }
    }
}
