package com.emagroup.sdk;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class EmaService extends Service {

    private static final String TAG = "EmaService";

    private static final int INTERVAL_TIME_FIRST = 1000 * 30;//30秒
    private static final int INTERVAL_TIME_SENCOND = 1000 * 50 * 2;//2分钟
    private static final int INTERVAL_TIME_THIRD = 1000 * 60 * 5;//5分钟

    private boolean mFlagRuning = true;
    private HeartThread mHeartThread;

    @Override
    public IBinder onBind(Intent arg0) {

        return new LocalBinder();
    }

    /**
     * 首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或 onBind() 之前）。如果服务已在运行，则不会调用此方法。
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mHeartThread = new HeartThread();
        mHeartThread.start();
    }


    public void reStartHeart() {
        Log.e(TAG, "reStartHeart");
        if (mHeartThread != null) {
            mHeartThread.reSetHeart();
        }
    }


    private class HeartThread extends Thread {
        public int i = 0;

        @Override
        public void run() {

            while (mFlagRuning) {
                if (i < 2) {  //前1分钟 30秒一次 发送2次心跳包
                    EmaSendInfo.sendOnlineAlive();
                    trySleep(INTERVAL_TIME_FIRST);
                } else if (2 <= i && i < 4) {  //第1分钟到第5分钟  2分钟一次  发送2次心跳包
                    EmaSendInfo.sendOnlineAlive();
                    trySleep(INTERVAL_TIME_SENCOND);
                } else {  //之后都是5分钟发送一次
                    if (EmaUser.getInstance().getIsLogin()) {
                        EmaSendInfo.sendOnlineAlive();
                    }
                    trySleep(INTERVAL_TIME_THIRD);
                }
                i++;
            }
        }

        private void reSetHeart() {
            i = 0;
            EmaSendInfo.sendOnlineAlive();
        }
    }

    private void trySleep(int time) {
        try {
            Thread.sleep(time);                //在哪个线程里声明sleep，哪个线程睡眠
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 当服务不再使用且将被销毁时，系统将调用此方法。服务应该实现此方法来清理所有资源，如线程、注册的侦听器、接收器等。 这是service接收的最后一个调用。
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("emaService","onDestory");
        mFlagRuning = false;
    }


    public class LocalBinder extends Binder {
        EmaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return EmaService.this;
        }
    }
}
