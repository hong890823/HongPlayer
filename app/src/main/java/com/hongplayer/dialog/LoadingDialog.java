package com.hongplayer.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import com.hongplayer.R;
import com.hongplayer.base.BaseDialog;

import butterknife.BindView;

public class LoadingDialog extends BaseDialog {

    @BindView(R.id.msg_tv)
    TextView msgTv;

    public LoadingDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
    }

    public void setMsg(String msg) {
        if(msgTv != null && !TextUtils.isEmpty(msg)) {
            msgTv.setText(msg);
        }
    }

}
