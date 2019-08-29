package com.hongplayer.httpservice.serviceapi;


import com.hongplayer.bean.apiopen.VideoResult;
import com.hongplayer.bean.idataapi.BiliLiveData;
import com.hongplayer.httpservice.httpentity.BiliHttpResult;
import com.hongplayer.httpservice.service.HttpMethod;
import com.hongplayer.httpservice.service.VideoBaseApi;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * Created by ywl on 2016/5/19.
 */
public class VideoApi extends VideoBaseApi {

    private static final String API_OPEN_BASE_URL = "https://api.apiopen.top/";
    private static final String IDATA_API_BASE_URL = "http://api01.idataapi.cn:8000/";
    private static String IDATA_API_KEY = "Hfve6LtKMhkCZU1jpNr3vSoTJW69MMppS4G2SEZH1Emd9O0Rn420enjgqR7frFB8";

    private static String LOL = "lol";
    private static String PUBG ="pubg";

    private static VideoApi videoApi;
    private VideoService videoService;
    private BilibiliService bilibiliService;

    public VideoApi() {
        videoService = HttpMethod.getInstance(API_OPEN_BASE_URL).createApi(VideoService.class);
        bilibiliService = HttpMethod.getInstance(IDATA_API_BASE_URL).createApi(BilibiliService.class);
    }

    public static VideoApi getInstance() {
        if(videoApi == null) {
            videoApi = new VideoApi();
        }
        return videoApi;
    }
    /*-------------------------------------获取的方法-------------------------------------*/

    public void getVideoList(Observer<List<VideoResult>> subscriber){
        Observable<List<VideoResult>> observable = videoService.getVideoList()
                .map(new HttpResultFunc<VideoResult>());
        toSubscribe(observable, subscriber);
    }

    public void getLolLiveList(Observer<BiliHttpResult> subscriber, int pageToken){
        Observable observable = bilibiliService.getLiveList(IDATA_API_KEY,LOL,pageToken);
        toSubscribe(observable,subscriber);

    }

    public void getPubgLiveList(Observer<BiliHttpResult> subscriber,int pageToken){
        Observable observable = bilibiliService.getLiveList(IDATA_API_KEY,PUBG,pageToken);
        toSubscribe(observable,subscriber);
    }

}
