package com.emagroup.sdk.pay;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.emagroup.sdk.comm.ConfigManager;
import com.emagroup.sdk.comm.EmaProgressDialog;
import com.emagroup.sdk.comm.HttpInvoker;
import com.emagroup.sdk.comm.HttpInvokerConst;
import com.emagroup.sdk.comm.ResourceManager;
import com.emagroup.sdk.comm.Url;
import com.emagroup.sdk.user.EmaUser;
import com.emagroup.sdk.utils.LOG;
import com.emagroup.sdk.utils.PropertyField;
import com.emagroup.sdk.utils.ToastHelper;
import com.emagroup.sdk.utils.UCommUtil;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SetPayLimit {

	private static final String TAG = "SetPayLimit";
	
	private static final int CODE_CHECK_PASSW_SUCC = 20;
	
	private Context mContext;
	private EmaUser mEmaUser;
	private ConfigManager mConfigManager;
	private ResourceManager mResourceManager;
	
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case EmaProgressDialog.CODE_LOADING_START:
				mProgress.showProgress("验证密码中...");
				break;
			case EmaProgressDialog.CODE_LOADING_END:
				mProgress.closeProgress();
				break;
			case CODE_CHECK_PASSW_SUCC:
				showChangeLimitDialog((String)msg.obj);
				break;
			}
		};
	};
	// 进度条
	private EmaProgressDialog mProgress;
	
	public SetPayLimit(Context context){
		mContext = context;	
		
		mEmaUser = EmaUser.getInstance();
		mConfigManager = ConfigManager.getInstance(mContext);
		mResourceManager = ResourceManager.getInstance(mContext);
		mProgress = new EmaProgressDialog(mContext);
	}
	
	/**
	 * 显示密码验证对话框
	 */
	public void showPasswCheckDialog(){
		final Dialog dialog = new Dialog(mContext, mResourceManager.getIdentifier("ema_activity_dialog", "style"));
		View view = LayoutInflater.from(mContext).inflate(mResourceManager.getIdentifier("ema_set_pay_limit_enter_passw", "layout"), null);
		final EditText edtPassw = (EditText) view.findViewById(mResourceManager.getIdentifier("ema_edt_paypassw", "id"));
		edtPassw.setInputType(InputType.TYPE_CLASS_TEXT |
				InputType.TYPE_TEXT_VARIATION_PASSWORD);
		//限制密码不能输入中文，和指定的英文
		edtPassw.setKeyListener(new NumberKeyListener() {
			@Override
			public int getInputType() {
				return android.text.InputType.TYPE_CLASS_TEXT;
			}
			@Override
			protected char[] getAcceptedChars() {
				// TODO Auto-generated method stub
				return PropertyField.PASSW_DIGITS;
			}
		});
		//取消
		view.findViewById(mResourceManager.getIdentifier("ema_btn_cancel", "id")).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LOG.d(TAG, "取消密码验证");
				dialog.dismiss();
			}
		});
		//确定
		view.findViewById(mResourceManager.getIdentifier("ema_btn_sure", "id")).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String passw = edtPassw.getText().toString();
				if(UCommUtil.isStrEmpty(passw)){
					LOG.d(TAG, "密码输入为空");
					ToastHelper.toast(mContext, PropertyField.ERROR_PASSW_CAN_NOT_NULL);
					return;
				}
				if(passw.length() < 6 || passw.length() > 16){
					LOG.d(TAG, "密码长度不符合");
					ToastHelper.toast(mContext, PropertyField.ERROR_PASSW_INCORRECT_LENGTH);
					return;
				}
				mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_START);
				dialog.dismiss();
				doCheckPassw(passw);
			}
		});
		dialog.setContentView(view);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	/**
	 * 验证密码
	 */
	private void doCheckPassw(final String passw){
		Map<String, String> params = new HashMap<String, String>();
		params.put("app_id", mConfigManager.getAppId());
		params.put("sid", mEmaUser.getAccessSid());
		params.put("uuid", mEmaUser.getUUID());
		params.put("wallet_pwd", passw);
		String sign = UCommUtil.getSign(mConfigManager.getAppId(),
				mEmaUser.getAccessSid(),
				mEmaUser.getUUID(),
				mConfigManager.getAppKEY());
		params.put("sign", sign);
		
		new HttpInvoker().postAsync(Url.getPayUrlCheckPayPassw(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS:
						LOG.d(TAG, "密码验证通过");
						Message msg = new Message();
						msg.what = CODE_CHECK_PASSW_SUCC;
						msg.obj = passw;
						mHandler.sendMessage(msg);
						break;
					case HttpInvokerConst.PAY_RECHARGE_FAILED_PASSW_ERROR:
					case HttpInvokerConst.SDK_RESULT_ACCOUNT_NOT_EXIST:
						ToastHelper.toast(mContext, PropertyField.ERROR_PASSW_ERROR);
						LOG.d(TAG, "密码验证失败");
						break;
					default:
						LOG.d(TAG, "密码验证失败");
						ToastHelper.toast(mContext, PropertyField.ERROR_PASSW_CHECK_FAILED);
						break;
					}
				} catch (Exception e) {
					ToastHelper.toast(mContext, PropertyField.ERROR_PASSW_CHECK_FAILED);
					LOG.w(TAG, "验证失败", e);
				}
			}
		});
	}
	
	/**
	 * 显示修改支付限额对话框
	 */
	private void showChangeLimitDialog(final String passw){
		final Dialog dialog = new Dialog(mContext, mResourceManager.getIdentifier("ema_activity_dialog", "style"));
		View view = LayoutInflater.from(mContext).inflate(mResourceManager.getIdentifier("ema_set_pay_limit", "layout"), null);
		final EditText edtLimit = (EditText) view.findViewById(mResourceManager.getIdentifier("ema_edt_paylimit", "id"));
		edtLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
		edtLimit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(!UCommUtil.isStrEmpty(s.toString())){
					isLimitOk(edtLimit);
				}
			}
		});
		((TextView) view.findViewById(mResourceManager.getIdentifier("ema_txt_prompt", "id")))
			.setText("当前支付限额：" + PropertyField.EMA_COIN_UNIT);
		//取消
		view.findViewById(mResourceManager.getIdentifier("ema_btn_cancel", "id")).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LOG.d(TAG, "取消修改支付限额");
				dialog.dismiss();
			}
		});
		//确定
		view.findViewById(mResourceManager.getIdentifier("ema_btn_sure", "id")).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(UCommUtil.isStrEmpty(edtLimit.getText().toString())){
					ToastHelper.toast(mContext, "支付限额不能为空");
					return;
				}
				dialog.dismiss();
				doChangeLimit(edtLimit.getText().toString(), passw);
			}
		});
		dialog.setContentView(view);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	/**
	 * 修改支付额度
	 */
	private void doChangeLimit(final String limit, String passw){
		Map<String, String> params = new HashMap<String, String>();
		params.put("app_id", mConfigManager.getAppId());
		params.put("sid", mEmaUser.getAccessSid());
		params.put("uuid", mEmaUser.getUUID());
		params.put("cur_passwd", passw);
		params.put("limit", Integer.valueOf(limit) * 100 + "");
		String sign = UCommUtil.getSign(mConfigManager.getAppId(),
				mEmaUser.getAccessSid(),
				mEmaUser.getUUID(),
				mConfigManager.getAppKEY());
		params.put("sign", sign);
		
		 new HttpInvoker().postAsync(Url.getPayUrlSetPayLimit(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS:
						LOG.d(TAG, "修改支付限额成功");
						ToastHelper.toast(mContext, "修改支付限额成功");
				//		mEmaUser.setPayLimit(Integer.valueOf(limit) * 100);
						Activity ac = (Activity) mContext;
						if(ac instanceof PayMabiActivity){
							PayMabiActivity pac = (PayMabiActivity) ac;
							Message msg = new Message();
							msg.what = PayMabiActivity.CODE_SET_LIMIT_SUCC;
							pac.onPayLimitCallBack(msg);
						}
						break;
					case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR:
						LOG.d(TAG, "签名验证失败");
					default:
						LOG.d(TAG, "修改支付限额失败");
						ToastHelper.toast(mContext, "修改支付限额失败");
						break;
					}
				} catch (Exception e) {
					LOG.w(TAG, "修改支付额度失败");
					ToastHelper.toast(mContext, "修改支付限额失败");
				}
			}
		});
		
	}
	
	/**
	 * 检查输入的限额是否符合规定
	 * 首先不能以0开头
	 * 其实最多5位数字
	 * @param str
	 * @param edtLimit
	 */
	private void isLimitOk(EditText edtLimit){
		String limit = edtLimit.getText().toString();
		if(limit.startsWith("0")){
			edtLimit.setText("");
		}
		if(limit.length() > 5){
			edtLimit.setText(limit.subSequence(0, 5));
			edtLimit.setSelection(edtLimit.getText().toString().length());
		}
	}
}
