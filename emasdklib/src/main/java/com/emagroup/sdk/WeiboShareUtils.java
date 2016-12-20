package com.emagroup.sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;

/**
 * Created by Administrator on 2016/11/14.
 */
public class WeiboShareUtils {
    private static WeiboShareUtils mInstance;
     private final IWeiboShareAPI mWeiboShareAPI;
    private final Activity mActivity;
    public EmaSDKListener mListener;
    public Bitmap bitmap;
    public  final  static  int SHARE_TEXT=1;
    public  final  static int SHARE_IMAGE=2;
    public  final   static int SHARE_WEBPAGE=3;
    public static WeiboShareUtils getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new WeiboShareUtils(activity);
        }
        return mInstance;
    }

    private WeiboShareUtils(Activity activity){
        this.mActivity=activity;
       mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(activity,
               ConfigManager.getInstance(mActivity).getWeiBoAppId());
        mWeiboShareAPI.registerApp();


    }

    public void doWeiboShare(String text ,Bitmap  bitmap) {

     /*   if(TextUtils.isEmpty(text)||bitmap==null){
            Toast.makeText(mActivity,"请输入完整参数",Toast.LENGTH_LONG).show();
            return;
        }

        if(!mWeiboShareAPI.isWeiboAppInstalled()){
            Toast.makeText(mActivity,"未安装或版本过低, 请下载更新的版本",Toast.LENGTH_LONG).show();
            return;
        } */
      // 1. 初始化微博的分享消息
          /*  WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
            weiboMessage.textObject = getTextObj(text);
          weiboMessage.imageObject =getImageObj(bitmap);
            // 2. 初始化从第三方到微博的消息请求
            SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
            // 用transaction唯一标识一个请求
            request.transaction = String.valueOf(System.currentTimeMillis());
            request.multiMessage = weiboMessage;

            // 3. 发送请求消息到微博，唤起微博分享界面
            mWeiboShareAPI.sendRequest(mActivity, request);*/
    }

    public void doWeiboShareImage/*Text*/(/*String text ,*/EmaSDKListener listener,Bitmap  bitmap) {
        this.mListener = listener;
        this.bitmap=bitmap;
        if(/*TextUtils.isEmpty(text)||*/bitmap==null){
            Toast.makeText(mActivity,"请输入完整参数",Toast.LENGTH_LONG).show();
            return;
        }

        if(!mWeiboShareAPI.isWeiboAppInstalled()){
            Toast.makeText(mActivity,"未安装或版本过低, 请下载更新的版本",Toast.LENGTH_LONG).show();
            return;
        }

        startWeiBoEntryActivity(SHARE_IMAGE,new Intent());

      /*  WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
         ImageObject imageObject = new ImageObject();*/

        //BitmapDrawable bitmapDrawable = (BitmapDrawable) mImageView.getDrawable();
        //设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
       // Bitmap bitmap = BitmapFactory.decodeResource(mActivity.getResources(),icon);
     //   Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 150, 150, true);

       /* imageObject.setImageObject(bitmap);
        weiboMessage.imageObject =imageObject;

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        boolean statu=  mWeiboShareAPI.sendRequest(mActivity, request);
        Log.i("doWeiBoShareWebpage","doWeiBoShareWebpage statu "+statu);*/

      //  return imageObject;
    }

    private void startWeiBoEntryActivity(int type,Intent intent) {
        intent.setClass(mActivity,WeiBoEntryactivity.class);
        intent.putExtra("sharType",type);
        mActivity.startActivity(intent);
    }

    public   void doWeiboShareText (String text,EmaSDKListener listener){
        this.mListener = listener;
        if(TextUtils.isEmpty(text)/*||bitmap==null*/){
            Toast.makeText(mActivity,"请输入完整参数",Toast.LENGTH_LONG).show();
            return;
        }

        if(!mWeiboShareAPI.isWeiboAppInstalled()){
            Toast.makeText(mActivity,"未安装或版本过低, 请下载更新的版本",Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent=new Intent();
        intent.putExtra("text",text);
        startWeiBoEntryActivity(SHARE_TEXT,intent);

       /* WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
         weiboMessage.textObject = getTextObj(text);

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest(mActivity, request);
        boolean statu=  mWeiboShareAPI.sendRequest(mActivity, request);
        Log.i("doWeiBoShareWebpage","doWeiBoShareWebpage statu "+statu);*/
    }

    public void doWeiBoShareWebpage(String title, String description, Bitmap bitmap, String url, EmaSDKListener listener){
        this.mListener=listener;
        this.bitmap=bitmap;
        if(TextUtils.isEmpty(title) ||TextUtils.isEmpty(description)||TextUtils.isEmpty(url)||bitmap==null ){
            Toast.makeText(mActivity,"请输入完整参数",Toast.LENGTH_LONG).show();
            return;
        }

        if(!mWeiboShareAPI.isWeiboAppInstalled()){
            Toast.makeText(mActivity,"未安装或版本过低, 请下载更新的版本",Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent=new Intent();
        intent.putExtra("title",title);
        intent.putExtra("description",description);
        intent.putExtra("url",url);
        startWeiBoEntryActivity(SHARE_WEBPAGE,intent);

     /*   WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = title;
        mediaObject.description = description;
        mediaObject.actionUrl = url;
        mediaObject.defaultText = "Webpage 默认文案";
     //   Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
        mediaObject.setThumbImage(bitmap);
        weiboMessage.mediaObject=mediaObject;


        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
      boolean statu=  mWeiboShareAPI.sendRequest(mActivity, request);
        Log.i("doWeiBoShareWebpage","doWeiBoShareWebpage statu "+statu);*/
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



    public void onNewIntent(Intent intent) {
        if(mWeiboShareAPI!=null){
            mWeiboShareAPI.handleWeiboResponse(intent, (IWeiboHandler.Response) mActivity);
        }
    }

    public interface Response extends IWeiboHandler.Response{
        void onResponse(BaseResponse var1);
    }



}
