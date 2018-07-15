# AppStartUp

## 1、启动类型
Android App 的启动分为三种类型：

    （1）冷启动

    （2）热启动

    （3）温启动

冷启动，是指 Android 系统后台不存在该 App 进程，用户点击图标启动 App 的过程。

热启动，是指用户通过 Home 键返回 launcher 页，但 Android 系统后台存在该 App 进程，且存在若干个 Activity ，用户点击App图标后，将看到点击 Home 键之前的页面的启动过程。

温启动，是指用户通过不断按下 back 返回键返回 launcher 页，Android 系统后台存在该 App 进程，但不存在 Activity，用户点击 App 图标后，将看到首页画面的启动过程。


## 2、启动速度
作为一个 App 开发者，主要关心 App 启动过程中的启动速度，即 App 启动起来快不快；如果启动很慢，用户就会对 App 失去耐心，从而造成卸载。

    注意：App 的启动速度主要是指冷启动；而冷启动过程，指的是从 Application 子类的onCreate()函数开始，到首个Activity可见的过程。

下面将通过若干个简单 demo 介绍影响启动速度的主要因素、启动速度调试工具和调试过程。

## 3、启动速度优化

首先看一下，最简单的 App 启动速度是啥样的！
新建 StartUp 的空工程，里面创建一个 App类，继承自 Application。

    public class App extends Application{
        @Override
        public void onCreate() {
            super.onCreate();
        }
    }

然后，将这个类加入 AndroidManifest.xml 文件，其他不改变。

    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.github.wq923.startup">

        <application
            android:name=".App"
            android:allowBackup="true"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:supportsRtl="true"
            android:theme="@style/AppTheme">
            <activity android:name=".MainActivity">
                <intent-filter>
                    <action android:name="android.intent.action.MAIN" />

                    <category android:name="android.intent.category.LAUNCHER" />
                </intent-filter>
            </activity>
        </application>

    </manifest>


使用 adb 工具，查看 App 启动时间，注意，查看冷启动时间（如果App在后台，需要先杀掉）。

    //格式为：adb shell am start -W 包名/包名.路径.XXXActivity
    adb shell am start -W com.github.wq923.startup/com.github.wq923.startup.MainActivity

结果：

    F:\github\StartUp>adb shell am start -W com.github.wq923.startup/com.github.wq923.startup.MainActivity
    Starting: Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=com.github.wq923.star
    tup/.MainActivity }
    Status: ok
    Activity: com.github.wq923.startup/.MainActivity
    ThisTime: 318
    TotalTime: 318
    Complete

    F:\github\StartUp>

上面的 ThisTime 和 TotalTime 表示启动时间，为 318ms。可见，一个空项目的启动时间是很快的，而且基本感觉不到任何卡顿。

### 3.1 启动速度杀手——耗时操作

耗时操作是指，在App类的onCreate()函数或首页Activity生命周期函数里执行了大量同步的耗时代码。比如：项目中用到的大量第三方sdk的初始化、文件操作、数据库操作、网络请求等。

#### （1）、模拟耗时操作
下面在以上函数中模拟一下耗时操作。

在 App 类模拟耗时操作：
    public class App extends Application {

        private static final String TAG = "App";

        @Override
        public void onCreate() {
            super.onCreate();

            Log.d(TAG, "onCreate: ");

            initSDK();
        }


        //模拟耗时操作
        private int initSDK() {

            int j = 0;

            for (int i = 0; i < 60000000; i++) {
                j++;
            }
            return j;
        }
    }

结果，通过 adb 命令行查看启动耗时：

    F:\github\StartUp>adb shell am start -W com.github.wq923.startup/com.github.wq923.startup.MainActivity
    Starting: Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=com.github.wq923.star
    tup/.MainActivity }
    Status: ok
    Activity: com.github.wq923.startup/.MainActivity
    ThisTime: 2764
    TotalTime: 2764
    Complete

可见启动时间增长到了2764ms，接近3s，App启动出现明显卡顿。同样，将这段代码放到 Activity 的onCreate()、onResume()、onStart()函数里，效果一样。

#### （2）、解决方法
将同步代码，改为异步执行。对上述代码进行简单修改，创建子线程调用initSDK()：

    //异步调用
    new Thread(new Runnable() {
                @Override
                public void run() {
                    initSDK();
                }
            }).start();

修改后，观察启动时间：

    F:\github\StartUp>adb shell am start -W com.github.wq923.startup/com.github.wq923.startup.MainActivity
    Starting: Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=com.github.wq923.star
    tup/.MainActivity }
    Status: ok
    Activity: com.github.wq923.startup/.MainActivity
    ThisTime: 268
    TotalTime: 268
    Complete

可见，异步调用耗时代码，不阻塞主线程的情况下，启动时间就恢复了。
因此，优化耗时操作，可以先查找项目中App类onCreate函数和首页Activity生命周期函数里是否有耗时操作，如果有，则将其通过异步的方式调用。

另一方面，很多第三方SDK都建议放在Application初始化，我们也可以将其延迟到使用的地方才进行初始化操作；优先让第一屏界面绘制出来。

#### （3）、进一步优化
如果通过异步加载，用户可以很快进入App主页，提高启动速度，但此时如果数据没有准备好，将意味着用户必须在主页进行等待，这样的交互显然也是不够友好的。

为此，目前常见的 App 都设计了 Splash 页面，比如：
![Alt Text](https://github.com/wq923/AppStartUp/blob/master/image/qq.png)
![Alt Text](https://github.com/wq923/AppStartUp/blob/master/image/hangbanguanjia.png)
![Alt Text](https://github.com/wq923/AppStartUp/blob/master/image/jingdong.png)
![Alt Text](https://github.com/wq923/AppStartUp/blob/master/image/feicahngzhun.png)
![Alt Text](https://github.com/wq923/AppStartUp/blob/master/image/tianmao.png)

Splash页面既可以让用户感觉很快的进入了应用，又可以通过该页面短暂的（2s-6s）的呈现，在内部完成数据异步加载，sdk初始化等耗时操作，为主页面准备数据，大大提高了用户体验。
同时，站在程序开发的角度，Splash页面的设计将大量初始化等业务无关的逻辑从App子类和主页面剥离出来，提高了启动速度，降低了代码耦合性。

Splash 页面创建
新建 SplashActivity.java 类：

    //闪屏页
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

新建 activity_splash.xml 布局文件：

    //设置背景为闪屏界面，splash图片随机找
    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:orientation="vertical" android:layout_width="match_parent"
        android:background="@drawable/splash"
        android:layout_height="match_parent">

    </LinearLayout>

在 AndroidManifest.xml文件中注册activity，并设置为启动页面

        <activity android:name=".SplashActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>



正在踢 克罗地亚对法国的世界杯决赛。上半场法国2:1领先，我猜克罗地亚逆转！
这届佩里西奇踢得真棒！


