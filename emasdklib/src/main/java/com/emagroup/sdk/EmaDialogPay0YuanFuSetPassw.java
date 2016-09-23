package com.emagroup.sdk;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EmaDialogPay0YuanFuSetPassw extends Dialog implements android.view.View.OnClickListener {

//	测试外网地址（内网测试地址没端口号）：http://openapi.test.nonobank.com:8084/nono-web/creditAuth/updatePaymentPassword
//		预发地址：https://openapi.idc.nonobank.com/nono-web/creditAuth/updatePaymentPassword 
//		生产地址：https://openapi.nonobank.com/nono-web/creditAuth/updatePaymentPassword 

	
	public static interface OnResultListener{
		public abstract void onResult(String code, String result);
	}
	
	private static final String TAG = EmaDialogPay0YuanFuSetPassw.class.toString();

	private Context mContext;
	private ResourceManager mResourceManager;// 资源管理
	private OnResultListener mListener;

	//views
	private TextView mBtnSure;
	private TextView mBtnCancel;
	private EditText mEdtPassw;//密码输入框
	
	//保存按钮的资源ID
	private int mIDBtnSure;
	private int mIDBtnCancle;
	
	// 进度条
	private EmaProgressDialog mProgress;
	
	public EmaDialogPay0YuanFuSetPassw(Context context, OnResultListener listener) {
		super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
		this.mContext = context;
		mResourceManager = ResourceManager.getInstance(context);
		mProgress = new EmaProgressDialog(context);
		this.mListener = listener;
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		
		initView();
	}

	private void initView(){
		setContentView(mResourceManager.getIdentifier("ema_pay_0yuanfu_set_passw", "layout"));
		mBtnCancel = (TextView) findViewById(getIDBtnCancel());
		mBtnSure = (TextView) findViewById(getIDBtnSure());
		mEdtPassw = (EditText) findViewById(mResourceManager.getIdentifier("ema_edt_passw", "id"));
		mBtnSure.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == getIDBtnSure()){//设置密码
			setPaymentPassword();
		}else if(id == getIDBtnCancel()){//取消密码设置
			//do nothing, just dismiss
			this.dismiss();
		}
	}
	
	/**
	 * 设置交易密码
	 * 密码必须是6位数字
	 */
	private void setPaymentPassword(){
		String passw = mEdtPassw.getText().toString();
		if(UCommUtil.isStrEmpty(passw)){
			ToastHelper.toast(mContext, "密码不能为空");
			return;
		}
		if(passw.length() != 6){
			ToastHelper.toast(mContext, "密码必须是6位数字");
			return;
		}
		if(!passw.matches("^\\d+$")){
			ToastHelper.toast(mContext, "密码必须是6位数字");
			return;
		}
		mProgress.showProgress("设置密码...", false, false);
		Map<String, String> params = new HashMap<String, String>();
		JSONObject json = new JSONObject();
		String merchant = ConfigManager.getInstance(mContext).getMerchant();
		try {
			//json.put("openId", EmaUser.getInstance().getUUID());
			json.put("merchant", merchant);
			String passwrod = UCommUtil.MD5(mEdtPassw.getText().toString());
			json.put("password", passwrod);
			//json.put("msgKey", UCommUtil.MD5(EmaUser.getInstance().getUUID() + merchant + passwrod));
		} catch (Exception e) {
		}
		params.put("request", json.toString());
		UCommUtil.testMapInfo(params);
		new HttpInvoker().postAsync(Url.LINGYUANFU_UPDATE_PAYMENT_PASSWORD, params, new HttpInvoker.OnResponsetListener() {
			
			@Override
			public void OnResponse(String result) {
				closeProgress();
				try {
					JSONObject data = new JSONObject(result);
					mListener.onResult(data.getString("result"), data.getString("message"));
					if(data.getString("result").equals("1")){
						EmaDialogPay0YuanFuSetPassw.this.dismiss();
					}
				} catch (Exception e) {
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
