package com.emagroup.sdk;

import android.app.Activity;

import java.util.Map;

/**
 * Created by Administrator on 2016/8/22.
 */
public class EmaSDK {
    private static EmaSDK mInstance;
    private EmaSDKListener reciveMsgListener;

    private EmaSDK() {
    }

    public static EmaSDK getInstance() {
        if (mInstance == null) {
            mInstance = new EmaSDK();
        }
        return mInstance;
    }


    public void init(Activity activity, EmaSDKListener listener){
        Ema.getInstance().init(activity,listener);
    }



    public void doLogin(){
        Ema.getInstance().Login();
    }

    public void doLogout() {
        Ema.getInstance().Logout();
    }

    // TODO: 2016/9/2 支付方法还未完全统一，暂时空实现，记得的info--map
    public void doPay(Map<String,String> info, final EmaSDKListener listener){
        //在这里把这个map转化到emapayinfo里面  目前需要 商品name，pid，价格，数量，    new EmaPayInfo();

        /*Ema.getInstance().pay(info, new EmaPayListener() {
            @Override
            public void onPayCallBack(Message msg) {
                listener.onCallBack(msg.what,msg.toString());
            }
        });*/
    }


    public void doShowToolbar() {
        Ema.getInstance().showToolBar();
    }


    public void doHideToobar() {
        Ema.getInstance().hideToolBar();
    }

    public void doSetRecivePushListner(EmaSDKListener listener){
        this.reciveMsgListener=listener;
    }

    /**
     * 个推的reciver收到透传消息后回调该方法
     * @param msgCode
     * @param msgObj
     */
    public void makeCallBack(int msgCode, String msgObj){
        if(reciveMsgListener == null){
            LOG.w("warn", "未设置回调");
            return;
        }
        reciveMsgListener.onCallBack(msgCode,msgObj);
    }


    public String getChannelId(){
        return Ema.getInstance().getChannelId();
    }


    public void onResume() {
        Ema.getInstance().onResume();
    }

    public void onPause() {
        Ema.getInstance().onPause();
    }

    public void onStop() {
        Ema.getInstance().onStop();
    }

    public void onDestroy() {
        Ema.getInstance().onDestroy();
    }

}
