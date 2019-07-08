package com.hong.libplayer.player;

import com.hong.libplayer.listener.HOnErrorListener;
import com.hong.libplayer.listener.HOnPreparedListener;

public class HPlayer {
    static {
        System.loadLibrary("HPlayer");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("postproc-54");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");
    }

    private HOnPreparedListener onPreparedListener;
    private HOnErrorListener onErrorListener;

    public void setOnPreparedListener(HOnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public void setOnErrorListener(HOnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    private String dataSource;

    public void setDataSource(String dataSource){
        this.dataSource = dataSource;
    }

    public void prepare(){
        native_prepare(dataSource,false);
    }

    public void start() {
        native_start();
    }

    private native void native_prepare(String url,boolean isOnlyMusic);

    private native void native_start();

    private native void native_test();

    private void calledOnPrepared(){
        if(onPreparedListener!=null)onPreparedListener.onPrepared();
    }

    private void calledOnError(String errorMsg){
        if(onErrorListener!=null)onErrorListener.onError(errorMsg);
    }
}
