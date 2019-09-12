package com.hongplayer.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.WindowManager;

import com.hong.libplayer.listener.HOnErrorListener;
import com.hong.libplayer.listener.HOnPreparedListener;
import com.hong.libplayer.opengl.HGLSurfaceView;
import com.hong.libplayer.player.HPlayer;
import com.hongplayer.R;
import com.hongplayer.base.BaseActivity;

import butterknife.BindView;

public class VideoLiveActivity extends BaseActivity implements HOnPreparedListener, HOnErrorListener {

//    @BindView(R.id.video_surface_view)
    HGLSurfaceView surfaceView;

    /**
     * 真视通-大华摄像头：rtsp://admin:zst123456@10.1.26.230:554/cam/realmonitor?channel=1&subtype=0
     * 真视通-89流媒体：rtsp://10.1.6.89/cam/realmonitor
     * */

    /**
     * http://www.imooc.com/article/91381
     * */
    private String liveUrl;
    private HPlayer player;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_live);
        surfaceView = findViewById(R.id.video_surface_view);
//        liveUrl = (String) getIntent().getExtras().get("url");
        liveUrl = "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=172434&resourceType=video&editionType=default&source=aliyun&playUrlType=url_oss";
        liveUrl = deleteHttps(liveUrl);
        player = new HPlayer();
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setDataSource(liveUrl);
        player.setHglSurfaceView(surfaceView);
        player.prepare();
    }

    /**
     * 把带有Https协议的媒体流去掉Https，改为Http才可以
     * */
    private String deleteHttps(String url){
        String noHttpsUrl = url;
        if(!TextUtils.isEmpty(url)){
            if(url.contains("https")){
                noHttpsUrl = url.replaceAll("https","http");
            }
        }
        return noHttpsUrl;
    }

    /**
     * HOnPreparedListener的接口回调
     * FFmpeg已经准备好，可以开始播放了
     * */
    @Override
    public void onPrepared() {
        player.start();
    }


    @Override
    public void onError(int errorCode, String errorMsg) {

    }
}
