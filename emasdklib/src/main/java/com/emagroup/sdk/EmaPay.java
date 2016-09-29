package com.emagroup.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 将每一次的支付过程当作一个对象
 *
 * @author yang.zhang
 */
public class EmaPay {

    private static final String TAG = "EmaPay";
    private static final int ORDER_SUCCESS = 11; // 订单创建成功
    private static final int ORDER_FAIL = 12;

    private static EmaPay mInstance;
    private static final Object synchron = new Object();
    private final EmaProgressDialog mProgress;

    private Context mContext;
    private EmaUser mEmaUser;
    private ConfigManager mConfigManager;
    private DeviceInfoManager mDeviceInfoManager;
    public  EmaPayListener mPayListener;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ORDER_SUCCESS://成功
                    //ToastHelper.toast(mContext, "订单ok");
                    doNextPay((EmaPayInfo) msg.obj);
                    break;
                case ORDER_FAIL:
                    ToastHelper.toast(mContext, "订单创建失败");
                    break;

            }
        }

    };

    private EmaPay(Context context) {
        mContext = context;
        mEmaUser = EmaUser.getInstance();
        mConfigManager = ConfigManager.getInstance(context);
        mDeviceInfoManager = DeviceInfoManager.getInstance(context);
        mProgress = new EmaProgressDialog(context);
    }

    public static EmaPay getInstance(Context context) {
        if (mInstance == null) {
            synchronized (synchron) {
                if (mInstance == null) {
                    mInstance = new EmaPay(context);
                }
            }
        }
        return mInstance;
    }


    /**
     * 开启支付
     *
     * @param
     */
    public void pay(final EmaPayInfo payInfo, EmaPayListener payListener) {
        this.mPayListener=payListener;
        if (!mEmaUser.getIsLogin()) {
            ToastHelper.toast(mContext, "还未登陆，请先登陆！");
            LOG.d(TAG, "没有登陆，或者已经退出！");
            return;
        }

        //发起购买---->对订单号及信息的请求
        Map<String, String> params = new HashMap<>();
        params.put("pid", payInfo.getProductId());
        params.put("token",mEmaUser.getmToken());
        params.put("quantity", payInfo.getProductNum());
        params.put("appId",ConfigManager.getInstance(mContext).getAppId());
        if(!TextUtils.isEmpty(payInfo.getGameTransCode())){
            params.put("gameTransCode", payInfo.getGameTransCode());
        }
        LOG.e("Emapay_pay",payInfo.getProductId()+".."+mEmaUser.getmToken()+".."+payInfo.getProductNum());

        String sign = ConfigManager.getInstance(mContext).getAppId()+(TextUtils.isEmpty(payInfo.getGameTransCode())?null:payInfo.getGameTransCode())+payInfo.getProductId()+payInfo.getProductNum()+mEmaUser.getmToken()+EmaUser.getInstance().getAppKey();
        LOG.e("rawSign",sign);
        sign = UCommUtil.MD5(sign);
        params.put("sign", sign);

        new HttpInvoker().postAsync(Url.getOrderStartUrl(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        //mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            String message= jsonObject.getString("message");
                            String status= jsonObject.getString("status");

                            JSONObject productData = jsonObject.getJSONObject("data");
                            boolean coinEnough = productData.getBoolean("coinEnough");
                            String orderId = productData.getString("orderId");
                            JSONObject productInfo = productData.getJSONObject("productInfo");

                            String appId = productInfo.getString("appId");
                            String channelId = productInfo.getString("channelId");
                            String channelProductCode = productInfo.getString("channelProductCode");
                            String description = productInfo.getString("description");
                            String emaProductCode = productInfo.getString("emaProductCode");
                            String productName = productInfo.getString("productName");
                            String productPrice = productInfo.getString("productPrice");
                            String unit = productInfo.getString("unit");

                            payInfo.setOrderId(orderId);
                            payInfo.setUid(mEmaUser.getmUid());
                            payInfo.setCoinEnough(coinEnough);
                            payInfo.setProductName(productName);
                            payInfo.setPrice(Integer.parseInt(productPrice));
                            payInfo.setDescription(description);

                            LOG.e("createOrder",message+coinEnough+orderId+unit+productPrice);

                            Message msg = new Message();
                            msg.what = ORDER_SUCCESS;
                            msg.obj = payInfo;
                            mHandler.sendMessage(msg);
                        } catch (Exception e) {
                            LOG.w(TAG, "login error", e);
                            mHandler.sendEmptyMessage(ORDER_FAIL);
                        }
                    }
                });

    }


    private void doNextPay(EmaPayInfo payInfo) {
        Intent intent = null;
        if (payInfo.isCoinEnough()) {
            if(!payInfo.isReChargePay()){
                LOG.d(TAG, "余额足够，显示钱包支付");
                intent = new Intent(mContext, PayMabiActivity.class);

            }else {  // 如果是充值来的，就静默走这个
                PayMabiActivity.doPayNoKeyWord(payInfo);
                return;
            }
        } else {
            LOG.d(TAG, "余额不足，显示第三方支付");
            intent = new Intent(mContext, PayTrdActivity.class);
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable("payInfo", payInfo);
        intent.putExtras(bundle);
        mContext.startActivity(intent);
//        接收端
//        Intent intent = this.getIntent();
//        payInfo=(EmaPayInfo)intent.getParcelableExtra("payInfo");
    }


    /**
     *  //TODO 取消订单
     */
    private void cancelOrder(){

    }

    /**
     * 构建支付参数
     */
    protected Map<String, String> buildPayParams() {
        Map<String, String> map = new HashMap<String, String>();
        /*map.put("client_id", ConfigManager.getInstance(Ema.getInstance().getContext()).getChannel());
        map.put("app_id", mConfigManager.getAppId());
        map.put("order_amount", (int) (mPayInfoBean.getAmount_pricebean().getPriceFen()) + "");
        map.put("amount", (int) (mPayInfoBean.getAmount_pricebean().getPriceFen()) + "");
        map.put("bank_id", "0");
        map.put("app_order_id", mPayInfoBean.getApp_order_id());//cp_id
        map.put("product_id", mPayInfoBean.getProduct_id());
        map.put("product_name", mPayInfoBean.getProduct_name());
        map.put("product_num", mPayInfoBean.getProduct_num() + "");*/
        map.put("device_id", mDeviceInfoManager.getDEVICE_ID());
        map.put("channel", mConfigManager.getChannel());
        return map;
    }

    /**
     * 设置支付回调
     * @param code
     * @param obj
     */
    public void makePayCallback(int code, Object obj) {
        if (mPayListener == null) {
            LOG.w(TAG, "未设置支付回调");
            return;
        }
        Message msg = new Message();
        msg.what = code;
        msg.obj = obj;
        mPayListener.onPayCallBack(msg);
    }

}
