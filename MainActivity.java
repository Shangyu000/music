package com.example.testdemo;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testdemo.adapter.MusicAdapter;
import com.example.testdemo.bean.MusicBean;
import com.example.testdemo.model.MusicModel;
import com.example.testdemo.myutil.HtmlStringUtil;
import com.example.testdemo.presenter.MusicPresenter;
import com.example.testdemo.service.MusicService;
import com.example.testdemo.myutil.ImmersiveStatusBarUtil;
import com.example.testdemo.myutil.PermissionUtil;
import com.example.testdemo.view.BaseActivity;
import com.example.testdemo.view.LocalMusicView;
import com.example.testdemo.view.SongLrcActivity;

import java.util.List;

public class MainActivity extends BaseActivity<MusicPresenter, LocalMusicView> implements LocalMusicView {

    private static final String TAG = "MainActivity";
    //全面屏手势切换
    private ConstraintLayout mUIRootLayout,mLayoutBottom;
    private RecyclerView mMusicRv;
    private MusicAdapter mMusicAdapter;
    private MusicModel mMusicModel;
    //UI控件
    private TextView tv_song;
    private ImageView iv_album,iv_play_toggle,iv_list;
    private MyViewClickListener myClickListener;
    //绑定音乐服务
    private MyCom mMyCom;
    private Intent mServiceIntent;
    private MusicService mMusicService;

//    private FragmentPlayCall mPlayCallBack;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        mUIRootLayout = findViewById(R.id.music_activity_layout_ui_root);
        mUIRootLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                v.setPadding(v.getPaddingRight(),v.getPaddingTop(),v.getPaddingLeft(),insets.getSystemWindowInsetBottom());
                return insets;
            }
        });
        initUI();

    }

    @Override
    protected void onStart() {
        super.onStart();
        this.startService(mServiceIntent);
        this.bindService(mServiceIntent,mMyCom, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unbindService(mMyCom);
    }

    @Override
    protected void onDestroy() {  //清空引用变量
        super.onDestroy();
        this.stopService(mServiceIntent);
        releaseInterface();
        releaseUI();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void receiveNextPlay(String Action) {
        if (mMusicService == null) return;
        switch (Action) {
            case "play/pause":
                clickBottomPlay();
                if(!mMusicService.isFirstPlay() && mMusicService.getCurrentPosition() > 0){
                    Log.d(TAG, "receiveNextPlay: 更新通知栏");
                    mMusicService.UpdateNotification(true);
                }
                break;
            case "next":
            case "previous":
                if (!mMusicService.isOnCompleted())
                    mMusicService.OnNextPlay(Action);
                else mMusicService.setOnCompleted(false);
                break;
            case "love":
                break;
            case "close":
                mMusicService.OnCloseNotification();
                iv_play_toggle.setImageResource(R.drawable.iv_main_play);
                break;
            case "update":
                if (mMusicService.isPlaying()) iv_play_toggle.setImageResource(R.drawable.iv_main_pause);
                else iv_play_toggle.setImageResource(R.drawable.iv_main_play);

                tv_song.setText(HtmlStringUtil.SongSingerName(mMusicService.getCurrentTitle(),mMusicService.getCurrentArtist()));

                iv_album.setImageBitmap(null);
                iv_album.setImageBitmap(mMusicService.getCurrentBitmap());
                break;
        }
    }

    @Override
    protected MusicPresenter createPresenter() {
        return new MusicPresenter();
    }


    @Override
    public void showErrorMessage(String msg) {
        Log.d(TAG, "showErrorMessage: " + msg);
    }

    @Override
    public void showLocalMusic(List<MusicBean> beans) {
        if(beans != null ){
            if (beans.size() > 0) {
                mMusicService.setMusicBeanList(beans);
            }
            UpdateMusicAdapter(beans);
        }
    }
//
//    @Override
//    public void showAlbumBitmap(Bitmap bitmap) {
//        if(bitmap == null) {
//            iv_album.setImageResource(R.drawable.cover_default);
//            Log.d(TAG, "专辑图片不显示.................");
//        }
//        else {
//            iv_album.setImageBitmap(null);
//            iv_album.setImageBitmap(bitmap);
//            Log.d(TAG, "专辑图片显示.................");
//        }
//    }

    @Override
    protected void init() {  //先于onCreate()执行

        ImmersiveStatusBarUtil.transparentBar(this,true);

        //初始化绑定音乐服务
        mServiceIntent = new Intent(this,MusicService.class);
        mMyCom = new MyCom();
//        mPlayCallBack = new FragmentPlayCallBack();
        //初始化点击事件
        myClickListener = new MyViewClickListener();
    }

    private void initUI() {
        mLayoutBottom =findViewById(R.id.music_activity_bottom_layout);
        mMusicRv = findViewById(R.id.music_activity_rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        mMusicRv.setLayoutManager(layoutManager);

        tv_song = findViewById(R.id.music_activity_bottom_tv_song);
        iv_album = findViewById(R.id.music_activity_bottom_iv_album);
        iv_play_toggle = findViewById(R.id.music_activity_bottom_iv_play);
        iv_list = findViewById(R.id.music_activity_bottom_iv_list);

        //设置监听
        mLayoutBottom.setOnClickListener(myClickListener);
        iv_play_toggle.setOnClickListener(myClickListener);

    }

    private class MyCom implements ServiceConnection{

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyMusicBinder binder = (MusicService.MyMusicBinder) service;
            mMusicService= binder.getMusicService();

            SyncMusicInformation(); //同步音乐信息
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("TAG", "onServiceDisconnected...");
            if (mMusicService != null) mMusicService = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void SyncMusicInformation() {
        presenter.fetch();//查询本地音乐数据，显示音乐列表
        tv_song.setText(HtmlStringUtil.SongSingerName(mMusicService.getCurrentTitle(),mMusicService.getCurrentArtist()));
        if (mMusicService.isPlaying()) iv_play_toggle.setImageResource(R.drawable.iv_main_pause);
        else iv_play_toggle.setImageResource(R.drawable.iv_main_play);
        iv_album.setImageBitmap(null);
        iv_album.setImageBitmap(mMusicService.getCurrentBitmap());
    }


    private void UpdateMusicAdapter(List<MusicBean> beans) {
        Log.d("TAG", "UpdateMusicAdapter: "+beans.size());
        if (mMusicAdapter != null) {
            mMusicAdapter = null;
            mMusicAdapter = new MusicAdapter(beans,this);
        }else mMusicAdapter = new MusicAdapter(beans,this);
        mMusicRv.setAdapter(mMusicAdapter);
        mMusicAdapter.notifyDataSetChanged();
        setEventListener(beans);
    }

    private void setEventListener(List<MusicBean> beans) {   //响应点击事件
        if(beans == null || beans.size() <= 0) return;
        mMusicAdapter.setItemClickListener(new MusicAdapter.ItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void ItemClickListener(View v, int position) {
                //列表点击事件
                Log.d("TAG", "点击了列表" + position);
                if(mMusicService == null) return;
                MusicBean bean = beans.get(position);

                if((mMusicService.getCurrentTitle() + mMusicService.getCurrentArtist() + mMusicService.getCurrentAlbum()).equals(bean.getTitle()+bean.getArtist()+bean.getAlbum())){
                    sendBroadcast(new Intent("play/pause"));
//                    if(!mMusicService.isPlaying()) {
//                        mMusicService.OnContinueOrPausePlay(); //接着上次播放
//                        iv_play_toggle.setImageResource(R.drawable.iv_main_pause);
//                    }
//                    else {
//                        mMusicService.OnPause();
//                        iv_play_toggle.setImageResource(R.drawable.iv_main_play);
//                    }
                }else {
                    mMusicService.setNotification(bean);
                    mMusicService.OnPlay(bean.getPath());
//                    iv_play_toggle.setImageResource(R.drawable.iv_main_pause);
                }
//                //显示音乐信息
//                presenter.fetch(bean.getPath()); //专辑图片
//                tv_song.setText(HtmlStringUtil.SongSingerName(bean.getTitle(),bean.getArtist()));

            }

            @Override
            public void ItemViewClickListener(View v, int position) {
                //图标点击事件
                Log.d("TAG", "点击了图标 " + position);
            }
        });
    }

//    private class FragmentPlayCallBack implements BaseFragment.OnClickMusicItemListener{
//        @Override
//        public void onClick(MusicBean bean){
//            if(mMusicService == null) return;
//            mMusicService.OnPlay(bean.getPath());
//        }
//    }

    private class MyViewClickListener implements View.OnClickListener{

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View v) {
            if(mMusicService == null) return;
            switch (v.getId()){
                case R.id.music_activity_bottom_iv_play:
                    sendBroadcast(new Intent("play/pause"));
//                   if(mMusicService.isPlaying()){
//                       mMusicService.OnPause();
//                       iv_play_toggle.setImageResource(R.drawable.iv_main_play);
//                   }else {
//                       if(mMusicService.isFirstPlay()){
//                           Toast.makeText(MainActivity.this,"请点击音乐列表",Toast.LENGTH_SHORT).show();
//                       }else {
//                           mMusicService.OnContinuePlay();
//                           iv_play_toggle.setImageResource(R.drawable.iv_main_pause);
//                       }
//                   }
                    break;
                case R.id.music_activity_bottom_layout:
                    Intent intent = new Intent(MainActivity.this, SongLrcActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void clickBottomPlay(){
        if (mMusicService.isPlaying()) {
            mMusicService.OnPause();
            iv_play_toggle.setImageResource(R.drawable.iv_main_play);
        }else {
            if (mMusicService.isFirstPlay() && mMusicService.getCurrentPosition() > 0) {
                //接着上次播放
                mMusicService.OnContinueOrPausePlay();//【播放可能1】从上次关闭APP前的音乐播放位置继续播放
                iv_play_toggle.setImageResource(R.drawable.iv_pause);

            }else if (mMusicService.isFirstPlay() && mMusicService.getCurrentPosition() == 0) {
                Log.d(TAG, "onClick: 安装App之后的第一次播放");
                Toast.makeText(MainActivity.this,"请点击音乐列表播放音乐",Toast.LENGTH_SHORT).show();
            } else {//暂停后播放
                mMusicService.OnContinueOrPausePlay();
                iv_play_toggle.setImageResource(R.drawable.iv_pause);
            }
        }
    }

    private void releaseInterface() {
        if(mMyCom != null) mMyCom = null;
        if(mServiceIntent != null) mServiceIntent =null;
        if(mMusicService != null) mMusicService =null;
        if(mMusicAdapter != null) mMusicAdapter =null;
//        if(mPlayCallBack != null) mPlayCallBack =null;
        if(myClickListener != null) myClickListener =null;
    }

    private void releaseUI() {
        if(tv_song != null) tv_song = null;
        if(iv_list != null) iv_list =null;
        if(iv_play_toggle != null) iv_play_toggle =null;
        if(iv_album != null) {
            iv_album.setImageBitmap(null);
            iv_album =null;
        }
//        if(mPlayCallBack != null) mPlayCallBack =null;
        if(mMusicRv != null) {
            mMusicRv.setAdapter(null);
            mMusicRv =null;
        }
        if(mUIRootLayout != null) mUIRootLayout = null;
    }

}
