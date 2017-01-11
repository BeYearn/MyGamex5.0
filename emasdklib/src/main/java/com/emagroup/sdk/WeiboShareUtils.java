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
    public void doWeiboShareImage(EmaSDKListener listener,Bitmap  bitmap) {
        this.mListener = listener;
        this.bitmap=bitmap;
        if(bitmap==null){
            Toast.makeText(mActivity,"请输入完整参数",Toast.LENGTH_LONG).show();
            return;
        }

        if(!mWeiboShareAPI.isWeiboAppInstalled()){
            Toast.makeText(mActivity,"未安装或版本过低, 请下载更新的版本",Toast.LENGTH_LONG).show();
            return;
        }
        startWeiBoEntryActivity(SHARE_IMAGE,new Intent());
    }

    private void startWeiBoEntryActivity(int type,Intent intent) {
        intent.putExtra("sharePf","webo");
        intent.setClass(mActivity,WeiBoEntryActivity.class);
        intent.putExtra("sharType",type);
        mActivity.startActivity(intent);
    }

    public   void doWeiboShareText (String text,EmaSDKListener listener){
        this.mListener = listener;
        if(TextUtils.isEmpty(text)){
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
