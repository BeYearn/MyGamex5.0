package com.example.myggame;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaSDK;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.ToastHelper;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;

import java.util.HashMap;

public class MainActivity extends Activity implements OnClickListener, IWeiboHandler.Response {

    private Button btLogin;
    protected boolean isSuccess;
    private Handler uiHandler;
    private Button btPay;
    private LinearLayout myLayout;
    private Button btLogout;
    private Button btShowBar;
    private Button btHideBar;
    private Button btSwichAccount;
    private IWeiboShareAPI mWeiboShareAPI;
    private Button btShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 创建微博分享接口实例-----------------------------------------------------------------------------------
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, "721964606");
        mWeiboShareAPI.registerApp();


        //微博-------------------------------------------------------------------------------------


        uiHandler = new Handler();
        btLogin = (Button) findViewById(R.id.bt_login);
        btPay = (Button) findViewById(R.id.bt_pay);
        btLogout = (Button) findViewById(R.id.bt_logout);
        btShowBar = (Button) findViewById(R.id.bt_showbar);
        btHideBar = (Button) findViewById(R.id.bt_hidebar);
        btSwichAccount = (Button) findViewById(R.id.bt_swichaccount);
        btShare = (Button) findViewById(R.id.bt_share);


        EmaSDK.getInstance().init("6cdd60ea0045eb7a6ec44c54d29ed402", this, new EmaSDKListener() {
            //EmaSDK.getInstance().init("5600441101c8818c4480d3c503742a3b",this, new EmaSDKListener() {
            //EmaSDK.getInstance().init("800a924c499772bac7b76432803ea47a",this, new EmaSDKListener() {  //10001
            @Override
            public void onCallBack(int arg0, String arg1) {
                Log.e("mainactivity", arg0 + "++++++++++++++++ " + arg1);
                switch (arg0) {
                    case EmaCallBackConst.INITSUCCESS://初始化SDK成功回调
                        isSuccess = true;
                        Log.e("Mainactivity", "sdk初始化成功");
                        break;
                    case EmaCallBackConst.INITFALIED://初始化SDK失败回调
                        Log.e("Mainactivity", "sdk初始化失败");
                        break;
                    case EmaCallBackConst.LOGINSUCCESS://登陆成功回调
                        ToastHelper.toast(MainActivity.this, "登陆成功");
                        Log.e("Mainactivity", EmaUser.getInstance().getNickName());
                        Log.e("Mainactivity", EmaUser.getInstance().getAllianceUid());
                        Log.e("Mainactivity", EmaSDK.getInstance().getChannelId());
                        break;
                    case EmaCallBackConst.LOGINCANELL://登陆取消回调
                        break;
                    case EmaCallBackConst.LOGINFALIED://登陆失败回调
                        Log.e("++++++++++", Thread.currentThread().getName());
                        ToastHelper.toast(MainActivity.this, "登陆失败");
                        break;
                    case EmaCallBackConst.LOGOUTSUCCESS://登出成功回调
                        break;
                    case EmaCallBackConst.LOGOUTFALIED://登出失败回调
                        break;
                    case EmaCallBackConst.ACCOUNTSWITCHSUCCESS:
                        Log.e("EMASDK", "ACCOUNTSWITCHSUCCESS");//切换帐号成功回调
                        break;
                    case EmaCallBackConst.ACCOUNTSWITCHFAIL:
                        break;
                }
            }
        });

        EmaSDK.getInstance().doSetRecivePushListner(new EmaSDKListener() {
            @Override
            public void onCallBack(int resultCode, String data) {
                if (resultCode == EmaCallBackConst.RECIVEMSG_MSG) {
                    // TODO:  data为拿到的推送数据,自行处理
                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                }
            }
        });
        //initPayListner();

        btLogin.setOnClickListener(this);
        btPay.setOnClickListener(this);
        btLogout.setOnClickListener(this);
        btShowBar.setOnClickListener(this);
        btHideBar.setOnClickListener(this);
        btSwichAccount.setOnClickListener(this);
        btShare.setOnClickListener(this);

        Log.e("++++++++++", Thread.currentThread().getName());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
        /*mWeiboShareAPI.handleWeiboResponse(intent, new IWeiboHandler.Response() {
            @Override
            public void onResponse(BaseResponse baseResponse) {
                if (baseResponse != null) {
                    Log.e("weibofenxiang",baseResponse.toString());
                    switch (baseResponse.errCode) {
                        case WBConstants.ErrorCode.ERR_OK:
                            ToastHelper.toast(MainActivity.this, "share successful");
                            break;
                        case WBConstants.ErrorCode.ERR_CANCEL:
                            ToastHelper.toast(MainActivity.this, "share cancel");
                            break;
                        case WBConstants.ErrorCode.ERR_FAIL:
                            ToastHelper.toast(MainActivity.this, "share fail");
                            break;
                    }
                }
            }
        });*/
        mWeiboShareAPI.handleWeiboResponse(intent, this);
    }

    @Override
    public void onResponse(BaseResponse baseResponse) {
        if (baseResponse != null) {
            Log.e("weibofenxiang", baseResponse.toString());
            switch (baseResponse.errCode) {
                case WBConstants.ErrorCode.ERR_OK:
                    ToastHelper.toast(MainActivity.this, "share successful");
                    break;
                case WBConstants.ErrorCode.ERR_CANCEL:
                    ToastHelper.toast(MainActivity.this, "share cancel");
                    break;
                case WBConstants.ErrorCode.ERR_FAIL:
                    ToastHelper.toast(MainActivity.this, "share fail");
                    break;
            }
        }
    }
    //微博-------------------------------------------------------------------------------------
    private void doShare() {
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
        mWeiboShareAPI.sendRequest(this, request);
    }

    private ImageObject getImageObj() {
        ImageObject imageObject = new ImageObject();
        //BitmapDrawable bitmapDrawable = (BitmapDrawable) mImageView.getDrawable();
        //设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ema_floating_icon);
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

    //微博-------------------------------------------------------------------------------------

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_login:
                MainActivity.this.runOnUiThread(  //有的接入会发生不在主线程调用该方法，所以最外面加上这个
                        new Runnable() {
                            @Override
                            public void run() {
                                EmaSDK.getInstance().doLogin();
                            }
                        }
                );

                /*if (isSuccess) {
                } else {
                    Toast.makeText(this, "sdk未初始化成功,不能登录", Toast.LENGTH_LONG).show();
                }*/
                break;
            case R.id.bt_logout:
                EmaSDK.getInstance().doLogout();
                break;
            case R.id.bt_showbar:
                EmaSDK.getInstance().doShowToolbar();
                break;
            case R.id.bt_hidebar:
                EmaSDK.getInstance().doHideToobar();
                break;
            case R.id.bt_pay:

                HashMap<String, String> payInfoMap = new HashMap<>();
                payInfoMap.put(EmaConst.EMA_PAYINFO_PRODUCT_ID, "10001");
                payInfoMap.put(EmaConst.EMA_PAYINFO_PRODUCT_COUNT, "1");
                payInfoMap.put(EmaConst.EMA_GAMETRANS_CODE, "游戏透传参数");
                //payInfoMap.put("Product_Name","gold");
                //payInfoMap.put("Server_Id", "13");
                //payInfoMap.put("Role_Id","1");
                //payInfoMap.put("Role_Name", "1");
                //payInfoMap.put("Role_Grade", "1");
                //payInfoMap.put("Role_Balance", "1");

                EmaSDK.getInstance().doPay(payInfoMap, new EmaSDKListener() {
                    @Override
                    public void onCallBack(int arg0, String arg1) {
                        Log.d(String.valueOf(arg0), arg1);
                        switch (arg0) {
                            case EmaCallBackConst.PAYSUCCESS:// 支付成功回调
                                ToastHelper.toast(MainActivity.this, "pay successful---");
                                break;
                            case EmaCallBackConst.PAYFALIED:// 支付失败回调
                                ToastHelper.toast(MainActivity.this, "pay failed---");
                                break;
                            case EmaCallBackConst.PAYCANELI:// 支付取消回调
                                ToastHelper.toast(MainActivity.this, "pay Cancel");
                                break;
                        }
                    }
                });
                break;
            case R.id.bt_swichaccount:
                Log.e("bt_swichaccount", "dianjile ");
                EmaSDK.getInstance().doSwichAccount();
                break;
            case R.id.bt_share:
                doShare();
                break;
        }


    }

    /*private void showDialog(String str) {
        final String curMsg = str;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                //dialog参数设置
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);  //先得到构造器
                builder.setTitle("提示"); //设置标题
                //builder.setMessage("是否确认退出?"); //设置内容
                //builder.setIcon(R.drawable.ic_launcher);//设置图标，图片id即可
                //设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        *//*builder.setItems(items,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, items[which], Toast.LENGTH_SHORT).show();

            }
        });*//*
                builder.setMessage(curMsg);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.create().show();
            }
        });
    }*/


    @Override
    protected void onDestroy() {
        EmaSDK.getInstance().onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        EmaSDK.getInstance().onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        EmaSDK.getInstance().onResume();
        super.onResume();
    }


    @Override
    protected void onStop() {
        EmaSDK.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        EmaSDK.getInstance().onBackPressed(new EmaBackPressedAction() {
            @Override
            public void doBackPressedAction() {
                //游戏需要做的逻辑
                ToastHelper.toast(MainActivity.this, "我是游戏自己的逻辑xxxxxxxxxxxxx");
                MainActivity.super.onBackPressed();
            }
        });
        //super.onBackPressed();
    }

}
