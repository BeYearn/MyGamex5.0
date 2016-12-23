package com.emagroup.sdk;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class TrdWeixinPay {

    private static final int CREAT_PRE_ORDER_SUCCESS = 0;
    private static final String TAG = "TrdWeixinPay";

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CREAT_PRE_ORDER_SUCCESS:
                    JSONObject obj = (JSONObject) msg.obj;
                    doNextWxPay(obj);
                    break;
            }
        }
    };
    private static IWXAPI msgApi;
    private static String wachatAppId;
    private static Handler normalHandler;

    /**
     * 调用微信开始充值
     */
    public static void startRecharge(Activity activity, EmaPayInfo payInfo, Handler handler) {
        normalHandler=handler;

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
        params.put("orderId", payInfo.getOrderId());
        params.put("uid", EmaUser.getInstance().getmUid());
        params.put("token", EmaUser.getInstance().getToken());

        new HttpInvoker().postAsync(Url.getWeixinPayPreOrder(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        try {
                            Log.e(TAG, result);

                            JSONObject json = new JSONObject(result);
                            int resultCode = json.getInt("status");

                            if (resultCode == 0) {

                                JSONObject data = json.getJSONObject("data");
                                Message message = Message.obtain();
                                message.what = CREAT_PRE_ORDER_SUCCESS;
                                message.obj = data;
                                mHandler.sendMessage(message);
                            }
                        } catch (Exception e) {
                            LOG.w(TAG, "loginAutoLogin error", e);
                        }
                    }
                });
    }


    private static void doNextWxPay(JSONObject obj) {

        try {
            PayReq request = new PayReq();
            request.appId = wachatAppId;
            request.partnerId = obj.getString("mch_id");
            request.prepayId = obj.getString("prepay_id");
            request.packageValue = "Sign=WXPay";
            request.nonceStr = obj.getString("nonce_str");
            request.timeStamp = obj.getString("timestamp");
            request.sign = obj.getString("sign");

            /*PayReq req = new PayReq();
            req.appId = wachatAppId;
            req.partnerId = obj.getString("mch_id");
            req.prepayId = obj.getString("prepay_id");
            req.packageValue = "Sign=WXPay";
            req.nonceStr = genNonceStr();
            req.timeStamp = String.valueOf(genTimeStamp());

            List<NameValuePair> signParams = new LinkedList<NameValuePair>();
            signParams.add(new BasicNameValuePair("appid", req.appId));
            signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
            signParams.add(new BasicNameValuePair("package", req.packageValue));
            signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
            signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
            signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

            req.sign = sign(signParams);*/


            if (request.checkArgs()) {
                boolean b = msgApi.sendReq(request);    //回调在wx的那个activtiy
                Log.e("msgApi",b+"");
            } else {
                Log.e(TAG, "doNextWxPay,参数不全");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                PayUtil.doCheckOrderStatus(normalHandler);
                break;
            case -2:     //用户取消
                normalHandler.sendEmptyMessage(PayTrdActivity.PAY_ACTIVITY_DIALOG_CANLE);
                UCommUtil.makePayCallBack(EmaCallBackConst.PAYCANELI, "订单取消");
                break;
            case -1:    //失败
                normalHandler.sendEmptyMessage(PayTrdActivity.PAY_ACTIVITY_DIALOG_FAIL);
                UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "订单支付失败");
                break;
        }
    }

//------------------------------以下是本地加签的一些方法----------------------------------------------------------------------------------

    /**
     * 随机字符串
     *
     * @return
     */
    private static String genNonceStr() {
        Random random = new Random();
        return getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }
    public static String getMessageDigest(byte[] buffer) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(buffer);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 支付时间戳
     *
     * @return
     */
    private static long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 签名
     *
     * @param params
     * @return
     */
    public static String sign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=" + "0c1cbcabb7b3a5610093cf7328078730");
        String appSign = getMessageDigest(sb.toString().getBytes()).toUpperCase();
        return appSign;
    }

}
