package com.emagroup.sdk.pay;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Context;
import com.emagroup.sdk.comm.ConfigManager;
import com.emagroup.sdk.comm.DeviceInfoManager;
import com.emagroup.sdk.comm.EmaCallBackConst;
import com.emagroup.sdk.comm.EmaProgressDialog;
import com.emagroup.sdk.comm.HttpInvoker;
import com.emagroup.sdk.comm.HttpInvokerConst;
import com.emagroup.sdk.comm.Url;
import com.emagroup.sdk.user.EmaUser;
import com.emagroup.sdk.utils.EmaConst;
import com.emagroup.sdk.utils.LOG;
import com.emagroup.sdk.utils.PropertyField;
import com.emagroup.sdk.utils.ToastHelper;
import com.emagroup.sdk.utils.UCommUtil;

public class TrdWeixinPay {

	private static final String TAG = TrdWeixinPay.class.toString();
	
	/**
	 * 调用微信开始支付
	 */
	public static void startPay(final Context context){
		LOG.d(TAG, "获取支付宝订单号");
		LOG.d(TAG, "orderInfo:" + EmaPayProcessManager.getInstance().getWeixinOrderInfo());
		if(EmaPayProcessManager.getInstance().getWeixinOrderInfo() == null){
			getPayOrderId(context);
		}else{
			PayUtil.sendPayMessage((Activity)context, EmaProgressDialog.CODE_LOADING_END, null);
			pay((Activity)context, EmaPayProcessManager.getInstance().getWeixinOrderInfo());
		}
	}
	
	/**
	 * 调用微信开始充值
	 * @param context
	 */
	public static void startRecharge(Activity activity, EmaPriceBean money){
		getRechargeOrderId(activity, money);
	}
	
	/**
	 * 获取微信充值订单号
	 * @param activity
	 * @param money
	 */
	private static void getRechargeOrderId(final Context context, final EmaPriceBean money){
		EmaUser emaUser = EmaUser.getInstance();
		ConfigManager configManager = ConfigManager.getInstance(context);
		DeviceInfoManager deviceInfoManager = DeviceInfoManager.getInstance(context);
		EmaPay emaPay = EmaPay.getInstance(context);
		
		Map<String, String> params = new HashMap<String, String>();
		
		params.put("client_id", ConfigManager.getInstance(context).getChannel());// 发起站点
		params.put("app_id", configManager.getAppId());
		params.put("partition_id", String.valueOf(0));//分区ID
		params.put("server_id", String.valueOf(0));//服务器ID
		params.put("order_amount", money.getPriceFen() + "");// 订单金额
		params.put("amount", money.getPriceFen() + "");// 总额
		params.put("point", String.valueOf(0));// 积分
		params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_WEIXINPAY + "");// 充值方式ID
		params.put("bank_id", "0");// bank_id:银行ID
		params.put("coupon", null);// 优惠券
		params.put("app_order_id", "order" + System.currentTimeMillis());// 第三方订单号
		params.put("product_id", "0");// 商品ID
		params.put("product_name", PropertyField.EMA_COIN_UNIT);// 商品名字
		params.put("product_num", String.valueOf(1));// 商品数量
		params.put("ext", "充值钱包");// 附加信息
		params.put("wallet_amount","0");
		params.put("change_app_id","1001");//充值钱包特定参数
		params.put("device_id", deviceInfoManager.getDEVICE_ID());//设备ID
		params.put("channel", configManager.getChannel());
		
		params.put("sid", emaUser.getAccessSid());
		params.put("uuid", emaUser.getUUID());
		
