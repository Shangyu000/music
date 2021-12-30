package com.example.testdemo.model;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.testdemo.bean.MusicBean;

import java.util.List;

/**
 * 作用:访问本机外部存储的音乐文件
 * 网络、数据源、数据库、server、thread。。。
 */
public interface MusicModel {
    void showLocalMusic(OnMusicListener onMusicListener, Context context);
//    void loadAlbumBitmap(OnLoadAlbumBitmapCallBack onAlbumCallBack,String Path);
    interface OnMusicListener{
        void OnComplete(List<MusicBean> beans);
    }
//    interface OnLoadAlbumBitmapCallBack{
//        void onCallBack(Bitmap bitmap);
//    }
}
