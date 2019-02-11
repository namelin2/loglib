package org.goagent.loglib.adapter.impl;

import android.util.Log;

import org.goagent.loglib.adapter.LogAdapter;

/**************************************
 * AndroidLogAdapter
 * v1.0
 * author:Arlen
 * create date:2016/12/9 11:15
 **************************************/
public class AndroidLogAdapter implements LogAdapter
{
    @Override
    public void d(String tag, String message)
    {
        Log.d(tag, message);
    }

    @Override
    public void e(String tag, String message)
    {
        Log.e(tag, message);
    }

    @Override
    public void w(String tag, String message)
    {
        Log.w(tag, message);
    }

    @Override
    public void i(String tag, String message)
    {
        Log.i(tag, message);
    }

    @Override
    public void v(String tag, String message)
    {
        Log.v(tag, message);
    }

    @Override
    public void wtf(String tag, String message)
    {
        Log.wtf(tag, message);
    }
}
