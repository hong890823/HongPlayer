package com.hongplayer.httpservice.serviceapi;


import com.hongplayer.httpservice.service.HttpMethod;
import com.hongplayer.httpservice.service.VideoBaseApi;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * Created by ywl on 2016/5/19.
 */
public class VideoApi extends VideoBaseApi {

    public static final String BASE_URL = "https://api.apiopen.top/";
    public static VideoApi videoApi;
    public VideoService videoService;
    public VideoApi() {
        videoService = HttpMethod.getInstance(BASE_URL).createApi(VideoService.class);
    }

    public static VideoApi getInstance() {
        if(videoApi == null) {
            videoApi = new VideoApi();
        }
        return videoApi;
    }
    /*-------------------------------------获取的方法-------------------------------------*/

    public void getVideoList(Observer<VideoRootBean> subscriber)
    {
        Observable observable = videoService.getVideoList()
                .map(new HttpResultFunc<VideoRootBean>());

        toSubscribe(observable, subscriber);
    }


}
