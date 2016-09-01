package com.emagroup.sdk.wrapper;

import android.content.Context;
import android.os.Message;

import com.emagroup.sdk.Ema;
import com.emagroup.sdk.comm.EmaSDKListener;
import com.emagroup.sdk.pay.EmaPayInfo;
import com.emagroup.sdk.pay.EmaPayListener;
import com.emagroup.sdk.utils.LOG;

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


    public void init(Context context, EmaSDKListener listener){
        Ema.getInstance().init(context,listener);
    }



    public void doLogin(){
        Ema.getInstance().Login();
    }

    public void doLogout() {
        Ema.getInstance().Logout();
    }

    public void doPay(EmaPayInfo info, final EmaSDKListener listener){
        Ema.getInstance().pay(info, new EmaPayListener() {
            @Override
            public void onPayCallBack(Message msg) {
                listener.onCallBack(msg.what,msg.toString());
            }
        });
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

    public void makeCallBack(int msgCode, String msgObj){
        if(reciveMsgListener == null){
            LOG.w("warn", "未设置回调");
            return;
        }
        reciveMsgListener.onCallBack(msgCode,msgObj);
    }

    public void onStart() {
        Ema.getInstance().onStart();
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
