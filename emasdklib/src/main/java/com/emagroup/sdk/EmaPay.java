package com.emagroup.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

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
    //private EmaUser mEmaUser;  因为mEmauser这个对象的状态有可能在变化，所以不应该直接得到一个示例就放在这里一劳永逸，每次应该getInstance；
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
    private EmaPayInfo mPayInfo;

    private EmaPay(Context context) {
        mContext = context;
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
    public void pay(EmaPayInfo payInfo, EmaPayListener payListener) {
        this.mPayInfo=payInfo;
        this.mPayListener=payListener;

        try {
            if(mPayInfo.getGameTransCode().getBytes().length>256){
                throw new RuntimeException("参数过长，超过256byte");
            }
        }catch(Exception e){
            Log.e("pay","参数过长，超过256byte");
            return;
        }

        if (!EmaUser.getInstance().getIsLogin()) {
            ToastHelper.toast(mContext, "还未登陆，请先登陆！");
            LOG.d(TAG, "没有登陆，或者已经退出！");
            return;
        }

        //发起购买---->对订单号及信息的请求
        Map<String, String> params = new HashMap<>();
        params.put("pid", mPayInfo.getProductId());
        params.put("token",EmaUser.getInstance().getToken());
        params.put("quantity", mPayInfo.getProductNum());
        params.put("appId",ConfigManager.getInstance(mContext).getAppId());
        if(!TextUtils.isEmpty(mPayInfo.getGameTransCode())){
            params.put("gameTransCode", mPayInfo.getGameTransCode());
        }
        LOG.e("Emapay_pay",mPayInfo.getProductId()+".."+EmaUser.getInstance().getToken()+".."+mPayInfo.getProductNum());

        String sign = ConfigManager.getInstance(mContext).getAppId()+(TextUtils.isEmpty(mPayInfo.getGameTransCode())?null:mPayInfo.getGameTransCode())+mPayInfo.getProductId()+mPayInfo.getProductNum()+EmaUser.getInstance().getToken()+EmaUser.getInstance().getAppKey();
        //LOG.e("rawSign",sign);
        sign = UCommUtil.MD5(sign);
        params.put("sign", sign);

        new HttpInvoker().postAsync(Url.getOrderStartUrl(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        //mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
                        try {
                            Log.e("creatOrder",result);

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

                            mPayInfo.setOrderId(orderId);
                            mPayInfo.setUid(EmaUser.getInstance().getAllianceUid());
                            mPayInfo.setCoinEnough(coinEnough);
                            mPayInfo.setProductName(productName);
                            mPayInfo.setPrice(Integer.parseInt(productPrice)*Integer.parseInt(mPayInfo.getProductNum()));  // 总额
                            mPayInfo.setDescription(description);
                            mPayInfo.setProductId(channelProductCode); // 新加的，不过对于官方平台来说emaProductCode和channelProductCode一样的

                            LOG.e("createOrder",message+coinEnough+orderId+unit+productPrice);

                            Message msg = new Message();
                            msg.what = ORDER_SUCCESS;
                            msg.obj = mPayInfo;
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
     * 取消订单
     */
    public void cancelOrder(){
        if(mPayInfo==null){
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("orderId", mPayInfo.getOrderId());
        params.put("token",EmaUser.getInstance().getToken());

        new HttpInvoker().postAsync(Url.getRejectOrderUrl(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        try {
                            Log.e("cancleOrder",mPayInfo.getProductId()+"...."+result);

                        } catch (Exception e) {
                            LOG.w(TAG, "login error", e);
                        }
                    }
                });
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
