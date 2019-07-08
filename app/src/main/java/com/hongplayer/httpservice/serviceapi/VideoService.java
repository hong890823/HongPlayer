package com.hongplayer.httpservice.serviceapi;


import com.hongplayer.httpservice.httpentity.VideoHttpResult;

import io.reactivex.Observable;
import retrofit2.http.GET;


public interface VideoService {

    @GET("todayVideo")
    Observable<VideoHttpResult<VideoRootBean>> getVideoList();

}
