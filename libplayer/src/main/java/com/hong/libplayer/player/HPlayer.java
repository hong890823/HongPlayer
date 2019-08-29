package com.hong.libplayer.player;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.hong.libplayer.listener.HOnErrorListener;
import com.hong.libplayer.listener.HOnGlSurfaceViewCreateListener;
import com.hong.libplayer.listener.HOnPreparedListener;
import com.hong.libplayer.listener.HCommonCode;
import com.hong.libplayer.opengl.HGLSurfaceView;
import com.hong.libplayer.opengl.HRender;

import java.nio.ByteBuffer;

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


    private String dataSource;
    private HGLSurfaceView hglSurfaceView;
    private Surface surface;
    private MediaCodec mediaCodec;

    private HOnPreparedListener onPreparedListener;
    private HOnErrorListener onErrorListener;

    private boolean isOnlyMusic;
    private boolean isOnlySoft;

    public void setOnPreparedListener(HOnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public void setOnErrorListener(HOnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setDataSource(String dataSource){
        this.dataSource = dataSource;
    }

    public void setHglSurfaceView(HGLSurfaceView hglSurfaceView) {
        this.hglSurfaceView = hglSurfaceView;
        //接口的实现在这里，接口的调用以及参数的赋值在HRender中的initRenderMediaCodec中
        hglSurfaceView.setOnGlSurfaceViewCreateListener(new HOnGlSurfaceViewCreateListener() {
            @Override
            public void onGlSurfaceViewCreate(Surface surface) {
                HPlayer.this.surface = surface;
            }

            @Override
            public void onCutVideoImg(Bitmap bitmap) {

            }
        });
    }

    public void prepare(){
        native_prepare(dataSource,false);
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                native_start();
            }
        }).start();
    }



    /**
     * 我是分割线，分割与JNI相关的方法
     * */



    private native void native_prepare(String url,boolean isOnlyMusic);

    private native void native_start();

    private void calledOnPrepared(){
        if(onPreparedListener!=null)onPreparedListener.onPrepared();
    }

    private void calledOnError(int errorCode,String errorMsg){
        if(onErrorListener!=null)onErrorListener.onError(errorCode,errorMsg);
    }

    public boolean isOnlyMusic() {
        return isOnlyMusic;
    }

    public void setOnlyMusic(boolean onlyMusic) {
        isOnlyMusic = onlyMusic;
    }

    public boolean isOnlySoft() {
        return isOnlySoft;
    }

    public void setOnlySoft(boolean onlySoft) {
        isOnlySoft = onlySoft;
    }

    public void initMediaCodec(int mimeType,int width,int height,byte[] csd0,byte[] csd1){
        if(surface!=null){
            try{
                hglSurfaceView.setCodecType(HRender.RENDER_MEDIA_CODEC);
                String mType = getMimeType(mimeType);
                MediaFormat mediaFormat = MediaFormat.createVideoFormat(mType,width,height);
                mediaFormat.setInteger(MediaFormat.KEY_WIDTH,width);
                mediaFormat.setInteger(MediaFormat.KEY_HEIGHT,height);
                mediaFormat.setLong(MediaFormat.KEY_MAX_INPUT_SIZE,width*height);
                mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(csd0));
                mediaFormat.setByteBuffer("csd-1",ByteBuffer.wrap(csd1));
                Log.d("HongPlayer",mediaFormat.toString());
                mediaCodec = MediaCodec.createDecoderByType(mType);
                if(surface!=null){
                    mediaCodec.configure(mediaFormat,surface,null,0);
                    mediaCodec.start();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
            if(onErrorListener!=null)onErrorListener.onError(HCommonCode.H_STATUS_SURFACE_NULL,"surface is null");
        }
    }

    private String getMimeType(int type) {
        if(type == 1){
            return "video/avc";
        }
        else if(type == 2){
            return "video/hevc";
        }
        else if(type == 3){
            return "video/mp4v-es";
        }
        else if(type == 4){
            return "video/x-ms-wmv";
        }
        return "";
    }

}
