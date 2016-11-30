package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;

import cn.emagroup.sdk.R;

/**
 * Created by Administrator on 2016/11/14.
 */
public class WeixinShareUtils {
    private static WeixinShareUtils mInstance;
    private final Activity mActivity;
    private final IWXAPI mWeixinapi;
    public static EmaSDKListener mListener;

    public static WeixinShareUtils getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new WeixinShareUtils(activity);
        }
        return mInstance;
    }

    private WeixinShareUtils(Activity activity){
        this.mActivity=activity;
      mWeixinapi = WXAPIFactory.createWXAPI(activity, "wx3b310a6bcccbd788", true);
        mWeixinapi.registerApp("wx3b310a6bcccbd788");

       /* mWeixinapi = WXAPIFactory.createWXAPI(activity, "wx9c31edc5e693ec1d");
        mWeixinapi.registerApp("wx9c31edc5e693ec1d");*/
    }


    public void doWeixinShare(EmaSDKListener listener) {
        this.mListener=listener;
        doWxShareText();
    }


    private void doWxShareText() {
        WXTextObject textObject = new WXTextObject();
        textObject.text="我是微信模板";

        WXMediaMessage wxMediaMessage = new WXMediaMessage();
        wxMediaMessage.mediaObject=textObject;
        wxMediaMessage.description="我是微信模板description";

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
        req.message = wxMediaMessage;
        req.scene =SendMessageToWX.Req.WXSceneSession;// 或者SendMessageToWX.Req.WXSceneTimeline

        // 调用api接口发送数据到微信
        mWeixinapi.sendReq(req);

    }
    private void doWxShareImg1(){
        Bitmap bmp = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.ema_floating_icon);
        WXImageObject imgObj = new WXImageObject(bmp);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;// 或者SendMessageToWX.Req.WXSceneTimeline
        mWeixinapi.sendReq(req);
    }
    private void doWxShareImg2(){
        //String path = SDCARD_ROOT + "/test.png";
        String path = "storage/emulated/0/DCIM/Camera"+ "/cup.jpg";
        File file = new File(path);
        if (!file.exists()) {
            ToastHelper.toast(mActivity,"请检查路径");
            return;
        }

        WXImageObject imgObj = new WXImageObject();
        imgObj.setImagePath(path);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        Bitmap bmp = BitmapFactory.decodeFile(path);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;// 或者SendMessageToWX.Req.WXSceneTimeline
        mWeixinapi.sendReq(req);
    }

    private void doWxShareImg3(){
        String url = "http://weixin.qq.com/zh_CN/htmledition/images/weixin/weixin_logo0d1938.png";

        try{
            WXImageObject imgObj = new WXImageObject();
            //imgObj.imageUrl=url;

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = imgObj;

            Bitmap bmp = BitmapFactory.decodeStream(new URL(url).openStream());
            Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
            bmp.recycle();
            msg.thumbData = bmpToByteArray(thumbBmp, true);

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
            req.message = msg;
            req.scene = SendMessageToWX.Req.WXSceneSession;// 或者SendMessageToWX.Req.WXSceneTimeline
            mWeixinapi.sendReq(req);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // 分享网页 注意这里的那个图一定要小于30k
    private  void doWxShareWebpage(){
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = "http://www.baidu.com";
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "WebPage Title WebPage Title WebPage Title WebPage Title WebPage Title WebPage Title WebPage Title WebPage Title WebPage Title Very Long Very Long V";
        msg.description = "WebPage Description WebPage Description WebPage Description WebPage Description WebPage Description WebPage Description WebPage Descr";
        Bitmap thumb = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.ic_launcher);
        msg.thumbData = bmpToByteArray(thumb, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction =  String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;// 或者SendMessageToWX.Req.WXSceneTimeline
        mWeixinapi.sendReq(req);
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


 /*   public void login()
    {
        boolean sIsWXAppInstalledAndSupported = mWeixinapi.isWXAppInstalled()
                && mWeixinapi.isWXAppSupportAPI();
        if (!sIsWXAppInstalledAndSupported)
        {
            Toast.makeText(mActivity,"未安装或版本过低, 请下载更新的版本",Toast.LENGTH_LONG).show();
            return;

        }
         AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
             }

            @Override
            protected String doInBackground(Void... params) {
                return "";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                //发送请求
                SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "weixin_login";
                mWeixinapi.sendReq(req);
            }
        };
        asyncTask.execute();
    }*/
}
