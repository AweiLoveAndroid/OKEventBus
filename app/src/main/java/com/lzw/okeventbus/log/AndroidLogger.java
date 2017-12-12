package com.lzw.okeventbus.log;

import android.util.Log;

import java.util.logging.Level;

import com.lzw.okeventbus.log.Logger;

public class AndroidLogger implements Logger {

    private static final boolean ANDROID_LOG_AVAILABLE;

    static {
        boolean android = false;
        try {
            android = Class.forName("android.util.Log") != null;
        } catch (ClassNotFoundException e) {
            // OK
        }
        ANDROID_LOG_AVAILABLE = android;
    }

    public static boolean isAndroidLogAvailable() {
        return ANDROID_LOG_AVAILABLE;
    }


    private final String tag;

    public AndroidLogger(String tag) {
        this.tag = tag;
    }

    @Override
    public void log(Level level, String msg) {
        if (level != Level.OFF) {
            Log.println(mapLevel(level), tag, msg);
        }
    }

    @Override
    public void log(Level level, String msg, Throwable th) {
        if (level != Level.OFF) {
            // That's how Log does it internally
            Log.println(mapLevel(level), tag, msg + "\n" + Log.getStackTraceString(th));
        }
    }

    private int mapLevel(Level level) {
        int value = level.intValue();
        if (value < 800) { // below INFO
            if (value < 500) { // below FINE
                return Log.VERBOSE;
            } else {
                return Log.DEBUG;
            }
        } else if (value < 900) { // below WARNING
            return Log.INFO;
        } else if (value < 1000) { // below ERROR
            return Log.WARN;
        } else {
            return Log.ERROR;
        }
    }
}