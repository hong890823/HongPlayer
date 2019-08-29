package com.hongplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.hong.libplayer.listener.HOnErrorListener;
import com.hong.libplayer.listener.HOnPreparedListener;
import com.hong.libplayer.opengl.HGLSurfaceView;
import com.hong.libplayer.player.HPlayer;
import com.hongplayer.R;
import com.hongplayer.base.BaseActivity;

import butterknife.BindView;

public class VideoLiveActivity extends BaseActivity implements HOnPreparedListener, HOnErrorListener {

    @BindView(R.id.video_surface_view)
    HGLSurfaceView surfaceView;

    private String liveUrl;
    private HPlayer player;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_live);
//        liveUrl = (String) getIntent().getExtras().get("url");
        liveUrl = "http://ali.cdn.kaiyanapp.com/1566296371445_9236d1c3.mp4?auth_key=1566890429-0-0-ad6449725948e182dff26f1526f97383";
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
