package com.hongplayer.httpservice.service;

import com.hongplayer.httpservice.httpentity.VideoHttpResult;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class VideoBaseApi {

    public <T> void toSubscribe(Observable<T> o, Observer<T> s){
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }

    /**
     * 用来统一处理Http的resultCode,并将HttpResult的Data部分剥离出来返回给subscriber
     *
     * @param <T>   Subscriber真正需要的数据类型，也就是Data部分的数据类型
     *           Func1(I,O) 输入和输出
     */
    public class HttpResultFunc<T> implements Function<VideoHttpResult<T>, List<T>> {

        @Override
        public List<T> apply(VideoHttpResult<T> videoHttpResult) {
            if (videoHttpResult.getCode() == 200) {
                return videoHttpResult.getResult();
            }
            throw new ExceptionApi(videoHttpResult.getCode(), videoHttpResult.getMessage());
        }
    }

}
