package com.emagroup.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.igexin.sdk.PushManager;

import java.util.Map;

import com.emagroup.sdk.analytics.EmaSendInfo;
import com.emagroup.sdk.comm.ConfigManager;
import com.emagroup.sdk.comm.EmaCallBackConst;
import com.emagroup.sdk.comm.EmaSDKListener;
import com.emagroup.sdk.pay.EmaPay;
import com.emagroup.sdk.pay.EmaPayInfo;
import com.emagroup.sdk.pay.EmaPayListener;
import com.emagroup.sdk.ui.SplashDialog;
import com.emagroup.sdk.ui.ToolBar;
import com.emagroup.sdk.user.EmaAutoLogin;
import com.emagroup.sdk.user.EmaUser;
import com.emagroup.sdk.user.RegisterByPhoneDialog;
import com.emagroup.sdk.user.RoleInfo;
import com.emagroup.sdk.utils.CrashHandler;
import com.emagroup.sdk.utils.EmaConst;
import com.emagroup.sdk.utils.LOG;
import com.emagroup.sdk.utils.LOGToSdcardHelper;
import com.emagroup.sdk.utils.UCommUtil;
import com.emagroup.sdk.utils.USharedPerUtil;

public class Ema {

	private static final String TAG = "Ema";
	
	private SplashDialog mSplashDialog;
	
	private Context mContext;
	private EmaSDKListener mEmaListener;
	//标记
	private boolean mFlagToolbarShowing;//标记在切换到主界面时是否在显示toolbar
	private boolean mFlagIsInitSuccess;//标记初始化是否成功
	private boolean mFlagIsShowSplash = true;//标记是否显示闪屏
	
