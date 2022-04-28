package com.example.auraplay;

import android.app.Application;
import android.os.SystemClock;

public class SplashTimer extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SystemClock.sleep(3000);
    }
}
