package com.mzw.gifdemoa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private GifView gif_view;
    private ProgressBar gif_view_loading;
    private static int w = 0, h = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gif_view = findViewById(R.id.id_gif_view);
        gif_view_loading = findViewById(R.id.id_gif_view_loading);

        w = getWindowManager().getDefaultDisplay().getWidth();
        h = getWindowManager().getDefaultDisplay().getHeight();



        findViewById(R.id.id_layout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String mImageUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1542515934424&di=df10ef12a8ec7bc7625a675bf18128cd&imgtype=0&src=http%3A%2F%2Fs3.sinaimg.cn%2Fmw690%2F002c2mEVzy7nKnBsVCqc2%26690";
        gif_view.setDownload_addr(mImageUrl,gif_view_loading);
        gif_view.setScreenH(h);
        gif_view.setScreenW(w);
    }
}
