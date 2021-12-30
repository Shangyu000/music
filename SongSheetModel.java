package com.example.testdemo.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import com.example.testdemo.bean.MusicBean;

import java.util.List;

/**
 * Author: 12453
 * Date: 2021/3/8
 * 作用: 获取数据的主入口，持有此对象才能获得接口回调的数据
 */
public interface SongSheetModel {
    void showLocalMusic(OnLoadMusicListener onLoadListener, Context context);
    void loadAlbumBitmap(OnLoadAlbumBitmapCallBack onAlbumCallBack, String Path);
    void showLocalSheet(OnLocalSheetListener onLocalSheetListener, SQLiteDatabase database);
    interface OnLoadMusicListener{
        void onComplete(List<MusicBean> beans);
    }
    interface OnLoadAlbumBitmapCallBack{
        void onCallBack(Bitmap bitmap);
    }
    interface OnLocalSheetListener{
//        void onComplete(List<SongSheetBean> bean);
        void onError(String msg);
    }
}
