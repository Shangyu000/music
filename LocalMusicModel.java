package com.example.testdemo.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;


import androidx.annotation.RequiresApi;

import com.example.testdemo.bean.MusicBean;
import com.example.testdemo.myutil.HtmlStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : 12453
 * @since : 2021/3/8
 * 作用:
 */
public class LocalMusicModel implements MusicModel {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void showLocalMusic(OnMusicListener onMusicListener, Context context) {
        onMusicListener.OnComplete(getLocalMusic(context));
    }

//    @Override
//    public void loadAlbumBitmap(OnLoadAlbumBitmapCallBack onAlbumCallBack, String Path) {
//        onAlbumCallBack.onCallBack(getAlbumBitmap(Path));
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<MusicBean> getLocalMusic(Context context){
        if (context == null) return null;

        context = context.getApplicationContext();
        List<MusicBean> beans = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = resolver.query(uri,null,null,null);
        int id = 0;
        while (cursor != null && cursor.moveToNext()) {
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            if(duration < 90000) continue;

            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            id++;
            String sid = String.valueOf(id);
            MusicBean bean = new MusicBean(sid,title,artist,album,path,path,duration);
            beans.add(bean);
        }
        if (cursor != null && !cursor.isClosed()) cursor.close();
        return beans;
    }

//    /**
//     * 注释： 获取本地音乐文件的专辑图片（bitmap）
//     * @param Path 音乐文件路径
//     **/
//    private Bitmap getAlbumBitmap(String Path){
//        if (Path == null || TextUtils.isEmpty(Path)) return null;
//        if (!HtmlStringUtil.FileExists(Path)) return null;
//
//        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
//        metadataRetriever.setDataSource(Path);
//        byte[] picture = metadataRetriever.getEmbeddedPicture();  //获取专辑图片速度快
//        metadataRetriever.release();    //关闭音乐对象
//        if (picture == null) return null;
//
//        return BitmapFactory.decodeByteArray(picture,0,picture.length);  //将专辑图片转换成bitmap图片
//    }
}
