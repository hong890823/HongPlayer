package com.hongplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hong.libplayer.listener.HOnPreparedListener;
import com.hong.libplayer.opengl.HGLSurfaceView;
import com.hong.libplayer.player.HPlayer;
import com.hongplayer.R;
import com.hongplayer.base.BaseActivity;

import butterknife.BindView;

public class VideoLiveActivity extends BaseActivity implements HOnPreparedListener {

    @BindView(R.id.video_surface_view)
    HGLSurfaceView surfaceView;

    private String liveUrl;
    private HPlayer player;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_live);
        liveUrl = (String) getIntent().getExtras().get("url");
        liveUrl = "http://js.live-play.acgvideo.com/live-js/798500/live_36514186_3847856.flv?wsSecret=9ef2a98b2dab0ea2d0fd8e29285630d9&wsTime=1562665541&trid=10319457c58f48a5afb8f680e92f7f3a&order=1&sig=no";
        player = new HPlayer();
        player.setOnPreparedListener(this);
        player.setDataSource(liveUrl);
        player.prepare();

    }

    /**
     * HOnPreparedListener的接口回调
     * FFmpeg已经准备好，可以开始播放了
     * */
    @Override
    public void onPrepared() {
        player.start();
    }


}
