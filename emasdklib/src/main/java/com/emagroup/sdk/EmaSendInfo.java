package com.emagroup.sdk;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class EmaSendInfo {

	private static final String TAG = "EmaSendInfo";
	private static final String ONLINE_TIME = "online_time";
	
	/**
	 * 发送心跳包
	 */
	public static void sendOnlineAlive(int totleTime){
		LOG.d(TAG, "sendOnlineAlive");
		Context context = Ema.getInstance().getContext();
		Map<String, String> params = new HashMap<String, String>();
		/*params.put(PropertyField.APP_ID, ConfigManager.getInstance(context).getAppId());
		params.put(PropertyField.SEND_CHANNEL_ID, ConfigManager.getInstance(context).getChannel());
		params.put(PropertyField.UUID, EmaUser.getInstance().getUUID());
		params.put(PropertyField.IP, DeviceInfoManager.getInstance(context).getIP());
		params.put(PropertyField.SEND_DEVICE_ID, DeviceInfoManager.getInstance(context).getDEVICE_ID());
		params.put(PropertyField.GAME_SERVER_ID, "");
		params.put(PropertyField.ROLE_ID, "");
		JSONObject json = new JSONObject();
		try {
			json.put(ONLINE_TIME, totleTime);

		} catch (Exception e) {
		}
		params.put(PropertyField.MEMO, json.toString());//备注信息
		UCommUtil.testMapInfo(params);*/
		params.put("token", EmaUser.getInstance().getmToken());
		new HttpInvoker().postAsync(Url.getHeartbeatUrl(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				LOG.d(TAG, "heartbeat result__:" + result);
			}
		});
	}
	
	/**
	 * 创建角色
	 */
	public static void createRole(){
		LOG.d(TAG, "createRole");
		Context context = Ema.getInstance().getContext();
		Map<String, String> params = new HashMap<String, String>();
		params.put(PropertyField.APP_ID, ConfigManager.getInstance(context).getAppId());
		params.put(PropertyField.SEND_CHANNEL_ID, ConfigManager.getInstance(context).getChannel());
		//params.put(PropertyField.UUID, EmaUser.getInstance().getUUID());
		params.put(PropertyField.IP, DeviceInfoManager.getInstance(context).getIP());
		params.put(PropertyField.SEND_DEVICE_ID, DeviceInfoManager.getInstance(context).getDEVICE_ID());
		params.put(PropertyField.GAME_SERVER_ID, "");
		params.put(PropertyField.ROLE_ID, "");
		params.put(PropertyField.ROLE_NAME, "");
		params.put(PropertyField.ROLE_SEX, "");
		params.put(PropertyField.ROLE_TYPE, "");
		params.put(PropertyField.ROLE_CAMP, "");
		
		new HttpInvoker().postAsync(Url.getGatherInfoUrlGameRole(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				LOG.d(TAG, "createRole result__:" + result);
			}
		});
	}
	
	/**
	 * 初始化，向服务器发送设备信息
	 */
	public static void sendInitDeviceInfo(){
		LOG.d(TAG, "sendInitDeviceInfo");
		Map<String, String> params = DeviceInfoManager.getInstance(Ema.getInstance().getContext()).deviceInfoGather();
		new HttpInvoker().postAsync(Url.getSendInfoUrlInitDeviceInfo(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				LOG.d(TAG, result);
			}
		});
	}
	
	/**
	 * 登录成功，发送用户信息
	 */
	public static void sendLoginSuccInfo(){
		LOG.d(TAG, "sendLoginSuccInfo");
		Map<String, String> params = new HashMap<String, String>();
		ConfigManager configManager = ConfigManager.getInstance(Ema.getInstance().getContext());
		params.put(PropertyField.APP_ID, configManager.getAppId());
		params.put(PropertyField.SEND_CHANNEL_ID, configManager.getChannel());
		params.put(PropertyField.SEND_DEVICE_ID, DeviceInfoManager.getInstance(Ema.getInstance().getContext()).getDEVICE_ID());
		
		new HttpInvoker().postAsync(Url.getSendInfoUrlLoginSucc(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				LOG.d(TAG, result);
			}
		});
	}
	
	/**
	 * 注册，发送设备信息
	 */
	public static void sendRegisterInfo(){
		LOG.d(TAG, "sendRegisterDeviceInfo");
		Map<String, String> params = new HashMap<String, String>();
		ConfigManager configManager = ConfigManager.getInstance(Ema.getInstance().getContext());
		params.put(PropertyField.APP_ID, configManager.getAppId());
		params.put(PropertyField.SEND_CHANNEL_ID, configManager.getChannel());
		params.put(PropertyField.SEND_DEVICE_ID, DeviceInfoManager.getInstance(Ema.getInstance().getContext()).getDEVICE_ID());
		new HttpInvoker().postAsync(Url.getSendInfoUrlRegisterSucc(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				LOG.d(TAG, result);
			}
		});
	}
	
}
