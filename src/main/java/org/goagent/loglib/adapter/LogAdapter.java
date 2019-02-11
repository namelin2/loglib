package org.goagent.loglib.adapter;

/**************************************
 * LogAdapter
 * v1.0
 * author:Arlen
 * create date:2016/12/9 11:14
 **************************************/
public interface LogAdapter
{
    void d(String tag, String message);

    void e(String tag, String message);

    void w(String tag, String message);

    void i(String tag, String message);

    void v(String tag, String message);

    void wtf(String tag, String message);
}
