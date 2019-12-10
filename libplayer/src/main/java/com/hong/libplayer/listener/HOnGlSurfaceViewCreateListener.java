package com.hong.libplayer.listener;

import android.graphics.Bitmap;
import android.view.Surface;

public interface HOnGlSurfaceViewCreateListener {

    void onGlSurfaceViewCreate(Surface surface);

    void onCutVideoImg(Bitmap bitmap);

}
