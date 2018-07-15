package com.github.wq923.startup;

import android.app.Application;
import android.util.Log;

/**
 * Created by 13521838583@163.com on 2018-7-14.
 *
 */

public class App extends Application {

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: ");

//                        initSDK();
//
        new Thread(new Runnable() {
            @Override
            public void run() {
                initSDK();
            }
        }).start();
    }


    private int initSDK() {

        int j = 0;

        for (int i = 0; i < 60000000; i++) {
            j++;
        }
        return j;
    }
}














