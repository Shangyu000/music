package com.example.testdemo.view;

import android.graphics.Bitmap;

import com.example.testdemo.bean.MusicBean;

import java.util.List;

/**
 * @author : 12453
 * @since : 2021/3/8
 * 作用:
 */
public interface LocalMusicView extends BaseView{
    void showLocalMusic(List<MusicBean> beans);
    //数据
//    void showAlbumBitmap(Bitmap bitmap);
//    //数据
}

