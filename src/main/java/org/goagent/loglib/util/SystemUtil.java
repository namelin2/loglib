package org.goagent.loglib.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.Locale;

/**************************************
 * SystemUtil  系统工具类
 * company：setmath(数韬)
 * author:Arlen
 * version:v1.0
 * desc:
 * 1、获取手机制造厂商
 * 2、获取手机型号
 * 3、获取手机系统当前使用的语言
 * 4、获取Android系统版本号
 * 5、获取手机IMEI串号
 * 6、获取手机中的语言列表
 * create date:2018/6/21 11:57
 * history desc: 
 * recent version:v1.0
 * version desc:
 * modified author:
 * modified date:
 * modified phone：
 **************************************/
public class SystemUtil
{

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return  语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     * @param ctx 上下文参数
     * @return  手机IMEI
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getDeviceId();
        }
        return null;
    }
}
