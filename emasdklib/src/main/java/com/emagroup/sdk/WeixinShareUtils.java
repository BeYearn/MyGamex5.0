package com.emagroup.sdk;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import cn.emagroup.sdk.R;

/**
 * Created by Administrator on 2016/11/14.
 */
public class WeixinShareUtils {
    private static WeixinShareUtils mInstance;
    private final Activity mActivity;
    private final IWXAPI mWeixinapi;
    public static EmaSDKListener mListener;
    private static final int MMAlertSelect1  =  0;
    private static final int MMAlertSelect2  =  1;
    private static final int MMAlertSelect3  =  2;

    public static WeixinShareUtils getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new WeixinShareUtils(activity);
        }
        return mInstance;
    }

    private WeixinShareUtils(Activity activity){
        this.mActivity=activity;
       /* mWeixinapi = WXAPIFactory.createWXAPI(activity, "wx3b310a6bcccbd788", true);
        mWeixinapi.registerApp("wx3b310a6bcccbd788");*/
        mWeixinapi = WXAPIFactory.createWXAPI(activity, "wx9c31edc5e693ec1d", true);
        mWeixinapi.registerApp("wx9c31edc5e693ec1d");

    }


    public void doWeixinShare(EmaSDKListener listener,String url,String title,String description,Bitmap bitmap) {
        this.mListener=listener;
        //   doWxShareText();
        // doWxShareMusic(MMAlertSelect2);
        //   doWxShareVideo(MMAlertSelect2);
        //   doWxShareImg1(/*MMAlertSelect3*/);
   //     doWxShareImg3();
          doWxShareWebpage(url,title,description,bitmap);
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
    private void doWxShareImg1( /*int type */){
      /*   WXMediaMessage msg=null;
        switch (type){
            case MMAlertSelect1:
                Bitmap bmp = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.ema_floating_icon);
                WXImageObject imgObj = new WXImageObject(bmp);

               msg = new WXMediaMessage();
                msg.mediaObject = imgObj;

                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
                bmp.recycle();
                msg.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图
                break;
            case MMAlertSelect2:
                String path = "storage/emulated/0/DCIM/Camera"+ "/cup.jpg";
                File file = new File(path);
                if (!file.exists()) {
                    ToastHelper.toast(mActivity,"请检查路径");
                    return;
                }

                 imgObj = new WXImageObject();
                imgObj.setImagePath(path);

                  msg = new WXMediaMessage();
                msg.mediaObject = imgObj;

                 bmp = BitmapFactory.decodeFile(path);
                  thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
                bmp.recycle();
                msg.thumbData = bmpToByteArray(thumbBmp, true);

                break;
            case MMAlertSelect3:
                String url = "http://pic14.nipic.com/20110524/1365113_101909990000_2.jpg";


                imgObj = new WXImageObject();
                //imgObj.imageUrl=url;

                msg = new WXMediaMessage();
                msg.mediaObject = imgObj;

                try {bmp = BitmapFactory.decodeStream(new URL(url).openStream());
                thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
                bmp.recycle();
                msg.thumbData = bmpToByteArray(thumbBmp, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
      sendReq(msg);*/
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
        final String url = "http://pic14.nipic.com/20110524/1365113_101909990000_2.jpg";

        final WXMediaMessage msg=new WXMediaMessage();

          new Thread() {
                public void run() {
                    Bitmap bmp = null;
                    try {
                        bmp = BitmapFactory.decodeStream(new URL(url).openStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
                //    bmp.recycle();

                    msg.thumbData = bmpToByteArray(thumbBmp, true);

                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
                    req.message = msg;
                    req.scene = SendMessageToWX.Req.WXSceneSession;// 或者SendMessageToWX.Req.WXSceneTimeline
                    mWeixinapi.sendReq(req);
                }
            }.start();

           // Bitmap bmp = BitmapFactory.decodeStream(new URL(url).openStream());
          /*  Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
            bmp.recycle();
            msg.thumbData = bmpToByteArray(thumbBmp, true);

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
            req.message = msg;
            req.scene = SendMessageToWX.Req.WXSceneSession;// 或者SendMessageToWX.Req.WXSceneTimeline
            mWeixinapi.sendReq(req);*/
    /*    } catch(Exception e) {
            e.printStackTrace();
        }*/
    }

    // 分享网页 注意这里的那个图一定要小于30k
    private  void doWxShareWebpage(String url,String title,String description,Bitmap bitmap/*,int scene*/){
       /* WXWebpageObject webpage = new WXWebpageObject();
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
        mWeixinapi.sendReq(req);*/

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title =title;
        msg.description = description;
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
        msg.thumbData = bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction =  String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;// 或者SendMessageToWX.Req.WXSceneTimeline
        mWeixinapi.sendReq(req);
    }

    private void doWxShareMusic(int type){
        WXMediaMessage msg = null;

        switch (type){
            case MMAlertSelect1:
                WXMusicObject music = new WXMusicObject();
                music.musicUrl="http://ok.96x.cn/2014/ting30_chang30_4yue/%E4%BA%B2%E7%88%B1%E7%9A%84%E6%88%91%E7%88%B1%E4%BD%A0%20%E7%A5%81%E9%9A%86.mp3";

                msg = new WXMediaMessage();
                msg.mediaObject = music;
                msg.title = "Music Title Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long";
                msg.description = "Music Album Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long";

                Bitmap thumb = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.ic_launcher);
                msg.thumbData = bmpToByteArray(thumb, true);
                break;
            case MMAlertSelect2:
                 music = new WXMusicObject();
                music.musicLowBandUrl = "http://www.qq.com";

                 msg = new WXMediaMessage();
                msg.mediaObject = music;
                msg.title = "Music Title";
                msg.description = "Music Album";

                thumb = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.ic_launcher);
                msg.thumbData = bmpToByteArray(thumb, true);
                break;
        }
        sendReq(msg);
    }

    private void doWxShareVideo(int type){
        WXMediaMessage msg = null;

        switch (type){
            case MMAlertSelect1:
                WXVideoObject video = new WXVideoObject();
                video.videoUrl = "http://www.baidu.com";

                msg = new WXMediaMessage(video);
                msg.title = "Video Title Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long";
                msg.description = "Video Description Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long";
                Bitmap thumb = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.ic_launcher);
                msg.thumbData = bmpToByteArray(thumb, true);

                break;
            case MMAlertSelect2:
                video = new WXVideoObject();
                video.videoLowBandUrl = "http://www.qq.com";

                msg = new WXMediaMessage(video);
                msg.title = "Video Title";
                msg.description = "Video Description";
                break;

        }
        sendReq(msg);
    }

    private void sendReq(WXMediaMessage msg) {
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
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
}
