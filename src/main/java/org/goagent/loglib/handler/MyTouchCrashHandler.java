package org.goagent.loglib.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import org.goagent.loglib.callback.MailThreadStateListener;
import org.goagent.loglib.file.LogWriteToFile;
import org.goagent.loglib.util.LogUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

/**************************************
 * MyTouchCrashHandler
 * company：setmath(数韬)
 * author:Arlen
 * version:v1.0
 * desc:
 * create date:2018/6/21 13:51
 * history desc: 
 * recent version:v1.0
 * version desc:
 * modified author:
 * modified date:
 * modified phone：
 **************************************/
public class MyTouchCrashHandler implements UncaughtExceptionHandler
{
    private static final String TAG = "MythouCrashHandler---->";
    private UncaughtExceptionHandler defaultUEH;
    private final String CONFIG_EXCEPTION = "config_exception";
    private final String KEY_EXCEPTION = "key_exception";
    private final String KEY_DATE = "key_date";
    private int total;
    private Context mContext;
    private long today;

    private boolean isUpdateLogDays = false;

    //构造函数，获取默认的处理方法
    public MyTouchCrashHandler(Context context)
    {

        this.mContext = context;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        getInternetTime();
    }

    public boolean isUpdateLogDays()
    {
        return isUpdateLogDays;
    }


    //这个接口必须重写，用来处理我们的异常信息
    @Override
    public void uncaughtException(final Thread thread, final Throwable ex)
    {
        showUserAboutCrashInfoCollect();
        total++;
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        //获取跟踪的栈信息，除了系统栈信息，还把手机型号、系统版本、编译版本的唯一标示
        StackTraceElement[] trace = ex.getStackTrace();
        StackTraceElement[] trace2 = new StackTraceElement[trace.length + 3];
        System.arraycopy(trace, 0, trace2, 0, trace.length);
        trace2[trace.length] = new StackTraceElement("Android", "MODEL", android.os.Build.MODEL, -1);
        trace2[trace.length + 1] = new StackTraceElement("Android", "VERSION", android.os.Build.VERSION.RELEASE, -1);
        trace2[trace.length + 2] = new StackTraceElement("Android", "FINGERPRINT", android.os.Build.FINGERPRINT, -1);
        //追加信息，因为后面会回调默认的处理方法
        ex.setStackTrace(trace2);
        ex.printStackTrace(printWriter);
        //把上面获取的堆栈信息转为字符串，打印出来
        StringBuilder sb = new StringBuilder();
        final String stacktrace = result.toString();
        printWriter.close();
        sb.append("\r\n==================应用程序崩溃日志,今日已发生次数：" + total + "次================\r\n\r\n")
                .append(stacktrace)
                .append("\r\n======================================================================\r\n");
        //这里把刚才异常堆栈信息写入缓存的Log日志里面
        saveTotalException(mContext, total);
        LogWriteToFile.getInstance().writeLog(sb.toString(), new MailThreadStateListener()
        {
            @Override
            public void sendSuccess()
            {
                defaultUEH.uncaughtException(thread, ex);
                LogUtils.e("日志提交邮件发送成功");
            }

            @Override
            public void sendFail()
            {
                defaultUEH.uncaughtException(thread, ex);
                LogUtils.e("日志提交邮件发送失败");
            }
        });
        // defaultUEH.uncaughtException(thread, ex);
    }

    /*读取异常统计总数信息*/
    private int readTotalException(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_EXCEPTION, 0);
        return sp.getInt(KEY_EXCEPTION, 0);
    }

    /*保存异常统计总数信息*/
    private void saveTotalException(Context context, int total)
    {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_EXCEPTION, 0);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt(KEY_EXCEPTION, total);
        edit.apply();
    }

    /*读取异常统计总数信息*/
    private long readDate(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_EXCEPTION, 0);
        return sp.getLong(KEY_DATE, 0L);
    }

    /*保存异常统计总数信息*/
    private void saveDate(Context context, long total)
    {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_EXCEPTION, 0);
        SharedPreferences.Editor edit = sp.edit();
        edit.putLong(KEY_DATE, total);
        edit.apply();
    }

    private void getInternetTime()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                URLConnection uc=null;
                try
                {
                    URL url = new URL("https://www.baidu.com");
                    uc = url.openConnection();//生成连接对象
                    uc.setConnectTimeout(6000);
                    uc.setReadTimeout(3000);
                    uc.connect(); //发出连接
                    today = uc.getDate();//取得网站日期时间
                } catch (Exception e)
                {
                    e.printStackTrace();
                    today = System.currentTimeMillis();
                } finally
                {
                    initTotal();
                }
            }
        }).start();
    }

    private void initTotal()
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//当前日期
        try
        {
            final long oldTime = readDate(mContext);
            final String oldTimeStr = sdf.format(new Date(oldTime));
            final String nowTimeStr = sdf.format(new Date(today));
            final boolean sameTime = oldTimeStr.equals(nowTimeStr);
            if (!TextUtils.isEmpty(oldTimeStr) && !TextUtils.isEmpty(nowTimeStr) && !sameTime)
            {
                total = 0;
                saveDate(mContext, today);
            }else {
                total = readTotalException(mContext);
            }
        } catch (NullPointerException e)
        {
            e.printStackTrace();
            try
            {
                total = readTotalException(mContext);
            } catch (NullPointerException e2)
            {
                e2.printStackTrace();
                total = 0;
            }
        }
    }

    private void showUserAboutCrashInfoCollect()
    {
        // 使用Toast来显示异常信息
        new Thread()
        {

            @Override
            public void run()
            {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序出现异常,正在收集崩溃日志中...",
                        Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
    }
}
