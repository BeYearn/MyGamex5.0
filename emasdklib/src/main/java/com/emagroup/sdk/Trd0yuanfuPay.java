package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 0元付的支付流程
 * ------------------------------------
 * step1：首先判断是否开通了0元付的信用支付
 * 	测试外网地址（内网测试地址没端口号）：http://openapi.test.nonobank.com:8084/nono-web/creditPayment/isPaymentActivated
 *	预发地址：https://openapi.idc.nonobank.com/nono-web/creditPayment/isPaymentActivated 
 *	生产地址：https://openapi.nonobank.com/nono-web/creditPayment/isPaymentActivated 
 * 
 * 	结果：
 * 		未开通：需要用webView跳转到0元付指定的url界面，进行信用支付的开通操作（目前url还未给到）
 * 				url：	https://openapi.idc.nonobank.com/h5/yima?openId=xxx  xxx是ema平台的userId 这个地址是测试环境的，生产是另外的地址
 * 		开通了：继续step2
 * 
 * step2：查询用户是否设置了密码
 *  测试外网地址（内网测试地址没端口号）：http://openapi.test.nonobank.com:8084/nono-web/creditAuth/isPaymentPasswordExist
 *  预发地址：https://openapi.idc.nonobank.com/nono-web/creditAuth/isPaymentPasswordExist 
 *  生产地址：https://openapi.nonobank.com/nono-web/creditAuth/isPaymentPasswordExist 
 * 
 *	结果：
 *		未设置：跳转step3
 *		已设置：跳转step4
 * 
 * step3：为用户设置密码
 * 	测试外网地址（内网测试地址没端口号）：http://openapi.test.nonobank.com:8084/nono-web/creditAuth/updatePaymentPassword
 *  预发地址：https://openapi.idc.nonobank.com/nono-web/creditAuth/updatePaymentPassword 
 *  生产地址：https://openapi.nonobank.com/nono-web/creditAuth/updatePaymentPassword 
 *
 *	结果:
 *		设置成功：跳转step4
 *		设置失败：提示用户设置密码失败
 * 
 * step4：弹出密码输入框，让用户输入0元付支付密码，并验证密码是否正确
 *  测试外网地址（内网测试地址没端口号）：http://openapi.test.nonobank.com:8084/nono-web/creditAuth/verifyPaymentPassword
 *  预发地址：https://openapi.idc.nonobank.com/nono-web/creditAuth/verifyPaymentPassword 
 *  生产地址：https://openapi.nonobank.com/nono-web/creditAuth/verifyPaymentPassword 
 *	
 *	结果：
 *		密码正确：跳转step5
 *		密码错误：提示用户密码错误
 *
 *
 * step5：为这次支付申请一个订单号，从ema服务器
 * 	结果：
 * 		申请订单成功：跳转step6
 * 		申请订单失败：理论上不存在，存在即BUG
 * 
 * step6：调用支付接口进行支付，支付结果在客户端返回
 *  测试外网地址（内网测试地址没端口号）：http://openapi.test.nonobank.com:8084/nono-web/creditPayment/pay
 *  预发地址：https://openapi.idc.nonobank.com/nono-web/creditPayment/pay 
 *  生产地址：https://openapi.nonobank.com/nono-web/creditPayment/pay
 *   
 * 结果：
 * 		支付成功：跳转step7
 * 		支付失败：提示支付失败
 * 
 * step7：客户端返回0元支付成功，通知ema服务器去0元支付的服务器去做订单支付成功的验证(客户端只负责通知服务器，订单是否成功的验证由服务器去做的)
 *  测试外网地址（内网测试地址没端口号）：https://openapi.test.nonobank.com:8084/nono-web/creditPayment/isPayOrder 
 *  预发地址：https://openapi.idc.nonobank.com/nono-web/creditPayment/isPayOrder 
 *  生产地址：https://openapi.nonobank.com/nono-web/creditPayment/isPayOrder  
 *  
 * 结果：
 * 		验证成功：通知发货服务器发货
 * 		验证失败：这个正常情况下应该不存在，存在即 BUG
 * 
 * ------------------------------------
 * @author zhangyang
 *
 */
public class Trd0yuanfuPay {

	private static final String TAG = Trd0yuanfuPay.class.toString();
	
