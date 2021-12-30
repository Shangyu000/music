package com.example.testdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testdemo.R;
import com.example.testdemo.bean.MusicBean;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private List<MusicBean> beans;
    private Context context;
    //点击事件接口
    private ItemClickListener mItemClickListener;

    public interface ItemClickListener{
        void ItemClickListener(View v, int position);
        void ItemViewClickListener(View v,int position);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public MusicAdapter(List<MusicBean> beans, Context context) {
        this.beans = beans;
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music_list, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicBean bean = beans.get(position);
        holder.idTv.setText(bean.getId());
        holder.titleTv.setText(bean.getTitle());
        holder.singerMoreTv.setText(bean.getArtist()+" - "+bean.getAlbum());

        holder.mLayout.setOnClickListener(new View.OnClickListener() {   //列表点击事件
            @Override
            public void onClick(View v) {
                mItemClickListener.ItemClickListener(v,position);
            }
        });

        holder.moreIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.ItemViewClickListener(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (beans != null && beans.size() > 0) return beans.size();
        return 0;
    }

    class MusicViewHolder extends RecyclerView.ViewHolder{    //绑定控件
        private TextView idTv,titleTv,singerMoreTv;
        private RelativeLayout mLayout;
        private ImageView moreIv;
        private MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            idTv = itemView.findViewById(R.id.item_local_music_number);
            titleTv = itemView.findViewById(R.id.item_local_music_song);
            singerMoreTv = itemView.findViewById(R.id.item_local_music_singer_and_album);

            moreIv = itemView.findViewById(R.id.item_local_music_more);
            mLayout = itemView.findViewById(R.id.item_music_list_layout);
        }
    }
}
