package com.example.testdemo.presenter;

import android.content.Context;


import com.example.testdemo.view.BaseView;

import java.lang.ref.WeakReference;

/**
 * @author : 12453
 * @since : 2021/3/8
 * 作用:
 */
public class BasePresenter<T extends BaseView> {

    protected WeakReference<T> mLocalMusicView;
    protected Context context;

    /**
     * 注释： 初始化presenter层
     * @param view BaseView的子类泛型
     * return null
     * @author 12453
     * time 2021/3/8 16:58
     **/
    public void AttachView(T view,Context context){
        mLocalMusicView = new WeakReference<>(view);
        this.context = context.getApplicationContext();
    }

    public void DetachView(){
        if (mLocalMusicView != null) {
            mLocalMusicView.clear();
            mLocalMusicView = null;
        }
        if (context != null) context = null;
    }
}
