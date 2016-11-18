package com.emagroup.sdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterDialog extends Dialog implements android.view.View.OnClickListener {
	
	private static final String TAG = "RegisterDialog";

	private final static int CODE_AUTO_REGISTER_GETINFO_SUCCESS = 0;// 自动注册成功
	private final static int CODE_REGISTER_FALIED = 1;// 自动注册失败
	private final static int CODE_REGISTER_ACCOUNT_EXIST = 2;//账户已存在
	private final static int CODE_REGISTER_SUCCESS = 3;//注册成功
	private final static int CODE_REGISTER_ERROR_EMAIL = 4;//邮箱格式错误
	
	private Activity mActivity;
	private ResourceManager mResourceManager;//资源管理
	private ConfigManager mConfigManager;//配置项管理
	private DeviceInfoManager mDeviceInfoManager;//设备信息管理
	private EmaUser mEmaUser;//当前用户的信息
	
	private LoginSuccDialog mLoginSuccDialog;// 登录成功后显示的对话框
	
	//Views
	private EditText mEdtNameView;
	private EditText mEdtPasswView;
	private Button mBtnRegister;
	private Button mBtnRegisterByPhone;
	private Button mBtnReturnLogin;

	private Map<String, Integer> mIDmap;
	
	private String mLoginName;//登录名
	private String mLoginPassw;//登录密码
	
	//进度条
	private EmaProgressDialog mProgress;
	
	//标记
	private boolean mFlagIsInitData;//标记是否进行了自动注册过程
	
	public RegisterDialog(Context context) {
		super(context);
		mActivity = (Activity) context;
		mResourceManager = ResourceManager.getInstance(mActivity);
		mDeviceInfoManager = DeviceInfoManager.getInstance(mActivity);
		mConfigManager = ConfigManager.getInstance(mActivity);
		mEmaUser = EmaUser.getInstance();
		mProgress = new EmaProgressDialog(mActivity);
		mFlagIsInitData = false;
	}

	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			mProgress.closeProgress();
			switch(msg.what){
			case CODE_AUTO_REGISTER_GETINFO_SUCCESS://自动注册获取默认账户信息成功
				doAutoRegisterGetInfoSucc();
				break;
			case CODE_REGISTER_FALIED://注册失败
				ToastHelper.toast(mActivity, "注册失败");
				UCommUtil.makeUserCallBack(EmaCallBackConst.REGISTERFALIED, "注册失败");
				break;
			case CODE_REGISTER_ACCOUNT_EXIST://用户名已存在
				ToastHelper.toast(mActivity, "账户已存在");
				UCommUtil.makeUserCallBack(EmaCallBackConst.REGISTERFALIED, "账户已存在");
				break;
			case CODE_REGISTER_ERROR_EMAIL://邮箱格式错误
				ToastHelper.toast(mActivity, "邮箱格式错误");
				UCommUtil.makeUserCallBack(EmaCallBackConst.REGISTERFALIED, "邮箱格式错误");
				break;
			case CODE_REGISTER_SUCCESS://注册成功
				ToastHelper.toast(mActivity, "注册成功");
				RegisterDialog.this.dismiss();
				UCommUtil.makeUserCallBack(EmaCallBackConst.REGISTERSUCCESS, "注册成功");
				// 保存登录成功用户的信息
				mEmaUser.saveLoginUserInfo(mActivity);
				// 显示登录成功后的对话框
				mLoginSuccDialog = new LoginSuccDialog(mActivity, false);
				mLoginSuccDialog.start();
				break;
			case EmaProgressDialog.CODE_LOADING_START://显示进度条
				mProgress.showProgress((String)msg.obj);
				break;
			case EmaProgressDialog.CODE_LOADING_END://关闭进度条
				mProgress.closeProgress();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		this.setCanceledOnTouchOutside(false);
		
		initView();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(!mFlagIsInitData){
			initData();
			mFlagIsInitData = true;
		}
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		//请求服务器获取分配的用户名和密码
		doAutoRegisterGetInfo();
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		setContentView(mResourceManager.getIdentifier("ema_register", "layout"));
		mEdtNameView = (EditText) findViewById(getId("ema_register_loginname"));
		mEdtPasswView = (EditText) findViewById(getId("ema_register_passwd"));
		mBtnRegister = (Button) findViewById(getId("ema_register_login"));
		mBtnRegisterByPhone = (Button) findViewById(getId("ema_register_phoneLogin"));
		mBtnReturnLogin = (Button) findViewById(getId("ema_register_returnLogin"));
		
		mBtnRegister.setOnClickListener(this);
		mBtnRegisterByPhone.setOnClickListener(this);
		mBtnReturnLogin.setOnClickListener(this);

		mEdtPasswView.setKeyListener(new NumberKeyListener() {
			
			@Override
			public int getInputType() {
				return android.text.InputType.TYPE_CLASS_TEXT;
			}
			
			@Override
			protected char[] getAcceptedChars() {
				return PropertyField.PASSW_DIGITS;
			}
		});
		
	}
	
	/**
	 * click事件监听
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == getId("ema_register_login")){//注册并登录
			LOG.d(TAG, "**********TEST me ************");
			doRegister();
		}else if(id == getId("ema_register_phoneLogin")){//进入手机注册界面
			doRegisterByPhone();
		}else if(id == getId("ema_register_returnLogin")){//返回到登录见面
			doReturnLogin();
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		UCommUtil.makeUserCallBack(EmaCallBackConst.LOGINCANELL, "取消注册");
		ToolBar.getInstance(mActivity).showToolBar();
	}
	
	/**
	 * 请求服务器一键注册获取用户名和密码
	 */
	private void doAutoRegisterGetInfo(){
		mProgress.showProgress("自动注册中...", false, false);
		Map<String, String> params = new HashMap<String, String>();
		params.put("app_id", mConfigManager.getAppId());
		UCommUtil.testMapInfo(params);
		new HttpInvoker().postAsync(Url.getRegisterUrlAutoMakeAccount(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
				try {
					JSONObject json = new JSONObject(result);
					JSONObject accoutInfo = json.getJSONObject("account_info");
					
					mLoginName = accoutInfo.getString("loginname");
					mLoginPassw = accoutInfo.getString("passwd");
					
					if(mLoginName == null || mLoginPassw == null){
						LOG.d(TAG, "自动注册失败");
					}else{
						LOG.d(TAG, "自动注册成功");
						mHandler.sendEmptyMessage(CODE_AUTO_REGISTER_GETINFO_SUCCESS);
					}
					
				} catch (Exception e) {
					LOG.d(TAG, "doAutoRegister error", e);
				}
			}
		});
		
	}
	
	
	/**
	 * 一键注册获取用户名和密码成功
	 */
	private void doAutoRegisterGetInfoSucc(){
		if(mLoginName != null && mLoginPassw != null){
			mEdtNameView.setText(mLoginName);
			mEdtPasswView.setText(mLoginPassw);
		}
	}
	
	/**
	 * 注册，并在注册完成后直接登录
	 */
	private void doRegister(){
		if(checkIsAutoRegister()){
			doRegisterAuto();
		}else{
			doRegisterNormal();
		}
	}
	
	/**
	 * 正常的注册，不用从服务器获取的默认账户进行注册登录
	 */
	private void doRegisterNormal(){
		final String account = mEdtNameView.getText().toString();
		String passw = mEdtPasswView.getText().toString();
		if(UserUtil.checkLoginInputIsOk(mActivity, account, passw)){
			//判断账号是否是系统保留账号，如果是的话则提示
			if(isSystemAccount(account))
				return;
			if(is11NumberAccount(account))
				return;
			mProgress.showProgress("注册中...");
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("loginname", account);
			params.put("passwd", passw);
			params.put("device_id", mDeviceInfoManager.getDEVICE_ID());
			params.put("app_id", mConfigManager.getAppId());
			params.put("channel", mConfigManager.getChannel());
			params.put("channelTag", mConfigManager.getChannelTag());

			new HttpInvoker().postAsync(Url.getRegisterUrlNomarl(), params, new HttpInvoker.OnResponsetListener() {
				@Override
				public void OnResponse(String result) {
					mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
					try {
						JSONObject json = new JSONObject(result);
						int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
						switch(resultCode){
						case HttpInvokerConst.SDK_RESULT_SUCCESS://注册成功
							mEmaUser.setNickName(account);
							//mEmaUser.setUUID(json.getString("uuid"));
							//mEmaUser.setSid(json.getString("sid"));
							LOG.d(TAG, "注册成功");
							mHandler.sendEmptyMessage(CODE_REGISTER_SUCCESS);
							break;
						case HttpInvokerConst.REGISTER_ACCOUNT_EXIST://账户已存在
							LOG.d(TAG, "账户已存在");
							mHandler.sendEmptyMessage(CODE_REGISTER_ACCOUNT_EXIST);
							break;
						case HttpInvokerConst.REGISTER_ERROR_EMAIL://邮箱格式错误
							LOG.d(TAG, "邮箱格式错误");
							mHandler.sendEmptyMessage(CODE_REGISTER_ERROR_EMAIL);
							break;
						default:
							LOG.d(TAG, "注册失败");
							mHandler.sendEmptyMessage(CODE_REGISTER_FALIED);
							break;
						}
					} catch (Exception e) {
						LOG.e(TAG, "doRegisterNormal error", e);
						mHandler.sendEmptyMessage(CODE_REGISTER_FALIED);
					}
				}
			});
		}
	}
	
	/**
	 * 检查账号是否是系统保留账号
	 * @return
	 */
	private boolean isSystemAccount(String str) {
		if(str.length() != 12)
			return false;
		String Str1 = (String) str.subSequence(0, 3);
		String Str2 = (String) str.subSequence(3, str.length() - 1);
		if (Str1.matches("[a-z]+") && Str2.matches("[0-9]+")) {
			LOG.d(TAG, "系统保留账号！！！！");
			ToastHelper.toast(mActivity, "账号已存在");
			return true;
		}
		return false;
	}
	
	/**
	 * 判断账号是否是11位纯数字
	 * @param str
	 * @return
	 */
	private boolean is11NumberAccount(String str){
		if(str.length() == 11 && str.matches("[0-9]+")){
			LOG.d(TAG, "账号是11位纯数字，不支持");
			ToastHelper.toast(mActivity, "普通帐号暂不支持11位纯数字！");
			return true;
		}
		return false;
	}
	
	/**
	 * 使用服务器获取到的默认账户进行注册登录（实际上进行的应该是登录操作）
	 */
	private void doRegisterAuto(){
		mProgress.showProgress("注册中...");
		Map<String, String> params = new HashMap<String, String>();
		params.put("loginname", mEdtNameView.getText().toString());
		params.put("passwd", mEdtPasswView.getText().toString());
		params.put("channel", mConfigManager.getChannel());
		params.put("channelTag", mConfigManager.getChannelTag());
		params.put("device_id", mDeviceInfoManager.getDEVICE_ID());
		params.put("app_id", mConfigManager.getAppId());
		
		UCommUtil.buildUrl(Url.getRegisterUrlAuto(), params);
		
		new HttpInvoker().postAsync(Url.getRegisterUrlAuto(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS://注册成功
						mEmaUser.setNickName(mLoginName);
						//mEmaUser.setUUID(json.getString("uuid"));
						//mEmaUser.setSid(json.getString("sid"));
						LOG.d(TAG, "注册成功！");
						mHandler.sendEmptyMessage(CODE_REGISTER_SUCCESS);
						break;
					case HttpInvokerConst.REGISTER_ACCOUNT_EXIST://账户已存在（这种情况按理不应该会出现）
						LOG.d(TAG, "注册失败！账户已存在");
						mHandler.sendEmptyMessage(CODE_REGISTER_ACCOUNT_EXIST);
						break;
					default:
						LOG.d(TAG, "注册失败! ");
						mHandler.sendEmptyMessage(CODE_REGISTER_FALIED);
						break;
					}
				} catch (Exception e) {
					LOG.e(TAG, "doRegisterAuto error", e);
					mHandler.sendEmptyMessage(CODE_REGISTER_FALIED);
				}
			}
		});
	}
	
	/**
	 * 检查用户是否用默认的账号进行注册登录
	 * @return
	 */
	private boolean checkIsAutoRegister(){
		if(mLoginName == null || mLoginPassw == null)
			return false;
		if(mLoginName.equals(mEdtNameView.getText().toString()))
			return true;
		return false;
	}
	
	/**
	 * 进入手机注册界面
	 */
	private void doRegisterByPhone(){
		clear();
		this.dismiss();
		RegisterByPhoneDialog.getInstance(Ema.getInstance().getContext()).show();
	}
	
	/**
	 * 返回登录界面
	 */
	private void doReturnLogin(){
		clear();
		this.dismiss();
		LoginDialog.getInstance(Ema.getInstance().getContext()).show();
	}
	/**
	 * 清理(不确定是否有用，为了让系统回收这个dialog)
	 */
	private void clear(){
		mActivity = null;
		mResourceManager = null;
		mConfigManager = null;
		mDeviceInfoManager = null;
		mEmaUser = null;
		mLoginName = null;
		mLoginPassw = null;
		mLoginSuccDialog = null;
	}
	
	/**
	 * 方法目的（为了防止重复去获取资源ID） 
	 * @param key
	 * @return
	 */
	private int getId(String key){
		if(mIDmap == null){
			mIDmap = new HashMap<String, Integer>();
		}
		if(!mIDmap.containsKey(key)){
			mIDmap.put(key, mResourceManager.getIdentifier(key, "id"));
		}
		return mIDmap.get(key);
	}
}