	private static final int CODE_PAYMENT_IS_ACTIVATED = 100;//用户已激活0元付
	private static final int CODE_PAYMENT_IS_NOT_ACTIVATED = 101;//用户未激活0元付
	private static final int CODE_PAYMENT_PASSW_EXIST = 102;//用户设置了密码
	private static final int CODE_PAYMENT_PASSW_NOT_EXIST = 103;//用户未设置密码
	private static final int CODE_PAYMENT_PASSW_UPDATE_SUCC = 104;//用户设置密码成功
	private static final int CODE_PAYMENT_PASSW_UPDATE_FAILED = 105;//用户设置密码失败
	private static final int CODE_PAYMENT_PASSW_VERIFY_SUCC = 106;//用户密码正确
	private static final int CODE_PAYMENT_PASSW_VERIFY_FAILED = 107;//用户密码错误
	private static final int CODE_PAYMENT_PARAMS_ERROR = -1;//参数错误
	
	private static interface on0YuanFuResultListener{
		public abstract void onResult(int code, String result);
	}
	
	/**
	 * 开始支付
	 * @param activity
	 * @param handler
	 */
	public static void startPay(final Activity activity, PayTrdItemBean bean, final Handler handler){
		start0YuanFu(activity, null, bean, handler);
	}
	
	/**
	 * 开始充值
	 * @param activity
	 * @param money
	 * @param handler
	 */
	public static void startRecharge(Activity activity, EmaPriceBean money, Handler handler){
		start0YuanFu(activity, money, null, handler);
	}
	
