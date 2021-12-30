package com.example.testdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleEventObserver;

/*
* 提取所有service共有的方法或对象
* */
public abstract class BaseService extends Service implements LifecycleEventObserver {

    protected static final String PLAYER_SORT_PLAY = "SORT_PLAY";
    protected static final String PLAYER_RANDOM_PLAY = "RANDOM_PLAY";
    protected static final String PLAYER_REPEAT_PLAY = "REPEAT_PLAY";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    protected  void init(){}

}
