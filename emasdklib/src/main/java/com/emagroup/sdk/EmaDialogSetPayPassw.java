package com.emagroup.sdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EmaDialogSetPayPassw extends Dialog implements android.view.View.OnClickListener {

	

	private static final String TAG = "EmaDialogSetPayPassw";
		
	private Activity mActivity;
	private ResourceManager mResourceManager;// 资源管理
	private ConfigManager mConfigManager;// 配置项管理
	private EmaUser mEmaUser;// 当前登录用户信息
	private Handler mHandler;
	
	//views
	private TextView mBtnSure;
	private TextView mBtnCancel;
	private EditText mEdtPassw;//密码输入框
	
	//保存按钮的资源ID
	private int mIDBtnSure;
	private int mIDBtnCancle;
	
	// 进度条
	private EmaProgressDialog mProgress;
	
	public EmaDialogSetPayPassw(Context context, Handler handler) {
		super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
		mActivity = (Activity) context;
		mResourceManager = ResourceManager.getInstance(mActivity);
		mConfigManager = ConfigManager.getInstance(mActivity);
		mEmaUser = EmaUser.getInstance();
		mHandler = handler;
		mProgress = new EmaProgressDialog(mActivity);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		
		initView();
	}
	
	private void initView() {
		setContentView(mResourceManager.getIdentifier("ema_set_pay_passw", "layout"));
		mBtnCancel = (TextView) findViewById(getIDBtnCancel());
		mBtnSure = (TextView) findViewById(getIDBtnSure());
		mEdtPassw = (EditText) findViewById(mResourceManager.getIdentifier("ema_edt_passw", "id"));
		mBtnSure.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		
		mEdtPassw.setKeyListener(new NumberKeyListener() {
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
	 * Click监听事件
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == getIDBtnSure()){
			doSetPassw();
		}else if(id == getIDBtnCancel()){
			//通知取消设置支付密码
			mHandler.sendEmptyMessage(PayConst.CODE_SET_PASSW_CANCEL);
		}
	}
	
	/**
	 * 设置支付密码
	 */
	public void doSetPassw(){
		final String passw = mEdtPassw.getText().toString();
		if(UCommUtil.isStrEmpty(passw)){
			LOG.d(TAG, "密码为空");
			ToastHelper.toast(mActivity, PropertyField.ERROR_PASSW_CAN_NOT_NULL);
			return;
		}
		if(passw.length() < 6 || passw.length() > 16){
			LOG.d(TAG, "密码长度不符合规矩");
			ToastHelper.toast(mActivity, PropertyField.ERROR_PASSW_INCORRECT_LENGTH);
			return;
		}
		
		mProgress.showProgress("设置密码中...", false, false);
		
		Map<String, String> params = new HashMap<>();
		params.put("uid", mEmaUser.getmUid());
		params.put("chargePwd", passw);
		params.put("token",mEmaUser.getmToken());
		new HttpInvoker().postAsync(Url.getSetWalletPwdUrl(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt("status");
					Message msg = new Message();
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS://设置成功
						LOG.d(TAG, "密码设置成功！！！");
						mEmaUser.setIsWalletHasPassw(true);
						msg.what = PayConst.CODE_SET_PASSW_SUCC;
						break;
					default:
						LOG.d(TAG, "密码设置失败！！！");
						msg.what = PayConst.CODE_SET_PASSW_FAILED;
						break;
					}
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					mHandler.sendEmptyMessage(PayConst.CODE_SET_PASSW_FAILED);
					LOG.w(TAG, "doSetPassw error", e);
				}
			}
		});
	}
	
	public void closeProgress(){
		mProgress.closeProgress();
	}
	
	/**
	 * 获取确定按钮的资源ID
	 * @return
	 */
	private int getIDBtnSure(){
		if(mIDBtnSure == 0){
			mIDBtnSure = mResourceManager.getIdentifier("ema_btn_sure", "id");
		}
		return mIDBtnSure;
	}
	
	/**
	 * 获取取消按钮的资源ID
	 * @return
	 */
	private int getIDBtnCancel(){
		if(mIDBtnCancle == 0){
			mIDBtnCancle = mResourceManager.getIdentifier("ema_btn_cancel", "id");
		}
		return mIDBtnCancle;
	}
	
}
