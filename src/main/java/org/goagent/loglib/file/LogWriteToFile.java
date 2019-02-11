package org.goagent.loglib.file;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

import org.goagent.loglib.callback.MailThreadStateListener;
import org.goagent.loglib.mail.MailThread;
import org.goagent.loglib.util.AndroidDes3Utils;
import org.goagent.loglib.util.SystemUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**************************************
 * LogWriteToFile
 * company：setmath(数韬)
 * author:Arlen
 * version:v1.0
 * desc:
 * create date:2018/6/21 10:01
 * history desc: 
 * recent version:v1.0
 * version desc:
 * modified author:
 * modified date:
 * modified phone：
 **************************************/
public class LogWriteToFile
{
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//当前日期
    private static final SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//当前时间
    private Context mContext;
    private String header;
    private boolean isSendMail;
    private String defaultErrorFileName;
    //no instance
    private LogWriteToFile()
    {
    }

    private static LogWriteToFile instance;

    public static LogWriteToFile getInstance()
    {
        if (instance == null)
        {
            synchronized (LogWriteToFile.class)
            {
                if (instance == null)
                {
                    instance = new LogWriteToFile();
                }
            }
        }
        return instance;
    }

    public void init(Context context,boolean isSendMail)
    {
        this.mContext = context;
        this.isSendMail=isSendMail;
        StringBuilder sb = new StringBuilder();
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        sb.append("\r\n").append(sdfTime.format(new Date(System.currentTimeMillis()))).append(" ");
        sb.append("ID：").append(AndroidDes3Utils.encode(getAndroidID(mContext))).append(" ");
        sb.append("名称：").append(getAppName(mContext)).append(" ");
        sb.append("包名：").append(AndroidDes3Utils.encode(mContext.getPackageName())).append("\r\n");
        sb.append("设备信息【").append("手机厂商：").append(SystemUtil.getDeviceBrand())
                .append(",手机型号：").append(SystemUtil.getSystemModel())
                .append(",手机当前系统语言：").append(SystemUtil.getSystemLanguage())
                .append("，Android系统版本号：").append(SystemUtil.getSystemVersion()).append("\r\n\r\n")
                .append("屏幕分辨率：").append(width).append("*").append(height).append("\t\t")
                .append("虚拟按键高度：").append(getNavigationBarHeight(mContext)).append("\t\t")
                .append("内存：").append(Tools.getTotalMemory(mContext)).append("\t\t")
                .append("可用内存：").append(Tools.getMemoryFree(mContext)).append("\t\t")
                .append("当前版本：").append(Tools.getAppInfo(mContext)).append("\t\t")
                .append("】\r\n");
        defaultErrorFileName= getDiskCacheDir(mContext) + "/" + getAppName()+"_error_log_"+sdf.format(new Date(System.currentTimeMillis())) + ".txt";
        header = sb.toString();
    }
    /**
     * @param content 日志内容
     * 抛出文件路径错误或IO读写异常
     */
    public void writeLog(String content)
    {
        // System.out.println(content);
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        //  writer.write("\r\n======================================================\r\n");
        sb.append(content);
        writeLog("", sb.toString());
    }

    /**
     * @param content 日志内容
     * @param mMailThreadStateListener  回调监听
     * 抛出文件路径错误或IO读写异常
     */
    public void writeLog(String content,final MailThreadStateListener mMailThreadStateListener)
    {
        // System.out.println(content);
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        //  writer.write("\r\n======================================================\r\n");
        sb.append(content);
        writeAppExceptionLog(defaultErrorFileName, sb.toString(),mMailThreadStateListener);
    }

    /**
     * 追加文件：使用FileWriter
     *
     * @param fileName 存放日志的文件地址
     * @param content  日志内容
     */
    public void writeLog(String fileName, String content)
    {

        if (TextUtils.isEmpty(fileName))
        {
            fileName = getDiskCacheDir(mContext) + "/" +getAppName()+"_"+ sdf.format(new Date(System.currentTimeMillis())) + ".txt";
        }
        //  System.out.println("文件地址："+fileName);
        FileWriter writer = null;
        try
        {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(fileName, true);
            writer.write(content);
            //  writer.write("\r\n======================================================\r\n");
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    /**
     * 写入崩溃日志记录
     * 追加文件：使用FileWriter
     * @param fileName 存放日志的文件地址
     * @param content  日志内容
     * @param mMailThreadStateListener  回调监听
     */
    public void writeAppExceptionLog(String fileName, String content,final MailThreadStateListener mMailThreadStateListener)
    {
        if (TextUtils.isEmpty(fileName))
        {
            fileName =defaultErrorFileName;
        }
        //  System.out.println("文件地址："+fileName);
        FileWriter writer = null;
        try
        {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(fileName, true);
            writer.write(content);
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            if (isSendMail)
            {
                MailThread mailThread = new MailThread(getAppName(mContext)+"_异常日志报告_"+sdf.format(new Date(System.currentTimeMillis())),content,mMailThreadStateListener);
                mailThread.start();
            }else {
                if (null!=mMailThreadStateListener)
                {
                    mMailThreadStateListener.sendFail();
                }
            }
        }
    }

    /**
     * 获取缓存文件夹
     *
     * @param context 日志内容
     * @return 缓存文件夹地址
     */
    private String getDiskCacheDir(Context context)
    {
        String cachePath;
        //isExternalStorageEmulated()设备的外存是否是用内存模拟的，是则返回true
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageEmulated())
        {
            cachePath = context.getExternalCacheDir().getAbsolutePath();
        } else
        {
            cachePath = context.getCacheDir().getAbsolutePath();
        }
        return cachePath;
    }

    /**
     * 获取设备AndroidID
     *
     * @param context 上下文
     * @return AndroidID
     */
    @SuppressLint("HardwareIds")
    public String getAndroidID(Context context)
    {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取应用程序名称
     */
    private String getAppName()
    {
        return "pbBuy";
    }
    /**
     * 获取应用程序名称
     */
    private String getAppName(Context context)
    {
        try
        {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return "未获取App名称";
    }
    private   int getNavigationBarHeight(Context context) {
        int navigationBarHeight = -1;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

}
