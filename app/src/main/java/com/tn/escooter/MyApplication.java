package com.tn.escooter;


import android.app.Application;
import android.content.Context;

import com.androidnetworking.AndroidNetworking;
import com.jacksonandroidnetworking.JacksonParserFactory;
import com.tn.escooter.buletooth.QBlueToothManager;
import com.tn.escooter.utils.CacheStore;
import com.tn.escooter.utils.Model.User;
import com.tn.escooter.utils.utils;

import java.util.Date;

public class MyApplication extends Application {
    public static int TYPE = 0;
    public static String SCOOTER_TYPE = utils.bluetooth_type.LENZOD;
    public static MyApplication myApplication;
    public static User cur_user = new User(null, 0, null, null);
    public static float max_speed = 0f;
    public static float cur_odo = 0f;
    public static Date odo_save_time = null;
    public static boolean mine = false;
    public static String app_version = "1.0.4";
    public static String app_date = "2021.4.3";


    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
    }

    public void onCreate() {
        super.onCreate();
        myApplication = this;
        QBlueToothManager.getInstance().init(this);
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());
        CacheStore.getInstance();
    }

}