	/**
	 * 开始0元付的支付流程
	 * @param activity
	 * @param money
	 * @param handler
	 */
	private static void start0YuanFu(final Activity activity, final EmaPriceBean money, final PayTrdItemBean bean, final Handler handler){
		on0YuanFuResultListener listener = new on0YuanFuResultListener() {
			@Override
			public void onResult(int code, String result) {
				switch(code){
				case CODE_PAYMENT_IS_ACTIVATED://用户已激活0元付，开始查看用户是否设置了支付密码
					UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_END, "");
					UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_START, "检测用户安全设置");
					isPaymentPasswordExist(activity, this);
					break;
				case CODE_PAYMENT_IS_NOT_ACTIVATED://用户未激活0元付，开始激活
					UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_END, "");
					//startPaymentActivated(activity, Url.URL_LINGYUANFU_PAYMENT_ACTIVITY + EmaUser.getInstance().getUUID());
					break;
				case CODE_PAYMENT_PASSW_EXIST://用户设置了密码，弹出密码框让用户输入密码
					UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_END, "");
					//float price = EmaPay.getInstance(activity).getPayInfo().getPrice();
					//showPasswDialog(activity, this, price);
					break;
				case CODE_PAYMENT_PASSW_NOT_EXIST://用户未设置密码，弹出密码设置框，让用户设置密码
					UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_END, "");
					showPasswSetDialog(activity, this);
					break;
				case CODE_PAYMENT_PASSW_UPDATE_FAILED://用户设置密码失败
					ToastHelper.toast(activity, "密码设置失败");
					break;
				case CODE_PAYMENT_PASSW_UPDATE_SUCC://用户设置密码成功，开始验证密码
					//float price1 = EmaPay.getInstance(activity).getPayInfo().getPrice();
					//showPasswDialog(activity, this, price1);
					break;
				case CODE_PAYMENT_PASSW_VERIFY_FAILED://用户输入支付密码错误
					ToastHelper.toast(activity, "密码错误");
					break;
				case CODE_PAYMENT_PASSW_VERIFY_SUCC://用户输入支付密码正确，开始获取订单
					UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_START, "开始获取订单号");
					if(money == null){//支付
						getPayOrderId(activity, bean, handler);
					}else{//充值
						getRechargeOrderId(activity, handler, money);
					}
					break;
				case CODE_PAYMENT_PARAMS_ERROR://参数错误
					LOG.d(TAG, result);
					break;
				}
			}
		};
		//第一步查看用户是否激活了
		UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_START, "检测用户是否激活0元付信用支付");
		isPaymentActivated(activity, listener);
	}
	
	/**
	 * 判断用户是否开通了0元付的信用支付
	 * @param activity
	 */
	private static void isPaymentActivated(final Activity activity, final on0YuanFuResultListener listener){
		Map<String, String> params = new HashMap<String, String>();
		JSONObject json = new JSONObject();
		String merchant = ConfigManager.getInstance(activity).getMerchant();
		try {
			//json.put("openId", EmaUser.getInstance().getUUID());
			json.put("merchant", merchant);
			//json.put("msgKey", UCommUtil.MD5(EmaUser.getInstance().getUUID() + merchant));
		} catch (Exception e) {
		}
		params.put("request", json.toString());
		UCommUtil.testMapInfo(params);
		new HttpInvoker().postAsync(Url.LINGYUANFU_IS_PAYMENT_ACTIVITY, params, new HttpInvoker.OnResponsetListener() {
			
			@Override
			public void OnResponse(String result) {
				LOG.d(TAG, "on response result:" + result);
				try {
					JSONObject data = new JSONObject(result);
					LOG.d(TAG, "msg:" + data.getString("message"));
					if(data.getString("result").equals("1")){//已开通，检查用户是否设置了支付密码
						LOG.d(TAG, "Payment is already Activated");
						listener.onResult(CODE_PAYMENT_IS_ACTIVATED, "");
					}else if(data.getString("result").equals("0")){//未开通
						LOG.d(TAG, "Payment is not Activated");
						listener.onResult(CODE_PAYMENT_IS_NOT_ACTIVATED, "");
					}else if(data.getString("result").equals("-1")){//参数错误：xxxx
						listener.onResult(CODE_PAYMENT_PARAMS_ERROR, "isPaymentActivated params is wrong!!!");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 判断用户是否设置了支付密码
	 * @param context
	 */
	private static void isPaymentPasswordExist(final Context context, final on0YuanFuResultListener listener){
		Map<String, String> params = new HashMap<String, String>();
		JSONObject json = new JSONObject();
		String merchant = ConfigManager.getInstance(context).getMerchant();
		try {
			//json.put("openId", EmaUser.getInstance().getUUID());
			json.put("merchant", merchant);
			//json.put("msgKey", UCommUtil.MD5(EmaUser.getInstance().getUUID() + merchant));
		} catch (Exception e) {
		}
		params.put("request", json.toString());
		UCommUtil.testMapInfo(params);
		new HttpInvoker().postAsync(Url.LINGYUANFU_IS_PAYMENT_PASSWORD_EXIST, params, new HttpInvoker.OnResponsetListener() {
			
			@Override
			public void OnResponse(String result) {
				LOG.d(TAG, "on response result:" + result);
				try {
					JSONObject data = new JSONObject(result);
					LOG.d(TAG, "msg:" + data.getString("message"));
					if(data.getString("result").equals("1")){//密码存在
						LOG.d(TAG, "Payment passw is exist");
						listener.onResult(CODE_PAYMENT_PASSW_EXIST, "");
					}else if(data.getString("result").equals("0")){//密码不存在
						LOG.d(TAG, "Payment passw is not exist");
						listener.onResult(CODE_PAYMENT_PASSW_NOT_EXIST, "");
					}else if(data.getString("result").equals("-1")){//参数错误：xxxx
						listener.onResult(CODE_PAYMENT_PARAMS_ERROR, "isPaymentPasswordExist params is wrong!!!");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 进行0元付的信用开通操作
	 */
	private static void startPaymentActivated(final Context context, final String activityUrl){
		new Handler(context.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(context, WebViewSimpleActivity.class);
				intent.putExtra(WebViewSimpleActivity.INTENT_URL, activityUrl);
				context.startActivity(intent);
			}
		});
	}

	/**
	 * 显示设置密码对话框，让用户设置0元付密码
	 * @param context
	 */
	private static void showPasswSetDialog(final Context context, final on0YuanFuResultListener listener){
		new Handler(context.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				EmaDialogPay0YuanFuSetPassw dialog = new EmaDialogPay0YuanFuSetPassw(context, new EmaDialogPay0YuanFuSetPassw.OnResultListener() {
					@Override
					public void onResult(String code, String result) {
						LOG.d(TAG, "code__:" + code +  "  on response result:" + result);
						if(code.equals("1")){//密码设置成功
							LOG.d(TAG, "set payment passw succ");
							listener.onResult(CODE_PAYMENT_PASSW_UPDATE_SUCC, result);
						}else if(code.equals("0")){//密码设置失败
							LOG.d(TAG, "set payment passw wrong");
							listener.onResult(CODE_PAYMENT_PASSW_UPDATE_FAILED, result);
						}else if(code.equals("-1")){//参数错误：xxxx
							listener.onResult(CODE_PAYMENT_PARAMS_ERROR, "set passw failed params is wrong!!!");
						}
					}
				});
				dialog.show();
			}
		});
	}
	
	/**
	 * 显示密码框提示用户输入0元付密码
	 */
	private static void showPasswDialog(final Context context, final on0YuanFuResultListener listener, final float price){
		new Handler(context.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				EmaDialogPay0YuanFuPassw dialog = new EmaDialogPay0YuanFuPassw(context, price, new EmaDialogPay0YuanFuPassw.OnResultListener() {
					@Override
					public void onResult(String code, String result) {
						LOG.d(TAG, "code__:" + code + "on response result:" + result);
						if(code.equals("1")){//密码正确
							LOG.d(TAG, "payment passw is right");
							listener.onResult(CODE_PAYMENT_PASSW_VERIFY_SUCC, "");
						}else if(code.equals("0")){//密码错误
							LOG.d(TAG, "payment passw is wrong");
							listener.onResult(CODE_PAYMENT_PASSW_VERIFY_FAILED, "");
						}else if(code.equals("-1")){//参数错误：xxxx
							listener.onResult(CODE_PAYMENT_PARAMS_ERROR, "verify passw params is wrong!!!");
						}
					}
				});
				dialog.show();
			}
		});
	}
	
	/**
	 * 调用0元付支付接口进行支付
	 */
	private static void pay(final Context context, final String orderId, final Handler handler, String amount){
		UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_START, "支付中。。。");
		Map<String, String> params = new HashMap<String, String>();
		JSONObject json = new JSONObject();
		String merchant = ConfigManager.getInstance(context).getMerchant();
		try {
			//json.put("openId", EmaUser.getInstance().getUUID());
			json.put("merchant", merchant);
			json.put("amount", amount);
			json.put("orderNo", orderId);
			json.put("type", "0");
			json.put("orderTime", UCommUtil.DateFormat(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
//			openId+merchant+orderNo+amount+type
			//json.put("msgKey", UCommUtil.MD5(EmaUser.getInstance().getUUID() + merchant + orderId + amount + 0));
		} catch (Exception e) {
			LOG.e(TAG, "build request error:", e);
		}
		params.put("request", json.toString());
//		UCommUtil.testMapInfo(params);
		new HttpInvoker().postAsync(Url.LINGYUANFU_PAY, params, new HttpInvoker.OnResponsetListener() {
			
			@Override
			public void OnResponse(String result) {
				LOG.d(TAG, "on response result:" + result);
				UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_START, "");
				try {
					JSONObject data = new JSONObject(result);
					LOG.d(TAG, "msg:" + data.getString("message"));
					if(data.getString("result").equals("1")){//支付成功，通知EMA服务器，去查询订单状态
						LOG.d(TAG, "pay success");
						notifyEmaServer(context, orderId, handler);
					}else if(data.getString("result").equals("0")){//支付失败
						showPayReuslt(context, EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "");
						LOG.d(TAG, "pay failed");
					}else if(data.getString("result").equals("-1")){//参数错误：xxxx
						LOG.d(TAG, "params is wrong!!!");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 通知EMA服务器支付成功，去查询订单状况
	 */
	private static void notifyEmaServer(final Context context, String orderId, final Handler handler){
		UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_START, "查询支付状态");
		Map<String, String> params = new HashMap<String, String>();
		EmaUser mEmaUser = EmaUser.getInstance();
		ConfigManager mConfigManager = ConfigManager.getInstance(context);
		params.put("trade_id", orderId);
		params.put("app_id", mConfigManager.getAppId());
		params.put("client_id", mConfigManager.getChannel());
		/*params.put("sid", mEmaUser.getAccessSid());
		params.put("uuid", mEmaUser.getUUID());
		String sign = UCommUtil.getSign(
				mConfigManager.getAppId(),
				mEmaUser.getAccessSid(),
				mEmaUser.getUUID(),
				mConfigManager.getAppKEY());
		params.put("sign", sign);*/
		UCommUtil.testMapInfo(params);
		new HttpInvoker().postAsync(Url.get0YuanFUNotifyUrl(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				LOG.d(TAG, "on response result:" + result);
				UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_END, "");
				UCommUtil.sendMesg(handler, PayConst.CODE_PAY_0YUANFU_RESULT, result);
			}
		});
	}
	
	/**
	 * 显示支付结果
	 * @param context
	 * @param actionType
	 * @param resultType
	 * @param promptInfo
	 */
	private static void showPayReuslt(final Context context, final int actionType, final int resultType, final String promptInfo){
		new Handler(context.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				new EmaDialogPayPromptResult(context, actionType, resultType, promptInfo).show();
			}
		});
	}
	
	/**
	 * 获取支付订单号
	 * @param context
	 * @param handler
	 */
	private static void getPayOrderId(final Context context, final PayTrdItemBean bean, final Handler handler){
		EmaUser mEmaUser = EmaUser.getInstance();
		//EmaPayInfo payInfo = EmaPay.getInstance(context).getPayInfo();
		ConfigManager mConfigManager = ConfigManager.getInstance(context);
		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", ConfigManager.getInstance(context).getChannel());
		params.put("app_id", mConfigManager.getAppId());
//		int price = payInfoBean.getAmount_pricebean().getPriceFen() * bean.getDiscount() / 100;
//		int price = payInfoBean.getAmount_pricebean().getPriceFen();
		/*params.put("order_amount", price + "");
		params.put("amount", price + "");
		params.put("bank_id", "0");
		params.put("app_order_id", payInfoBean.getApp_order_id());//cp_id
		params.put("product_id", payInfoBean.getProduct_id());
		params.put("product_name", payInfoBean.getProduct_name());
		params.put("product_num", payInfoBean.getProduct_num() + "");*/
		params.put("device_id", DeviceInfoManager.getInstance(context).getDEVICE_ID());
		params.put("channel", mConfigManager.getChannel());
		params.put("wallet_pwd", "0");
		params.put("wallet_amount", "0");
		/*params.put("sid", mEmaUser.getAccessSid());
		params.put("uuid", mEmaUser.getUUID());
		params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_0YUANFU + "");
		String sign = UCommUtil.getSign(
				mConfigManager.getAppId(),
				mEmaUser.getAccessSid(),
				mEmaUser.getUUID(),
				mConfigManager.getAppKEY());
		params.put("sign", sign);*/
		
		UCommUtil.testMapInfo(params);
		
		new HttpInvoker().postAsync(Url.getPayUrlRecharge(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				try {
					UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_END, "");
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS://获取订单号成功
						LOG.d(TAG, "获取订单号成功");
						JSONObject data = json.getJSONObject("data");
						String orderId = data.getString("trade_id");
						// 调用0元付 进行支付
						//float price = EmaPay.getInstance(context).getPayInfo().getPrice();
						//pay(context, orderId, handler, price + "");
						break;
					case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR://签名验证失败
						LOG.d(TAG, "签名验证失败，获取订单号失败");
						UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "签名验证失败，获取订单号失败");
						break;
					case HttpInvokerConst.PAY_RECHARGE_FAILED_ORDERID_REPEAT://订单号重复
						LOG.d(TAG, "订单号重复,获取订单号失败");
						UCommUtil.showSystemBusyDialog(context);
						break;
					default:
						LOG.d(TAG, "获取订单号失败，失败原因未知");
						UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "获取订单号失败");
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
	 * 获取充值订单号
	 * @param context
	 * @param handler
	 */
	private static void getRechargeOrderId(final Context context, final Handler handler, final EmaPriceBean money){
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
		params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_0YUANFU + "");// 充值方式ID
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
		
		/*params.put("sid", emaUser.getAccessSid());
		params.put("uuid", emaUser.getUUID());
		
		String sign = UCommUtil.getSign(configManager.getAppId(),
				emaUser.getAccessSid(),
				emaUser.getUUID(),
				configManager.getAppKEY());
		params.put("sign", sign);*/
		UCommUtil.testMapInfo(params);
		new HttpInvoker().postAsync(Url.getPayUrlRecharge(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				UCommUtil.sendMesg(handler, EmaProgressDialog.CODE_LOADING_END, "");
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS://获取订单号成功
						LOG.d(TAG, "获取订单号成功");
						JSONObject data = json.getJSONObject("data");
						String orderId = data.getString("trade_id");
						pay((Activity)context, orderId, handler, money.getPriceYuan() + "");
						break;
					case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR://签名验证失败
						LOG.d(TAG, "签名验证失败，获取订单号失败");
						UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "签名验证失败，获取订单号失败");
						break;
					case HttpInvokerConst.PAY_RECHARGE_FAILED_ORDERID_REPEAT://订单号重复
						LOG.d(TAG, "订单号重复,获取订单号失败");
						UCommUtil.showSystemBusyDialog(context);
						break;
					default:
						LOG.d(TAG, "获取订单号失败，失败原因未知");
						UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "获取订单号失败");
						break;
					}
				} catch (Exception e) {
					LOG.w(TAG, "getOrderId error", e);
					UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "获取订单号失败");
				}
			}
		});
	}
}
