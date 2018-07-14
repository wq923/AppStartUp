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

然后，将整个类加入 AndroidManifest.xml 文件，其他不改变。

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
