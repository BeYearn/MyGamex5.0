package com.emagroup.sdk;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TrdWeixinPay {

    private static final int CREAT_PRE_ORDER_SUCCESS = 0;
    private static final String TAG = "TrdWeixinPay";

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CREAT_PRE_ORDER_SUCCESS:
                    doNextWxPay();
                    break;
            }
        }
    };
    private static IWXAPI msgApi;
    private static String wachatAppId;

    /**
     * 调用微信开始充值
     */
    public static void startRecharge(Activity activity, EmaPayInfo payInfo, Handler handler) {

        msgApi = WXAPIFactory.createWXAPI(activity, null);

        wachatAppId = ConfigManager.getInstance(activity).getWachatAppId();
        // 将该app注册到微信
        msgApi.registerApp(wachatAppId);

        if (msgApi.isWXAppSupportAPI()) {
            creatPreWxOrder(activity, payInfo);
        } else {
            ToastHelper.toast(activity, "未安装微信或者版本太低");
        }
    }

    private static void creatPreWxOrder(Activity activity, EmaPayInfo payInfo) {
        ConfigManager configManager = ConfigManager.getInstance(activity);
        Map<String, String> params = new HashMap<String, String>();
        params.put("appId", configManager.getAppId());
        params.put("outTradeNo", payInfo.getOrderId());
        params.put("tradeType", "APP");
        params.put("uid", EmaUser.getInstance().getmUid());

        new HttpInvoker().postAsync(Url.getQQwalletPreOrder(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        try {
                            JSONObject json = new JSONObject(result);
                            int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);


                            if (resultCode == 0) {
                                Message message = Message.obtain();
                                message.what = CREAT_PRE_ORDER_SUCCESS;
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


    private static void doNextWxPay() {


        PayReq request = new PayReq();
        request.appId = wachatAppId;
       /* request.partnerId = data.getString("mch_id");
        request.prepayId = data.getString("prepay_id");
        request.packageValue = data.getString("package");
        request.nonceStr = data.getString("nonce_str");
        request.timeStamp = data.getString("timestamp");
        request.sign = data.getString("sign");*/
        if (request.checkArgs()) {
            msgApi.sendReq(request);  //回调在wx的那个activtiy
        } else {
            Log.e(TAG, "doNextWxPay,参数不全");
        }

    }


    /**
     * 从WXPayEntryActivity来的支付结果
     *
     * @param resp
     */
    public static void doResultWxPay(BaseResp resp) {
        switch (resp.errCode) {
            case 0:     //成功
                PayUtil.doCheckOrderStatus(mHandler);
                break;
            case -2:     //用户取消
                mHandler.sendEmptyMessage(PayTrdActivity.PAY_ACTIVITY_DIALOG_CANLE);
                UCommUtil.makePayCallBack(EmaCallBackConst.PAYCANELI, "订单取消");
                break;
            case -1:    //失败
                mHandler.sendEmptyMessage(PayTrdActivity.PAY_ACTIVITY_DIALOG_FAIL);
                UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "订单支付失败");

                break;
        }
    }
}
