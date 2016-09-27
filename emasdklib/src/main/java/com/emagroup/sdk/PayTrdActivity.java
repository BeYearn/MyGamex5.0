package com.emagroup.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

public class PayTrdActivity extends Activity implements OnClickListener {

	private static final String TAG = "PayTrdActivity";
	
	private static final int REQUEST_CODE_PAY_MABI = 10;
	private static final int REQUEST_CODE_PAY_TENPAY = 11;
	private static final int REQUEST_CODE_PAY_GAMECARD = 12;
	private static final int REQUEST_CODE_PAY_PHONECARD = 13;

	public static final int CODE_PAY_TENTPAY_URL = 50;//获取财付通支付的url
	public static final int CODE_PAY_TENPAY_URL_FAILED = 51;//获取财付通支付的url失败

	private EmaUser mEmaUser;
	private ConfigManager mConfigManager;
	private DeviceInfoManager mDeviceInfoManager;
	private ResourceManager mResourceManager;

	//views
	private ImageView mBtnBackToGameView;
	private TextView mTxtTotalPrice;
	private TextView mTxtUserName;
	private TextView mTxtProductName;
	private GridView mGridView;

	private PayTrdListAdapter mAdapter;

	private EmaPayInfo mPayInfo;

	//保存按钮的资源ID
	private int mIDBtnBackToGameView;

	// 进度条
	private EmaProgressDialog mProgress;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case PayConst.CODE_PAY_GET_TRD_PAY_LIST://获取第三方支付渠道完成
				if(msg.obj instanceof List){
					mAdapter = new PayTrdListAdapter(PayTrdActivity.this, (List<PayTrdItemBean>) msg.obj);
					mGridView.setAdapter(mAdapter);
				}
				break;
			case CODE_PAY_TENTPAY_URL://获取到财付通支付的url成功
				mProgress.closeProgress();
				doPayByTenpay((String) msg.obj);
				break;
			case CODE_PAY_TENPAY_URL_FAILED://获取财付通支付的url失败
				ToastHelper.toast(PayTrdActivity.this, "跳转失败");
				break;
			case PayConst.CODE_PAY_ALI_RESULT://支付宝支付结果（此处也就是充值结果）
				if(msg.obj != null && msg.obj instanceof String){
					doResultAlipay((String)msg.obj);
				}
				break;
			case PayConst.CODE_PAY_0YUANFU_RESULT://0元付支付结果
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
		//将activity添加到列表，方便管理
		EmaPayProcessManager.getInstance().addPayActivity(this);
		EmaPayProcessManager.getInstance().clearWeixinOrderInfo();
		
		mEmaUser = EmaUser.getInstance();
		mConfigManager = ConfigManager.getInstance(this);
		mDeviceInfoManager = DeviceInfoManager.getInstance(this);
		mResourceManager = ResourceManager.getInstance(this);
		mProgress = new EmaProgressDialog(this);
		mIDBtnBackToGameView = 0;

		//获得订单信息
		Intent intent = this.getIntent();
		mPayInfo = intent.getParcelableExtra("payInfo");
		
		initView();
		
		initData();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		mTxtProductName.setText(mPayInfo.getProductName());
		mTxtTotalPrice.setText(mPayInfo.getPrice()+"");
		mTxtUserName.setText(mEmaUser.getNickName());
		
		PayUtil.getPayTrdList(this, mHandler);
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		setContentView(mResourceManager.getIdentifier("ema_pay_thrdpay", "layout"));
		mBtnBackToGameView = (ImageView) findViewById(getID_BtnBackToGame());
		mTxtTotalPrice = (TextView) findViewById(mResourceManager.getIdentifier("ema_pay_thrd_total_price_textview", "id"));
		mTxtUserName = (TextView) findViewById(mResourceManager.getIdentifier("ema_pay_thrd_username_textview", "id"));
		mTxtProductName = (TextView) findViewById(mResourceManager.getIdentifier("ema_pay_thrd_goodsname_textview", "id"));
		mGridView = (GridView) findViewById(mResourceManager.getIdentifier("ema_pay_thrd_PayList", "id"));
		
