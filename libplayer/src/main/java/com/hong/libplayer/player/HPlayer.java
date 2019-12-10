package com.hong.libplayer.player;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.hong.libplayer.listener.HOnErrorListener;
import com.hong.libplayer.listener.HOnGlSurfaceViewCreateListener;
import com.hong.libplayer.listener.HOnInfoListener;
import com.hong.libplayer.listener.HOnLoadListener;
import com.hong.libplayer.listener.HOnPreparedListener;
import com.hong.libplayer.listener.HCommonCode;
import com.hong.libplayer.opengl.HGLSurfaceView;
import com.hong.libplayer.opengl.HRender;
import com.hong.libplayer.util.LogUtil;

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
    /**
     * 视频硬件编解码器
     */
    private MediaCodec mediaCodec;
    /**
     * 视频解码器的BufferInfo
     */
    private MediaCodec.BufferInfo mediaBufferInfo = new MediaCodec.BufferInfo();

    private HOnPreparedListener onPreparedListener;
    private HOnErrorListener onErrorListener;
    private HOnLoadListener onLoadListener;
    private HOnInfoListener onInfoListener;

    private boolean isOnlyMusic;
    private boolean isOnlySoft;
    private HTimeBean timeBean;
    private int lastCurrTime = 0;

    public int getDuration(){
        return native_duration();
    }

    public void setOnPreparedListener(HOnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public void setOnErrorListener(HOnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setOnLoadListener(HOnLoadListener onLoadListener){
        this.onLoadListener = onLoadListener;
    }

    public void setOnInfoListener(HOnInfoListener onInfoListener){
        this.onInfoListener = onInfoListener;
    }

    public void setDataSource(String dataSource){
        this.dataSource = dataSource;
    }

    public void setHglSurfaceView(HGLSurfaceView hglSurfaceView) {
        this.hglSurfaceView = hglSurfaceView;
        //接口的实现在这里，接口的调用以及参数的赋值在HRender中的initRenderMediaCodec中
        hglSurfaceView.setOnGlSurfaceViewCreateListener(new HOnGlSurfaceViewCreateListener() {
            @Override
            public void onGlSurfaceViewCreate(Surface s) {
                if(s!=null) LogUtil.logD("Player类接收到有效的surface对象");
                if(surface==null)surface = s;
            }

            @Override
            public void onCutVideoImg(Bitmap bitmap) {

            }
        });
    }

    public void prepare(boolean isOnlyMusic){
        LogUtil.logD("Player类准备播放");
        native_prepare(dataSource,isOnlyMusic);
    }

    public void start() {
        LogUtil.logD("Player类开始播放");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(timeBean==null){
                    timeBean = new HTimeBean();
                }
                native_start();
            }
        }).start();
    }

    public void stop(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                native_stop();
                if(mediaCodec != null) {
                    try {
                        mediaCodec.flush();
                        mediaCodec.stop();
                        mediaCodec.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediaCodec = null;
                }
                if(hglSurfaceView != null) {
                    hglSurfaceView.setCodecType(-1);
                    hglSurfaceView.requestRender();
                }
            }
        }).start();
    }

    public void seek(final int seconds){
        new Thread(new Runnable() {
            @Override
            public void run() {
                lastCurrTime = seconds;
                native_seek(seconds);
            }
        }).start();
    }

    /**
     * 我是分割线，分割与JNI相关的方法
     * */

    private native void native_prepare(String url,boolean isOnlyMusic);

    private native void native_start();

    private native void native_stop();

    private native int native_duration();

    private native void native_seek(int seconds);

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
                LogUtil.logD(mediaFormat.toString());
                mediaCodec = MediaCodec.createDecoderByType(mType);
                if(surface!=null){
                    LogUtil.logD("硬件解码初始化与surface绑定成功");
                    mediaCodec.configure(mediaFormat,surface,null,0);
                    mediaCodec.start();
                }
            }catch(Exception e){
                LogUtil.logE("硬解码初始化失败"+e.toString());
            }
        }else{
            if(onErrorListener!=null)onErrorListener.onError(HCommonCode.H_STATUS_SURFACE_NULL,"surface is null");
        }
    }

    /**
     * 硬解执行：该方法正常执行的话就会触发到MediaCodec中绑定的surface中的surfaceTexture的SurfaceTexture.OnFrameAvailableListener接口的onFrameAvailable方法
     * */
    public void decodeMediaCodec(byte[] bytes,int size,int pts){
        if(bytes!=null && mediaCodec!=null && mediaBufferInfo!=null){
            try{
                //请求一个输入缓存，返回得到分配到的输入缓存的索引
                int inputBufferIndex = mediaCodec.dequeueInputBuffer(10);
                if(inputBufferIndex >= 0){
                    //根据索引获取到输入缓存数组
                    ByteBuffer byteBuffer = mediaCodec.getInputBuffers()[inputBufferIndex];
                    //数组清空，防止以前的数据污染数组
                    byteBuffer.clear();
                    //把实际数据放入数组中
                    byteBuffer.put(bytes);
                    //输入缓存入队列
                    mediaCodec.queueInputBuffer(inputBufferIndex,0,size,pts,0);
                }
                //获取输出缓存，并把信息存储到mediaBufferInfo中
                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(mediaBufferInfo,10);
//                LogUtil.logE("硬解码值---"+outputBufferIndex);
                while(outputBufferIndex >= 0){
                    //释放输出缓存
                    mediaCodec.releaseOutputBuffer(outputBufferIndex,true);
                    //再获取输出缓存，并把信息存储到mediaBufferInfo中
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(mediaBufferInfo,10);
                }
            }catch(Exception e){
                LogUtil.logE("硬解码失败"+e.getMessage());
            }
        }
    }

    /**
     * 软解执行
     * */
    public void setFrameData(int w, int h, byte[] y, byte[] u, byte[] v) {
        if(hglSurfaceView != null){
            hglSurfaceView.setCodecType(0);
            hglSurfaceView.setYUVData(w, h, y, u, v);
        }
    }

    public void onVideoInfo(int currentTime,int totalTime){
        if(onInfoListener!=null && timeBean!=null){
            if(currentTime < lastCurrTime) currentTime = lastCurrTime;
            timeBean.setCurrentSeconds(currentTime);
            timeBean.setTotalSeconds(totalTime);
            onInfoListener.onInfo(timeBean);
            lastCurrTime = currentTime;
        }
    }

    public void onComplete(){

    }

    public void onLoad(boolean isLoading){
        if(onLoadListener!=null){
            onLoadListener.onLoad(isLoading);
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
