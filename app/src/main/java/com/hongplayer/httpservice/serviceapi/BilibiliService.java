package com.hongplayer.httpservice.serviceapi;


import com.hongplayer.bean.idataapi.BiliLiveData;
import com.hongplayer.httpservice.httpentity.BiliHttpResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface BilibiliService {

    @GET("liveroom/bilibili")
    Observable<BiliHttpResult<BiliLiveData>> getLiveList(@Query("apikey")String apiKey, @Query("kw")String channel, @Query("pageToken")int pageToken);

}
