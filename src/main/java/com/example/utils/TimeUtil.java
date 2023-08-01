package com.example.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-21  20:33
 * @Description: 时间工具类
 */
public class TimeUtil {

    /**
     * @author chenyim
     * @date 2023/6/21 20:34
     * @Description  返回当前时间，String
     */
    public static String getCurrentTime(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * @author chenyim
     * @date 2023/6/21 20:34
     * @Description  返回当前时间，datetime格式
     */
    public static Date getCurrentDateTime() throws ParseException {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        return df.parse(getCurrentTime());
    }

    //将Java date转化为 数据库Date
    public static java.sql.Timestamp dtot(java.util.Date d) {
        if (null ==d)
            return null;
        return new java.sql.Timestamp(d.getTime());

}

    //将数据库Date 转化为java date
    public static java.util.Date ttod(java.sql.Timestamp t) {
        if (null ==t)
            return null;
        return new java.util.Date(t.getTime());

}
}
