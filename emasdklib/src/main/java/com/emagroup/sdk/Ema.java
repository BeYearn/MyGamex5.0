package com.emagroup.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;

import com.igexin.sdk.PushManager;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.Tencent;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.emagroup.sdk.USharedPerUtil.getParam;
import static com.emagroup.sdk.USharedPerUtil.setParam;

public class Ema {

	private static final String TAG = "Ema";
	 public static  final int DIALOG_SHOW_FROM_LOGIN=1;//绑定提示框在登录时显示
	public static  final int DIALOG_SHOW_FROM_CREATE_ORDER=2;//绑定提示框在创建订单前显示
	 private EmaBinderAlertDialog emaBinderAlertDialog;
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
	private int accountType;

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
	public void init(String appKey,Context context, EmaSDKListener listener){
		if(!mFlagIsInitSuccess){
			LOG.e(TAG, "初始化开始......");

			EmaUser.getInstance().setAppKey(appKey);
			mContext = context;
			mEmaListener = listener;

			//初始化，从配置文件设定服务器地址
			ConfigManager.getInstance(getContext()).initServerUrl();

			//先检查维护状态,并能从该方法中拿到appkey（这一步现在放在闪屏dialog中）
			//checkSDKStatus();

			//闪屏
			showSplash();

			//清除上次的日志记录文件
			LOGToSdcardHelper.cleanFile();

			//从配置配置文件设定是否要将日志写入sdcard
			LOGToSdcardHelper.setWriteFlag(ConfigManager.getInstance(getContext()).isNeedLogToSdcard());
			
			//记录崩溃
			CrashHandler crashHandler = CrashHandler.getInstance();
			crashHandler.init(mContext);

			//个推初始化
			PushManager.getInstance().initialize(context.getApplicationContext());

			//初始化第三方sdk
			initThirdSDK();
			
			//埋点，发送初始化信息
			//EmaSendInfo.sendInitDeviceInfo();

			/*//检查基本的配置是否正确,仿佛并无卵用（因为不会得到null的）
			if(checkInitIsOK()){
				mFlagIsInitSuccess = true;
				//初始化成功调整到闪屏关闭后回调
			}else{
				mFlagIsInitSuccess = false;
				makeCallBack(EmaCallBackConst.INITFALIED, "初始化失败！");
			}*/    //  已改为在下面成功回调时mFlagIsInitSuccess置为true！！！！！！！

		}
	}

