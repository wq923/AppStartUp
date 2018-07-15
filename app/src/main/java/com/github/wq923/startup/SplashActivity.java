package com.github.wq923.startup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by 13521838583@163.com on 2018-7-15.
 *
 */

public class SplashActivity extends AppCompatActivity {

    private static final int MSG_INIT = 1;
    private static final int DELAY_TIME = 3000;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_INIT:
                {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //展示闪屏页，等待 3s 后，跳转到主页面。等待的这 3s 很关键！
        //此处可以进行异步初始化，数据预加载等操作
        //满足了快速启动 app，且主页面少等待的需求
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(MSG_INIT, DELAY_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_INIT);
    }
}
