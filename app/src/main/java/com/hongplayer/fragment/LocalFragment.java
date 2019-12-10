package com.hongplayer.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.hongplayer.R;
import com.hongplayer.activity.VideoLiveActivity;
import com.hongplayer.adapter.LocalListAdapter;
import com.hongplayer.base.BaseFragment;
import com.hongplayer.bean.LocalListBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class LocalFragment extends BaseFragment{

    private LocalListAdapter videoListAdapter;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    private List<LocalListBean> datas;
    private int count = 0;
    private List<String> paths;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_local_layout);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle("wlPlayer");
        datas = new ArrayList<>();
        paths = new ArrayList<>();

        videoListAdapter = new LocalListAdapter(getActivity(), datas);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoListAdapter);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED//读取存储卡权限
                    || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x1001);
            } else {
                initData();
            }
        } else {
            initData();
        }

        videoListAdapter.setOnItemClickListener(new LocalListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LocalListBean localListBean) {
                if(localListBean != null) {
                    if(!localListBean.isFile()) {
                        List<LocalListBean> d = getDirFiles(localListBean.getPath());
                        if(d.size() > 0)
                        {
                            paths.add(localListBean.getParent());
                            count++;
                            datas.clear();
                            datas.addAll(d);
                            videoListAdapter.notifyDataSetChanged();
                            System.out.println("count:" + count);
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "没有下级目录了", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Bundle bundle = new Bundle();
                        bundle.putString("url", localListBean.getPath());
                        VideoLiveActivity.startActivity(getActivity(), VideoLiveActivity.class, bundle);
                    }

                }
            }
        });
    }

    private void initData() {
        datas.clear();
        paths.clear();
        File file = Environment.getExternalStorageDirectory().getAbsoluteFile();
        paths.add(file.getAbsolutePath());
        File[] files = file.listFiles();
        for(int i = 0; i < files.length; i++) {
            LocalListBean videoListBean = new LocalListBean();
            videoListBean.setParent(file.getAbsolutePath());
            videoListBean.setName(files[i].getName());
            videoListBean.setPath(files[i].getAbsolutePath());
            videoListBean.setFile(files[i].isFile());
            datas.add(videoListBean);
        }

        LocalListBean bean = new LocalListBean();
        bean.setName("Test");
        bean.setPath(Environment.getExternalStorageDirectory()+"/test.mkv");
        bean.setFile(true);
        datas.add(bean);

        LocalListBean bean1 = new LocalListBean();
        bean.setName("Test1");
        bean.setPath(Environment.getExternalStorageDirectory()+"/test1.mkv");
        bean.setFile(true);
        datas.add(bean1);

        videoListAdapter.notifyDataSetChanged();
    }

    private List<LocalListBean> getDirFiles(String path) {
        List<LocalListBean> videos = new ArrayList<>();
        File file = new File(path);
        if(file.exists()) {
            File[] files = file.listFiles();
            if(files != null && files.length > 0) {
                for(int i = 0; i < files.length; i++) {
                    LocalListBean videoListBean = new LocalListBean();
                    videoListBean.setFile(!files[i].isDirectory());
                    videoListBean.setPath(files[i].getAbsolutePath());
                    videoListBean.setName(files[i].getName());
                    videoListBean.setParent(path);
                    videos.add(videoListBean);
                }

            }
        }
        return videos;
    }

    public int backDir() {
        if(count > 0) {
            count--;
            if(count != 0) {
                List<LocalListBean> d = getDirFiles(paths.get(paths.size() - 1));
                datas.clear();
                datas.addAll(d);
                paths.remove(paths.get(paths.size() - 1));
                videoListAdapter.notifyDataSetChanged();
            } else {
                initData();
            }
            return 0;
        } else {
            getActivity().finish();
            return 1;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initData();
        } else {
            Toast.makeText(getActivity(), "请允许读取存储卡权限", Toast.LENGTH_SHORT).show();
        }
    }

}
