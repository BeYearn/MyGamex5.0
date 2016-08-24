package cn.emagroup.sdk.wrapper;

import android.content.Context;

import cn.emagroup.sdk.Ema;
import cn.emagroup.sdk.comm.EmaSDKListener;

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