		mBtnBackToGameView.setOnClickListener(this);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				PayTrdItemBean bean = (PayTrdItemBean) mAdapter.getItem(position);
				String key = bean.getChannelCode();
				if(key.equals(PayConst.PAY_TRD_QIANBAO)){//钱包支付
					
					//PayUtil.GoPayByMabi(PayTrdActivity.this, REQUEST_CODE_PAY_MABI);
					ToastHelper.toast(PayTrdActivity.this,"暂不支持");
				}else if(key.equals(PayConst.PAY_TRD_TENPAY)){//财付通支付
					
					//mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_START);
					//PayUtil.GoPayByTenpay(PayTrdActivity.this);
					ToastHelper.toast(PayTrdActivity.this,"暂不支持");
				}else if(key.equals(PayConst.PAY_TRD_GAMECARD)){//游戏卡支付
					
					//PayUtil.GoPayByGameCardpay(PayTrdActivity.this, REQUEST_CODE_PAY_GAMECARD);
					ToastHelper.toast(PayTrdActivity.this,"暂不支持");
				}else if(key.equals(PayConst.PAY_TRD_PHONE_CARD)){//手机卡支付
					
					//PayUtil.GoPayByPhoneCardpay(PayTrdActivity.this, REQUEST_CODE_PAY_PHONECARD);
					ToastHelper.toast(PayTrdActivity.this,"暂不支持");
				}else if(key.equals(PayConst.PAY_TRD_ALIPAY)){//支付宝支付
					
					//PayUtil.GoPayByAlipay(PayTrdActivity.this, mHandler);   现在如下：钱不够先走充值完了再用余额支付（用户感知为直接用钱买）
					//把上面得到的那个mPayInfo拿好，充值ok后还得用它钱包支付。
					EmaPayInfo rechargePayInfo = new EmaPayInfo();
					rechargePayInfo.setOrderId("xxxxxxxx"); //服务器加签后会补上
					rechargePayInfo.setProductName(mPayInfo.getProductName());
					rechargePayInfo.setPrice(mPayInfo.getPrice());
					rechargePayInfo.setDescription(mPayInfo.getDescription());
					PayUtil.GoRecharegeByAlipay(PayTrdActivity.this, rechargePayInfo, mHandler);
					
				}else if(key.equals(PayConst.PAY_TRD_WEIXIN)){//微信支付
					
					//mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_START);
					//PayUtil.GoPayByWeixin(PayTrdActivity.this, mHandler);
					ToastHelper.toast(PayTrdActivity.this,"暂不支持");
				}else if(key.equals(PayConst.PAY_TRD_0YUANFU)){//0元付
					ToastHelper.toast(PayTrdActivity.this,"暂不支持");
					//PayUtil.GoPayBy0yuanfu(PayTrdActivity.this, bean,  mHandler);
					
				}
			}
		});
	}
	
	/**
	 * Click监听事件
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == getID_BtnBackToGame()){//返回游戏
			showCancelPromptDialog();
		}
	}
	
	/**
	 * 0元付支付结果处理
	 * @param result
	 */
	private void doResult0YuanFu(String result){
		try {
			JSONObject json = new JSONObject(result);
			int resultCode = json.getInt("retcode");
			switch(resultCode){
			case HttpInvokerConst.SDK_RESULT_SUCCESS://获取订单号成功
				LOG.d(TAG, "查询订单成功");
				UCommUtil.makePayCallBack(EmaCallBackConst.PAYSUCCESS, "查询订单成功，支付成功");
				showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_SUCC, "");
				break;
			case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR://签名验证失败
				LOG.d(TAG, "签名验证失败，查询订单失败");
				UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "签名验证失败，查询订单失败");
				showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "签名验证失败");
				break;
			default:
				LOG.d(TAG, "查询订单失败，原因未知");
				UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "查询订单失败，原因未知");
				showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "");
				break;
			}
		} catch (Exception e) {
			LOG.w(TAG, "pay failed", e);
			UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "查询订单失败，原因未知");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "");
		}
	}
	
	/**
	 * 支付宝充值结果
	 */
	private void doResultAlipay(String result){
		TrdAliPayResult data = new TrdAliPayResult(result);
		if(data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_SUCC)){//支付 充值成功
		
			//UCommUtil.makePayCallBack(EmaCallBackConst.PAYSUCCESS, "支付成功");
			//showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_SUCC, "");

			//充值成功，此时再重新走一边支付
			LOG.e("paythirdActivity","充值完毕再走一次钱包支付");
			mPayInfo.setReChargePay(true);
			EmaPay.getInstance(Ema.getInstance().getContext()).pay(mPayInfo,EmaPay.getInstance(Ema.getInstance().getContext()).mPayListener);
			PayTrdActivity.this.finish();
		
		}else if(data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_ON_PAYING)){//正在处理中
		
			UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "正在处理中");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_OTHERS, "正在处理中");
		
		}else if(data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_PAY_CANCEL)){//用户中途取消
		
			UCommUtil.makePayCallBack(EmaCallBackConst.PAYCANELI, "用户中途取消");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_CANCEL, "");
		
		}else if(data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_FAILED)){//订单支付失败
		
			UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "订单支付失败");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "");

		}else if(data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_NETWORK_ERROR)){//网络连接出错
			
			UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "网络连接出错");
			showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "请检查网络连接是否正常");
		
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
	private void doPayByTenpay(String url){
		Intent intent = new Intent(this, WebViewActivity.class);
		intent.putExtra(WebViewActivity.INTENT_TITLE, "财付通");
		intent.putExtra(WebViewActivity.INTENT_URL, url);
		intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_TENPAY);
		intent.putExtra(WebViewActivity.INTENT_INFORGAME, true);
		startActivityForResult(intent, REQUEST_CODE_PAY_TENPAY);
	}
	
	/**
	 * 获取支付url后的回调
	 */
	public void onPayCallBack(Message msg){
		mHandler.sendMessage(msg);
	}
	
	
	/**
	 * 监听返回按钮
	 */
	@Override
	public void onBackPressed() {
		showCancelPromptDialog();
	}

	/**
	 * 显示对话框提示用户是否确认退出支付
	 */
	private void showCancelPromptDialog(){
		new EmaDialogPayPromptCancel(this).show();
	}
	
	/**
	 * 获取返回游戏按钮的资源ID
	 * @return
	 */
	private int getID_BtnBackToGame(){
		if(mIDBtnBackToGameView == 0){
			mIDBtnBackToGameView = mResourceManager.getIdentifier("ema_pay_thrd_return_game", "id");
		}
		return mIDBtnBackToGameView;
	}
	
}
