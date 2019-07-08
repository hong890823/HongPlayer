package com.hongplayer.httpservice.serviceapi;


import com.hongplayer.bean.apiopen.VideoResult;
import com.hongplayer.httpservice.httpentity.VideoHttpResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;


public interface BilibiliService {

    @GET("todayVideo")
    Observable<VideoHttpResult<VideoResult>> getVideoList();

}
