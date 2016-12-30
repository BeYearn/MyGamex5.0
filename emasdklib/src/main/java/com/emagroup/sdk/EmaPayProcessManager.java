package com.emagroup.sdk;

import android.app.Activity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EmaPayProcessManager {

	private static final String TAG = EmaPayProcessManager.class.toString();
	
	private static EmaPayProcessManager mInstance;
	private static final Object synchron = new Object();

	private List<Activity> mPayAcList;
	private List<Activity> mRechargeAcList;

	private int mWeixinActionType;
	//微信下单成功后存储的数据，每次从游戏进入支付时清空，微信支付 回调后也清空
	private JSONObject mWeixinOrderInfo;
	
	private EmaPayProcessManager() {
	}

	public static EmaPayProcessManager getInstance() {
		if (mInstance == null) {
			synchronized (synchron) {
				if (mInstance == null) {
					mInstance = new EmaPayProcessManager();
				}
			}
		}
		return mInstance;
	}
	
	/**
	 * 获取微信支付订单返回数据
	 * @return
	 */
	public JSONObject getWeixinOrderInfo(){
		return mWeixinOrderInfo;
	}
	
	/**
	 * 设置微信支付订单返回数据
	 * @param orderInfo
	 */
	public void setWeixinOrdreInfo(JSONObject orderInfo){
		mWeixinOrderInfo = orderInfo;
	}
	
	/**
	 * 清空微信支付订单返回数据
	 */
	public void clearWeixinOrderInfo(){
		mWeixinOrderInfo = null;
	}
	
	/**
	 * 获取微信支付的操作类型：  支付   or  充值
	 * 默认是支付
	 */
	public int getWeixinActionType(){
		if(mWeixinActionType == 0){
			mWeixinActionType = EmaConst.PAY_ACTION_TYPE_PAY;
		}
		return mWeixinActionType;
	}
	
	/**
	 * 设置微信支付的操作类型：  支付   or  充值
	 * @param actionType
	 */
	public void setWeixinActionType(int actionType){
		mWeixinActionType = actionType;
	}

	/**
	 * 添加支付界面
	 * 
	 * @param ac
	 */
	public void addPayActivity(Activity ac) {
		LOG.d(TAG, "add payActivity:" + ac.toString());
		mPayAcList = addActivity(mPayAcList, ac);
		LOG.d(TAG, "mPayAcList is null ?? " + (mPayAcList == null));
	}

	/**
	 * 移除支付界面
	 * 
	 * @param ac
	 */
	public void removePayActivity(Activity ac) {
		removeActiivty(mRechargeAcList, ac);
	}

	/**
	 * 关闭充值界面  并显示悬浮窗
	 */
	public void closePay() {
		LOG.d(TAG, "close Pay");
		close(mPayAcList);
	}

	/**
	 * 添加充值界面
	 * 
	 * @param ac
	 */
	public void addRechargeActivity(Activity ac) {
		LOG.d(TAG, "add recharge activity: " + ac.toString());
		mRechargeAcList = addActivity(mRechargeAcList, ac);
	}

	/**
	 * 移除充值界面
	 * 
	 * @param ac
	 */
	public void removeRechargeActiivty(Activity ac) {
		removeActiivty(mRechargeAcList, ac);
	}

	/**
	 * 关闭
	 */
	public void closeRecharge() {
		LOG.d(TAG, "close Recharge");
		close(mRechargeAcList);
	}

	/**
	 * 关闭全部
	 */
	public void closeAll(){
		LOG.d(TAG, "close all");
		closeRecharge();
		closePay();
		ToolBar.getInstance(Ema.getInstance().getContext()).showToolBar();
	}

	
	
	private List<Activity> addActivity(List<Activity> list, Activity ac) {
		if (list == null) {
			list = new ArrayList<Activity>();
		}
		list.add(ac);
		return list;
	}

	private void removeActiivty(List<Activity> list, Activity ac) {
		if (list != null && list.contains(ac)) {
			list.remove(ac);
		}
	}

	private void close(List<Activity> list) {
		LOG.d(TAG, "close list is null ?? " + (list == null));
		if (list != null && list.size() > 0) {
			for (Activity ac : list) {
				LOG.d(TAG, "close actiity: " + ac.toString());
				ac.finish();
				ac = null;
			}
		}
	}

}
