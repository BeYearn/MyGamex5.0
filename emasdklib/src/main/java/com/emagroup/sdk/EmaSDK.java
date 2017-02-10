package com.emagroup.sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;

import java.util.Map;

import static com.emagroup.sdk.UCommUtil.doShare;

/**
 * Created by Administrator on 2016/8/22.
 */
public class EmaSDK {
    private static EmaSDK mInstance;
    private EmaSDKListener reciveMsgListener;
    public String title,url,summary;
    public Bitmap bitmap;
    public EmaSDKListener mListener;
    private Activity mActivity;

    private EmaSDK() {
    }

    public static EmaSDK getInstance() {
        if (mInstance == null) {
            mInstance = new EmaSDK();
        }
        return mInstance;
    }


    public void init(String appKey, Activity activity, EmaSDKListener listener) {
        this.mActivity=activity;
        Ema.getInstance().init(appKey, activity, listener);
    }


    public void doLogin() {
        Ema.getInstance().Login();
    }

    public void doLogout() {
        Ema.getInstance().Logout();
    }

    public void doSwichAccount() {
        Ema.getInstance().swichAccount();
    }

    // TODO: 2016/9/22 暂时先传pid和count这两个参数
    public void doPay(Map<String, String> info, final EmaSDKListener listener) {
        //在这里把这个map转化到emapayinfo里面  目前需要 商品pid，数量
        EmaPayInfo emaPayInfo = new EmaPayInfo();
        for (Map.Entry<String, String> entry : info.entrySet()) {
            String infoValue = entry.getValue();
            switch (entry.getKey()) {
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
                listener.onCallBack(msg.what, msg.toString());
            }
        });
    }


    public void doShowToolbar() {
        Ema.getInstance().showToolBar();
    }


    public void doHideToobar() {
        Ema.getInstance().hideToolBar();
    }

    public void doSetRecivePushListner(EmaSDKListener listener) {
        this.reciveMsgListener = listener;
    }

    /**
     * 个推的reciver收到透传消息后回调该方法
     *
     * @param msgCode
     * @param msgObj
     */
    public void makeCallBack(int msgCode, String msgObj) {
        if (reciveMsgListener == null) {
            LOG.w("warn", "未设置回调");
            return;
        }
        reciveMsgListener.onCallBack(msgCode, msgObj);
    }

    public boolean isEma() {
        return !(Ema.getInstance().getChannelId().length() == 6);
    }

    public String getChannelId() {
        return Ema.getInstance().getChannelId();
    }

    public String getChannelTag() {
        return Ema.getInstance().getChannelTag();
    }


    public void doShareImage(final Activity activity, final EmaSDKListener listener, final Bitmap bitmap) {
        doShare(activity, new ShareDialog.OnBtnListener() {
            @Override
            public void onWeiBoClick() {
                WeiboShareUtils.getInstance(activity).doWeiboShareImage(listener, bitmap);
            }

            @Override
            public void onWechatFriendsClick() {
                WeixinShareUtils.getInstance(activity).doWxShareImg(listener, bitmap, SendMessageToWX.Req.WXSceneSession);
            }

            @Override
            public void OnWechatQuanClick() {
                WeixinShareUtils.getInstance(activity).doWxShareImg(listener, bitmap, SendMessageToWX.Req.WXSceneTimeline);
            }

            @Override
            public void OnQQClick() {
              //  QQShareUtils.getIntance(activity).shareQQFriendImage(listener, bitmap);
                EmaSDK.this.mListener=listener;
                EmaSDK.this.bitmap=bitmap;
                Intent intent=new Intent(activity,WeiBoEntryActivity.class);
                intent.putExtra("sharePf","QQ");
                intent.putExtra("sharType",QQShareUtils.SHARE_QQ_FRIEDNS_IMAGE);
                activity.startActivity(intent);
            }

            @Override
            public void OnQZoneClick() {
                // Toast.makeText(activity,"QQ空间无图片分享",Toast.LENGTH_SHORT).show();
             //   QQShareUtils.getIntance(activity).shareQzoneImage(listener, bitmap);
                EmaSDK.this.mListener=listener;
                EmaSDK.this. bitmap=bitmap;
                Intent intent=new Intent(activity,WeiBoEntryActivity.class);
                intent.putExtra("sharePf","QQ");
                intent.putExtra("sharType",QQShareUtils.SHARE_QQ_QZONE_IMAGE);
                activity.startActivity(intent);
            }
        });
    }

    public void doShareText(final Activity activity, final EmaSDKListener listener, final String text) {
        doShare(activity, new ShareDialog.OnBtnListener() {
            @Override
            public void onWeiBoClick() {
                WeiboShareUtils.getInstance(activity).doWeiboShareText(text, listener);
            }

            @Override
            public void onWechatFriendsClick() {
                WeixinShareUtils.getInstance(activity).doWxShareText(listener, text, SendMessageToWX.Req.WXSceneSession);
            }

            @Override
            public void OnWechatQuanClick() {
                WeixinShareUtils.getInstance(activity).doWxShareText(listener, text, SendMessageToWX.Req.WXSceneTimeline);
            }

            @Override
            public void OnQQClick() {
                Toast.makeText(activity, "QQ好友无文字分享", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnQZoneClick() {
              //  QQShareUtils.getIntance(activity).shareQzoneText(text, listener);

                EmaSDK.this.mListener=listener;
                EmaSDK.this.summary=text;
                Intent intent=new Intent(activity,WeiBoEntryActivity.class);
                intent.putExtra("sharePf","QQ");
                intent.putExtra("sharType",QQShareUtils.SHARE_QQ_QZONE_TEXT);
                activity.startActivity(intent);
            }
        });
    }

    public void doShareWebPage(final Activity activity, final EmaSDKListener listener, final String url,
                               final String title, final String description, final Bitmap bitmap) {
        doShare(activity, new ShareDialog.OnBtnListener() {
            @Override
            public void onWeiBoClick() {
                WeiboShareUtils.getInstance(activity).doWeiBoShareWebpage(title, description, bitmap, url, listener);
            }

            @Override
            public void onWechatFriendsClick() {
                WeixinShareUtils.getInstance(activity).doWxShareWebpage(listener, url, title, description, bitmap, SendMessageToWX.Req.WXSceneSession);
            }

            @Override
            public void OnWechatQuanClick() {
                WeixinShareUtils.getInstance(activity).doWxShareWebpage(listener, url, title, description, bitmap, SendMessageToWX.Req.WXSceneTimeline);
            }

            @Override
            public void OnQQClick() {
                EmaSDK.this.mListener=listener;
                EmaSDK.this.bitmap=bitmap;
                EmaSDK.this.title=title;
                EmaSDK.this.url=url;
                EmaSDK.this.summary=description;
                Intent intent=new Intent(activity,WeiBoEntryActivity.class);
                intent.putExtra("sharePf","QQ");
                intent.putExtra("sharType",QQShareUtils.SHARE_QQ_FRIEDNS_WEBPAGE);
                activity.startActivity(intent);
               // QQShareUtils.getIntance(activity).shareQQFriendsWebPage(listener, title, url, description, bitmap);
            }

            @Override
            public void OnQZoneClick() {
               // QQShareUtils.getIntance(activity).shareQzoneWebPage(listener, title, url, description, bitmap);
                EmaSDK.this.mListener=listener;
                EmaSDK.this.bitmap=bitmap;
                EmaSDK.this.title=title;
                EmaSDK.this.url=url;
                EmaSDK.this.summary=description;
                Intent intent=new Intent(activity,WeiBoEntryActivity.class);
                intent.putExtra("sharePf","QQ");
                intent.putExtra("sharType",QQShareUtils.SHARE_QQ_QZONE_WEBPAGE);
                activity.startActivity(intent);
            }
        });
    }

    public void startRecordScreen(){
        UCommUtil.recordScreen(mActivity);
    }

    public void onNewIntent(Intent intent) {
        WeiboShareUtils.getInstance((Activity) Ema.getInstance().getContext()).onNewIntent(intent);
        Ema.getInstance().onNewIntent(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    public void onRestart() {
        Ema.getInstance().onRestart();
    }

    public void onDestroy() {
        Ema.getInstance().onDestroy();
    }

    public void onBackPressed(EmaBackPressedAction action) {
        Ema.getInstance().onBackPressed(action);
    }

}
