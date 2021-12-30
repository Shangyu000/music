package com.example.testdemo.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.testdemo.bean.MusicBean;
import com.example.testdemo.myutil.HtmlStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: 12453
 * Date: 2021/3/8
 * 作用:
 */
public class AllSongSheetModel implements SongSheetModel {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void showLocalMusic(OnLoadMusicListener onLoadListener, Context context) {
        onLoadListener.onComplete(getLocalMusic(context));
    }

    @Override
    public void loadAlbumBitmap(OnLoadAlbumBitmapCallBack onAlbumCallBack, String Path) {
        onAlbumCallBack.onCallBack(getAlbumBitmap(Path));
    }

    @Override
    public void showLocalSheet(OnLocalSheetListener onLocalSheetListener, SQLiteDatabase database) {
//        onLocalSheetListener.onComplete(getDataBaseSongSheet(onLocalSheetListener, database));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<MusicBean> getLocalMusic(Context context){
        if (context == null) return null;
        context = context.getApplicationContext();
        List<MusicBean> beans = new ArrayList<>();

        Cursor cursor;
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;//外部存储地址

        cursor = resolver.query(uri,null,null,null);//查询全部
        int id = 0;
        while (cursor != null && cursor.moveToNext()){
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            if(duration < 90000) continue;
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            id++;
            String sid = String.valueOf(id);

            //Log.d("", "getLocalMusic: "+title);
            MusicBean bean = new MusicBean(sid,title,artist,album,path,path,duration);
            beans.add(bean);
        }
        if (cursor != null && !cursor.isClosed()) cursor.close();
        return beans;
    }
    /**
     * 注释： 获取本地音乐文件的专辑图片（bitmap）
     * @param Path 音乐文件路径
     **/
    private Bitmap getAlbumBitmap(String Path){
        if (Path == null || TextUtils.isEmpty(Path)) return null;
        if (!HtmlStringUtil.FileExists(Path)) return null;

        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(Path);
        byte[] picture = metadataRetriever.getEmbeddedPicture();  //获取专辑图片速度快
        metadataRetriever.release();    //关闭音乐对象
        if (picture == null) return null;

        return BitmapFactory.decodeByteArray(picture,0,picture.length);  //将专辑图片转换成bitmap图片
    }


//    private List<SongSheetBean> getDataBaseSongSheet(OnLocalSheetListener onLocalSheetListener,SQLiteDatabase database){
//        List<SongSheetBean> mDatas = new ArrayList<>();
//        //Log.d("111", "getDataBaseSongSheet: "+database+", "+!database.isOpen());
//        if(database == null || !database.isOpen()) return mDatas;
//        Cursor cursor = null;
//        try {
//            cursor = database.rawQuery("select * from SongSheet_table", null);
//            //System.out.println("title = ");
//            while (cursor != null && cursor.moveToNext()) {
//                String firstAlbumPath = cursor.getString(cursor.getColumnIndex("firstAlbumPath"));
//                String alias = cursor.getString(cursor.getColumnIndex("alias"));
//                String title = cursor.getString(cursor.getColumnIndex("title"));
//                Log.d("model", "title: "+title+" ,alias: "+alias);
//                if (alias.equals("上次播放")) continue;
//
//                String countReview = "SELECT COUNT(*) FROM "+title;
//                SQLiteStatement statement = database.compileStatement(countReview);
//                String count = (int)statement.simpleQueryForLong()+"首";
//                statement.close();
//
//                SongSheetBean sheetBean = new SongSheetBean(alias,firstAlbumPath,count);
//                mDatas.add(sheetBean);
//            }
//        }catch (Exception e){
//            Log.d("AllSongSheetModel", "getDataBaseSongSheet: 获取数据异常"+e);
//        }finally {
//            if(cursor != null && !cursor.isClosed()) cursor.close();
//        }
//        if(mDatas.isEmpty()) onLocalSheetListener.onError("查询到0张歌单");
//        return mDatas;
//    }
}
