package com.example.testdemo.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.testdemo.R;
import com.example.testdemo.customview.TextCircleView;
import com.example.testdemo.dialog.GetTipsDialog;
import com.example.testdemo.myutil.ImmersiveStatusBarUtil;

public class WelcomeActivity extends AppCompatActivity {

    /*动态申请读、写权限*/
    private static final int REQUEST_PERMISSION_CODE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private RelativeLayout mUIRootLayout;
    private TextCircleView tv_time;
    private TextView tv_line1;
    private long time = 3077;  //两秒钟后进入LoginActivity
    private boolean isHideNavigation = false,isStartAnimation = false;
    private Animation mShowAnimation;

    private CountDownTimer mDownTimer = new CountDownTimer(time,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("TAG", "onTick: ");
            if (!isStartAnimation) {    //文字动画显示
                tv_line1.setVisibility(View.VISIBLE);
                tv_line1.setAnimation(mShowAnimation);
                isStartAnimation = true;
            }
            if (millisUntilFinished >= 1000) {
                String time = String.valueOf(millisUntilFinished / 1000);
                tv_time.setText(time);
                //开始淡出动画
                if(millisUntilFinished < 1900){
                    Log.d("Welcome", "onTick: 是时候隐藏状态栏了！");
                    Window window = getWindow();
                    if (window != null) {
                        if (!isHideNavigation) isHideNavigation = true;
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    }
                }
            }
        }

        @Override
        public void onFinish() {
            Log.d("TAG", "onFinish: ");
            startLoginActivity();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ImmersiveStatusBarUtil.transparentBar(this,false);
        super.onCreate(savedInstanceState);
        Log.d("TAG", "onCreate: ");
        setContentView(R.layout.activity_welcome);
        mUIRootLayout = findViewById(R.id.welcome_activity_bottom_layout);
        mUIRootLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {  //底部ui适配
                Log.d("TAG", "onApplyWindowInsets: ");
                if (insets.getSystemWindowInsetBottom() < 288 && !isHideNavigation) {
                    int paddingBottom = insets.getSystemWindowInsetBottom();//给予一个最低padding高度，底部导航栏高度
                    v.setPadding(v.getPaddingRight(),v.getPaddingTop(),v.getPaddingLeft(),paddingBottom < 30 ? 30 : paddingBottom);
                }
                return insets;
            }
        });
        tv_time = findViewById(R.id.welcome_activity_top_tv_timer);
        tv_line1 = findViewById(R.id.welcome_activity_top_tv_sky);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TAG", "onStart: ");
        if (mShowAnimation == null) mShowAnimation = AnimationUtils.loadAnimation(this,R.anim.view_gradually_show);
        if (!isGetPermission()) {
            GetTipsDialog tipsDialog = new GetTipsDialog(this,R.style.DialogTheme);
            tipsDialog.setCancelable(false);
            tipsDialog.show();
            tipsDialog.setOnChooseListener(new GetTipsDialog.OnChooseListener() {
                @Override
                public void onChoose(boolean result) {
                    if (result) {
                        getStorage();
                    }else finish();
                }
            });
        }else mDownTimer.start();
    }

    @Override
    protected void onDestroy() {  //消除引用,空指针判定
        super.onDestroy();
        Log.d("TAG", "onDestroy: ");
        if(mDownTimer != null) {
            mDownTimer.cancel();
            mDownTimer = null;
        }
        if (mShowAnimation != null) {
            mShowAnimation.reset();
            mShowAnimation.cancel();
            mShowAnimation = null;
        }
        if (tv_line1 != null) tv_line1= null;
        if (tv_time != null) tv_time= null;
        if (mUIRootLayout != null) mUIRootLayout= null;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //回调监听权限是否同意
        Log.d("TAG", "onRequestPermissionsResult: ");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("TAG", "onRequestPermissionsResult: 确认动态获取读写权限");
                    mDownTimer.start();
                }else {
                    Log.d("TAG", "onRequestPermissionsResult: 没有动态获取读写权限");
                    finish();
                }
                break;
        }

    }

    private void getStorage() {
        Log.d("TAG", "getStorage: ");
        /*动态获取存储权限的方法*/
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){ //判断Android版本是否大于6.0 || 在API(26)以后规定必须要动态获取权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,PERMISSIONS_STORAGE,REQUEST_PERMISSION_CODE);
            }
        }

    }
    private boolean isGetPermission(){
        Log.d("TAG", "isGetPermission: ");
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED; //判断是否已获取权限;
    }

    private void startLoginActivity(){
        Log.d("TAG", "startLoginActivity: ");
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        //关闭当前页面
        finish();
    }

}