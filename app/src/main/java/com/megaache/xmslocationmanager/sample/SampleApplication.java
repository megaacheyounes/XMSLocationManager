package com.megaache.xmslocationmanager.sample;

import android.app.Application;

import com.megaache.xmslocationmanager.XMSLocationManager;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        org.xms.g.utils.GlobalEnvSetting.init(this, null);
        XMSLocationManager.enableLog(true);
    }
}
