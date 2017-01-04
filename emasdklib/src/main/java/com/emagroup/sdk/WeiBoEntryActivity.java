package com.emagroup.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.tencent.tauth.Tencent;

import static com.emagroup.sdk.WeiboShareUtils.SHARE_TEXT;
import static com.emagroup.sdk.WeiboShareUtils.SHARE_WEBPAGE;

/**
 * Created by Administrator on 2016/12/20.
 */

public class WeiBoEntryActivity extends Activity implements IWeiboHandler.Response {
    private IWeiboShareAPI mWeiboShareAPI;
    private boolean canShare;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this,
                /*"721964606"*/  /*"1008659864"*/  ConfigManager.getInstance(this).getWeiBoAppId());
        mWeiboShareAPI.registerApp();

        canShare= (boolean) USharedPerUtil.getParam(this,"canWbShare",true);

        Intent intent = getIntent();

        Log.e("WeiBoEntryActivity", "onCreate");
        if (canShare) {
            if(intent.getStringExtra("sharePf").equals("webo")){//微博分享
                switch (intent.getIntExtra("sharType", 0)) {
                    case WeiboShareUtils.SHARE_IMAGE:
                        shareImage();
                        break;
                    case SHARE_WEBPAGE:
                        doWeiBoShareWebpage(intent.getStringExtra("title"), intent.getStringExtra("description"), intent.getStringExtra("url"));
                        break;
                    case SHARE_TEXT:
                        doWeiboShareText(intent.getStringExtra("text"));
                        break;
                }
           }else {
                switch (intent.getIntExtra("sharType", 0)) {
                    case QQShareUtils.SHARE_QQ_FRIEDNS_IMAGE:
                       /* QQShareUtils.getIntance*/new QQShareUtils(WeiBoEntryActivity.this).shareQQFriendImage();
                        break;
                    case QQShareUtils.SHARE_QQ_FRIEDNS_WEBPAGE:
                       /* QQShareUtils.getIntance*/new QQShareUtils(WeiBoEntryActivity.this).shareQQFriendsWebPage();
                        break;
                    case  QQShareUtils.SHARE_QQ_QZONE_IMAGE:
                       /* QQShareUtils.getIntance*/new QQShareUtils(WeiBoEntryActivity.this).shareQzoneImage();
                        break;
                    case QQShareUtils.SHARE_QQ_QZONE_TEXT:
                        /*QQShareUtils.getIntance*/new QQShareUtils(WeiBoEntryActivity.this).shareQzoneText();
                        break;
                    case  QQShareUtils.SHARE_QQ_QZONE_WEBPAGE:
                        /*QQShareUtils.getIntance*/new QQShareUtils(WeiBoEntryActivity.this).shareQzoneWebPage();
                        break;

                }

            }
            USharedPerUtil.setParam(this,"canWbShare",false);
        }
    }


    public void doWeiboShareText(String text) {

        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.textObject = getTextObj(text);

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面

        boolean statu = mWeiboShareAPI.sendRequest(this, request);
        Log.i("doWeiBoShareWebpage", "doWeiBoShareWebpage statu " + statu);
    }

    private void shareImage() {

        Log.e("WeiBoEntryActivity", "shareImage"+this.hashCode());
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        ImageObject imageObject = new ImageObject();

        imageObject.setImageObject(WeiboShareUtils.getInstance(this).bitmap);
        weiboMessage.imageObject = imageObject;

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        boolean statu = mWeiboShareAPI.sendRequest(this, request);
        Log.i("doWeiBoShareWebpage", "doWeiBoShareWebpage statu " + statu);
    }

    public void doWeiBoShareWebpage(String title, String description, String url) {

        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.textObject = getTextObj(title+"\n"+description+" "+url);


        ImageObject imageObject = new ImageObject();

        imageObject.setImageObject(WeiboShareUtils.getInstance(this).bitmap);
        weiboMessage.imageObject = imageObject;




       /* WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = title;
        mediaObject.description = description;
        mediaObject.actionUrl = url;
        mediaObject.defaultText = "Webpage 默认文案";
        //   Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
        mediaObject.setThumbImage(WeiboShareUtils.getInstance(this).bitmap);
        weiboMessage.mediaObject = mediaObject;*/


        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        boolean statu = mWeiboShareAPI.sendRequest(this, request);
        Log.i("doWeiBoShareWebpage", "doWeiBoShareWebpage statu " + statu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mWeiboShareAPI != null) {
            mWeiboShareAPI.handleWeiboResponse(intent, (IWeiboHandler.Response) this);
        }
    }

    @Override
    public void onResponse(BaseResponse baseResponse) {
        if (baseResponse != null) {
            Log.e("WeiBoEntryActivity", baseResponse.toString());
            UCommUtil.shareCallback(this, baseResponse);

            USharedPerUtil.setParam(this,"canWbShare",true);
        }
       finish();
    }

    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject getTextObj(String text) {
        TextObject textObject = new TextObject();
        textObject.text = text;
        return textObject;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode,resultCode,data,new QQShareUtils(WeiBoEntryActivity.this).emIUiListener);

    }
}
