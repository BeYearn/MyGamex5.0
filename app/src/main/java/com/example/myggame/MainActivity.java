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
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements OnClickListener/*, Response */{

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

    private IWXAPI mWeixinapi;
    /*    private Button btWxShare;*/
    private Button btUpGameInfo;
    private Button btAgain_init;
    private Button bt_snap_shot;
    private Button btEmShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiHandler = new Handler();
        btLogin = (Button) findViewById(R.id.bt_login);
        btPay = (Button) findViewById(R.id.bt_pay);
        btLogout = (Button) findViewById(R.id.bt_logout);
        btShowBar = (Button) findViewById(R.id.bt_showbar);
        btHideBar = (Button) findViewById(R.id.bt_hidebar);
        btSwichAccount = (Button) findViewById(R.id.bt_swichaccount);
        // btWbShare = (Button) findViewById(R.id.bt_wbshare);
        btUpGameInfo= (Button) findViewById(R.id.up_game_info);
        btEmShare = (Button) findViewById(R.id.bt_emshare);
        btAgain_init= (Button) findViewById(R.id.again_init);
        bt_snap_shot = (Button) findViewById(R.id.bt_snap_shot);
        init();


        EmaSDK.getInstance().doSetRecivePushListner(new EmaSDKListener() {
            @Override
            public void onCallBack(int resultCode, String data) {
                if (resultCode == EmaCallBackConst.RECIVEMSG_MSG) {
                    // TODO:  data为拿到的推送数据,自行处理
                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();

                }
            }
        });
        btAgain_init.setOnClickListener(this);
        btLogin.setOnClickListener(this);
        btPay.setOnClickListener(this);
        btLogout.setOnClickListener(this);
        btShowBar.setOnClickListener(this);
        btHideBar.setOnClickListener(this);
        btSwichAccount.setOnClickListener(this);
        btUpGameInfo.setOnClickListener(this);
      /*  btWxShare.setOnClickListener(this);*/
        btEmShare.setOnClickListener(this);
        bt_snap_shot.setOnClickListener(this);
        Log.e("++++++++++", Thread.currentThread().getName());
    }

    private void init() {
        //EmaSDK.getInstance().init("6cdd60ea0045eb7a6ec44c54d29ed402", this, new EmaSDKListener() {
        EmaSDK.getInstance().init("5600441101c8818c4480d3c503742a3b",this, new EmaSDKListener() {
        //EmaSDK.getInstance().init("800a924c499772bac7b76432803ea47a",this, new EmaSDKListener() {  //10001
        @Override
        public void onCallBack(int arg0, String arg1) {
            Log.e("mainactivity", arg0 + "++++++++++++++++ " + arg1);
            switch (arg0) {
                case EmaCallBackConst.INITSUCCESS://初始化SDK成功回调
                    isSuccess = true;
                    ToastHelper.toast(MainActivity.this,"sdk初始化成功");
                    break;
                case EmaCallBackConst.INITFALIED://初始化SDK失败回调
                    ToastHelper.toast(MainActivity.this,"sdk初始化失败");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            init();
                        }
                    });
                    break;
                case EmaCallBackConst.LOGINSUCCESS://登陆成功回调
                    ToastHelper.toast(MainActivity.this, "登陆成功");
                    Log.e("Mainactivity", EmaUser.getInstance().getNickName());
                    Log.e("Mainactivity", EmaUser.getInstance().getAllianceUid());
                    Log.e("Mainactivity", EmaUser.getInstance().getmUid());
                    Log.e("Mainactivity", EmaSDK.getInstance().getChannelId());
                    Log.e("Mainactivity", EmaSDK.getInstance().getChannelTag());
                   // ToastHelper.toast(MainActivity.this,EmaSDK.getInstance().isEma()+"");

                    submitRoleinfo("24001");
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
    }


    //微博-------------------------------------------------------------------------------------
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
        EmaSDK.getInstance().onNewIntent(intent);
    }

  /*  @Override
    public void onResponse(BaseResponse baseResponse) {
        if (baseResponse != null) {
            Log.e("weibofenxiang", baseResponse.toString());
            UCommUtil.shareCallback(this, baseResponse);
        }
    }*/

    //微博-------------------------------------------------------------------------------------

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.bt_snap_shot:
              /*  try {
                    EmaSDK.getInstance().doShareImage(MainActivity.this, new ShareEmaSDKListener(), Snapshot.saveBitmap(view, MainActivity.this));
                    // shareImage();;
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                break;
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
            case R.id.bt_emshare:
                  shareImage();
                //  shareWebPage();
                //     shareText();
                break;
            case R.id.again_init:
               init();
           /*   new AsyncTask<Void, Void, Bitmap>() {
                   @Override
                   protected Bitmap doInBackground(Void... voids) {
                       return Snapshot.getBitmap("https://testing-platform.lemonade-game.com:8443/wap/img/tbs57.png");
                   }

                   @Override
                   protected void onPostExecute(Bitmap bitmap) {
                       ((ImageView)findViewById(R.id.image)).setImageBitmap(bitmap);
                   }
               }.execute();*/

                break;
            case R.id.up_game_info:
                submitRoleinfo("24001");
                break;


        }
    }

    private void submitRoleinfo(String zoneId){
        Map<String,String > param=new HashMap<String, String>();
        param.put("roleId","00001");
        param.put("roleName","emasdk");
        param.put("roleLevel","11");
        param.put("zoneId",zoneId);
        param.put("zoneName","emasever");
        param.put("dataType","0");
        param.put("ext","99999");
        EmaUser.getInstance().submitLoginGameRole(param);
    }

   private void shareWebPage() {
        String url = "http://www.baidu.com";//
        String title = "WebPage Title WebPage Title" ;
        String description = "WebPage Description";
        Bitmap bitmap =BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        EmaSDK.getInstance().doShareWebPage(MainActivity.this, new ShareEmaSDKListener(), url, title, description, bitmap);
    }

    private void shareText() {
        String text = "emashare Text";
        EmaSDK.getInstance().doShareText(MainActivity.this, new ShareEmaSDKListener(), text);
    }

    private void shareImage() {


        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),/* R.drawable.ema111 */ R.drawable.ic_launcher);
        EmaSDK.getInstance().doShareImage(MainActivity.this, new ShareEmaSDKListener(), bitmap);
    }

    class ShareEmaSDKListener implements EmaSDKListener {

        @Override
        public void onCallBack(int resultCode, String decr) {
            switch (resultCode) {
                case EmaCallBackConst.EMA_SHARE_OK:
                    ToastHelper.toast(MainActivity.this, decr);
                    //分享成功
                    break;
                case EmaCallBackConst.EMA_SHARE_CANCLE:
                    ToastHelper.toast(MainActivity.this, decr);
                    //分享取消
                    break;
                case EmaCallBackConst.EMA_SHARE_FAIL:
                    //分享拒绝
                    ToastHelper.toast(MainActivity.this, decr);
                    break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EmaSDK.getInstance().onActivityResult(requestCode, resultCode, data);

    }


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
    protected void onRestart() {
        super.onRestart();
        EmaSDK.getInstance().onRestart();
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