	//绑定服务
	private ServiceConnection mServiceCon = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		}
	};
	
	private static Ema mInstance;
	private static final Object synchron = new Object();
	private Ema(){
		mFlagToolbarShowing = false;
	}
	
	public static Ema getInstance(){
		if(mInstance == null){
			synchronized (synchron) {
				if(mInstance == null){
					mInstance = new Ema();
				}
			}
		}
		return mInstance;
	}

	
	/**
	 * EmaSDK初始化
	 */
	public void init(Context context, EmaSDKListener listener){
		if(!mFlagIsInitSuccess){
			//清除上次的日志记录文件
			LOGToSdcardHelper.cleanFile();
			
			mContext = context;
			mEmaListener = listener;
			
			//初始化，从配置文件设定服务器地址
			ConfigManager.getInstance(getContext()).initServerUrl();
			//从配置配置文件设定是否要将日志写入sdcard
			LOGToSdcardHelper.setWriteFlag(ConfigManager.getInstance(getContext()).isNeedLogToSdcard());
			
			LOG.d(TAG, "初始化开始");
			
			//记录崩溃
			CrashHandler crashHandler = CrashHandler.getInstance();
			crashHandler.init(mContext);

			//个推初始化
			PushManager.getInstance().initialize(context.getApplicationContext());
			//初始化第三方sdk
			initThirdSDK();
			
			//闪屏
			showSplash();
			
			//绑定服务
//			Intent serviceIntent = new Intent(mContext, EmaService.class);
//			mContext.bindService(serviceIntent, mServiceCon, Context.BIND_AUTO_CREATE);
			
			//埋点，发送初始化信息
			EmaSendInfo.sendInitDeviceInfo();
			//检查基本的配置是否正确
			if(checkInitIsOK()){
				mFlagIsInitSuccess = true;
				makeCallBack(EmaCallBackConst.INITSUCCESS, "初始化完成");
			}else{
				mFlagIsInitSuccess = false;
				makeCallBack(EmaCallBackConst.INITFALIED, "初始化失败！");
			}
			//显示悬浮窗
			showToolBar();
		}
	}
	
	/**
	 * 检查初始化结果
	 */
	private boolean checkInitIsOK(){
		ConfigManager configManager = ConfigManager.getInstance(mContext);
		if(UCommUtil.isStrEmpty(configManager.getAppId())){
			LOG.w(TAG, "APP_ID为空，请检查APP_ID配置");
			return false;
		}
		if(UCommUtil.isStrEmpty(configManager.getAppKEY())){
			LOG.w(TAG, "APP_KEY为空，请检查APP_KEY配置");
			return false;
		}
		if(UCommUtil.isStrEmpty(configManager.getChannel())){
			LOG.w(TAG, "Channel为空，请检查Channel配置");
			return false;
		}
		if(mEmaListener == null){
			LOG.w(TAG, "回调为空，请检查监听回调");
			return false;
		}
		return true;
	}

	/**
	 * 第三方sdk初始化
	 */
	private void initThirdSDK(){
		//TODO 初始化第三方的sdk  （支付等）
	}
	
	/**
	 * 设置是否显示闪屏
	 * @param showSplash
	 */
	public void setShowSplashFlag(boolean showSplash){
		mFlagIsShowSplash = showSplash;
	}
	
	/**
	 * 闪屏
	 */
	private void showSplash(){
		if(mFlagIsShowSplash){
			mSplashDialog = new SplashDialog(getContext());
			mSplashDialog.start();
		}
	}
	
	/**
	 * 执行登录操作
	 */
	public void Login(){
		hideToolBar();
		if(!mFlagIsInitSuccess){
			LOG.d(TAG, "初始化失败，禁止登录");
			return;
		}
		if(EmaAutoLogin.getInstance(getContext()).isAutoLogin()){

			EmaAutoLogin.getInstance(getContext()).doLoginAuto();

		}else{
			((Activity)mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					//new LoginDialog(getContext()).show();    现在首次登录显示的是手机注册的那个页面
					new RegisterByPhoneDialog(getContext()).show();
				}
			});
		}
	}
	
	/**
	 * 游戏方登陆后，获取到了游戏角色信息后，调用此接口（必接）
	 * 
	 * 提交游戏数据
	 */
	public void submitLoginGameRole(Map<String, String> map){
		LOG.d(TAG, "submitLoginGameRole");
		RoleInfo info = new RoleInfo();
		info.setRoleId(map.get(EmaConst.EMA_SUBMIT_ROLE_ID));
		info.setRoleName(map.get(map.get(EmaConst.EMA_SUBMIT_ROLE_NAME)));
		info.setRoleLevel(map.get(EmaConst.EMA_SUBMIT_ROLE_LEVEL));
		info.setServerId(map.get(EmaConst.EMA_SUBMIT_SERVER_ID));
		info.setServerName(map.get(EmaConst.EMA_SUBMIT_SERVER_NAME));
		//EmaUser.getInstance().setRoleInfo(info);
	}
	
	/**
	 * 登出操作
	 */
	public void Logout(){
		LOG.d(TAG, "Logout");
		EmaUser.getInstance().clearPayInfo();
		EmaUser.getInstance().clearUserInfo();
		USharedPerUtil.setParam(getContext(),"token","");
		USharedPerUtil.setParam(getContext(),"nickname","");
		USharedPerUtil.setParam(getContext(),"uid","");
		USharedPerUtil.setParam(getContext(),"accountType",-1);
		makeCallBack(EmaCallBackConst.LOGOUTSUCCESS, "登出成功");
	}
	
	/**
	 * 获取当前是否是登录状态
	 * @return
	 */
	public boolean isLogin(){
		return EmaUser.getInstance().getIsLogin();
	}
	
	/**
	 * 开启支付操作
	 * @param
	 */
	public void pay(EmaPayInfo payInfo, EmaPayListener payListener){
		if(!mFlagIsInitSuccess){
			LOG.d(TAG, "初始化失败，禁止操作");
			return;
		}
		hideToolBar();
		EmaPay.getInstance(getContext()).pay(payInfo, payListener);
	}
	
	/**
	 * 显示悬浮窗
	 */
	public void showToolBar(){
		if(!mFlagIsInitSuccess){
			LOG.d(TAG, "初始化失败，禁止操作");
			return;
		}
		ToolBar.getInstance(getContext()).showToolBar();
	}
	
	/**
	 * 隐藏悬浮窗
	 */
	public void hideToolBar(){
		if(!mFlagIsInitSuccess){
			LOG.d(TAG, "初始化失败，禁止操作");
			return;
		}
		ToolBar.getInstance(getContext()).hideToolBar();
	}
	
	/**
	 * 设置回调信息
	 * @param msgCode
	 * @param msgObj
	 */
	public void makeCallBack(int msgCode, String msgObj){
		if(mEmaListener == null){
			LOG.w(TAG, "未设置回调");
			return;
		}
		mEmaListener.onCallBack(msgCode,msgObj);
	}
	
	public Context getContext(){
		return mContext;
	}
	
	/**
	 * Destory里需要做一些处理
	 */
	public void onDestroy(){
		LOG.d(TAG, "onDestroy");
		
		/*if(mSplashDialog != null && mSplashDialog.isShowing()){
			mSplashDialog.dismiss();
		}
		
//		mContext.unbindService(mServiceCon);
		mFlagIsInitSuccess = false;
		mFlagToolbarShowing = false;
		
		EmaUser.getInstance().clearPayInfo();
		EmaUser.getInstance().clearUserInfo();
		ConfigManager.getInstance(mContext).clear();
		try {
			ToolBar.getInstance(getContext()).hideToolBar();
		} catch (Exception e) {
		}*/
	}
	
	public void onStop(){
		LOG.d(TAG, "onStop");
	}
	
	/**
	 * 观察在切出界面的时候，toolbar是否正在显示
	 */
	public void onPause(){
		LOG.d(TAG, "onPause");
		try {
			mFlagToolbarShowing = ToolBar.getInstance(getContext()).isToolbarShowing();
			ToolBar.getInstance(getContext()).hideToolBar();
		} catch (Exception e) {
		}
	}
	
	/**
	 * 如果切出界面的时候toolbar正在显示，那么回来的时候需要重新显示toolbar
	 */
	public void onResume(){
		LOG.d(TAG, "onResume");
			ToolBar.getInstance(getContext()).showToolBar();

	}
	
	public void onStart(){
		LOG.d(TAG, "onStart");
	}
	
	public void onRestart(){
		LOG.d(TAG, "onRestart");
	}
	
}
