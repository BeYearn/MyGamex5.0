package com.emagroup.sdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class EmaService extends Service {
	
	private static final String TAG = "EmaService";
	
	private static final int INTERVAL_TIME_FIRST = 1000 * 30;//30秒
	private static final int INTERVAL_TIME_SENCOND = 1000 * 50 * 2;//2分钟
	private static final int INTERVAL_TIME_THIRD = 1000 * 60 * 5;//5分钟
//	private static final int INTERVAL_TIME_THIRD = 5000;
	private boolean mFlagRuning = true;
	private int mIntervalTime = 0;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		new MyThread().start();
	}
	
	private class MyThread extends Thread{
		 @Override
		public void run() {
			 EmaSendInfo.sendOnlineAlive(mIntervalTime);
			 for(int i=0; i<2; i++){//前1分钟 30秒一次 发送2次心跳包
				 trySleep(INTERVAL_TIME_FIRST);
				 mIntervalTime += 30;
				 EmaSendInfo.sendOnlineAlive(mIntervalTime);
			 }
			 for(int i=0; i<2; i++){//第1分钟到第5分钟  2分钟一次  发送2次心跳包
				 trySleep(INTERVAL_TIME_SENCOND);
				 mIntervalTime += 120;
				 EmaSendInfo.sendOnlineAlive(mIntervalTime);
			 }
			 //之后都是5分钟发送一次
			 while(mFlagRuning){
				 mIntervalTime += 300;
				 trySleep(INTERVAL_TIME_THIRD);
				if(EmaUser.getInstance().getIsLogin()){
					EmaSendInfo.sendOnlineAlive(mIntervalTime);
				}
			}
		 }
	}
	
	private void trySleep(int time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mFlagRuning = false;
	}
}
