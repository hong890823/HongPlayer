package com.hongplayer.base;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hongplayer.R;
import com.hongplayer.dialog.LoadingDialog;
import com.hongplayer.util.CommonUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseFragment extends Fragment{
    public View contentView;
    protected int layoutResId;
    private LoadingDialog loadingDialog;

    @Nullable
    @BindView(R.id.title_tv)
    TextView titleTv;
    @Nullable
    @BindView(R.id.system_bar_layout)
    View systemBarLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(layoutResId, container, false);
        ButterKnife.bind(this, contentView);
        if(systemBarLayout != null) {
            initSystemBar(systemBarLayout);
        }
        return contentView;
    }

    public void initSystemBar(View systemBar) {
        if (Build.VERSION.SDK_INT >= 19) {
            if (systemBar != null) {
                systemBar.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) systemBar.getLayoutParams();
                lp.height = CommonUtil.getStatusHeight(getActivity());
                systemBar.requestLayout();
            }
        } else {
            if (systemBar != null) {
                systemBar.setVisibility(View.GONE);
            }
        }
    }

    public void setContentView(int layoutResId) {
        this.layoutResId = layoutResId;
    }


    public void setTitle(String title) {
        if (titleTv != null && !TextUtils.isEmpty(title)) {
            titleTv.setText(title);
        }
    }

    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    public void showLoadDialog(String msg) {
        if(loadingDialog == null) {
            loadingDialog = new LoadingDialog(getActivity());
        }
        loadingDialog.show();
        loadingDialog.setMsg(msg);
    }

    public void hideLoadDialog(){
        if(loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

}
