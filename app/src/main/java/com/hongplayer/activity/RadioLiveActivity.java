package com.hongplayer.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hong.libplayer.listener.HOnInfoListener;
import com.hong.libplayer.listener.HOnLoadListener;
import com.hong.libplayer.listener.HOnPreparedListener;
import com.hong.libplayer.player.HPlayer;
import com.hong.libplayer.player.HTimeBean;
import com.hong.libplayer.util.HTimeUtil;
import com.hongplayer.R;
import com.hongplayer.base.BaseActivity;
import com.hongplayer.bean.RadioLiveChannelBean;
import com.hongplayer.widget.SquareImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


public class RadioLiveActivity extends BaseActivity {

    @BindView(R.id.siv_logo)
    SquareImageView squareImageView;
    @BindView(R.id.tv_live_name)
    TextView tvLiveName;
    @BindView(R.id.pb_load)
    ProgressBar pbLoad;
    @BindView(R.id.iv_play)
    ImageView ivPlay;
    @BindView(R.id.tv_time)
    TextView tvTime;

    private List<RadioLiveChannelBean> datas;
    private int index = -1;
    private HPlayer player;
    private boolean isPlay = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_live);
        setBackView();
        String data = getIntent().getExtras().getString("data");
        index = getIntent().getExtras().getInt("index");
        Gson gson = new Gson();
        datas = gson.fromJson(data, new TypeToken<List<RadioLiveChannelBean>>(){}.getType());

        player = new HPlayer();
        playRadio(datas.get(index));

        player.setOnPreparedListener(new HOnPreparedListener() {
            @Override
            public void onPrepared() {
                player.start();
                Message message = Message.obtain();
                message.what = 4;
                handler.sendMessage(message);
            }
        });
        player.setOnLoadListener(new HOnLoadListener() {
            @Override
            public void onLoad(boolean load) {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = load;
                handler.sendMessage(message);
            }
        });
        player.setOnInfoListener(new HOnInfoListener() {
            @Override
            public void onInfo(HTimeBean timeBean) {
                Message message = Message.obtain();
                message.what = 2;
                message.obj = timeBean;
                handler.sendMessage(message);
            }
        });

//        player.setWlOnCompleteListener(new WlOnCompleteListener() {
//            @Override
//            public void onComplete() {
//                isPlay = false;
//            }
//        });

//        player.setWlOnStopListener(new WlOnStopListener() {
//            @Override
//            public void onStop() {
//                Message message = Message.obtain();
//                message.what = 3;
//                handler.sendMessage(message);
//            }
//        });

        player.prepare(true);

    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1)
            {
                boolean load = (boolean) msg.obj;
                if(load)
                {
                    pbLoad.setVisibility(View.VISIBLE);
                    ivPlay.setVisibility(View.INVISIBLE);
                }
                else
                {
                    pbLoad.setVisibility(View.INVISIBLE);
                    ivPlay.setVisibility(View.VISIBLE);
                }
            }
            else if(msg.what == 2)
            {
                HTimeBean timeBean = (HTimeBean) msg.obj;
                if(timeBean.getTotalSeconds() > 0) {
                    tvTime.setText(HTimeUtil.secdsToDateFormat(timeBean.getTotalSeconds()) + "/" + HTimeUtil.secdsToDateFormat(timeBean.getCurrentSeconds()));
                } else {
                    tvTime.setText(HTimeUtil.secdsToDateFormat(timeBean.getCurrentSeconds()));
                }
            }
            else if(msg.what == 3)
            {
                playRadio(getPlayChannelBean());
            }
            else if(msg.what == 4)
            {
                ivPlay.setImageResource(R.mipmap.ic_play_pause);
            }
        }
    };

//    @Override
//    public void onClickBack() {
//        super.onClickBack();
//        this.finish();
//        if(player != null) {
//            player.stop(true);
//        }
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        if(player != null) {
            player.stop();
        }
    }

    @OnClick(R.id.iv_pre)
    public void onClickPre(View view) {
        if(player != null && index > 0) {
            index--;
            player.stop();
        } else {
            showToast("已经到第一项了");
        }
    }

    @OnClick(R.id.iv_next)
    public void onClickNext(View view) {
        if(player != null && index < datas.size() - 1) {
            index++;
            player.stop();
        } else {
            showToast("已经到最后一项了");
        }
    }

    @OnClick(R.id.iv_play)
    public void onClickPlayPause(View view){
        if(player != null) {
            isPlay = !isPlay;
            if(isPlay) {
                ivPlay.setImageResource(R.mipmap.ic_play_pause);
//                player.resume();
            } else {
                ivPlay.setImageResource(R.mipmap.ic_play_play);
//                player.pause();
            }
        }
    }


    private void playRadio(RadioLiveChannelBean radioLiveChannelBean) {
        if(radioLiveChannelBean != null && player != null) {
            try {
                pbLoad.setVisibility(View.VISIBLE);
                ivPlay.setVisibility(View.INVISIBLE);
                player.setDataSource(radioLiveChannelBean.getStreams().get(0).getUrl());
                player.setOnlyMusic(true);
                tvLiveName.setText(radioLiveChannelBean.getLiveSectionName());
                setTitle(radioLiveChannelBean.getName());
                Glide.with(this).load(radioLiveChannelBean.getImg()).into(squareImageView);
                tvTime.setText("00:00:00");
                player.prepare(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showToast("不能再切换了");
        }
    }



    private RadioLiveChannelBean getPlayChannelBean() {
        if(index >= 0 && index < datas.size()) {
            return datas.get(index);
        }
        return null;
    }

}
