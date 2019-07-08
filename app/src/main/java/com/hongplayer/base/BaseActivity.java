package com.hongplayer.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hongplayer.R;
import com.hongplayer.util.CommonUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseActivity extends AppCompatActivity {

    @Nullable
    @BindView(R.id.title_tv)
    TextView titleTv;
    @Nullable
    @BindView(R.id.system_bar_layout)
    View systemBarLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
        //透明状态栏
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4全透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        if(systemBarLayout != null) {
            initSystemBar(systemBarLayout);
        }
    }

    public void initSystemBar(View systemBar) {
        if (Build.VERSION.SDK_INT >= 19) {
            if (systemBar != null) {
                systemBar.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) systemBar.getLayoutParams();
                lp.height = CommonUtil.getStatusHeight(this);
                systemBar.requestLayout();
            }
        } else {
            if (systemBar != null) {
                systemBar.setVisibility(View.GONE);
            }
        }
    }

    public void setTitle(String title) {
        if(titleTv != null) {
            titleTv.setText(title);
        }
    }

    public void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public static void startActivity(Context context, Class clz){
        Intent intent = new Intent(context, clz);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    public static void startActivity(Context context, Class clz, Bundle bundle){
        Intent intent = new Intent(context, clz);
        intent.putExtras(bundle);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

}
