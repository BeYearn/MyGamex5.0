package com.emagroup.sdk;

import android.util.Log;

public class LOG
{

    private static String stag = "EMASDK";

    public LOG()
    {
    }

    public static void setTag(String tag)
    {
        Log.w("LOG", (new StringBuilder()).append("log tag [").append(stag).append("] be replaced to [").append(tag).append("]").toString());
        stag = tag;
    }

    public static void d(String tag, String msg)
    {
        Log.d(stag, (new StringBuilder()).append("[").append(tag).append("] ").append(msg).toString());
        LOGToSdcardHelper.writeLog((new StringBuilder()).append(stag).append(" : [").append(tag).append("] ").append(msg).toString());
    }

    public static void d(String tag, String msg, Throwable t)
    {
        Log.d(stag, (new StringBuilder()).append("[").append(tag).append("] ").append(msg).toString(), t);
        LOGToSdcardHelper.writeLog((new StringBuilder()).append(stag).append(" : [").append(tag).append("] ").append(msg).toString());
        LOGToSdcardHelper.writeLog(t.getMessage());
    }

    public static void i(String tag, String msg)
    {
        Log.i(stag, (new StringBuilder()).append("[").append(tag).append("] ").append(msg).toString());
        LOGToSdcardHelper.writeLog((new StringBuilder()).append(stag).append(" : [").append(tag).append("] ").append(msg).toString());
    }

    public static void i(String tag, String msg, Throwable t)
    {
        Log.i(stag, (new StringBuilder()).append("[").append(tag).append("] ").append(msg).toString(), t);
        LOGToSdcardHelper.writeLog((new StringBuilder()).append(stag).append(" : [").append(tag).append("] ").append(msg).toString());
        LOGToSdcardHelper.writeLog(t.getMessage());
    }

    public static void w(String tag, String msg)
    {
        Log.w(stag, (new StringBuilder()).append("[").append(tag).append("] ").append(msg).toString());
        LOGToSdcardHelper.writeLog((new StringBuilder()).append(stag).append(" : [").append(tag).append("] ").append(msg).toString());
    }

    public static void w(String tag, String msg, Throwable t)
    {
        Log.w(stag, (new StringBuilder()).append("[").append(tag).append("] ").append(msg).toString(), t);
        LOGToSdcardHelper.writeLog((new StringBuilder()).append(stag).append(" : [").append(tag).append("] ").append(msg).toString());
        LOGToSdcardHelper.writeLog(t.getMessage());
    }

    public static void e(String tag, String msg)
    {
        Log.e(stag, (new StringBuilder()).append("[").append(tag).append("] ").append(msg).toString());
        LOGToSdcardHelper.writeLog((new StringBuilder()).append(stag).append(" : [").append(tag).append("] ").append(msg).toString());
    }

    public static void e(String tag, String msg, Throwable t)
    {
        Log.e(stag, (new StringBuilder()).append("[").append(tag).append("] ").append(msg).toString(), t);
        LOGToSdcardHelper.writeLog((new StringBuilder()).append(stag).append(" : [").append(tag).append("] ").append(msg).toString());
        LOGToSdcardHelper.writeLog(t.getMessage());
    }

}
