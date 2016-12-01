package com.emagroup.sdk;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.Map;


/**
 * Created by Administrator on 2016/11/30.
 */

public class ThirdLoginUtils implements IUiListener {
    public static final String WECHAT_APP_ID = "wx9c31edc5e693ec1d";
    public static  final String QQ_APP_ID="";
  //  public static final String secret = "2b32e87f92911f41f369f0130a0ce8ca";
    private static ThirdLoginUtils instance=null;
    private Context mContext;
    private  IWXAPI mWeixinapi;
    private ThirdLoginAfter mWXLoginAfter;
    private Tencent mTencent;

    private ThirdLoginUtils(Context mContext) {
        this.mContext = mContext;
        mWeixinapi = WXAPIFactory.createWXAPI(mContext, WECHAT_APP_ID);
        mWeixinapi.registerApp(WECHAT_APP_ID);
     //   mTencent=Tencent.createInstance(QQ_APP_ID,mContext);
        //this.mWXLoginAfter=wxLoginAfter;
    }
    public static  ThirdLoginUtils getInstance(Context mContext){
        if(instance==null){
            instance=new ThirdLoginUtils(mContext);
        }
        return  instance;
    }

    public void wachateLogin(ThirdLoginAfter wxLoginAfter)
    {
        this.mWXLoginAfter=wxLoginAfter;
        boolean sIsWXAppInstalledAndSupported = mWeixinapi.isWXAppInstalled()
                && mWeixinapi.isWXAppSupportAPI();
        if (!sIsWXAppInstalledAndSupported)
        {
            Toast.makeText(mContext,"未安装或版本过低, 请下载更新的版本",Toast.LENGTH_LONG).show();
            return;

        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "weixin_login";
        mWeixinapi.sendReq(req);
    }



    public void wechatLogin(final SendAuth.Resp result)
    {

        Log.e("wechatLogin", "resp.result =" + result.errCode + "  result.state =" + result.state);
        if(result.errCode == 0)
        {
           mWXLoginAfter.wachateLoginAfter(result.code);

        }
        else if(result.errCode == -2)
        {

            Toast.makeText(mContext,"取消登录",Toast.LENGTH_SHORT).show();
        }
        else if(result.errCode == -4)
        {

            Toast.makeText(mContext,"用户拒绝授权",Toast.LENGTH_SHORT).show();
        }
    }

    public void qqLogin(ThirdLoginAfter thirdLoginAfter){

    }

    @Override
    public void onComplete(Object o) {

    }

    @Override
    public void onError(UiError uiError) {

    }

    @Override
    public void onCancel() {

    }

     interface   ThirdLoginAfter {
        void wachateLoginAfter(String result);
         void qqLoginAfter(Map<String,String> param);
    }
    /*interface   ThirdLoginAfter {
        void wachateLoginAfter(String result);

    }*/

}
