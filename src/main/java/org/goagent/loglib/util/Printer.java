package org.goagent.loglib.util;

/**************************************
 * Printer
 * v1.0
 * author:Arlen
 * create date:2016/12/9 11:13
 **************************************/
public interface Printer
{
    Printer t(String tag, int methodCount);

    Settings init(String tag);

    Settings getSettings();

    void d(String message, Object... args);

    void d(Object object);

    void e(String message, Object... args);

    void e(Throwable throwable, String message, Object... args);

    void w(String message, Object... args);

    void i(String message, Object... args);

    void v(String message, Object... args);

    void wtf(String message, Object... args);

    void json(String json);

    void xml(String xml);

    void log(int priority, String tag, String message, Throwable throwable);

    void resetSettings();
}
