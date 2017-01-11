package com.emagroup.sdk;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;


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

    private WeixinShareUtils(Activity activity) {
        this.mActivity = activity;
        mWeixinapi = WXAPIFactory.createWXAPI(activity, ConfigManager.getInstance(mActivity).getWachatAppId()/*"wx9c31edc5e693ec1d"*/, true);
        mWeixinapi.registerApp(ConfigManager.getInstance(mActivity).getWachatAppId()/*"wx9c31edc5e693ec1d"*/);


    }

    public void doWxShareText(EmaSDKListener listener, String text,int scene) {
        this.mListener = listener;
        boolean sIsWXAppInstalledAndSupported = mWeixinapi.isWXAppInstalled()
                && mWeixinapi.isWXAppSupportAPI();
        if (!sIsWXAppInstalledAndSupported) {
            Toast.makeText(mActivity, "未安装或版本过低, 请下载更新的版本", Toast.LENGTH_LONG).show();
            return;

        }

        if (listener == null || TextUtils.isEmpty(text)) {
            Toast.makeText(mActivity, "请输入完整参数", Toast.LENGTH_LONG).show();
            return;
        }
        WXTextObject textObject = new WXTextObject();
        textObject.text = text;

        WXMediaMessage wxMediaMessage = new WXMediaMessage();
        wxMediaMessage.mediaObject = textObject;
        wxMediaMessage.description = "description";

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
        req.message = wxMediaMessage;
        req.scene = scene;// 或者SendMessageToWX.Req.WXSceneTimeline   SendMessageToWX.Req.WXSceneSession

        // 调用api接口发送数据到微信
        boolean statu = mWeixinapi.sendReq(req);
        Ema.getInstance().saveWachatLoginFlag(false);
        Log.i("doWeiBoShareWebpage", "doWeiBoShareWebpage statu " + statu);

    }

    public void doWxShareImg(EmaSDKListener listener, Bitmap bitmap, int scene) {
        this.mListener = listener;
        boolean sIsWXAppInstalledAndSupported = mWeixinapi.isWXAppInstalled()
                && mWeixinapi.isWXAppSupportAPI();
        if (!sIsWXAppInstalledAndSupported) {
            Toast.makeText(mActivity, "未安装或版本过低, 请下载更新的版本", Toast.LENGTH_LONG).show();
            return;

        }

        if (listener == null || bitmap == null) {
            Toast.makeText(mActivity, "请输入完整参数", Toast.LENGTH_LONG).show();
            return;
        }

        WXImageObject imgObj = new WXImageObject(bitmap);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
        msg.thumbData = bmpToByteArray(thumbBmp, true);
        thumbBmp.recycle();


        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = scene;// 或者SendMessageToWX.Req.WXSceneTimeline  SendMessageToWX.Req.WXSceneSession
        boolean statu = mWeixinapi.sendReq(req);
        Ema.getInstance().saveWachatLoginFlag(false);
        Log.i("doWxShareImg", "doWxShareImg statu " + statu);

    }


    // 分享网页 注意这里的那个图一定要小于30k
    public void doWxShareWebpage(EmaSDKListener listener, String url, String title, String description, Bitmap bitmap, int scene) {
        this.mListener = listener;
        boolean sIsWXAppInstalledAndSupported = mWeixinapi.isWXAppInstalled()
                && mWeixinapi.isWXAppSupportAPI();
        if (!sIsWXAppInstalledAndSupported) {
            Toast.makeText(mActivity, "未安装或版本过低, 请下载更新的版本", Toast.LENGTH_LONG).show();
            return;

        }

        if (listener == null || TextUtils.isEmpty(url) || TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || bitmap == null) {
            Toast.makeText(mActivity, "请输入完整参数", Toast.LENGTH_LONG).show();
            return;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;

        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
        msg.thumbData = bmpToByteArray(thumbBmp, true);
        thumbBmp.recycle();


        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = scene;// 或者SendMessageToWX.Req.WXSceneTimeline  SendMessageToWX.Req.WXSceneSession
        boolean statu = mWeixinapi.sendReq(req);
        Ema.getInstance().saveWachatLoginFlag(false);
        Log.i("doWxShareWebpage", "doWxShareWebpage statu " + statu);

    }

    public byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
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

    /**
     * 把网络资源图片转化成bitmap
     * @param url  网络资源图片
     * @return Bitmap
     */
  /*  public static Bitmap GetLocalOrNetBitmap(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }*/
}
