package com.hongplayer.httpservice.serviceapi;


import com.hongplayer.bean.apiopen.VideoResult;
import com.hongplayer.httpservice.service.HttpMethod;
import com.hongplayer.httpservice.service.VideoBaseApi;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * Created by ywl on 2016/5/19.
 */
public class VideoApi extends VideoBaseApi {

    private static final String API_OPEN_BASE_URL = "https://api.apiopen.top/";
    private static final String IDATA_API_BASE_URL = "https://api01.idataapi.cn:8000/";

    public static VideoApi videoApi;
    public VideoService videoService;
    public VideoApi() {
        videoService = HttpMethod.getInstance(API_OPEN_BASE_URL).createApi(VideoService.class);
    }

    public static VideoApi getInstance() {
        if(videoApi == null) {
            videoApi = new VideoApi();
        }
        return videoApi;
    }
    /*-------------------------------------获取的方法-------------------------------------*/

    public void getVideoList(Observer<VideoResult> subscriber)
    {
        Observable observable = videoService.getVideoList()
                .map(new HttpResultFunc<VideoResult>());

        toSubscribe(observable, subscriber);
    }


}
