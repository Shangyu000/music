package com.example.testdemo.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;

import com.example.testdemo.R;
import com.example.testdemo.bean.MusicBean;
import com.example.testdemo.customview.MarqueeTextView;
import com.example.testdemo.myutil.HtmlStringUtil;
import com.example.testdemo.myutil.ImmersiveStatusBarUtil;
import com.example.testdemo.presenter.MusicPresenter;
import com.example.testdemo.service.MusicService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 作用:
 */
public class SongLrcActivity extends BaseActivity<MusicPresenter, LocalMusicView> implements LocalMusicView {

    private static final String TAG = SongLrcActivity.class.getSimpleName();

    private MarqueeTextView tv_song;
    private TextView tv_singer,tv_seekBar_start,tv_seekBar_end,tv_get_search;
    private ImageView iv_play_toggle,iv_center_album,iv_loading,iv_comment,iv_more,iv_share,
            iv_blur_bg,iv_play_mode,iv_love,iv_yest,iv_download;
    private SeekBar sb_songProgress,sb_volume;
    private Group mLrcGroup;
    private ConstraintLayout csl_center_layout,mLayoutUIRoot;
    //绑定音乐服务
    private MyConn mMyConn;
    private Intent mServiceIntent;
    private MusicService mMusicService;
    //
    private MyViewClickListener mClickListener;
    private SongProgressListener mProgressListener;
    private MusicVolumeListener mVolumeListener;
    private SimpleDateFormat mDateFormat;
    private boolean isSeekBarChanging = false;
    private Timer mTimerProgressAndVolume;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_lrc);
        initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.bindService(mServiceIntent,mMyConn, Context.BIND_AUTO_CREATE);
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
        this.unbindService(mMyConn);
        if (mTimerProgressAndVolume != null) {
            mTimerProgressAndVolume.purge();
            mTimerProgressAndVolume.cancel();
            mTimerProgressAndVolume = null;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseInterface();
        releaseUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {  //动态设置专辑图片大小
        super.onWindowFocusChanged(hasFocus);
        DisplayMetrics dm = new DisplayMetrics();
        Objects.requireNonNull(getWindowManager()).getDefaultDisplay().getMetrics(dm);
        if(iv_center_album.getWidth() < dm.widthPixels * 0.5){
            ViewGroup.LayoutParams params = iv_center_album.getLayoutParams();
            params.width = (int)(dm.widthPixels * 0.5 + 1);
            params.height = (int)(dm.widthPixels * 0.5 + 1);
            iv_center_album.setLayoutParams(params);
        }
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
                iv_play_toggle.setImageResource(R.drawable.iv_play);
                break;
            case "update":
                if (mMusicService.isPlaying()) iv_play_toggle.setImageResource(R.drawable.iv_pause);
                else iv_play_toggle.setImageResource(R.drawable.iv_play);

                tv_song.setText(mMusicService.getCurrentTitle(),getWindowManager());
                tv_singer.setText(mMusicService.getCurrentArtist());

                iv_center_album.setImageBitmap(null);
                iv_center_album.setImageBitmap(mMusicService.getCurrentBitmap());
                if (mMusicService.getDuration() > 0) {
                    sb_songProgress.setMax((int) mMusicService.getDuration());
                    tv_seekBar_end.setText(mDateFormat.format(mMusicService.getDuration()));
                }
                break;
        }
    }

    @Override
    protected MusicPresenter createPresenter() {
        return new MusicPresenter();
    }

    @Override
    public void showLocalMusic(List<MusicBean> beans) {

    }

