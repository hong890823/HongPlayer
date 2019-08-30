package com.hongplayer.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import com.hongplayer.R;
import com.hongplayer.adapter.ViewPagerAdapter;
import com.hongplayer.base.BaseActivity;
import com.hongplayer.fragment.AboutFragment;
import com.hongplayer.fragment.LocalFragment;
import com.hongplayer.fragment.RadioFragment;
import com.hongplayer.fragment.LiveFragment;
import com.hongplayer.fragment.VideoFragment;
import com.hongplayer.widget.TabLayoutView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.nav_tab)
    TabLayoutView tabLayoutView;

    private ViewPagerAdapter viewPagerAdapter;

    private VideoFragment videoFragment;
    private LiveFragment liveFragment;
    private RadioFragment radioFragment;
    private LocalFragment localFragment;
    private AboutFragment aboutFragment;
    private List<Fragment> fragmentPages;
    private int currentIndex = -1;

    private String[] titles = {"视频", "直播","广播", "本地", "关于"};
    private int[] imgs = {R.drawable.nav_video_selector, R.drawable.nav_video_selector,R.drawable.nav_audio_selector, R.drawable.nav_file_selector, R.drawable.nav_about_selector};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("视频");
        initFragments();
        initTabLayoutView();
    }

    private void initTabLayoutView(){
        tabLayoutView.setDataSource(titles, imgs, 0);
        tabLayoutView.setImageStyle(24, 24);
        tabLayoutView.setTextStyle(10, R.color.color_333333,R.color.color_ec4c48);
        tabLayoutView.initDatas();
        if(checkDeviceHasNavigationBar(this)){
            RelativeLayout.LayoutParams tabParams = (RelativeLayout.LayoutParams) tabLayoutView.getLayoutParams();
            tabParams.bottomMargin = getNavigationBarHeight(this);
            tabLayoutView.requestLayout();
        }
        tabLayoutView.setOnTabClickListener(new TabLayoutView.OnTabClickListener() {
            @Override
            public void onTabClick(int index) {
                viewPager.setCurrentItem(index);
            }
        });

    }

    private void initFragments() {
        fragmentPages = new ArrayList<>();
        videoFragment = new VideoFragment();
        liveFragment = new LiveFragment();
        radioFragment = new RadioFragment();
        localFragment = new LocalFragment();
        aboutFragment = new AboutFragment();

        fragmentPages.add(videoFragment);
        fragmentPages.add(liveFragment);
        fragmentPages.add(radioFragment);
        fragmentPages.add(localFragment);
        fragmentPages.add(aboutFragment);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentPages);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position, false);
                tabLayoutView.setSelectStyle(position);
                currentIndex = position;
                if(position == 0) {
                    setTitle("视频");
                }
                else if(position == 1){
                    setTitle("直播");
                }
                else if(position == 2) {
                    setTitle("广播");
                }
                else if(position == 3) {
                    setTitle("本地");
                }
                else if(position == 4) {
                    setTitle("关于");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        initViewPagerTransformer(viewPager);
    }

    private void initViewPagerTransformer(ViewPager viewPager){
        viewPager.setPageTransformer(false,new PageTransformer(){
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setPivotY(page.getHeight()/2);
                float maxRotate = 35f;
                float minScale = 0.8f;
                float maxTranslationX = page.getWidth()/5;
                if (position <= -1)
                { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    page.setRotationY(maxRotate);
                    page.setPivotX(0);
                    page.setScaleX(minScale);
                    page.setScaleY(minScale);
                    page.setTranslationX(maxTranslationX);
                } else if (position < 1)
                { // [-1,1]
                    page.setRotationY(-position * maxRotate);
                    if (position < 0)//[0,-1]
                    {
                        page.setPivotX(0);
                        page.setScaleX(1 + position * (1-minScale));
                        page.setScaleY(1 + position * (1-minScale));
                        page.setTranslationX(-position * maxTranslationX);
                    } else//[1,0]
                    {
                        page.setPivotX(page.getWidth());
                        page.setScaleX(1 - position * (1-minScale));
                        page.setScaleY(1 - position * (1-minScale));
                        page.setTranslationX(-position * maxTranslationX);
                    }
                } else
                { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    page.setRotationY(-1 * maxRotate);
                    page.setPivotX(page.getWidth());
                    page.setScaleX(minScale);
                    page.setScaleY(minScale);
                    page.setTranslationX(-maxTranslationX);
                }
            }
        });
    }

    /**
     * 判断手机是否有虚拟键盘
     * */
    @SuppressLint("NewApi")
    public static boolean checkDeviceHasNavigationBar(Context activity) {
        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        boolean hasMenuKey = ViewConfiguration.get(activity)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);
        if (!hasMenuKey && !hasBackKey) {
            // 做任何你需要做的,这个设备有一个导航栏
            return true;
        }
        return false;
    }

    /**
     * 获取虚拟键盘的高度
     * */
    public static int getNavigationBarHeight(Context activity) {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }


}