	/**
	 * 检查sdk是否维护状态，并能拿到appkey(这一步现在放在闪屏dialog中)
	 */
	private void checkSDKStatus() {
		//long time = SystemClock.currentThreadTimeMillis();
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
		// 初始化第三方的sdk  （支付等）
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
			//new LoginDialog(getContext()).show();    现在首次登录显示的是手机注册的那个页面
			RegisterByPhoneDialog.getInstance(getContext()).show();
		}
	}

	/**
	 * 登出操作
	 */
	public void Logout(){
		LOG.d(TAG, "Logout");
		EmaUser.getInstance().clearUserInfo();
		setParam(getContext(),"token","");
		setParam(getContext(),"nickname","");
		setParam(getContext(),"uid","");
		setParam(getContext(),"accountType",-1);
		makeCallBack(EmaCallBackConst.LOGOUTSUCCESS, "登出成功");
		ToolBar.getInstance(mContext).hideToolBar();
	}

	public void swichAccount() {
		LOG.d(TAG, "swichAccount");
		EmaUser.getInstance().clearUserInfo();
		setParam(getContext(),"token","");
		setParam(getContext(),"nickname","");
		setParam(getContext(),"uid","");
		setParam(getContext(),"accountType",-1);
		makeCallBack(EmaCallBackConst.ACCOUNTSWITCHSUCCESS, "切换帐号成功");
	}

	/**
	 * 开启支付操作
	 * @param
	 */
	public void pay(final EmaPayInfo payInfo, final EmaPayListener payListener){
		if(!mFlagIsInitSuccess){
			LOG.d(TAG, "初始化失败，禁止操作");
			return;
		}
		hideToolBar();
	/*	getUserInfo(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				emaBinderAlertDialog.dismiss();
				EmaPay.getInstance(getContext()).pay(payInfo, payListener);
			}
		},DIALOG_SHOW_FROM_CREATE_ORDER);*/
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

		if(EmaCallBackConst.LOGINSUCCESS==msgCode){ //在登录成功时
			//显示悬浮窗
			showToolBar();

			//查询所有用户信息
			/*getUserInfo(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					emaBinderAlertDialog.dismiss();
				}
			},DIALOG_SHOW_FROM_LOGIN);*/
			getUserInfo(null);

			//绑定服务,发送心跳
			Intent serviceIntent = new Intent(mContext, EmaService.class);
			mContext.bindService(serviceIntent, mServiceCon, Context.BIND_AUTO_CREATE);

		}else if(EmaCallBackConst.INITSUCCESS==msgCode){  // 在初始化成功时
			mFlagIsInitSuccess=true;
		}
	}


	public String getChannelId(){
		return ConfigManager.getInstance(mContext).getChannel();
	}

	public String getChannelTag(){
		return ConfigManager.getInstance(mContext).getChannelTag();
	}

	public Context getContext(){
		return mContext;
	}

	/**
	 * call一次拿到所有用户信息
	 */
	public void getUserInfo(final BindRemind bindRemind/*final View.OnClickListener onClickListener,*//* ,final int showFrom*/){
		Map<String, String> params = new HashMap<>();
		params.put("token",EmaUser.getInstance().getToken());
		params.put("uid",EmaUser.getInstance().getAllianceUid());
		params.put("appId",ConfigManager.getInstance(mContext).getAppId());
		new HttpInvoker().postAsync(Url.getUserInfoUrl(), params,
				new HttpInvoker.OnResponsetListener() {
					@Override
					public void OnResponse(String result) {
						try {
							boolean isNeedBindRemmind=false;
							JSONObject jsonObject = new JSONObject(result);
							String message= jsonObject.getString("message");
							String status= jsonObject.getString("status");

							JSONObject productData = jsonObject.getJSONObject("data");
							String email = productData.getString("email");
							boolean ifSetChargePwd = productData.getString("ifSetChargePwd").equals("1");
							String mobile = productData.getString("mobile");
							String nickname = productData.getString("nickname");
							String pfCoin = productData.getString("pfCoin");
							String uid = productData.getString("uid");

							EmaUser.getInstance().setEmail(email);
							EmaUser.getInstance().setIsWalletHasPassw(ifSetChargePwd);
							EmaUser.getInstance().setMobile(mobile);
							EmaUser.getInstance().setNickName(nickname);
							EmaUser.getInstance().setBalance(pfCoin);
							EmaUser.getInstance().setmUid(uid);
							EmaUser.getInstance().setAllianceUid(uid);

							LOG.e("getUserInfo",message+ifSetChargePwd+nickname+pfCoin+uid);
							if(productData.has("accountType")){
								accountType = productData.getInt("accountType");
								if(accountType==0){
								isNeedBindRemmind=true;
									if(bindRemind==null){//登录一天弹一次绑定提示
										if(isNeedShowBinder(mContext)){
											isNeedBindRemmind=true;
										}else{
											isNeedBindRemmind=false;
										}
									}
								}else{
									isNeedBindRemmind=false;
								}
							}else{
								isNeedBindRemmind=false;
							}



							if(isNeedBindRemmind){
							 ((Activity)mContext).runOnUiThread(new Runnable() {
								 @Override
								 public void run() {
									 emaBinderAlertDialog = new EmaBinderAlertDialog(mContext);
									 // emaBinderAlertDialog.setCanelClickListener(onClickListener);
								 	 emaBinderAlertDialog.setCanelClickListener(new View.OnClickListener() {
										 @Override
										 public void onClick(View view) {
											 emaBinderAlertDialog.dismiss();
											  if(bindRemind!=null){
												 bindRemind.canelNext();
											 }
										 }
									 });
									 emaBinderAlertDialog.show();
								 }
							 });

							}else{
							 if(bindRemind!=null){
								 bindRemind.canelNext();
							 }
						 }

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
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

		EmaUser.getInstance().clearPayInfo();
		EmaUser.getInstance().clearUserInfo();
		ConfigManager.getInstance(mContext).clear();*/
		try {
			ToolBar.getInstance(getContext()).hideToolBar();
		} catch (Exception e) {

		}
		mFlagToolbarShowing = false;
		mFlagIsInitSuccess = false;
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
		if(mFlagToolbarShowing){
			ToolBar.getInstance(getContext()).showToolBar();
		}
	}
	
	public void onStart(){
		LOG.d(TAG, "onStart");
	}
	
	public void onRestart(){
		LOG.d(TAG, "onRestart");
	}

	public void onBackPressed(EmaBackPressedAction action) {
		//官方sdk返回键没有操作,进行游戏的动作acion
		action.doBackPressedAction();
	}

	public void onNewIntent(Intent intent) {

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_LOGIN ||
				requestCode == Constants.REQUEST_APPBAR) {
			 Tencent.onActivityResultData(requestCode, resultCode, data,ThirdLoginUtils.getInstance(mContext).emIUiListener);
		}/*else if (requestCode == Constants.REQUEST_QZONE_SHARE) {
			Tencent.onActivityResultData(requestCode,resultCode,data,QQShareUtils.getIntance(mContext).emIUiListener);
		}else if (requestCode == Constants.REQUEST_QQ_SHARE) {
			Tencent.onActivityResultData(requestCode,resultCode,data,QQShareUtils.getIntance(mContext).emIUiListener);
		}*//*else if (requestCode == 0) {//QQ分享本地图片
			String path = null;
			if (resultCode == Activity.RESULT_OK) {
				if (data != null && data.getData() != null) {
					// 根据返回的URI获取对应的SQLite信息
					Uri uri = data.getData();
					path = QQShareUtils.getIntance(mContext).getPath(uri);
					QQShareUtils.getIntance(mContext).setImageUrl(path);
				}
			}
		}*/
	}

	public void saveWachatLoginFlag(boolean isWachatLogin){
		setParam(getContext(),"isWachatLogin",isWachatLogin);
	}

	public boolean isWachatLoginFlag(){
		return (Boolean) getParam(getContext(),"isWachatLogin",false);
	}

	public void saveWeboLoginVisibility(int weiBoVisibility){
		if(weiBoVisibility==0){
			setParam(getContext(),"weiBoVisibility",false);
		}else{
			setParam(getContext(),"weiBoVisibility",true);
		}

	}

	public boolean  getWeiBoLoginVisibility(){
		return (boolean) getParam(getContext(),"weiBoVisibility",false);
	}
	public void saveWachatLoginVisibility(int wachatVisibility){
		if(/*true*/wachatVisibility==0){
			setParam(getContext(),"wachatVisibility",false);
		}else{
			setParam(getContext(),"wachatVisibility",true);
		}
	}

	public boolean  getWachatLoginVisibility(){
		return (boolean) getParam(getContext(),"wachatVisibility",false);
	}
	public void saveQQLoginVisibility(int QQVisibility){
		if(/*true */QQVisibility==0){
			setParam(getContext(),"QQVisibility",false);
		}else{
			setParam(getContext(),"QQVisibility",true);
		}
	}

	public boolean  getQQLoginVisibility(){
		return (boolean) getParam(getContext(),"QQVisibility",false);
	}


	//微信登录后登录对话框被杀死，标识是否已经登陆过
	public  void setWechatCanLogin(Context context,boolean wechatCanLogin){
		setParam(context,"wechatCanLogin",wechatCanLogin);
	}
	public  Boolean getWechatCanLogin(Context context){
		return (Boolean) getParam(context,"wechatCanLogin",true);
	}

	public void saveShowBinderTime(Context context){
		Date date=new Date();
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
		setParam(mContext,"ShowBinderTime",simpleDateFormat.format(date));
	}

	public boolean isNeedShowBinder(Context context){
		boolean isNeedShow;
		Date date=new Date();
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
		String oldShowTime= (String) getParam(mContext,"ShowBinderTime","");
		if(TextUtils.isEmpty(oldShowTime)){
			isNeedShow=true;
			saveShowBinderTime(context);
		}else{
			String newShowTime=simpleDateFormat.format(date);
			if(oldShowTime.equals(newShowTime)){
				isNeedShow=false;
			}else{
				isNeedShow=true;
				saveShowBinderTime(context);
			}
		}

		return isNeedShow;
	}

	public void saveGameInfoJson(String gameInfoJson){
		setParam(mContext,"gameInfoJson",gameInfoJson);
	}
	public String getGameInfoJson (){
		return (String) getParam(mContext,"gameInfoJson","");
	}

	interface  BindRemind{
		void canelNext();
	}

}
