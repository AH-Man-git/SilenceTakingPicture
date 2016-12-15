package com.pounds.nonesurviewcamera;

import android.app.Application;

/**
 * Created by Administrator on 2016/12/14 0014.
 */

public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static boolean invadeMonitor() {
        return true;
    }

    public static void getInstance() {

    }
}
