package com.hongplayer.httpservice.serviceapi;

import com.hongplayer.bean.RadioLiveChannelBean;
import com.hongplayer.httpservice.httpentity.RadioHttpResult;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface RadioService {

    /**
     * 获取 token
     * @return
     */
    @POST("gettoken")
    Observable<RadioHttpResult<Integer>> getToken();

    /**
     * 根据Id获取直播列表
     * @param token
     * @param channelPlaceId
     * @param limit
     * @param offset
     * @return
     */
    @GET("channels/getlivebyparam")
    Observable<RadioHttpResult<List<RadioLiveChannelBean>>> getLiveByParam(@Query("token") String token, @Query("channelPlaceId") String channelPlaceId, @Query("limit") int limit, @Query("offset") int offset);
}
