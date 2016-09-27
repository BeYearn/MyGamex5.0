package com.emagroup.sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RechargeMabiActivity extends Activity implements OnClickListener {

	private static final String TAG = "PayRechargeMabiActivity";
	
	public static final int CODE_RECHARGE_TENPAY_URL = 10;//获取财付通url成功
	public static final int CODE_RECHARGE_TENPAY_URL_FAILED = 11;//获取财付通url失败
	public static final int INTENT_REQUEST_CODE_GAME_CARD = 100;
	public static final int INTENT_REQUEST_CODE_PHONE_CARD = 101;
	
	private ConfigManager mConfigManager;
	private DeviceInfoManager mDeviceInfoManager;
	private ResourceManager mResourceManager;
	private EmaPay mEmaPay;
	
	//views
	private Button mBtnBack;
	private ImageView mBtnReturnGame;
	private TextView mTxtAccount;
	private TextView mTxtBalance;
	private EditText mEdtRecharge;
	private Button mBtnRecharge10;
	private Button mBtnRecharge50;
	private Button mBtnRecharge100;
	private Button mBtnRecharge200;
	private Button mBtnRecharge500;
	private Button mBtnRecharge1000;
	private GridView mGridView;
	
	private PayTrdListAdapter mAdapter;
	
	// 进度条
	private EmaProgressDialog mProgress;
	
	private Map<String, Integer> mIDmap;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case PayConst.CODE_PAY_GET_TRD_PAY_LIST://获取到支付列表
				if(msg.obj instanceof List){
					mAdapter = new PayTrdListAdapter(RechargeMabiActivity.this, (List<PayTrdItemBean>) msg.obj);
					mGridView.setAdapter(mAdapter);
					//计算实际情况下GridView的高度
					if(mAdapter.getCount() > 4){
						View view = mAdapter.getView(1, null, mGridView);
						view.measure(0, 10);
						int cloumn = mAdapter.getCount() / 4;
						if(mAdapter.getCount() % 4 != 0){
							cloumn++;
						}
						ViewGroup.LayoutParams params = mGridView.getLayoutParams();
						params.height = (view.getMeasuredHeight() + 30) * cloumn;
						mGridView.setLayoutParams(params);
					}
				}
				break;
			case CODE_RECHARGE_TENPAY_URL://获取到财付通充值url
				doRecharge((String)msg.obj);
				break;
			case CODE_RECHARGE_TENPAY_URL_FAILED://获取财付通充值url失败
				ToastHelper.toast(RechargeMabiActivity.this, "跳转失败");
				break;
			case PayConst.CODE_PAY_ALI_RESULT://支付宝充值成功
				if(msg.obj != null && msg.obj instanceof String){
					doResultAlipay((String)msg.obj);
				}
				break;
			case PayConst.CODE_PAY_0YUANFU_RESULT://0元付充值结果
				if(msg.obj != null && msg.obj instanceof String){
					doResult0YuanFu((String)msg.obj);
				}
				break;
			case EmaProgressDialog.CODE_LOADING_START:
				mProgress.showProgress((String)msg.obj);
				break;
			case EmaProgressDialog.CODE_LOADING_END:
				mProgress.closeProgress();
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// 将activity添加到列表，方便管理
		EmaPayProcessManager.getInstance().addRechargeActivity(this);
		
		mConfigManager = ConfigManager.getInstance(this);
		mDeviceInfoManager = DeviceInfoManager.getInstance(this);
		mResourceManager = ResourceManager.getInstance(this);
		mEmaPay = EmaPay.getInstance(this);
		mProgress = new EmaProgressDialog(this);
		
		initView();
		initData();
		reFreshUserInfo();
	}

	/**
	 * 刷新一次用户信息，主要是为了刷新余额
	 */
	private void reFreshUserInfo() {
			Map<String, String> params = new HashMap<>();
			params.put("token",EmaUser.getInstance().getmToken());
			new HttpInvoker().postAsync(Url.getUserInfoUrl(), params,
					new HttpInvoker.OnResponsetListener() {
						@Override
						public void OnResponse(String result) {
							try {
								JSONObject jsonObject = new JSONObject(result);
								String message= jsonObject.getString("message");
								String status= jsonObject.getString("status");

								JSONObject productData = jsonObject.getJSONObject("data");
								String email = productData.getString("email");
								boolean ifSetChargePwd = productData.getString("ifSetChargePwd").equals("1");
								String mobile = productData.getString("mobile");
								String nickname = productData.getString("nickname");
								String pfCoin = productData.getString("pfCoin");
								String uid = productData.getString("uid");

								EmaUser.getInstance().setEmail(email);
								EmaUser.getInstance().setIsWalletHasPassw(ifSetChargePwd);
								EmaUser.getInstance().setMobile(mobile);
								EmaUser.getInstance().setNickName(nickname);
								EmaUser.getInstance().setBalance(pfCoin);
								EmaUser.getInstance().setmUid(uid);

								LOG.e("getUserInfo",message+ifSetChargePwd+nickname+pfCoin+uid);

								initData();  // 刷新一次余额

							} catch (Exception e) {
								LOG.w(TAG, "login error", e);
							}
						}
					});
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		setContentView(mResourceManager.getIdentifier("ema_recharge_mabi", "layout"));
		mBtnBack = (Button) findViewById(getId("ema_recharge_mabi_back"));
		mBtnReturnGame = (ImageView) findViewById(getId("ema_recharge_mabi_imageView_return"));
		mTxtAccount = (TextView) findViewById(getId("ema_recharge_mabi_account_text"));
		mTxtBalance = (TextView) findViewById(getId("ema_recharge_mabi_balance_text"));
		mEdtRecharge = (EditText) findViewById(getId("ema_recharge_EditText"));
		mBtnRecharge10 = (Button) findViewById(getId("ema_recharge_mabi_btn_10"));
		mBtnRecharge50 = (Button) findViewById(getId("ema_recharge_mabi_btn_50"));
		mBtnRecharge100 = (Button) findViewById(getId("ema_recharge_mabi_btn_100"));
		mBtnRecharge200 = (Button) findViewById(getId("ema_recharge_mabi_btn_200"));
		mBtnRecharge500 = (Button) findViewById(getId("ema_recharge_mabi_btn_500"));
		mBtnRecharge1000 = (Button) findViewById(getId("ema_recharge_mabi_btn_1000"));
		mGridView = (GridView) findViewById(getId("ema_recharge_mabi_PayList"));
		
		mBtnBack.setOnClickListener(this);
		mBtnReturnGame.setOnClickListener(this);
		mBtnRecharge10.setOnClickListener(this);
		mBtnRecharge50.setOnClickListener(this);
		mBtnRecharge100.setOnClickListener(this);
		mBtnRecharge200.setOnClickListener(this);
		mBtnRecharge500.setOnClickListener(this);
		mBtnRecharge1000.setOnClickListener(this);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				String rechargeMoney = mEdtRecharge.getText().toString();
				if(UCommUtil.isStrEmpty(rechargeMoney)){
					LOG.d(TAG, "输入金额为空");
					ToastHelper.toast(RechargeMabiActivity.this, "金额不能为空");
					return;
				}

				EmaPriceBean money = new EmaPriceBean(Float.valueOf(rechargeMoney), EmaPriceBean.TYPE_YUAN);  //TODO 这个玩意先放在这里，避免把它干掉到处爆红，应该用下面这个

				EmaPayInfo emaPayInfo = new EmaPayInfo();
				emaPayInfo.setProductName("充值");
				emaPayInfo.setDescription("钱包充值");
				emaPayInfo.setOrderId("xxxxxxxx");  //服务器加签后会补上
				emaPayInfo.setPrice(Integer.valueOf(rechargeMoney));

				PayTrdItemBean bean = (PayTrdItemBean) mAdapter.getItem(position);
				String key = bean.getChannelCode();
				if(key.equals(PayConst.PAY_TRD_TENPAY)){//财付通充值
					
					//mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_START);
					//PayUtil.GoRechargeByTenpay(RechargeMabiActivity.this, money);
					ToastHelper.toast(RechargeMabiActivity.this,"暂不支持");
				}else if(key.equals(PayConst.PAY_TRD_GAMECARD)){//游戏卡充值
					ToastHelper.toast(RechargeMabiActivity.this,"暂不支持");
					//PayUtil.GoRechargeByGamecard(RechargeMabiActivity.this, money);
					
				}else if(key.equals(PayConst.PAY_TRD_PHONE_CARD)){//手机充值
					ToastHelper.toast(RechargeMabiActivity.this,"暂不支持");
					//PayUtil.GoRechargeByPhonecard(RechargeMabiActivity.this, money);
					
				}else if(key.equals(PayConst.PAY_TRD_ALIPAY)){//支付宝充值
					
					PayUtil.GoRecharegeByAlipay(RechargeMabiActivity.this, emaPayInfo, mHandler);
					
				}else if(key.equals(PayConst.PAY_TRD_WEIXIN)){//微信充值
					ToastHelper.toast(RechargeMabiActivity.this,"暂不支持");
					//PayUtil.GoRechargeByWeixin(RechargeMabiActivity.this, money, mHandler);
					
				}else if(key.equals(PayConst.PAY_TRD_0YUANFU)){//0元付充值
					ToastHelper.toast(RechargeMabiActivity.this,"暂不支持");
					//PayUtil.GoRechargeBy0YuanFu(RechargeMabiActivity.this, money, mHandler);
					
				}
			}
		});
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		mTxtAccount.setText(EmaUser.getInstance().getNickName());
		mTxtBalance.setText(EmaUser.getInstance().getBalance());
		PayUtil.getRechargeList(this, mHandler);
	}

	/**
	 * 发送回调消息
	 * @param msg
	 */
	public void onRechargeCallBack(Message msg){
		mHandler.sendMessage(msg);
	}
	
	/**
	 * Click监听事件
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == getId("ema_recharge_mabi_back")){//返回
			this.finish();
		}else if(id == getId("ema_recharge_mabi_imageView_return")){//返回游戏
			new EmaDialogPayPromptCancel(this).show();
		}else if(id == getId("ema_recharge_mabi_btn_10")){//充值，下面同此
			doChangeView(10);
		}else if(id == getId("ema_recharge_mabi_btn_50")){
			doChangeView(50);
		}else if(id == getId("ema_recharge_mabi_btn_100")){
			doChangeView(100);
		}else if(id == getId("ema_recharge_mabi_btn_200")){
			doChangeView(200);
		}else if(id == getId("ema_recharge_mabi_btn_500")){
			doChangeView(500);
		}else if(id == getId("ema_recharge_mabi_btn_1000")){
			doChangeView(1000);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case INTENT_REQUEST_CODE_GAME_CARD://游戏卡充值返回
			if(resultCode == RESULT_OK){//游戏卡充值成功
				setResult(RESULT_OK);
				this.finish();
			}
			break;
		case INTENT_REQUEST_CODE_PHONE_CARD://手机卡充值返回
			if(resultCode == RESULT_OK){
				setResult(RESULT_OK);
				this.finish();
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 0元付支付结果
	 * @param result
	 */
	private void doResult0YuanFu(String result){
		sendBroadcast(new Intent(PropertyField.BROADCAST_RECHARGE_SUCC));
		try {
			JSONObject json = new JSONObject(result);
			int resultCode = json.getInt("retcode");
			switch(resultCode){
			case HttpInvokerConst.SDK_RESULT_SUCCESS://获取订单号成功
				LOG.d(TAG, "查询订单成功");
//				UCommUtil.makePayCallBack(EmaCallBackConst.PAYSUCCESS, "查询订单成功，支付成功");
				showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_SUCC, "");
				break;
			case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR://签名验证失败
				LOG.d(TAG, "签名验证失败，查询订单失败");
//				UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "签名验证失败，查询订单失败");
				showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_FAILED, "签名验证失败");
				break;
			default:
				LOG.d(TAG, "查询订单失败，原因未知");
//				UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "查询订单失败，原因未知");
				showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_FAILED, "");
				break;
			}
		} catch (Exception e) {
			LOG.w(TAG, "pay failed", e);
//			UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "查询订单失败，原因未知");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_FAILED, "");
		}
	}
	
	/**
	 * 支付宝充值结果
	 */
	private void doResultAlipay(String result){
		TrdAliPayResult data = new TrdAliPayResult(result);
		sendBroadcast(new Intent(PropertyField.BROADCAST_RECHARGE_SUCC));
		if(data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_SUCC)){//支付成功
		
			UCommUtil.makePayCallBack(EmaCallBackConst.PAYSUCCESS, "支付成功");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_SUCC, "充值成功");
		
		}else if(data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_ON_PAYING)){//正在处理中
		
			UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "正在处理中");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_OTHERS, "正在处理中");
		
		}else if(data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_PAY_CANCEL)){//用户中途取消
		
			UCommUtil.makePayCallBack(EmaCallBackConst.PAYCANELI, "用户中途取消");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_CANCEL, "用户退出充值");
		
		}else if(data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_FAILED)){//订单支付失败
		
			UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "订单支付失败");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_FAILED, "");

		}else if(data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_NETWORK_ERROR)){//网络连接出错
			
			UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "网络连接出错");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_FAILED, "请检查网络连接是否正常");
		
		}
	}
	
	/**
	 * 显示支付结束后的对话框
	 */
	private void showPayResultDialog(int actionType, int resultType, String promptInfo){
		new EmaDialogPayPromptResult(this, actionType, resultType, promptInfo).show();
	}
	
	/**
	 * 开始调用webView界面进行充值
	 */
	public void doRecharge(String url){
		Intent intent = new Intent(this, WebViewActivity.class);
		intent.putExtra(WebViewActivity.INTENT_TITLE, "财付通");
		intent.putExtra(WebViewActivity.INTENT_URL, url);
		intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_TENPAY);
		intent.putExtra(WebViewActivity.INTENT_INFORGAME, false);
		startActivityForResult(intent, 0);
	}

	/**
	 * 改变按钮样式
	 * @param money
	 */
	private void doChangeView(int money){
		int colorNormal = Color.rgb(0, 0, 0);
		int colorSelect = Color.rgb(255, 113, 17);
		mBtnRecharge10.setTextColor(colorNormal);
		mBtnRecharge50.setTextColor(colorNormal);
		mBtnRecharge100.setTextColor(colorNormal);
		mBtnRecharge200.setTextColor(colorNormal);
		mBtnRecharge500.setTextColor(colorNormal);
		mBtnRecharge1000.setTextColor(colorNormal);
		mEdtRecharge.setText(money + "");
		mEdtRecharge.setSelection(mEdtRecharge.getText().toString().length());
		switch(money){
		case 10:
			mBtnRecharge10.setTextColor(colorSelect);
			break;
		case 50:
			mBtnRecharge50.setTextColor(colorSelect);
			break;
		case 100:
			mBtnRecharge100.setTextColor(colorSelect);
			break;
		case 200:
			mBtnRecharge200.setTextColor(colorSelect);
			break;
		case 500:
			mBtnRecharge500.setTextColor(colorSelect);
			break;
		case 1000:
			mBtnRecharge1000.setTextColor(colorSelect);
			break;
		}
	}
	
	/**
	 * 方法目的（为了防止重复去获取资源ID） 
	 * @param key
	 * @return
	 */
	private int getId(String key){
		if(mIDmap == null){
			mIDmap = new HashMap<String, Integer>();
		}
		if(!mIDmap.containsKey(key)){
			mIDmap.put(key, mResourceManager.getIdentifier(key, "id"));
		}
		return mIDmap.get(key);
	}

	@Override
	protected void onStop() {
		ToolBar.getInstance(Ema.getInstance().getContext()).showToolBar();
		super.onStop();
	}
}
