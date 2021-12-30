package com.example.testdemo.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Pattern;

public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {

    private float textLength = 0f;  //文字长度
    private float viewWidth = 0f;  //滚动条长度
    private float tx = 0f;  //文本x轴坐标
    private float ty = 0f;
    private float temp_tx1 = 0.0f;  //文本当前长度
    private float temp_tx2 = 0x0f;  //文本当前变换长度
    private boolean isStarting = false; //文本滚动开关
    private Paint paint = null;
    private String text = "";  //显示文字
    private float speed; //文本滚动速度
    private boolean isFirstScroll = true; //是否是第一次滚动，用于设置首次滚动位置
    private static final int FirstScroll = 3; //设置首次滚动位置，第二次从最右边开始

    public MarqueeTextView(@NonNull Context context) {
        super(context);
    }

    public MarqueeTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(paint != null){
            paint.reset();
            paint = null;
        }
        text = null;
    }

    public void initScrollTextView(WindowManager windowManager,String text,float speed){
        paint = this.getPaint();
        this.text = text;
        this.speed = speed;
        textLength = paint.measureText(text); //获取当前文字长度
        viewWidth = this.getWidth();
        if(viewWidth == 0){
            Display display = windowManager.getDefaultDisplay();
            viewWidth = display.getWidth();
        }
        tx = textLength;
        temp_tx1 = viewWidth / FirstScroll + textLength;
        temp_tx2 = viewWidth / FirstScroll + textLength * 2;

        ty = this.getTextSize() + this.getPaddingTop();
    }
    //开始滚动
    public void startScroll(){
        if(!isFirstScroll) isFirstScroll = true;
        isStarting = true;
        this.invalidate();
    }
    //停止滚动
    public void stopScroll(){
        if(isFirstScroll) isFirstScroll = false;
        isStarting = false;
        this.invalidate();
    }
    //初始化滚动文本内容
    //显示内容，文字长度大于16，选择滚动
    public void setText(String text,WindowManager manager){
        int scrollLength,count = 0;
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]"); //中文字符
        Pattern r = Pattern.compile("[\u3040-\u309F\u30A0-\u30FF]"); //日文字符
        if(!p.matcher(text).find() && !r.matcher(text).find()){
            char[] x = text.toCharArray();
            for(int i=0;i<x.length;i++){
                if(x[i] >= 'A' && x[i] <= 'Z') count++;
            }
            scrollLength = 32;
        }else {  //中文与中文字符，可能不全是字母，需要判断
            for (int i=0;i<text.length();i++){
                String x = text.substring(i);
                if(p.matcher(x).find() || r.matcher(text).find()) count++;
            }
            scrollLength = 16;
        }
        int trueLength = count * 2 + (text.length() - count);
        if(trueLength >= 32) scrollLength = text.length();

        if (text.length() >= scrollLength){
            if(isStarting) stopScroll();
            setText("");
            initScrollTextView(manager,text,2);
            startScroll();
        }else {
            stopScroll();
            this.text = text;
            setText(text);
        }
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(isStarting){
            isFirstScroll = false;
            canvas.drawText(text,temp_tx1-tx,ty,paint);
            tx += speed;
            //当文字滚动到最左边
            if(tx > temp_tx2){
                //把文字设置到最右边开始
                tx = textLength;
                if(!isFirstScroll){
                    temp_tx1 = viewWidth + textLength;
                    temp_tx2 = viewWidth + textLength * 2;
                }
            }
            this.invalidate();
        }
        super.onDraw(canvas);
    }
}
