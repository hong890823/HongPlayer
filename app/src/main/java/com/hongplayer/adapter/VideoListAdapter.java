package com.hongplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hongplayer.R;
import com.hongplayer.bean.idataapi.BiliLiveData;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class VideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private List<BiliLiveData> datas;
    private OnItemClickListener onItemClickListener;

    public VideoListAdapter(Context context, List<BiliLiveData> datas) {
        this.context = context;
        this.datas = datas;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_list_adapter, parent, false);
        MyHolder holder = new MyHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyHolder myHolder = (MyHolder) holder;
        if(position % 2 == 1) {
            myHolder.vLeft.setVisibility(View.GONE);
            myHolder.vRight.setVisibility(View.VISIBLE);
        } else {
            myHolder.vLeft.setVisibility(View.VISIBLE);
            myHolder.vRight.setVisibility(View.GONE);
        }
        myHolder.vButtom.setVisibility(View.GONE);
        if(datas.size() % 2 == 0) {
            if(position == datas.size() - 1 || position == datas.size() - 2)
            {
                myHolder.vButtom.setVisibility(View.VISIBLE);
            }
        } else if(datas.size() % 2 == 1) {
            if(position == datas.size() - 1) {
                myHolder.vButtom.setVisibility(View.VISIBLE);
            }
        }

        final BiliLiveData biliLiveData = datas.get(position);
        myHolder.tvName.setText(biliLiveData.getTitle());
        Glide.with(context).load(biliLiveData.getCoverUrl()).into(myHolder.ivBg);
        myHolder.rlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null) {
                    onItemClickListener.onItemClick(biliLiveData);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.v_left)
        View vLeft;
        @BindView(R.id.v_right)
        View vRight;
        @BindView(R.id.iv_img)
        ImageView ivBg;
        @BindView(R.id.v_buttom)
        View vButtom;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.rl_item)
        RelativeLayout rlItem;

        public MyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(BiliLiveData biliLiveData);
    }

}
