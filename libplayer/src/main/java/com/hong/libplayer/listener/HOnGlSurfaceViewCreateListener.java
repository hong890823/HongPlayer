package com.hong.libplayer.listener;

import android.graphics.Bitmap;
import android.view.Surface;

/**
 * Created by hlwky001 on 2017/12/18.
 */

public interface HOnGlSurfaceViewCreateListener {

    void onGlSurfaceViewCreate(Surface surface);

    void onCutVideoImg(Bitmap bitmap);

}
