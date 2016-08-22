package com.example.martintest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.igexin.sdk.PushManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import cn.emagroup.sdk.Ema;
import cn.emagroup.sdk.comm.EmaCallBackConst;
import cn.emagroup.sdk.comm.EmaSDKListener;

//测试界面activity

@SuppressLint({ "ShowToast", "HandlerLeak" })
public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = "MARTIN";
	private EditText mEdtShowTxt;

	public Handler mhandler = new Handler() {
		public void handleMessage(Message message) {
//			Toast.makeText(MainActivity.this, (CharSequence) message.obj,
//					Toast.LENGTH_SHORT).show();
		}
	};

	String requestURL = "";
	private DemoPayDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏

		setContentView(R.layout.activity_main);
		findViews();
		checkphoneTime();
		init();
	}

	private void init() {
		// 初始化
		Ema.getInstance().init(this, new EmaSDKListener() {

			@Override
			public void onCallBack(int resultCode,String dec) {
				if(mhandler != null){
					//mhandler.sendMessage(msg);
				}
				switch (resultCode) {
					case EmaCallBackConst.INITSUCCESS://初始化SDK成功回调
						Toast.makeText(MainActivity.this, "sdk初始化成功", Toast.LENGTH_LONG).show();
						break;
					case EmaCallBackConst.INITFALIED://初始化SDK失败回调
						break;
					case EmaCallBackConst.LOGINSUCCESS://登陆成功回调
						break;
					case EmaCallBackConst.LOGINCANELL://登陆取消回调
						break;
					case EmaCallBackConst.LOGINFALIED://登陆失败回调
						Log.e("++++++++++", Thread.currentThread().getName());
						break;
					case EmaCallBackConst.LOGOUTSUCCESS://登出成功回调
						break;
					case EmaCallBackConst.LOGOUTFALIED://登出失败回调
						break;
				}
			}
		});

		PushManager.getInstance().initialize(this.getApplicationContext());
	}

	private void findViews() {
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_logout).setOnClickListener(this);
		findViewById(R.id.btn_pay_mabi).setOnClickListener(this);
		findViewById(R.id.btn_show_toolbar).setOnClickListener(this);
		findViewById(R.id.btn_others).setOnClickListener(this);
		findViewById(R.id.btn_hide_toolbar).setOnClickListener(this);

		findViewById(R.id.btn_show_sign).setOnClickListener(this);
		findViewById(R.id.btn_show_token).setOnClickListener(this);
		mEdtShowTxt = (EditText) findViewById(R.id.edt_showtxt);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_login) {// 登录
			doLogin();
		} else if (id == R.id.btn_logout) {// 登出
			doLogout();
		} else if (id == R.id.btn_pay_mabi) {// 支付
			doPay();
		} else if (id == R.id.btn_show_toolbar) {// 显示悬浮窗
			doShowToolbar();
		} else if (id == R.id.btn_hide_toolbar) {// 隐藏悬浮窗
			doHideToobar();
		} else if (id == R.id.btn_others) {// 开发测试按钮
			doTest();
		}
	}


	/**
	 * 登录
	 */
	private void doLogin() {
		Ema.getInstance().Login();
	}

	/**
	 * 登出
	 */
	private void doLogout() {
		 Ema.getInstance().Logout();
	}

	/**
	 * 支付
	 */
	private void doPay() {
		Ema.getInstance().hideToolBar();
		mDialog = new DemoPayDialog(this);
		mDialog.show();
		mDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				Ema.getInstance().showToolBar();
			}
		});
	}

	/**
	 * 显示悬浮窗
	 */
	private void doShowToolbar() {
		Ema.getInstance().showToolBar();
	}

	private void doHideToobar() {
		Ema.getInstance().hideToolBar();
	}

	/**
	 * 开发测试
	 */
	private void doTest() {
		// Intent intent = new Intent(MainActivity.this,EmaTenPay.class);
		// startActivity(intent);
	}

	// 登录验证
	// protected void GetServetAccessToken() {
	// new Thread(new Runnable() {
	// @SuppressLint("ShowToast")
	// @Override
	// public void run() {
	// String oauthURL = Ema.getInstance().GetServerUrl()
	// + "/oauth/token";
	// Map<String, String> parameter = new HashMap<String, String>();
	// parameter.clear();
	// parameter.put("app_id", Ema.getInstance().GetAppId());
	// parameter.put("code", Ema.getInstance().GetAccessCode());
	// parameter.put("grant_type", "authorization_code");
	//
	// long stamp = (int) (System.currentTimeMillis() / 1000);
	// stamp = stamp - stamp % 600;
	//
	// String sign = Ema.getInstance().GetAppId()
	// + Ema.getInstance().GetAccessCode()
	// + "authorization_code"
	// +
	// "http://client.emagroup.cn/index.php/test/oauth/request_receive_authcode"
	// + String.valueOf(stamp)
	// + "13a663914935c4f1569246ac1aaf3bb9";
	//
	// String mString = Tool.MD5(sign);
	// Ema.getInstance().EmaDebug("MainActivity",
	// "MD5 value= " + mString);
	//
	// parameter.put("sign", mString);
	//
	// String url_token = null;
	// try {
	// url_token = HttpInvoker.doHttpsPost(oauthURL, parameter);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// Ema.getInstance().EmaDebug("MainActivity",
	// "返回AccessToken＝" + url_token);//
	// {"access_token":"b0ac7b8aab8ee9ff27840b5a2506844ae06c0db5","expires_in":604800,"token_type":"bearer","scope":null,"refresh_token":"1a51679273cda27084425f046d1813cdd7d6e418"}
	//
	// try {
	// //
	// {"errno":0,"errmsg":"success","access_token":"280ec80574032153ab2a26ff26d3591caee59eda","expires_in":604800,"token_type":"bearer","scope":null,"refresh_token":"549cad58faa7fe2f6c68da0bdfa452016cb309be","sid":"2d3ffd4ff4fe49eea32366a2f2be6fc9"}
	// JSONObject result_token = new JSONObject(url_token);
	// Ema.getInstance().SetAccessToken(result_token.getString("access_token"));
	// Message mMessage = new Message();
	// mMessage.obj = "result_token == " + result_token.toString();
	// mMessage.what = 0;
	// mhandler.sendMessage(mMessage);
	//
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// Ema.getInstance().EmaDebug("MainActivity", "验证失败");
	// e.printStackTrace();
	// }
	// }
	// }).start();
	// }

	@Override
	protected void onStart() {
		super.onStart();
		Ema.getInstance().onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Ema.getInstance().onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Ema.getInstance().onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Ema.getInstance().onStop();
	}

	public void onDestroy() {
		super.onDestroy();
		Ema.getInstance().onDestroy();
	}

	/**
	 * 判断手机时间
	 * **/
	public static void checkphoneTime() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {
					URL url = new URL("http://www.bjtime.cn");
					// 取得资源对象
					URLConnection uc = url.openConnection(); // 生成连接对象
					uc.connect(); // 发出连接
					long ld = uc.getDate(); // 取得网站日期时间
					Log.d(TAG, "nettimer__ld :" + ld);
					long Servertimer = ld / 1000;
					Servertimer = Servertimer - Servertimer % 3600;
					Log.d(TAG, "nettimer : "+Servertimer);

					java.util.Date date = new Date();
					long timer = date.getTime();
					Log.d(TAG, "timmer ld:" + timer);
					Log.d(TAG, "current mills :" + System.currentTimeMillis());
//					Timestamp ts = new Timestamp(System.currentTimeMillis());
//					Log.d(TAG, "timer __ ld : " + ts.getTime());
					timer = timer / 1000;
					timer = timer - timer % 3600;

					Log.d(TAG, "timer:"+timer);

					if(Servertimer != timer)
					{
						Log.d(TAG, "请校准手机时间");
//						ToastHelper.toast(EmaSDK.getInstance().mActivity, "请校准手机时间!");
					}

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}}).start();
	}

}
