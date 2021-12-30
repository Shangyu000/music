package com.example.testdemo.myutil;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author : 12453
 * @since : 2021/1/7
 * 作用: 图片（Bitmap）处理工具类
 *              1.Bitmap图片高斯模糊
 *              2.Bitmap图片裁剪
 *              3.图片文件保存到相册
 */
public class PictureUtil {

    /**
     * 1.Bitmap高斯模糊
     * @param context 上下文对象
     * @param image   需要模糊的图片
     * @param blurRadius 设置渲染的模糊程度, 25f是最大模糊度,越大越模糊
     * @return 模糊处理后的Bitmap
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap blurBitmap(Context context, Bitmap image, float blurRadius) {
        float BITMAP_SCALE = 0.16f;// 图片缩放比例
        context = context.getApplicationContext();//单例持有，防止内存泄漏
        // 计算图片缩小后的长宽
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        // 将缩小后的图片做为预渲染的图片
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        // 创建一张渲染后的输出图片
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        // 创建RenderScript内核对象
        RenderScript rs = RenderScript.create(context);
        // 创建一个模糊效果的RenderScript的工具对象
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(blurRadius);
        // 设置blurScript对象的输入内存
        blurScript.setInput(tmpIn);
        // 将输出数据保存到输出内存中
        blurScript.forEach(tmpOut);
        // 将数据填充到Allocation中
        tmpOut.copyTo(outputBitmap);
        //释放资源：RenderScript内核对象
        tmpIn.destroy();
        tmpOut.destroy();
        blurScript.destroy();
        rs.destroy();
        return outputBitmap;
    }
    /**
     * 2.Bitmap图片裁剪,剪切专辑图片后得到Bitmap
     * @param bitmap 要从中截图的原始位图
     * int retX:起始x坐标
     * int retY：起始y坐标
     * int width：要截的图的宽度
     * int height：要截的图的宽度
     * @return 返回一个剪切好的Bitmap
     * @author 12453
     * date 2020/12/11 16:58
     **/
    public static Bitmap imageCropWithRect(Bitmap bitmap) {
        if (bitmap == null) return null;
        // 得到图片的宽，高
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int retX, retY,nw,nh;
        if (w > h) {//Log.d(TAG, "imageCropWithRect: W>H");
            nw = h / 2;
            nh = h;
            retX = (w - nw) / 2;
            retY = 0;
        } else {//Log.d(TAG, "imageCropWithRect: W<H");
            nw = w/4*3;
            nh = h/4*3;
            retX = w/8;
            retY = h/8;
        }
        return Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null, false);
    }
    /**
     * 将本地图片转成Bitmap
     * @param path 已有图片的路径
     * @return bitmap
     */
    public static Bitmap openImage(String path){
        Bitmap bitmap = null;
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
        }  catch (IOException e) {
            Log.d("Util", "本地图片转Bitmap失败");
        }
        return bitmap;
    }
    /**
     * bitmap转化成byte数组
     * @param bm 需要转换的Bitmap
     * @return byte[]
     */
    public static byte[] bitmapToBytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
