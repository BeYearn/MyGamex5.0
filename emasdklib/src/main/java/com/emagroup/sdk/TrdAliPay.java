package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.alipay.sdk.app.PayTask;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TrdAliPay {

	private static final String TAG = TrdAliPay.class.toString();
	
	public static final String RESULT_STATUS_SUCC = "9000";//订单支付成功
	public static final String RESULT_STATUS_ON_PAYING = "8000";//正在处理中
	public static final String RESULT_STATUS_FAILED = "4000";//订单支付失败
	public static final String RESULT_STATUS_PAY_CANCEL = "6001";//用户中途取消
	public static final String RESULT_STATUS_NETWORK_ERROR = "6002";//网络连接出错
	
	// 商户PID
	private static final String PARTNER = "2088021673809637";
	// 商户收款账号
	private static final String SELLER = "2088021673809637";

	
	/**
	 * 调用支付宝进行充值
	 * 
	 * 首先要去Ema平台服务器获取一个订单号，然后再去调用支付宝充值
	 */
	public static void startRecharge(Activity activity, EmaPayInfo payInfo, Handler handler){
		LOG.e(TAG, "进入支付宝支付..chongzhi");
		//getRechargeOrderId(activity, money, handler);
		getSignedPayInfo(activity,payInfo,handler);
	}

	/**
	 * 调用支付宝进行支付
	 * 首先要去Ema平台的服务器获取一个订单号，然后再去调用支付宝支付
	 */
	public static void startPay(final Activity activity, final Handler handler){
		LOG.e(TAG, "进入支付宝支付");
		getPayOrderId(activity, handler);
	}


	/**
	 * 将支付信息交给服务器签名拿回后发起支付
	 * @param activity
	 * @param payInfo
	 * @param handler
	 */
	private static void getSignedPayInfo(final Activity activity, EmaPayInfo payInfo, final Handler handler) {

		StringBuffer sb = new StringBuffer();
		// 服务接口名称， 固定值
		sb.append("service=\"mobile.securitypay.pay\"");
		// 签约合作者身份ID
		sb.append("&partner=\"").append(PARTNER).append("\"");
		// 参数编码， 固定值
		sb.append("&_input_charset=\"utf-8\"");
		//支付回调地址
		sb.append("&notify_url=\"").append(Url.getAliPayCallbackUrl()).append("\"");
		// 商户网站唯一订单号
		sb.append("&out_trade_no=\"").append(payInfo.getOrderId()).append("\"");
		// 商品名称
		sb.append("&subject=\"").append(payInfo.getProductName()).append("\"");
		sb.append("&payment_type=\"1\"");
		// 签约卖家支付宝账号
		sb.append("&seller_id=\"").append(SELLER).append("\"");
		// 商品金额
		sb.append("&total_fee=\"").append(payInfo.getPrice()).append("\"");
		// 商品详情
		sb.append("&body=\"").append(payInfo.getDescription()).append("\"");
		String rawPayInfoString = sb.toString();
		LOG.e(TAG, "TrdAliPay rawPayInfoString: " + rawPayInfoString);

		Map<String, String> params = new HashMap<>();
		params.put("orderInfo",rawPayInfoString);
		params.put("orderId",payInfo.getOrderId());
		params.put("token",EmaUser.getInstance().getToken());
		new HttpInvoker().postAsync(Url.getSignedPayInfoUrl(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt("status");
					switch(resultCode){
						case HttpInvokerConst.SDK_RESULT_SUCCESS:
							LOG.e(TAG, "获取加签信息成功");
							String signedPayInfo = json.getString("data");
							LOG.e(TAG, "TrdAliPay signedPayInfo: " + signedPayInfo);

							//拿到签名后的信息发起支付
							pay(activity, handler, signedPayInfo);
							break;
						default:
							LOG.e(TAG, "获取签名订单信息失败");
							UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "获取订单号失败");
							break;
					}
				} catch (Exception e) {
					LOG.e(TAG, "获取签名订单信息失败", e);
					UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "获取签名订单信息失败");
				}
			}
		});


	}


	/**
	 * 获取支付宝充值订单号
	 * @param money
	 */
	private static void getRechargeOrderId(final Context context, final EmaPriceBean money, final Handler handler){
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
		params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_ALIPAY + "");// 充值方式ID
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
		new HttpInvoker().postAsync(Url.getPayUrlRecharge(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS://获取订单号成功
						LOG.d(TAG, "获取订单号成功");
						JSONObject data = json.getJSONObject("data");
						String orderId = data.getString("trade_id");
						String alipayInfo = buildAlipayInfo(orderId, orderId, money.getPriceFen() / 100);
						//pay((Activity)context, handler, alipayInfo);
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
	 * 获取支付订单号
	 * @param context
	 * @param handler
	 */
	private static void getPayOrderId(final Context context, final Handler handler){
		EmaUser mEmaUser = EmaUser.getInstance();
		//EmaPayInfo payInfo = EmaPay.getInstance(context).getPayInfo();
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
		/*params.put("sid", mEmaUser.getAccessSid());
		params.put("uuid", mEmaUser.getUUID());
		params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_ALIPAY + "");
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
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS://获取订单号成功
						LOG.d(TAG, "获取订单号成功");
						JSONObject data = json.getJSONObject("data");
						String orderId = data.getString("trade_id");
						EmaPay pay = EmaPay.getInstance(Ema.getInstance().getContext());
						//String alipayInfo = buildAlipayInfo(orderId, pay.getPayInfo().getProductName(), (int)(pay.getPayInfo().getPrice()));
						//pay((Activity)context, orderId, handler, alipayInfo);
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
	 * 开启支付
	 */
	private static void pay(final Activity activity,final Handler handler, final String alipayInfo){
		//支付宝的调用需要异步
		new Thread(){
			public void run() {
				PayTask aliPay = new PayTask(activity);

				String result = aliPay.pay(alipayInfo);
				LOG.e(TAG, "Alipay result___:" + result);
				Message msg = new Message();
				msg.what = PayConst.CODE_PAY_ALI_RESULT;
				msg.obj = result;
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	/**
	 * 构建支付宝支付参数
	 * @return
	 */
	private static String buildAlipayInfo(String orderId, String product_name, int amount){
		EmaPay pay = EmaPay.getInstance(Ema.getInstance().getContext());
		StringBuffer sb = new StringBuffer();
		
		/*// 服务接口名称， 固定值
		sb.append("service=\"mobile.securitypay.pay\"");
		
		// 签约合作者身份ID
		sb.append("&partner=\"").append(PARTNER).append("\"");
		
		// 参数编码， 固定值
		sb.append("&_input_charset=\"utf-8\"");
		
		//支付回调地址
		sb.append("&notify_url=\"").append(Url.getAliPayCallbackUrl()).append("\"");
		
		// 商户网站唯一订单号
		sb.append("&out_trade_no=\"").append(orderId).append("\"");
		
		// 商品名称
		sb.append("&subject=\"").append(product_name).append("\"");
		sb.append("&payment_type=\"1\"");
		
		// 签约卖家支付宝账号
		sb.append("&seller_id=\"").append(SELLER).append("\"");
		
		// 商品金额
		sb.append("&total_fee=\"").append(amount).append("\"");
		
		// 商品详情
		//sb.append("&body=\"").append(pay.getPayInfo().getProductName()).append("\"");
		
		//签名
		String sign = UCommUtil.aliSign(sb.toString(), RSA_PRIVATE);
		try {
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (Exception e) {
			LOG.e(TAG, "", e);
		}
		//签名
		sb.append("&sign=\"").append(sign).append("\"");
		//签名算法
		sb.append("&sign_type=\"RSA\"");
		LOG.d(TAG, "payinfo___: " + sb.toString());*/
		return sb.toString();
	}
}
