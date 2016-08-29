package cn.emagroup.sdk.wrapper;

import android.content.Context;
import android.os.Message;

import cn.emagroup.sdk.Ema;
import cn.emagroup.sdk.comm.EmaSDKListener;
import cn.emagroup.sdk.pay.EmaPayInfo;
import cn.emagroup.sdk.pay.EmaPayListener;

/**
 * Created by Administrator on 2016/8/22.
 */
public class EmaSDK {
    private static EmaSDK mInstance;

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
