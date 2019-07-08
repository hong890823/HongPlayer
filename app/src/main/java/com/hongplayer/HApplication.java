package com.hongplayer;

import android.app.Application;


public class HApplication extends Application {

    private static HApplication instance;
    public static HApplication getInstance()
    {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

}
