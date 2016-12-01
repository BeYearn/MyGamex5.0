package com.emagroup.sdk;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.tencent.connect.common.Constants;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
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
    private ThirdLoginAfter mThirdLoginAfter;
    private Tencent mTencent;

    private ThirdLoginUtils(Context mContext) {
        this.mContext = mContext;
        mWeixinapi = WXAPIFactory.createWXAPI(mContext, WECHAT_APP_ID);
        mWeixinapi.registerApp(WECHAT_APP_ID);
     //   mTencent=Tencent.createInstance(QQ_APP_ID,mContext);
        //this.mThirdLoginAfter=wxLoginAfter;
    }
    public static  ThirdLoginUtils getInstance(Context mContext){
        if(instance==null){
            instance=new ThirdLoginUtils(mContext);
        }
        return  instance;
    }

    public void wachateLogin(ThirdLoginAfter wxLoginAfter)
    {
        this.mThirdLoginAfter =wxLoginAfter;
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
           mThirdLoginAfter.wachateLoginAfter(result.code);

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
        mThirdLoginAfter =thirdLoginAfter;
        mTencent.login((Activity) mContext,"get_simple_userinfo",this);//QQ回调接口下一行
    }

    @Override
    public void onComplete(Object o) {
            if(o==null){
                Log.i(this.getClass().getName(),"ThirdLoginUtils  qqLogin onComplete---"+o.toString());
                Toast.makeText(mContext,"登录失败",Toast.LENGTH_SHORT);
            }else{
                try {
                    JSONObject resultJson= (JSONObject) o;
                    Map<String,String> param=new HashMap();
                    param.put("qqAppId",QQ_APP_ID);
                    param.put("openId",resultJson.getString(Constants.PARAM_OPEN_ID));
                    mThirdLoginAfter.qqLoginAfter(param);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
    }

    @Override
    public void onError(UiError uiError) {
        Toast.makeText(mContext,"登录失败",Toast.LENGTH_SHORT);
    }

    @Override
    public void onCancel() {
        Toast.makeText(mContext,"取消登录",Toast.LENGTH_SHORT);
    }

     interface   ThirdLoginAfter {
        void wachateLoginAfter(String result);
         void qqLoginAfter(Map<String,String> param);
    }
    /*interface   ThirdLoginAfter {
        void wachateLoginAfter(String result);

    }*/

}
