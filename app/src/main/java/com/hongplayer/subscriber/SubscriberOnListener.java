package com.hongplayer.subscriber;

public interface SubscriberOnListener<T> {

    void onSucceed(T data);

    void onError(int code, String msg);
}
