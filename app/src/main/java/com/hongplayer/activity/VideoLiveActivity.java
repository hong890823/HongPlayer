package com.hongplayer.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hong.libplayer.listener.HOnErrorListener;
import com.hong.libplayer.listener.HOnInfoListener;
import com.hong.libplayer.listener.HOnLoadListener;
import com.hong.libplayer.listener.HOnPreparedListener;
import com.hong.libplayer.opengl.HGLSurfaceView;
import com.hong.libplayer.player.HPlayer;
import com.hong.libplayer.player.HTimeBean;
import com.hong.libplayer.util.HTimeUtil;
import com.hongplayer.R;
import com.hongplayer.base.BaseActivity;

import java.lang.ref.WeakReference;

import butterknife.BindView;

public class VideoLiveActivity extends BaseActivity implements HOnPreparedListener, HOnErrorListener, HOnLoadListener, HOnInfoListener {
    @BindView(R.id.video_surface_view)
    HGLSurfaceView surfaceView;
    @BindView(R.id.pb_loading)
    ProgressBar progressBar;
    @BindView(R.id.iv_pause)
    ImageView ivPause;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.seek_bar)
    SeekBar seekBar;
    @BindView(R.id.ly_action)
    LinearLayout lyAction;
    @BindView(R.id.iv_cut_img)
    ImageView ivCutImg;
    @BindView(R.id.iv_show_img)
    ImageView ivShowImg;

    /**
     * http://www.imooc.com/article/91381
     * */
    private String liveUrl;
    private HPlayer player;
    private VideoHandler videoHandler;
    private int position;//视频播放到进度条的哪个点上
    private boolean isSeekByUser;

    @Override
    public void onLoad(boolean isLoading) {
        Message msg = Message.obtain();
        msg.what=0;
        Bundle data = new Bundle();
        data.putBoolean("isLoading",isLoading);
        msg.setData(data);
        videoHandler.sendMessage(msg);
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        Message msg = Message.obtain();
        msg.what=1;
        Bundle data = new Bundle();
        data.putInt("errorCode",errorCode);
        data.putString("errorMsg",errorMsg);
        msg.setData(data);
        videoHandler.sendMessage(msg);
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
    public void onInfo(HTimeBean timeBean) {
        Message msg = Message.obtain();
        msg.what=3;
        msg.obj = timeBean;
        videoHandler.sendMessage(msg);
    }

    private static class VideoHandler extends Handler{
        private WeakReference<Context> reference;

        VideoHandler(Context context){
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Context context = reference.get();
            if(context!=null){
                if(context instanceof VideoLiveActivity){
                    VideoLiveActivity videoLiveActivity = (VideoLiveActivity)context;
                    Bundle data = msg.getData();
                    switch (msg.what){
                        case 0:
                            boolean isLoading = data.getBoolean("isLoading");
                            videoLiveActivity.progressBar.setVisibility(isLoading? View.VISIBLE:View.GONE);
                            break;
                        case 1:
                            int errorCode = data.getInt("errorCode");
                            String errorMsg = data.getString("errorMsg");
                            Toast.makeText(videoLiveActivity,errorCode+"---"+errorMsg,Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            HTimeBean timeBean = (HTimeBean) msg.obj;
                            if(timeBean.getTotalSeconds() > 0) {
                                videoLiveActivity.seekBar.setVisibility(View.VISIBLE);
                                if(videoLiveActivity.isSeekByUser) {
                                    videoLiveActivity.seekBar.setProgress(videoLiveActivity.position * 100 / timeBean.getTotalSeconds());
                                    videoLiveActivity.isSeekByUser = false;
                                } else {
                                    videoLiveActivity.seekBar.setProgress(timeBean.getCurrentSeconds() * 100 / timeBean.getTotalSeconds());
                                }
                                videoLiveActivity.tvTime.setText(HTimeUtil.secdsToDateFormat(timeBean.getTotalSeconds()) + "/" + HTimeUtil.secdsToDateFormat(timeBean.getCurrentSeconds()));
                            }
                            else {
                                videoLiveActivity.seekBar.setVisibility(View.GONE);
                                videoLiveActivity.tvTime.setText(HTimeUtil.secdsToDateFormat(timeBean.getCurrentSeconds()));
                            }
                            break;
                    }
                }
            }

        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_live);
        liveUrl = (String) getIntent().getExtras().get("url");
        liveUrl = deleteHttps(liveUrl);

        videoHandler = new VideoHandler(this);

        player = new HPlayer();
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnLoadListener(this);
        player.setOnInfoListener(this);
        player.setOnlyMusic(false);
        player.setDataSource(liveUrl);
        player.setHglSurfaceView(surfaceView);
        player.prepare(false);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position = player.getDuration() * progress / 100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekByUser = true;
                player.seek(position);
            }
        });

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

    @Override
    public void onBackPressed() {
        if(player!=null)player.stop();
        super.onBackPressed();
    }
}
