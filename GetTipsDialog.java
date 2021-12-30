package com.example.testdemo.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;


import com.example.testdemo.R;

import java.util.Objects;

public class GetTipsDialog extends AlertDialog implements View.OnClickListener {
    private TextView tv_confirm,tv_cancel;

    private OnChooseListener mOnChooseListener;
    public interface OnChooseListener{
        void onChoose(boolean result);
    }
    public void setOnChooseListener(OnChooseListener onChooseListener) {
        this.mOnChooseListener = onChooseListener;
    }

    public GetTipsDialog(@NonNull Context context, int themeResId) {
        super(context,themeResId);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setWinWidth();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_get_tips);

    }

    @Override
    protected void onStart() {
        super.onStart();
        tv_confirm = findViewById(R.id.dialog_get_tips_confirm);
        tv_cancel = findViewById(R.id.dialog_get_tips_cancel);
        tv_confirm.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        tv_confirm.setOnClickListener(null);
        tv_cancel.setOnClickListener(null);
        if(tv_confirm != null) tv_confirm = null;
        if(tv_cancel != null) tv_cancel = null;
        if(mOnChooseListener != null) mOnChooseListener = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_get_tips_confirm:
                mOnChooseListener.onChoose(true);
                break;
            case R.id.dialog_get_tips_cancel:
                mOnChooseListener.onChoose(false);
                break;
        }
        cancel();
    }

    private void setWinWidth() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = Objects.requireNonNull(dialogWindow).getAttributes();
        DisplayMetrics d = getContext().getResources().getDisplayMetrics(); // 获取屏幕宽、高用

        lp.width = (int) (d.widthPixels * 0.86); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(lp);
    }
}
