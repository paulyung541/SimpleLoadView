package com.ysy.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ysy.simpleloadview.SimpleLoadView;

public class MainActivity extends AppCompatActivity {
    private SimpleLoadView loadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadView = (SimpleLoadView) findViewById(R.id.loadview);
        loadView.setOnReloadListener(new SimpleLoadView.OnReloadListener() {
            @Override
            public void onReload() {
                loadView.showLoading();
            }
        });
        loadView.showLoading();
    }

    public void buttonClick(View view) {
        loadView.showReload();
    }
}
