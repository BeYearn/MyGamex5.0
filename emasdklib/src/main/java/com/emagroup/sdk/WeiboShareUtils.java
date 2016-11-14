package com.emagroup.sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;

import cn.emagroup.sdk.R;

/**
 * Created by Administrator on 2016/11/14.
 */
public class WeiboShareUtils {
    private static WeiboShareUtils mInstance;
    private final IWeiboShareAPI mWeiboShareAPI;
    private final Activity mActivity;

    public static WeiboShareUtils getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new WeiboShareUtils(activity);
        }
        return mInstance;
    }

    private WeiboShareUtils(Activity activity){
        this.mActivity=activity;
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(activity, "721964606");
        mWeiboShareAPI.registerApp();
    }

    public void doWeiboShare() {
            // 1. 初始化微博的分享消息
            WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
            weiboMessage.textObject = getTextObj();
            weiboMessage.imageObject = getImageObj();

            // 2. 初始化从第三方到微博的消息请求
            SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
            // 用transaction唯一标识一个请求
            request.transaction = String.valueOf(System.currentTimeMillis());
            request.multiMessage = weiboMessage;

            // 3. 发送请求消息到微博，唤起微博分享界面
            mWeiboShareAPI.sendRequest(mActivity, request);
    }

    private ImageObject getImageObj() {
        ImageObject imageObject = new ImageObject();
        //BitmapDrawable bitmapDrawable = (BitmapDrawable) mImageView.getDrawable();
        //设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        Bitmap bitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.ema_floating_icon);
        imageObject.setImageObject(bitmap);
        return imageObject;
    }

    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject getTextObj() {
        TextObject textObject = new TextObject();
        textObject.text = "我是微信分享模板";
        return textObject;
    }

    public void onNewIntent(Intent intent) {
        if(mWeiboShareAPI!=null){
            mWeiboShareAPI.handleWeiboResponse(intent, (IWeiboHandler.Response) mActivity);
        }
    }

    public interface Response extends IWeiboHandler.Response{
        void onResponse(BaseResponse var1);
    }
}
