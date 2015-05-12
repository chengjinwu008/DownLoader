package com.cjq.DownLoader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.net.URL;

public class MyActivity extends Activity {
    private ProgressBar progressBar;
    private EditText text;

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
        String url_string = text.getText().toString();
    }
}