		String sign = UCommUtil.getSign(configManager.getAppId(),
				emaUser.getAccessSid(),
				emaUser.getUUID(),
				configManager.getAppKEY());
		params.put("sign", sign);
		new HttpInvoker().postAsync(Url.getPayUrlRecharge(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS://获取订单号成功
						LOG.d(TAG, "微信统一下单成功");
						JSONObject data = json.getJSONObject("data");
						EmaPayProcessManager.getInstance().setWeixinOrdreInfo(data);
						pay((Activity)context, data);
						break;
					case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR://签名验证失败
						LOG.d(TAG, "签名验证失败，获取订单号失败:");
						ToastHelper.toast(context, "创建订单号失败 errorCode:" + resultCode);
						UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "签名验证失败，获取订单号失败");
						break;
					case HttpInvokerConst.PAY_RECHARGE_FAILED_ORDERID_REPEAT://订单号重复
						LOG.d(TAG, "订单号重复,获取订单号失败");
						UCommUtil.showSystemBusyDialog(context);
						break;
					default:
						LOG.d(TAG, "获取订单号失败，失败原因未知:unknow error");
						ToastHelper.toast(context, "创建订单号失败 errorCode:" + resultCode);
						UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "微信统一下单失败");
						break;
					}
				} catch (Exception e) {
					LOG.w(TAG, "getOrderId error", e);
					UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "获取订单号失败");
				}
			}
		});
	}
	
	/**
	 * 获取支付订单号
	 * @param context
	 * @param handler
	 */
	private static void getPayOrderId(final Context context){
		EmaUser mEmaUser = EmaUser.getInstance();
		EmaPayInfo payInfo = EmaPay.getInstance(context).getPayInfo();
		ConfigManager mConfigManager = ConfigManager.getInstance(context);
		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", ConfigManager.getInstance(context).getChannel());
		params.put("app_id", mConfigManager.getAppId());
		/*params.put("order_amount", (int)(payInfoBean.getAmount_pricebean().getPriceFen()) + "");
		params.put("amount", (int)(payInfoBean.getAmount_pricebean().getPriceFen()) + "");
		params.put("bank_id", "0");
		params.put("app_order_id", payInfoBean.getApp_order_id());//cp_id
		params.put("product_id", payInfoBean.getProduct_id());
		params.put("product_name", payInfoBean.getProduct_name());
		params.put("product_num", payInfoBean.getProduct_num() + "");*/
		params.put("device_id", DeviceInfoManager.getInstance(context).getDEVICE_ID());
		params.put("channel", mConfigManager.getChannel());
		params.put("wallet_pwd", "0");
		params.put("wallet_amount", "0");
		params.put("sid", mEmaUser.getAccessSid());
		params.put("uuid", mEmaUser.getUUID());
		params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_WEIXINPAY + "");
		String sign = UCommUtil.getSign(
				mConfigManager.getAppId(),
				mEmaUser.getAccessSid(),
				mEmaUser.getUUID(),
				mConfigManager.getAppKEY());
		params.put("sign", sign);
		
		UCommUtil.testMapInfo(params);
		UCommUtil.buildUrl(Url.getPayUrlRecharge(), params);
		
		new HttpInvoker().postAsync(Url.getPayUrlRecharge(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				PayUtil.sendPayMessage((Activity)context, EmaProgressDialog.CODE_LOADING_END, null);
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS://获取订单号成功
						LOG.d(TAG, "微信统一下单成功");
						JSONObject data = json.getJSONObject("data");
						EmaPayProcessManager.getInstance().setWeixinOrdreInfo(data);
						pay((Activity)context, data);
						break;
					case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR://签名验证失败
						LOG.d(TAG, "签名验证失败，获取订单号失败:");
						ToastHelper.toast(context, "创建订单号失败 errorCode:" + resultCode);
						UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "签名验证失败，获取订单号失败");
						break;
					case HttpInvokerConst.PAY_RECHARGE_FAILED_ORDERID_REPEAT://订单号重复
						LOG.d(TAG, "订单号重复,获取订单号失败");
						UCommUtil.showSystemBusyDialog(context);
						break;
					default:
						LOG.d(TAG, "获取订单号失败，失败原因未知:unknow error");
						ToastHelper.toast(context, "创建订单号失败 errorCode:" + resultCode);
						UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "微信统一下单失败");
						break;
					}
				} catch (Exception e) {
					LOG.w(TAG, "getOrderId error", e);
					UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "微信统一下单失败");
				}
			}
		});
	}
	
	/**
	 * 调起微信支付
	 * @param activity
	 * @param orderId
	 * @param handler
	 */
	private static void pay(Activity activity, JSONObject data){
		try {
			String wxAppid = data.getString("appid");
			EmaConst.EMA_WEIXIN_APPID = wxAppid;
			//注册应用到微信
			IWXAPI mWxApi = WXAPIFactory.createWXAPI(activity, wxAppid, false);
			mWxApi.registerApp(wxAppid);
			
			PayReq request = new PayReq();
			request.appId = wxAppid;
			request.partnerId = data.getString("mch_id");
			request.prepayId= data.getString("prepay_id");
			request.packageValue = data.getString("package");
			request.nonceStr= data.getString("nonce_str");
			request.timeStamp= data.getString("timestamp");
			request.sign= data.getString("sign");
			mWxApi.sendReq(request);
			LOG.d(TAG, "weixin send Request...");
		} catch (Exception e) {
			LOG.w(TAG, "weixin pay failed", e);
		}
	}
}
