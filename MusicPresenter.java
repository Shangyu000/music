package com.example.testdemo.presenter;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.testdemo.bean.MusicBean;
import com.example.testdemo.model.LocalMusicModel;
import com.example.testdemo.model.MusicModel;
import com.example.testdemo.view.LocalMusicView;

import java.util.List;

public class MusicPresenter<T extends LocalMusicView> extends BasePresenter {

    private static final String TAG = MusicPresenter.class.getSimpleName();

    private MusicModel mMusicModel = new LocalMusicModel(); //向上转型

    public void fetch(){
        if (mLocalMusicView != null && mMusicModel != null) {
            mMusicModel.showLocalMusic(new MusicModel.OnMusicListener() {
                @Override
                public void OnComplete(List<MusicBean> beans) {
                    Log.d(TAG, "OnComplete: "+(beans != null));
                    ((T) mLocalMusicView.get()).showLocalMusic(beans);
                }
            },context);
        }
    }
//    public void fetch(String Path){    //重载方法
//        if (mLocalMusicView != null && mMusicModel != null) {
//            mMusicModel.loadAlbumBitmap(new MusicModel.OnLoadAlbumBitmapCallBack() {
//                @Override
//                public void onCallBack(Bitmap bitmap) {
//                    ((T) mLocalMusicView.get()).showAlbumBitmap(bitmap);
//                }
//            }, Path);
//        }
//
//    }
}
