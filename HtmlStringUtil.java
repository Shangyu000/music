package com.example.testdemo.myutil;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import java.io.File;

/**
 * Function:  返回各种样式的字符串{Spanned类型}的工具类
 */
public class HtmlStringUtil {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Spanned SongSingerName(String title, String artist){
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(artist))
            return Html.fromHtml("<font color = \"#EEEEEE\">快去听听音乐吧</font>",
                                                Html.FROM_HTML_OPTION_USE_CSS_COLORS);
        if (TextUtils.isEmpty(artist) || artist.equals("<unknown>")) artist = "Unknown";

        String SongInformation = "<font color = \"#EEEEEE\">"+title+"</font>"+
                "<font color = \"#A9B7C6\"><small> - "+artist+"</small></font>";
        return Html.fromHtml(SongInformation,Html.FROM_HTML_OPTION_USE_CSS_COLORS);
    }
    public static String SheetTips(int count){
        return "已有歌单("+count+"个)";
    }
    /*public static String MusicTime(long duration){
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        return sdf.format(new Date(duration));
    }
    public static String getSystemTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
        return sdf.format(new Date());
    }
    public static String getTimeDifference(long time1,long time2){
        return "执行了"+(time2-time1)+"";
    }*/

    public static boolean FileExists(String targeFileAbsPath){
        try {
            File f = new File(targeFileAbsPath);
            if(f.exists()) return false;
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
