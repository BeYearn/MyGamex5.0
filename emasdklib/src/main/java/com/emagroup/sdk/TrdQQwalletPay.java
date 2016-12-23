package com.emagroup.sdk;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.api.OpenApiFactory;
import com.tencent.mobileqq.openpay.constants.OpenConstants;
import com.tencent.mobileqq.openpay.data.pay.PayApi;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * 使用qq钱包进行充值，支付
 *
 * @author yang.zhang
 */
public class TrdQQwalletPay {


    private static final int CREAT_PRE_ORDER_SUCCESS = 0;
    private static final String TAG = "TrdQQwalletPay";
    private static IOpenApi mOpenApi;

    private static String qqAppId;

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CREAT_PRE_ORDER_SUCCESS:
                    if (msg.obj instanceof HashMap) {
                        HashMap<String, String> params = (HashMap<String, String>) msg.obj;
                        doNextQQPay(params);
                    }
                    break;
            }
        }
    };
    private static EmaPayInfo mPayInfo;
    private static Handler normalHandler;

    public static void startRecharge(Activity activity, EmaPayInfo payInfo, Handler handler) {

        normalHandler=handler;

        qqAppId = ConfigManager.getInstance(activity).getQQAppId();
        mOpenApi = OpenApiFactory.getInstance(activity, qqAppId);

        if (mOpenApi.isMobileQQSupportApi(OpenConstants.API_NAME_PAY)) {
            creatPreqqOrder(activity, payInfo);
        } else {
            ToastHelper.toast(activity, "未安装手机qq或者版本太低");
        }

    }

    private static void creatPreqqOrder(Activity activity, EmaPayInfo payInfo) {
        mPayInfo = payInfo;

        ConfigManager configManager = ConfigManager.getInstance(activity);
        Map<String, String> params = new HashMap<>();
        params.put("appId", configManager.getAppId());
        params.put("orderId", payInfo.getOrderId());
        params.put("tradeType", "APP");
        params.put("uid", EmaUser.getInstance().getmUid());
        params.put("token", EmaUser.getInstance().getToken());

        new HttpInvoker().postAsync(Url.getQQwalletPreOrder(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        try {
                            JSONObject json = new JSONObject(result);
                            String resultCode = json.getString("status");

                            HashMap<String, String> QQApiparams = new HashMap<>();
                            JSONObject data = json.getJSONObject("data");

                            QQApiparams.put("mch_id", data.getString("mch_id"));
                            QQApiparams.put("nonce_str", data.getString("nonce_str"));
                            QQApiparams.put("prepay_id", data.getString("prepay_id"));
                            QQApiparams.put("sign", data.getString("sign"));
                            QQApiparams.put("sub_mch_id", data.getString("sub_mch_id"));
                            QQApiparams.put("trade_type", data.getString("trade_type"));

                            if (resultCode == "0") {
                                Message message = Message.obtain();
                                message.what = CREAT_PRE_ORDER_SUCCESS;
                                message.obj = QQApiparams;
                                mHandler.sendMessage(message);
                            } else {
                                Log.e(TAG, result);
                            }
                        } catch (Exception e) {
                            LOG.w(TAG, "loginAutoLogin error", e);
                        }
                    }
                });
    }

    private static void doNextQQPay(HashMap<String, String> params) {

        PayApi api = new PayApi();
        api.appId = qqAppId; // 在http://open.qq.com注册的AppId,参与支付签名，签名关键字key为appId
        api.serialNumber = mPayInfo.getOrderId(); // 支付序号,用于标识此次支付
        api.callbackScheme = "qwallet" + qqAppId; // QQ钱包支付结果回调给urlscheme为callbackScheme的activity.，参看后续的“支付回调结果处理”
        api.tokenId = params.get("prepay_id"); // QQ钱包支付生成的token_id
        api.pubAcc = ""; // 手Q公众帐号id.参与支付签名，签名关键字key为pubAcc
        api.pubAccHint = ""; // 支付完成页面，展示给用户的提示语：提醒关注公众帐号
        api.nonce = params.get("nonce_str"); // 随机字段串，每次支付时都要不一样.参与支付签名，签名关键字key为nonce
        api.timeStamp = System.currentTimeMillis()/1000; // 时间戳，为1970年1月1日00:00到请求发起时间的秒数
        api.bargainorId = params.get("mch_id"); // 商户号.参与支付签名，签名关键字key为bargainorId
        api.sigType = "HMAC-SHA1"; // 签名时，使用的加密方式，默认为"HMAC-SHA1"

        String rawSign = "appId=" + qqAppId + "&bargainorId=" + params.get("mch_id") + "&pubAcc=" + "&tokenId=" + params.get("prepay_id") + "&nonce=" + params.get("nonce_str");
        String qqAppKey=""+"&";


        //api.sig =...; // 商户Server下发的数字签名，生成的签名串，参看“数字签名”

        if (api.checkParams()) {
            mOpenApi.execApi(api);    //回调在PayThrdActivity中
        } else {
            Log.e(TAG, "doNextQQPay,参数不全");
        }
    }
}
