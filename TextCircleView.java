package com.example.testdemo.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/*
 * 欢迎界面倒计时
 * */

public class TextCircleView extends AppCompatTextView {

    //圆角大小，默认为10
    private  int mBorderRadius = 20;
    private Paint mPaint;

    public TextCircleView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init();
    }
    //xml创建TextCircleView调用这个构造函数
    public TextCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init();
    }
    //new TextCircleView调用这个构造函数
    public TextCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    /** * 初始化画笔 */
    public void init()
    {
        mPaint = new Paint();
    }
    /** * 调用onDraw绘制边框 */
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        //创建一个RectF，用来限定绘制圆弧的范围
        RectF rectf = new RectF();
        //设置画笔的颜色
        mPaint.setColor(getPaint().getColor());
        //设置画笔的样式，空心
        mPaint.setStyle(Paint.Style.STROKE);
        //设置抗锯齿
        mPaint.setAntiAlias(true);
        //设置画得一个半径，然后比较长和宽，以最大的值来确定长方形的长宽，确定半径
        int r = getMeasuredWidth() > getMeasuredHeight() ? getMeasuredWidth() : getMeasuredHeight();
        //如果设置的padding不一样绘制出来的是椭圆形。绘制的时候考虑padding
        //Log.i("边界", "宽度"+getMeasuredWidth()+"高度"+getMeasuredHeight()+"getPaddingLeft()"+getPaddingLeft()+"getPaddingTop"+getPaddingTop()+"getPaddingRight(): "+getPaddingRight()+"getPaddingBottom()"+getPaddingBottom());
        //当padding都为0的时候，绘制出来的就是RectF限定的区域就是一个正方形
        rectf.set(getPaddingLeft()+1.5f,getPaddingTop()+1,r - getPaddingRight()-0.5f,r - getPaddingBottom()-0.5f);
        /*//绘制文字
        String testString ="保存专辑封面";
        Rect bounds = new Rect();
        Paint paint=new Paint();
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);
        paint.getTextBounds(testString, 0, testString.length(), bounds);
        //绘制文字
        canvas.drawText(testString, getMeasuredWidth() / 2 - bounds.width() / 2, getMeasuredHeight() / 2 + bounds.height() / 2, paint);*/
        //绘制圆弧
        //绘制圆弧
        canvas.drawArc(rectf,0,360,false,mPaint);
        //canvas.drawRoundRect(rectf,mBorderRadius,mBorderRadius,mPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPaint.reset();
        mPaint = null;
    }
}
