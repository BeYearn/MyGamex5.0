package com.emagroup.sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;

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


    public void init(String appKey,Activity activity, EmaSDKListener listener){
        Ema.getInstance().init(appKey,activity,listener);
    }



    public void doLogin(){
        Ema.getInstance().Login();
    }

    public void doLogout() {
        Ema.getInstance().Logout();
    }

    public void doSwichAccount() {
        Ema.getInstance().swichAccount();
    }

    // TODO: 2016/9/22 暂时先传pid和count这两个参数
    public void doPay(Map<String,String> info, final EmaSDKListener listener){
        //在这里把这个map转化到emapayinfo里面  目前需要 商品pid，数量
        EmaPayInfo emaPayInfo = new EmaPayInfo();
        for (Map.Entry<String,String> entry :info.entrySet()){
            String infoValue=entry.getValue();
            switch (entry.getKey()){
                case EmaConst.EMA_PAYINFO_PRODUCT_ID:
                    emaPayInfo.setProductId(infoValue);
                    break;
                case EmaConst.EMA_PAYINFO_PRODUCT_COUNT:
                    emaPayInfo.setProductNum(infoValue);
                    break;
                case EmaConst.EMA_GAMETRANS_CODE:
                    emaPayInfo.setGameTransCode(infoValue);
                    break;
            }
        }
        Ema.getInstance().pay(emaPayInfo, new EmaPayListener() {
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

   /* public void doWeiboShare(Activity activity,String description ,Bitmap  bitmap){
        WeiboShareUtils.getInstance(activity).doWeiboShare(description,bitmap);
    }*/
    public  void doWeiBoShareImage(Activity activity,Bitmap bitmap){
        WeiboShareUtils.getInstance(activity).doWeiboShareImage(bitmap);
    }

    public  void doWeiBoShareWebpage(Activity activity,String title,String description,Bitmap bitmap,String url){
        WeiboShareUtils.getInstance(activity).doWeiBoShareWebpage(title,description,bitmap,url);
    }

    public  void doWeiBoShareText(Activity activity,String text){
        WeiboShareUtils.getInstance(activity).doWeiboShareText(text);
    }

    public void doWeixinShareWebpage(Activity activity,EmaSDKListener listener,String url,String title,String description,Bitmap bitmap,int scene) {
        WeixinShareUtils.getInstance(activity).doWxShareWebpage(listener,url,title,description,bitmap,scene);
    }

    public void doWxShareImg(Activity activity, EmaSDKListener listener,Bitmap bitmap,int scene){
        WeixinShareUtils.getInstance(activity).doWxShareImg(listener,bitmap,scene);
    }

    public void doWxShareText(Activity activity,EmaSDKListener listener,String text,String description,int scene){
        WeixinShareUtils.getInstance(activity).doWxShareText(listener,text,description,scene);
    }

    public void onNewIntent(Intent intent) {
        //WeiboShareUtils.getInstance(activity).onNewIntent(intent);
        Ema.getInstance().onNewIntent(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Ema.getInstance().onActivityResult(requestCode, resultCode, data);
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

    public void onRestart(){
        Ema.getInstance().onRestart();
    }

    public void onDestroy() {
        Ema.getInstance().onDestroy();
    }

    public void onBackPressed(EmaBackPressedAction action){
        Ema.getInstance().onBackPressed(action);
    }

}
