package com.emagroup.sdk;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class EmaDialogPay0YuanFuPassw extends Dialog implements android.view.View.OnClickListener {

	public static interface OnResultListener{
		public abstract void onResult(String code, String result);
	}
	
	private static final String TAG = EmaDialogPay0YuanFuPassw.class.toString();

	private Context mContext;
	private ResourceManager mResourceManager;// 资源管理
	private OnResultListener mListener;
	private float mPrice;
	
	//views
	private TextView mTxtPrice;
	private TextView mBtnSure;
	private TextView mBtnCancel;
	private EditText mEdtPassw;//密码输入框
	
	//保存按钮的资源ID
	private int mIDBtnSure;
	private int mIDBtnCancle;
	
	// 进度条
	private EmaProgressDialog mProgress;
	
	public EmaDialogPay0YuanFuPassw(Context context, float price, OnResultListener listener) {
		super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
		this.mContext = context;
		mResourceManager = ResourceManager.getInstance(context);
		mProgress = new EmaProgressDialog(context);
		mListener = listener;
		mPrice = price;
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
		setContentView(mResourceManager.getIdentifier("ema_pay_0yuanfu_passw", "layout"));
		mTxtPrice = (TextView) findViewById(mResourceManager.getIdentifier("ema_txt_price", "id"));
		mBtnCancel = (TextView) findViewById(getIDBtnCancel());
		mBtnSure = (TextView) findViewById(getIDBtnSure());
		mEdtPassw = (EditText) findViewById(mResourceManager.getIdentifier("ema_edt_passw", "id"));
		mBtnSure.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		mTxtPrice.setText("我们将从您的0元付账户扣除：" + mPrice + " 元");
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == getIDBtnSure()){//验证密码
			verifyPaymentPassword();
		}else if(id == getIDBtnCancel()){//取消密码输入
			//do nothing, just dismiss
			this.dismiss();
		}
	}
	
	/**
	 * 验证交易密码
	 */
	private void verifyPaymentPassword(){
		mProgress.showProgress("验证密码...", false, false);
		Map<String, String> params = new HashMap<String, String>();
		JSONObject json = new JSONObject();
		String merchant = ConfigManager.getInstance(mContext).getMerchant();
		try {
			json.put("openId", EmaUser.getInstance().getUUID());
			json.put("merchant", merchant);
			String passwrod = UCommUtil.MD5(mEdtPassw.getText().toString());
			json.put("password", passwrod);
			json.put("msgKey", UCommUtil.MD5(EmaUser.getInstance().getUUID() + merchant + passwrod));
		} catch (Exception e) {
		}
		params.put("request", json.toString());
		UCommUtil.testMapInfo(params);
		new HttpInvoker().postAsync(Url.LINGYUANFU_VERIFY_PAYMENT_PASSWORD, params, new HttpInvoker.OnResponsetListener() {
			
			@Override
			public void OnResponse(String result) {
				closeProgress();
				try {
					JSONObject data = new JSONObject(result);
					mListener.onResult(data.getString("result"), data.getString("message"));
					if(data.getString("result").equals("1")){
						EmaDialogPay0YuanFuPassw.this.dismiss();
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