//    @Override
//    public void showAlbumBitmap(Bitmap bitmap) {
//
//    }

    @Override
    public void showErrorMessage(String msg) {

    }

    @Override
    protected void init() {
        ImmersiveStatusBarUtil.transparentBar(this,true);
        Log.d(TAG, "init: ");
        //初始化绑定音乐服务
        mServiceIntent = new Intent(this,MusicService.class);
        mMyConn = new MyConn();
        //初始化点击事件监听
        mClickListener = new MyViewClickListener();
        mProgressListener = new SongProgressListener();
        mVolumeListener = new MusicVolumeListener();
    }

    private class MyConn implements ServiceConnection {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyMusicBinder binder = (MusicService.MyMusicBinder) service;
            mMusicService = binder.getMusicService();
            mDateFormat = new SimpleDateFormat("mm:ss", Locale.CHINA);
            if (mMusicService != null) SyncMusicInformation();//同步音乐信息
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
            if (mMusicService != null) mMusicService = null;
        }
    }

    /*同步当前播放的音乐信息*/
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void SyncMusicInformation() {
        iv_center_album.setImageBitmap(null);
        iv_center_album.setImageBitmap(mMusicService.getCurrentBitmap());
        tv_song.setText(mMusicService.getCurrentTitle(),getWindowManager());
        tv_singer.setText(mMusicService.getCurrentArtist());
        if (mMusicService.isPlaying()) iv_play_toggle.setImageResource(R.drawable.iv_pause);
        else iv_play_toggle.setImageResource(R.drawable.iv_play);
        if (mMusicService.getPLAYER_PLAY_MODE().equals(mMusicService.getPLAYER_SORT_PLAY())) {
            iv_play_mode.setImageResource(R.drawable.iv_sort_play);
        }else if(mMusicService.getPLAYER_PLAY_MODE().equals(mMusicService.getPLAYER_RANDOM_PLAY())){
            iv_play_mode.setImageResource(R.drawable.iv_random_play);
        } else if(mMusicService.getPLAYER_PLAY_MODE().equals(mMusicService.getPLAYER_REPEAT_PLAY())){
            iv_play_mode.setImageResource(R.drawable.iv_repeat_play);
        }

        //同步音乐播放进度
        UpdateMusicProgressAndVolume();
        if (mMusicService.getCurrentPosition() > 0) {
            //{ mMusicService.getCurrentPosition()}是MusicService的成员变量，可以放心调用
            //如果之前播放过音乐，会保留上一次的音乐进度信息{[long]mMusicService.getCurrentPosition()}
            //加载之前的音乐进度信息至TextView和SeekBar上
            tv_seekBar_start.setText(mDateFormat.format(mMusicService.getCurrentPosition()));
            sb_songProgress.setProgress(mMusicService.getCurrentPosition());//必须要先设置进度条的最大值，否则此行代码不生效
            sb_songProgress.setOnSeekBarChangeListener(mProgressListener);
        }
        UpdateSeekBarProgress();//启动 Timer和TimerTask子线程 根据条件 实时更新 当前音乐进度
        UpdateSeekBarVolume();//启动 TimerTask子线程 根据条件 实时更新 当前系统音量值

    }

    private class MyViewClickListener implements View.OnClickListener{
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View v) {
            if (mMusicService == null) return;
            switch (v.getId()) {
                case R.id.song_lrc_top_return:
                    finish();
                    break;
                case R.id.song_lrc_center_album:
                    if(iv_center_album.getVisibility() == View.VISIBLE) iv_center_album.setVisibility(View.INVISIBLE);
                    if(mLrcGroup.getVisibility() == View.INVISIBLE) mLrcGroup.setVisibility(View.VISIBLE);

                    break;
                case R.id.song_lrc_top_sounds:
                    if(iv_center_album.getVisibility() == View.INVISIBLE) iv_center_album.setVisibility(View.VISIBLE);
                    if(mLrcGroup.getVisibility() == View.VISIBLE) mLrcGroup.setVisibility(View.INVISIBLE);

                    break;
                case R.id.song_lrc_bottom_play:
                    sendBroadcast(new Intent("play/pause"));
                    break;
                case R.id.song_lrc_bottom_play_mode:
                    //切换播放模式
                    if (mMusicService.getPLAYER_PLAY_MODE().equals(mMusicService.getPLAYER_SORT_PLAY())) {
                        mMusicService.setPLAYER_PLAY_MODE(mMusicService.getPLAYER_RANDOM_PLAY());
                        iv_play_mode.setImageResource(R.drawable.iv_random_play);
                    }else if(mMusicService.getPLAYER_PLAY_MODE().equals(mMusicService.getPLAYER_RANDOM_PLAY())){
                        mMusicService.setPLAYER_PLAY_MODE(mMusicService.getPLAYER_REPEAT_PLAY());
                        iv_play_mode.setImageResource(R.drawable.iv_repeat_play);
                    } else if(mMusicService.getPLAYER_PLAY_MODE().equals(mMusicService.getPLAYER_REPEAT_PLAY())){
                        mMusicService.setPLAYER_PLAY_MODE(mMusicService.getPLAYER_SORT_PLAY());
                        iv_play_mode.setImageResource(R.drawable.iv_sort_play); }
                    break;
                case R.id.song_lrc_bottom_left:
                    mMusicService.OnNextPlay("previous");
                    break;
                case R.id.song_lrc_bottom_right:
                    mMusicService.OnNextPlay("next");
                    break;
            }
        }
    }

    private class SongProgressListener implements SeekBar.OnSeekBarChangeListener{
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mMusicService == null) return;
            if(isSeekBarChanging && seekBar.getMax() > 0 || progress >= 0)
                //在播放中滑动时，TextView开始时间显示滑动的时间
                tv_seekBar_start.setText(mDateFormat.format(progress));
            else if (!mMusicService.isFirstPlay() && !isSeekBarChanging)
                tv_seekBar_start.setText(mDateFormat.format(mMusicService.getCurrentPosition()));
            if (sb_songProgress.getMax() < 1 ||
                    sb_songProgress.getMax() != mMusicService.getDuration())
                UpdateMusicProgressAndVolume();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
            Log.d(TAG, "拖动进度条。。。。。");
        }  //滑动进度条

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;//结束触摸进度条
            if (mMusicService == null) return;

            if (seekBar.getProgress() < 1) mMusicService.setProgress(1);
            else mMusicService.setProgress(seekBar.getProgress());

            if (!mMusicService.isFirstPlay()) {
                iv_play_toggle.setImageResource(R.drawable.iv_pause);
                tv_seekBar_start.setText(mDateFormat.format(mMusicService.getCurrentPosition()));
            }else {
                if (mMusicService.getCurrentPosition() > 0) {
                    //接着上次播放
                    mMusicService.OnContinueOrPausePlay();//【播放可能1】从上次关闭APP前的音乐播放位置继续播放
                    iv_play_toggle.setImageResource(R.drawable.iv_pause);
                }else Toast.makeText(SongLrcActivity.this,"请先播放歌曲",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void UpdateMusicProgressAndVolume() {
        if(!mMusicService.isPlaying() || mMusicService.getCurrentPosition() > 0){
            //在MediaPlayer已经播放过的情况下可以获取
            //1.点击Item装载音乐文件并播放
            //2.重新打开APP，接着上次的播放位置继续播放的时候
            if(!(tv_song.getText()+tv_singer.getText().toString())
                    .equals(mMusicService.getCurrentTitle()+mMusicService.getCurrentArtist()))
                sb_songProgress.setSecondaryProgress(0);

            if (mMusicService.getDuration() > 0) {
                sb_songProgress.setMax((int) mMusicService.getDuration());
                tv_seekBar_end.setText(mDateFormat.format(mMusicService.getDuration()));
            }
            //音量进度条初始化
            if (mMusicService.getMaxVolume() > 0) {
                sb_volume.setMax(mMusicService.getMaxVolume());
                sb_volume.setProgress(mMusicService.getVolume());
                Log.d(TAG, "UpdateMusicProgressAndVolume: "+mMusicService.getVolume());
                sb_volume.setOnSeekBarChangeListener(mVolumeListener);
            }
        }
    }

    private void UpdateSeekBarProgress(){
        if (mTimerProgressAndVolume == null) {
            mTimerProgressAndVolume = new Timer();
            mTimerProgressAndVolume.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (sb_songProgress == null) return;
                    if (mMusicService == null) return;

                    if (sb_songProgress.getProgress() >= sb_songProgress.getMax()) {
                        if (sb_songProgress.getProgress() > 0) {
                            sb_songProgress.setProgress(0);
                        }
                        return;
                    }
                    if (!isSeekBarChanging && mMusicService.isPlaying()) {
                        //当 没有手动改变进度条并且音乐正在播放时 执行
                        if (!mMusicService.isPlayerPrepared()) {
                            //注： [long] MediaPlayer.getCurrentPosition()必须在MediaPlayer.prepare()之后调用
                            //否则报错(-38,0)
                            sb_songProgress.setProgress(mMusicService.getCurrentPosition());
                        }else sb_songProgress.setProgress(mMusicService.getCurrentPosition());
                        //更新第二层进度【缓存】
                        if (sb_songProgress.getSecondaryProgress() >= 0 &&
                                sb_songProgress.getMax() >= 0 &&
                                sb_songProgress.getSecondaryProgress() < sb_songProgress.getMax()) {

                            /*Log.d(TAG, "getSecondary="+sb_songProgress.getSecondaryProgress()
                                                             + ", getMax="+sb_songProgress.getMax());*/
                            int secondary = new Random().nextInt(sb_songProgress.getMax()/5);
                            int current = sb_songProgress.getSecondaryProgress();
                            if (mMusicService.getCurrentPosition() > 0 &&
                                    current < mMusicService.getCurrentPosition() &&
                                    mMusicService.getCurrentPosition() < sb_songProgress.getMax()) {
                                //如果该歌曲已经播放有一会儿了，就着其进度往后加载
                                current = mMusicService.getCurrentPosition();
                            }
                            if ((current += secondary) > sb_songProgress.getMax())
                                current = sb_songProgress.getMax();
                            sb_songProgress.setSecondaryProgress(current);
                        }
                    }/*else {
                        //本子线程休眠,也可以不休眠，延时设置大于200，小于500
                    }*/
                }
            },0,330);
        }
    }

    private class MusicVolumeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mMusicService != null && progress <= seekBar.getMax()) {
                Log.d(TAG, "onProgressChanged: 总音量："+mMusicService.getMaxVolume()+", 当前音量："+progress);
                mMusicService.setVolume(progress);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {isSeekBarChanging = true; }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mMusicService != null && seekBar.getProgress() <= seekBar.getMax())
                mMusicService.setVolume(seekBar.getProgress());
        }
    }

    private void UpdateSeekBarVolume(){
        if (mTimerProgressAndVolume != null) {
            mTimerProgressAndVolume.schedule(new TimerTask() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void run() {
                    if (mMusicService != null && mLrcGroup.getVisibility() == View.VISIBLE
                            && sb_volume.getProgress() != mMusicService.getVolume()) {
                        sb_volume.setProgress(mMusicService.getVolume());
                    }
                }
            },100,100);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void clickBottomPlay(){
        if (mMusicService.isPlaying()) {
            mMusicService.OnPause();
            iv_play_toggle.setImageResource(R.drawable.iv_play);
        }else {
            UpdateMusicProgressAndVolume();//
            if (mMusicService.isFirstPlay() && mMusicService.getCurrentPosition() > 0) {
                //接着上次播放
                mMusicService.OnContinueOrPausePlay();//从上次关闭APP前的音乐播放位置继续播放
                iv_play_toggle.setImageResource(R.drawable.iv_pause);

            }else if (mMusicService.isFirstPlay() && mMusicService.getCurrentPosition() == 0) {
                Log.d(TAG, "onClick: 安装App之后的第一次播放");
                Toast.makeText(SongLrcActivity.this,"请点击音乐列表播放音乐",Toast.LENGTH_SHORT).show();
            } else {//暂停后播放
                mMusicService.OnContinueOrPausePlay();
                iv_play_toggle.setImageResource(R.drawable.iv_pause);
            }
        }
    }

    private void initUI(){
        Log.d(TAG, "initUI: ");
        tv_song = findViewById(R.id.song_lrc_top_song);
        tv_singer = findViewById(R.id.song_lrc_top_singer);

        iv_center_album = findViewById(R.id.song_lrc_center_album);
        iv_center_album.setOnClickListener(mClickListener);

        iv_play_toggle = findViewById(R.id.song_lrc_bottom_play);
        iv_play_toggle.setOnClickListener(mClickListener);
        iv_play_mode = findViewById(R.id.song_lrc_bottom_play_mode);
        iv_play_mode.setOnClickListener(mClickListener);

        findViewById(R.id.song_lrc_bottom_left).setOnClickListener(mClickListener);
        findViewById(R.id.song_lrc_bottom_right).setOnClickListener(mClickListener);
        findViewById(R.id.song_lrc_top_return).setOnClickListener(mClickListener);
        //音乐进度控制与显示
        sb_songProgress = findViewById(R.id.song_lrc_bar);
        sb_songProgress.setOnSeekBarChangeListener(mProgressListener);
        tv_seekBar_start = findViewById(R.id.song_lrc_time_start);
        tv_seekBar_end = findViewById(R.id.song_lrc_time_end);
        //歌词显示
        mLrcGroup = findViewById(R.id.song_lrc_center_lrc_group);
        sb_volume = findViewById(R.id.song_lrc_top_bar_volume);
        sb_volume.setOnSeekBarChangeListener(mVolumeListener);
        findViewById(R.id.song_lrc_top_sounds).setOnClickListener(mClickListener);
    }

    private void releaseUI(){
        if (tv_song != null) tv_song = null;
        if (tv_singer != null) tv_singer = null;
        if (iv_play_toggle != null) iv_play_toggle = null;
        if (iv_play_mode != null) iv_play_mode = null;
        if (iv_center_album != null) {
            iv_center_album.setImageBitmap(null);
            iv_center_album = null;
        }
        if (sb_songProgress != null) sb_songProgress = null;
        if (tv_seekBar_start != null) tv_seekBar_start = null;
        if (tv_seekBar_end != null) tv_seekBar_end = null;

        if (mLrcGroup != null) mLrcGroup = null;
        if (sb_volume != null) sb_volume = null;
    }
    private void releaseInterface(){
        if (mMyConn != null) mMyConn = null;
        if (mServiceIntent != null) mServiceIntent = null;
        if (mMusicService != null) mMusicService = null;
        if (mClickListener != null) mClickListener = null;
        if (mProgressListener != null) mProgressListener = null;
        if (mVolumeListener != null) mVolumeListener = null;
        if (mDateFormat != null) mDateFormat = null;
        if (mTimerProgressAndVolume != null) {
            mTimerProgressAndVolume.purge();
            mTimerProgressAndVolume.cancel();
            mTimerProgressAndVolume = null;
        }
    }
}
