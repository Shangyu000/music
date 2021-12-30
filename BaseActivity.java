package com.example.testdemo.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testdemo.presenter.BasePresenter;

/**
 * 作用:
 */
public abstract class BaseActivity<T extends BasePresenter,V extends BaseView> extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    protected T presenter;
    private myBroadcastReceiver mBroadcastReceiver;
    private IntentFilter filter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = createPresenter(); //模板模式
        presenter.AttachView((V)this,this);

        initProtectObject();//私有
        init();
        registerSDK();
    }

    protected abstract T createPresenter();

    protected void init(){}
    protected void receiveNextPlay(String Action){}

    protected void registerSDK(){}
    protected void unRegisterSDK(){}

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterSDK();
        //置空Presenter层引用
        presenter.DetachView();
        presenter = null;
        releaseProtectObject();
    }
    private void initProtectObject(){
        //初始化广播接收器
        filter = new IntentFilter();
        filter.addAction("play/pause");
        filter.addAction("next");
        filter.addAction("previous");
        filter.addAction("love");
        filter.addAction("close");
        filter.addAction("update");
        filter.addAction("error");
        mBroadcastReceiver = new myBroadcastReceiver();
    }
    private void releaseProtectObject(){
        if (filter != null) filter = null;
        if (mBroadcastReceiver != null) mBroadcastReceiver = null;
    }
    //接收广播后要进行的动作
    private class myBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: "+intent.getAction());
            if (intent.getAction() == null) return;
            /*switch (intent.getAction()) {
                case "update":
                    break;
                case "previous":
                case "next":
                    break;
            }*/
            receiveNextPlay(intent.getAction());//"play/pause","next","previous"
        }
    }//class myBroadcastReceiver end
}
