package com.hongplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hongplayer.R;
import com.hongplayer.bean.LocalListBean;

import java.util.List;

public class LocalListAdapter extends RecyclerView.Adapter<LocalListAdapter.TypeHolder>{
    private Context context;
    private List<LocalListBean> datas;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public LocalListAdapter(Context context, List<LocalListBean> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public TypeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_local_list_layout, parent, false);
        TypeHolder holder = new TypeHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(TypeHolder holder, final int position) {
        holder.tvName.setText(datas.get(position).getName());
        holder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null)
                {
                    onItemClickListener.onItemClick(datas.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public class TypeHolder extends RecyclerView.ViewHolder
    {
        private TextView tvName;
        public TypeHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }

    public interface OnItemClickListener
    {
        void onItemClick(LocalListBean localListBean);
    }
}
