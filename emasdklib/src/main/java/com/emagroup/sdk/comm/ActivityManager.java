package com.emagroup.sdk.comm;

import java.util.ArrayList;
import java.util.List;

import com.emagroup.sdk.Ema;

import android.app.Activity;

/**
 * 管理sdk内弹出的activity ，这样方便返回游戏的功能实现，简化逻辑
 * @author zhangyang
 *
 */
public class ActivityManager {

	private static ActivityManager mInstance;
	public static final Object synchron = new Object();
	
	private List<Activity> mList;
	
	private ActivityManager(){
		mList = new ArrayList<Activity>();
	}
	
	public static ActivityManager getInstance(){
		if(mInstance == null){
			synchronized (synchron) {
				if(mInstance == null){
					mInstance = new ActivityManager();
				}
			}
		}
		return mInstance;
	}
	
	/**
	 * 添加activity到列表
	 * @param ac
	 */
	public void add(Activity ac){
		if(mList == null){
			mList = new ArrayList<Activity>();
		}
		mList.add(ac);
	}
	
	/**
	 * 从列表删除activity
	 * @param ac
	 */
	public void remove(Activity ac){
		if(mList != null && mList.contains(ac)){
			mList.remove(ac);
		}
	}
	
	/**
	 * 清空activity 列表
	 */
	public void clear(){
		if(mList != null && mList.size() > 0){
			mList.clear();
		}
	}
	
	/**
	 * 关闭列表内的所有activity
	 */
	public void closeAll(){
		closeAll(true);
	}
	
	/**
	 * 关闭列表内的所有activity，并观察是否显示悬浮窗
	 */
	public void closeAll(boolean showToolbar){
		if(mList != null && mList.size() > 0){
			for(Activity ac : mList){
				ac.finish();
			}
		}
		if(showToolbar){
			((Activity)Ema.getInstance().getContext()).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Ema.getInstance().showToolBar();
				}
			});	
		}
	}
	
}
