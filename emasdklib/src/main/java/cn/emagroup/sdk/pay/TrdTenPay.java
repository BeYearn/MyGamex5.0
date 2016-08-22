package cn.emagroup.sdk.pay;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import cn.emagroup.sdk.comm.ConfigManager;
import cn.emagroup.sdk.comm.DeviceInfoManager;
import cn.emagroup.sdk.comm.EmaProgressDialog;
import cn.emagroup.sdk.comm.HttpInvoker;
import cn.emagroup.sdk.comm.HttpInvokerConst;
import cn.emagroup.sdk.comm.Url;
import cn.emagroup.sdk.user.EmaUser;
import cn.emagroup.sdk.utils.LOG;
import cn.emagroup.sdk.utils.PropertyField;
import cn.emagroup.sdk.utils.ToastHelper;
import cn.emagroup.sdk.utils.UCommUtil;
import android.app.Activity;
import android.os.Message;

/**
 * 使用财付通进行充值，支付
 * @author yang.zhang
 *
 */
public class TrdTenPay {

	private static final String TAG = "RechargeTenPay";
	
	/**
	 * 获取财付通充值url
	 * @param activity
	 * @param money
	 */
	public static void getRechargeUrl(final Activity activity, EmaPriceBean money){
		EmaUser emaUser = EmaUser.getInstance();
		ConfigManager configManager = ConfigManager.getInstance(activity);
		DeviceInfoManager deviceInfoManager = DeviceInfoManager.getInstance(activity);
		EmaPay emaPay = EmaPay.getInstance(activity);
		
		Map<String, String> params = new HashMap<String, String>();
		
		params.put("client_id", ConfigManager.getInstance(activity).getChannel());// 发起站点
		params.put("app_id", configManager.getAppId());
		params.put("partition_id", String.valueOf(0));//分区ID
		params.put("server_id", String.valueOf(0));//服务器ID
		params.put("order_amount", money.getPriceFen() + "");// 订单金额
		params.put("amount", money.getPriceFen() + "");// 总额
		params.put("point", String.valueOf(0));// 积分
		params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_TENPAY + "");// 充值方式ID
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
		
		UCommUtil.testMapInfo(params);
		
		new HttpInvoker().postAsync(Url.getPayUrlRecharge(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				PayUtil.sendRechargeMessage(activity, EmaProgressDialog.CODE_LOADING_END, null);
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS:
						LOG.d(TAG, "获取财付通充值url成功");
						PayUtil.sendRechargeMessage(activity, RechargeMabiActivity.CODE_RECHARGE_TENPAY_URL, json.getString("redirect"));
						break;
					case HttpInvokerConst.PAY_RECHARGE_FAILED_ORDERID_REPEAT://订单号重复
						LOG.d(TAG, "订单号重复,获取订单号失败");
						UCommUtil.showSystemBusyDialog(activity);
						break;
					case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR:
						LOG.d(TAG, "签名验证失败");
					default:
						LOG.d(TAG, "获取财付通充值url失败");
						PayUtil.sendRechargeMessage(activity, RechargeMabiActivity.CODE_RECHARGE_TENPAY_URL_FAILED, null);
						break;
					}
				} catch (Exception e) {
					PayUtil.sendRechargeMessage(activity, RechargeMabiActivity.CODE_RECHARGE_TENPAY_URL_FAILED, null);
					LOG.w(TAG, "获取财付通充值url失败 with Exception", e);
				}
			}
		});
		
	}

	
	/**
	 * 获取财付通支付url
	 * @param activity
	 * @param money
	 */
	public static void getPayUrl(final Activity activity){
		EmaUser emaUser = EmaUser.getInstance();
		ConfigManager configManager = ConfigManager.getInstance(activity);
		DeviceInfoManager deviceInfoManager = DeviceInfoManager.getInstance(activity);
		EmaPay emaPay = EmaPay.getInstance(activity);
		
		Map<String, String> params = emaPay.buildPayParams();
		params.put("wallet_amount", "0");
		params.put("sid", emaUser.getAccessSid());
		params.put("uuid", emaUser.getUUID());
		params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_TENPAY + "");
		params.put("device_id", deviceInfoManager.getDEVICE_ID());
		params.put("channel", configManager.getChannel());
		String sign = UCommUtil.getSign(configManager.getAppId(),
				emaUser.getAccessSid(), emaUser.getUUID(), configManager.getAppKEY());
		params.put("sign", sign);
		
		UCommUtil.testMapInfo(params);
		
		new HttpInvoker().postAsync(Url.getPayUrlRecharge(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				PayUtil.sendPayMessage(activity, EmaProgressDialog.CODE_LOADING_END, null);
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS:
						LOG.d(TAG, "获取财付通支付url成功");
						PayUtil.sendPayMessage(activity, PayTrdActivity.CODE_PAY_TENTPAY_URL, json.getString("redirect"));
						break;
					case HttpInvokerConst.PAY_RECHARGE_FAILED_ORDERID_REPEAT://订单号重复
						LOG.d(TAG, "订单号重复,获取订单号失败");
						UCommUtil.showSystemBusyDialog(activity);
						break;
					case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR:
						LOG.d(TAG, "签名验证失败");
					default:
						PayUtil.sendPayMessage(activity, PayTrdActivity.CODE_PAY_TENPAY_URL_FAILED, null);
						LOG.d(TAG, "获取财付通支付url失败");
						break;
					}
				} catch (Exception e) {
					PayUtil.sendPayMessage(activity, PayTrdActivity.CODE_PAY_TENPAY_URL_FAILED, null);
					LOG.w(TAG, "getPayUrl 获取财付通支付url失败", e);
				}
			}
		});
	}
}
