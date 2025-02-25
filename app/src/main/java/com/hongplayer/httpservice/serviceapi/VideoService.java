package com.hongplayer.httpservice.serviceapi;


import com.hongplayer.bean.apiopen.VideoResult;
import com.hongplayer.httpservice.httpentity.VideoHttpResult;

import io.reactivex.Observable;
import retrofit2.http.GET;


public interface VideoService {

    @GET("todayVideo")
    Observable<VideoHttpResult<VideoResult>> getVideoList();

}
