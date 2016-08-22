package cn.emagroup.sdk.pay;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import cn.emagroup.sdk.Ema;
import cn.emagroup.sdk.comm.ConfigManager;
import cn.emagroup.sdk.comm.DeviceInfoManager;
import cn.emagroup.sdk.comm.EmaCallBackConst;
import cn.emagroup.sdk.comm.HttpInvoker;
import cn.emagroup.sdk.comm.HttpInvokerConst;
import cn.emagroup.sdk.comm.Url;
import cn.emagroup.sdk.user.EmaUser;
import cn.emagroup.sdk.utils.LOG;
import cn.emagroup.sdk.utils.ToastHelper;
import cn.emagroup.sdk.utils.UCommUtil;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

/**
 * 将每一次的支付过程当作一个对象
 * @author yang.zhang
 */
public class EmaPay {

	private static final String TAG = "EmaPay";

	private static EmaPay mInstance;
	private static final Object synchron = new Object();
	
	private Context mContext;
	private EmaUser mEmaUser;
	private ConfigManager mConfigManager;
	private DeviceInfoManager mDeviceInfoManager;
	private EmaPayListener mPayListener;

	private EmaPayInfoBean mPayInfoBean;
	
	private EmaPay(Context context){
		mContext = context;
		mEmaUser = EmaUser.getInstance();
		mEmaUser.clearPayInfo();
		mConfigManager = ConfigManager.getInstance(context);
		mDeviceInfoManager = DeviceInfoManager.getInstance(context);
	}
	
	public static EmaPay getInstance(Context context){
		if(mInstance == null){
			synchronized (synchron) {
				if(mInstance == null){
					mInstance = new EmaPay(context);
				}
			}
		}
		return mInstance;
	}
	
	/**
	 * 获取支付相关信息
	 * @return
	 */
	public EmaPayInfoBean getPayInfo(){
		return mPayInfoBean;
	}
	
	/**
	 * 开启支付
	 * @param context
	 */
	public void pay(EmaPayInfoBean payInfoBean, EmaPayListener payListener){
		//由pay入口给 mPayInfoBean赋值
		mPayInfoBean = payInfoBean;
		mPayListener = payListener;
		
		if(!mEmaUser.getIsLogin()){
			ToastHelper.toast(mContext, "还未登陆，请先登陆！");
			LOG.d(TAG, "没有登陆，或者已经退出！");
			return;
		}
		//检查支付参数是否符合相应的规则
		if(!payInfoBean.getFlagCheckOk()){
			ToastHelper.toast(mContext, payInfoBean.getErrorInfo());
			return;
		}
		
		/*PayUtil.getWalletSetting(mConfigManager.getAppId(),mEmaUser.getAccessSid(),
				mEmaUser.getUUID(), mConfigManager.getAppKEY(), new HttpInvoker.OnResponsetListener() {
					@Override
					public void OnResponse(String result) {
						try {
							JSONObject json = new JSONObject(result);
							int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
							switch(resultCode){
							case HttpInvokerConst.SDK_RESULT_SUCCESS:
								LOG.d(TAG, "获取钱包信息成功！");
								LOG.d(TAG, json.getInt("balance") + "");
								LOG.d(TAG, json.getInt("pay_limit") + "");
								mEmaUser.setBalance(json.getInt("balance"));
								mEmaUser.setPayLimit(json.getInt("pay_limit"));
								mEmaUser.setIsWalletHasPassw(json.getString("is_wallet_pwd"));
								doSelectPay();
								break;
							case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR://签名验证失败
								LOG.d(TAG, "签名验证失败");
							default:
								LOG.e(TAG, "获取钱包信息失败");
								makePayCallback(EmaCallBackConst.PAYFALIED, "获取钱包信息失败");
								break;
							}
						} catch (Exception e) {
							LOG.e(TAG, "pay error", e);
							makePayCallback(EmaCallBackConst.PAYFALIED, "支付时，获取余额失败！可能网络出现问题！");
						}
					}
				});*/
		doSelectPay();
	}
	
	/**
	 * 选择支付方式
	 */
	private void doSelectPay(){
		Intent intent = null;
		if(mPayInfoBean.getAmount_pricebean().getPriceFen() > mEmaUser.getBalance()){
			LOG.d(TAG, "支付总额大于余额！显示第三方支付");
			intent = new Intent(mContext, PayTrdActivity.class);
		}else{
			LOG.d(TAG, "支付总额小于余额，不显示第三方支付");
			intent = new Intent(mContext, PayMabiActivity.class);
		}
		mContext.startActivity(intent);
	}
	
	/**
	 * 构建支付参数
	 * @param
	 * @return
	 */
	protected Map<String, String> buildPayParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("client_id", ConfigManager.getInstance(Ema.getInstance().getContext()).getChannel());
		map.put("app_id", mConfigManager.getAppId());
		map.put("order_amount", (int)(mPayInfoBean.getAmount_pricebean().getPriceFen()) + "");
		map.put("amount", (int)(mPayInfoBean.getAmount_pricebean().getPriceFen()) + "");
		map.put("bank_id", "0");
		map.put("app_order_id", mPayInfoBean.getApp_order_id());//cp_id
		map.put("product_id", mPayInfoBean.getProduct_id());
		map.put("product_name", mPayInfoBean.getProduct_name());
		map.put("product_num", mPayInfoBean.getProduct_num() + "");
		map.put("device_id", mDeviceInfoManager.getDEVICE_ID());
		map.put("channel", mConfigManager.getChannel());
		return map;
	}
	
	/**
	 * 设置支付回调
	 * @param code
	 * @param obj
	 */
	public void makePayCallback(int code, Object obj){
		if(mPayListener == null){
			LOG.w(TAG, "未设置支付回调");
			return;
		}
		Message msg = new Message();
		msg.what = code;
		msg.obj = obj;
		mPayListener.onPayCallBack(msg);
	}
	
}
