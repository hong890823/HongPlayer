package com.hongplayer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hongplayer.R;
import com.hongplayer.activity.VideoLiveActivity;
import com.hongplayer.adapter.VideoListAdapter;
import com.hongplayer.adapter.WapHeaderAndFooterAdapter;
import com.hongplayer.base.BaseFragment;
import com.hongplayer.bean.apiopen.VideoContent;
import com.hongplayer.bean.apiopen.VideoContentData;
import com.hongplayer.bean.apiopen.VideoCover;
import com.hongplayer.bean.apiopen.VideoData;
import com.hongplayer.bean.apiopen.VideoResult;
import com.hongplayer.bean.idataapi.BiliLiveData;
import com.hongplayer.httpservice.httpentity.BiliHttpResult;
import com.hongplayer.httpservice.serviceapi.VideoApi;
import com.hongplayer.log.MyLog;
import com.hongplayer.subscriber.HttpSubscriber;
import com.hongplayer.subscriber.SubscriberOnListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 视频源来源：https://www.jianshu.com/p/e6f072839282
 * */
public class VideoFragment extends BaseFragment{

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private TextView loadMsgTv;

    private VideoListAdapter videoListAdapter;
    private WapHeaderAndFooterAdapter headerAndFooterAdapter;
    private List<VideoResult> datas;

    private int currentPage = 1;
    private boolean isLoad = true;
    private boolean isOver = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
        getVideoList();
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.color_ec4c48));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isLoad){
                    isLoad = true;
                    currentPage = 1;
                    getVideoList();
                }
            }
        });
    }

    private void initAdapter() {
        datas = new ArrayList<>();
        videoListAdapter = new VideoListAdapter(getActivity(), datas);
        headerAndFooterAdapter = new WapHeaderAndFooterAdapter(videoListAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);

        View footerView = LayoutInflater.from(getActivity()).inflate(R.layout.footer_layout, recyclerView, false);
        loadMsgTv = footerView.findViewById(R.id.tv_loadmsg);
        headerAndFooterAdapter.addFooter(footerView);

        recyclerView.setAdapter(headerAndFooterAdapter);

        videoListAdapter.setOnItemClickListener(new VideoListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String playUrl) {
                showLoadDialog("加载中");
                Bundle bundle = new Bundle();
                bundle.putString("url", playUrl);
                VideoLiveActivity.startActivity(getActivity(), VideoLiveActivity.class, bundle);
                hideLoadDialog();
            }
        });

        headerAndFooterAdapter.setOnloadMoreListener(recyclerView, new WapHeaderAndFooterAdapter.OnLoadMoreListener(){

            @Override
            public void onLoadMore() {
                if(isOver) {
                    return;
                }
                if(!isLoad) {
                    isLoad = true;
                    getVideoList();
                }
            }
        });
    }

    private void getVideoList() {
        VideoApi.getInstance().getVideoList(new HttpSubscriber<List<VideoResult>>(new SubscriberOnListener<List<VideoResult>>() {
            @Override
            public void onSucceed(List<VideoResult> data) {
                if(currentPage == 1) {
                    datas.clear();
                }
                if(data==null || data.size() == 0) {
                    loadMsgTv.setText("没有更多了");
                    isOver = true;
                } else {
                    loadMsgTv.setText("加载更多");
                    isOver = false;
                    currentPage++;
                }
                datas.addAll(selectVideoData(data));
                headerAndFooterAdapter.notifyDataSetChanged();
                isLoad = false;
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(int code, String msg) {
                Toast.makeText(getContext(), "msg", Toast.LENGTH_SHORT).show();
            }
        },getActivity()));
    }

    private List<VideoResult> selectVideoData(List<VideoResult> source){
        List<VideoResult> videoResultList = new ArrayList<>();
        if(source!=null){
            for(VideoResult videoResult:source){
                VideoData videoData = videoResult.getData();
                if(videoData!=null){
                    VideoContent videoContent = videoData.getContent();
                    if(videoContent!=null){
                        VideoContentData videoContentData = videoContent.getData();
                        if(videoContentData!=null){
                            String playUrl = videoContentData.getPlayUrl();
                            if(playUrl!=null){
                                videoResultList.add(videoResult);
                            }
                        }
                    }
                }
            }
        }
        return videoResultList;
    }


}
