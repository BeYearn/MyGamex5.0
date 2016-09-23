package com.emagroup.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *	游戏卡，手机卡  支付 | 充值
 * @author zhangyang
 *
 */
public class TrdCardActivity extends Activity implements OnClickListener {

	private static final String TAG = TrdCardActivity.class.toString();
	
	public static final int CODE_PAY_RECHARGE_RESULT = 100;//支付 或者 充值结果
	
	private EmaUser mEmaUser;
	private ConfigManager mConfigManager;
	private DeviceInfoManager mDeviceInfoManager;
	private ResourceManager mResourceManager;
	private EmaPay mEmaPay;
	
	public static final String INTENT_AMOUNT = "intent_amount";//操作金额
	public static final String INTENT_TYPE = "intent_type";//操作类型 支付 | 充值
	public static final int TYPE_GAMECARD_PAY = 1;//游戏卡支付操作
	public static final int TYPE_GAMECARD_RECHARGE = 2;//游戏卡充值操作
	public static final int TYPE_PHONECARD_PAY = 3;// 手机卡 支付操作
	public static final int TYPE_PHONECARD_RECHARGE = 4;//手机卡 充值操作
	
	//views
	private ImageView mBtnReturnGame;// 返回游戏按钮
	private TextView mTitleView;//标题
	private ListView mListView;//游戏卡的种类列表
	private MyGridView mGridView;//游戏卡对应的金额列表
	private TextView mTxtPrice;// 支付|充值 的金额（传递过来的）
	private EditText mEdtCardNumber;// 充值卡卡号
	private EditText mEdtCardPassw;// 充值卡密码
	private Button mBtnPay;// 下一步按钮
	
	private EmaProgressDialog mProgress;//进度条
	
	private Map<String, Integer> mIDmap;
	
	private List<PayTrdItemBean> mListSelectType;//游戏卡类型列表
	private List<PayTrdItemBean> mListSelectAmount;//充值金额列表
	private PayTrdItemBean mSelectTempBean;
	private PayTrdItemBean mSelectAmountBean;
	
	private MyListAdapter mListAdapter;
	private MyGridAdapter mGridAdapter;
	
	private EmaPriceBean mAmount;//传递过来的 充值|支付 金额
	private int mType;//标记操作类型
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case CODE_PAY_RECHARGE_RESULT:
				break;
			case EmaProgressDialog.CODE_LOADING_START:
				if(mProgress == null){
					mProgress = new EmaProgressDialog(TrdCardActivity.this);
				}
				mProgress.showProgress("支付中...", false, false);
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
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
		
		mEmaUser = EmaUser.getInstance();
		mConfigManager = ConfigManager.getInstance(this);
		mDeviceInfoManager = DeviceInfoManager.getInstance(this);
		mResourceManager = ResourceManager.getInstance(this);
		mEmaPay = EmaPay.getInstance(this);
		
		initView();
		initData();
	}

	private void initView() {
		setContentView(mResourceManager.getIdentifier("ema_pay_gcard_new", "layout"));
		mBtnReturnGame = (ImageView) findViewById(getId("ema_title_right_img"));
		mBtnReturnGame.setVisibility(View.VISIBLE);
		mTitleView = (TextView) findViewById(getId("ema_txt_title"));
		mTxtPrice = (TextView) findViewById(getId("ema_pay_gcard_need"));
		mListView = (ListView) findViewById(getId("ema_list_gcard"));
		mGridView = (MyGridView) findViewById(getId("ema_gridview_gcard"));
		mEdtCardNumber = (EditText) findViewById(getId("ema_pay_gamecard_card_number"));
		mEdtCardPassw = (EditText) findViewById(getId("ema_pay_gamecard_card_passw"));
		mBtnPay = (Button) findViewById(getId("ema_pay_gamecard_btn_pay"));

		mBtnReturnGame.setOnClickListener(this);
		mBtnPay.setOnClickListener(this);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				PayTrdItemBean bean = (PayTrdItemBean) mListAdapter.getItem(position);
				if(mSelectTempBean != null && mSelectTempBean != bean){
					mSelectTempBean.setSelect(false);
				}
				bean.setSelect(true);
				mSelectTempBean = bean;
				// 修改右边的面额列表
				mListSelectAmount = PayUtil.getTrdSelectCardAmount(mSelectTempBean, mAmount);
				if(mListSelectAmount != null && mListSelectAmount.size() > 0){
					mSelectAmountBean = mListSelectAmount.get(0);
					mSelectAmountBean.setSelect(true);
				}
				mGridAdapter.setData(mListSelectAmount);
				mGridAdapter.notifyDataSetChanged();
				mListAdapter.notifyDataSetChanged();
			}
		});
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				PayTrdItemBean bean = (PayTrdItemBean) mGridAdapter.getItem(position);
				if(mSelectAmountBean != null && mSelectAmountBean != bean){
					mSelectAmountBean.setSelect(false);
				}
				bean.setSelect(true);
				mSelectAmountBean = bean;
				mGridAdapter.notifyDataSetChanged();
			}
		});
	}

	private void initData() {
		mAmount = (EmaPriceBean) getIntent().getSerializableExtra(INTENT_AMOUNT);
		mType = getIntent().getIntExtra(INTENT_TYPE, 1);

		//将activity添加到列表，方便管理
		if(mType == TYPE_GAMECARD_PAY || mType == TYPE_PHONECARD_PAY){
			EmaPayProcessManager.getInstance().addPayActivity(this);
		}else{
			EmaPayProcessManager.getInstance().addRechargeActivity(this);
		}
		
		if(mType == TYPE_GAMECARD_PAY || mType == TYPE_GAMECARD_RECHARGE){
			mTitleView.setText("游戏卡-支付");
		}else{
			mTitleView.setText("手机卡-支付");	
		}
		
		mTxtPrice.setText(mAmount.getPriceYuan() + "元");
		
		mListSelectType = PayUtil.getTrdSelectCardType(mType);
		if(mListSelectType == null || mListSelectType.size() == 0){
			LOG.d(TAG, "暂不支持任何卡");
			return;
		}
		//默认第一个被选中
		mSelectTempBean = mListSelectType.get(0);
		mSelectTempBean.setSelect(true);
		
		mListSelectAmount = PayUtil.getTrdSelectCardAmount(mSelectTempBean, mAmount);
		//金额默认选中第一个
		mSelectAmountBean = mListSelectAmount.get(0);
		mSelectAmountBean.setSelect(true);
		
		mListAdapter = new MyListAdapter(this, mListSelectType);
		mListView.setAdapter(mListAdapter);
		mGridAdapter = new MyGridAdapter(this);
		mGridAdapter.setData(mListSelectAmount);
		mGridView.setAdapter(mGridAdapter);

		//JUST TEST
//		if(mType == TYPE_GAMECARD_PAY || mType == TYPE_GAMECARD_RECHARGE){
//			mEdtCardNumber.setText("8013380500206251");
//			mEdtCardPassw.setText("87597580");
//		}else{
//			mEdtCardNumber.setText("15765150338867885");
//			mEdtCardPassw.setText("151361380153941660");
//		}
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
	public void onClick(View v) {
		int id = v.getId();
		if(id == getId("ema_title_right_img")){//返回游戏
			new EmaDialogPayPromptCancel(this).show();
		}else if(id == getId("ema_pay_gamecard_btn_pay")){//下一步
			doStart();
		}
	}
	
	/**
	 * 开启 支付|充值
	 */
	public void doStart(){
		if(checkInputIsOk()){
			mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_START);
			if(mType == TYPE_GAMECARD_PAY || mType == TYPE_PHONECARD_PAY){//支付
				doStartPay(mType);
			}else if(mType == TYPE_GAMECARD_RECHARGE || mType == TYPE_PHONECARD_RECHARGE){//充值
				doStartRecharge(mType);
			}
		}
	}
	
	/**
	 * 检查输入是否符合规范
	 * @return
	 */
	private boolean checkInputIsOk(){
		if(UCommUtil.isStrEmpty(mEdtCardNumber.getText().toString())){
			ToastHelper.toast(this, "请输入充值卡号码");
			LOG.d(TAG, "未输入卡号");
			return false;
		}
		if(UCommUtil.isStrEmpty(mEdtCardPassw.getText().toString())){
			ToastHelper.toast(this, "请输入充值卡密码");
			LOG.d(TAG, "未输入充值卡密码");
			return false;
		}
		return true;
	}
	
	
	/**
	 * 开始支付
	 */
	private void doStartPay(int type){
		EmaUser emaUser = EmaUser.getInstance();
		ConfigManager configManager = ConfigManager.getInstance(this);
		DeviceInfoManager deviceInfoManager = DeviceInfoManager.getInstance(this);
		EmaPay emaPay = EmaPay.getInstance(this);
		
		Map<String, String> params = emaPay.buildPayParams();
		params.put("wallet_amount", "0");
		//params.put("sid", emaUser.getAccessSid());
		//params.put("uuid", emaUser.getUUID());
		if(type == TYPE_GAMECARD_PAY){
			params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_GAMECARD + "");
		}else{
			params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_PHONECARD + "");
		}
		params.put("amount", mSelectAmountBean.getAmount().getPriceFen() + "");
		params.put("bank_id", mSelectTempBean.getDrawableId() + "");
		String cardInfo = mEdtCardNumber.getText().toString() + "_" + mEdtCardPassw.getText().toString() + "_" + (int)mSelectAmountBean.getAmount().getPriceYuan();
		params.put("cardinfo", cardInfo);//目前格式 cardNum_cardPassw_面值
		params.put("device_id", deviceInfoManager.getDEVICE_ID());
		params.put("channel", configManager.getChannel());
		/*String sign = UCommUtil.getSign(configManager.getAppId(),
				emaUser.getAccessSid(), emaUser.getUUID(), configManager.getAppKEY());
		params.put("sign", sign);*/
		
		UCommUtil.buildUrl(Url.getPayUrlRecharge(), params);
		
		new HttpInvoker().postAsync(Url.getPayUrlRecharge(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
				boolean resultFlag = false;
				String showMsg = "";
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS:
						LOG.d(TAG, "游戏卡支付成功");
						showMsg = "游戏卡支付成功";
						resultFlag = true;
						break;
					case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR:
						LOG.d(TAG, "签名验证失败");
						showMsg = "签名验证失败";
					case HttpInvokerConst.PAY_RECHARGE_CARD_FAILED://支付失败
						LOG.d(TAG, "支付失败");
						showMsg = "支付失败";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_ON_PAYING://等待付款中
						LOG.d(TAG, "等待付款中");
						showMsg = "等待付款中";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_ORDER_OVER_TIME://订单过期
						LOG.d(TAG, "订单过期");
						showMsg = "订单过期";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_ORDER_UNSAVE_PAYED://已支付风险订单
						LOG.d(TAG, "已支付风险订单");
						showMsg = "已支付风险订单";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_ORDER_CHECKING://风控审核中
						LOG.d(TAG, "风控审核中");
						showMsg = "风控审核中";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_ORDER_UNSAVE_REFUND://风险订单支付拒绝，退款中
						LOG.d(TAG, "风险订单支付拒绝，退款中");
						showMsg = "风险订单支付拒绝，退款中";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_FAILED_UNKNOW://支付失败，原因未知
						LOG.d(TAG, "支付失败，原因未知");
						showMsg = "支付异常";
						break;
					default:
						LOG.d(TAG, "unknown resultCode :" + resultCode);
						showMsg = "订单支付中，请稍后查询";
						break;
					}
				} catch (Exception e) {
					LOG.w(TAG, "支付失败", e);
					showMsg = "支付失败";
				} finally{
					if(resultFlag){
						showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_SUCC, showMsg);
					}else{
						showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, showMsg);
					}
				}
			}
		});
	}
	
	/**
	 * 开始充值
	 */
	private void doStartRecharge(int type){
		EmaUser emaUser = EmaUser.getInstance();
		ConfigManager configManager = ConfigManager.getInstance(this);
		DeviceInfoManager deviceInfoManager = DeviceInfoManager.getInstance(this);
		EmaPay emaPay = EmaPay.getInstance(this);
		
		Map<String, String> params = new HashMap<String, String>();
		
		params.put("client_id", ConfigManager.getInstance(Ema.getInstance().getContext()).getChannel());// 发起站点
		params.put("app_id", configManager.getAppId());
		params.put("partition_id", String.valueOf(0));//分区ID
		params.put("server_id", String.valueOf(0));//服务器ID
		params.put("order_amount", mSelectAmountBean.getAmount().getPriceFen() + "");// 订单金额
		params.put("amount", mSelectAmountBean.getAmount().getPriceFen() + "");// 总额
		params.put("point", String.valueOf(0));// 积分
		if(type == TYPE_GAMECARD_RECHARGE){
			params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_GAMECARD + "");// 充值方式ID
		}else{
			params.put("charge_channel", PayConst.PAY_CHARGE_CHANNEL_PHONECARD + "");// 充值方式ID
		}
		String cardInfo = mEdtCardNumber.getText().toString() + "_" + mEdtCardPassw.getText().toString() + "_" + (int)mSelectAmountBean.getAmount().getPriceYuan();
		params.put("cardinfo", cardInfo);//目前格式 cardNum_cardPassw_面值
		params.put("bank_id", mSelectTempBean.getDrawableId() + "");// bank_id:银行ID
		params.put("coupon", null);// 优惠券
		params.put("app_order_id", "order" + System.currentTimeMillis());// 第三方订单号
		params.put("product_id", "0");// 商品ID
		params.put("product_name", PropertyField.EMA_COIN_UNIT);// 商品名字
		params.put("product_num", String.valueOf(1));// 商品数量
		params.put("ext", "充值钱包");// 附加信息
		params.put("change_app_id","1001");//充值钱包特定参数
		params.put("device_id", deviceInfoManager.getDEVICE_ID());//设备ID
		params.put("channel", configManager.getChannel());
		params.put("wallet_amount", "0");
		/*params.put("sid", emaUser.getAccessSid());
		params.put("uuid", emaUser.getUUID());
		
		String sign = UCommUtil.getSign(configManager.getAppId(),
				emaUser.getAccessSid(),
				emaUser.getUUID(),
				configManager.getAppKEY());
		params.put("sign", sign);*/
		
		UCommUtil.buildUrl(Url.getPayUrlRecharge(), params);
		
		new HttpInvoker().postAsync(Url.getPayUrlRecharge(), params, new HttpInvoker.OnResponsetListener() {
			@Override
			public void OnResponse(String result) {
				mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
				boolean resultFlag = false;
				String showMsg = "";
				try {
					JSONObject json = new JSONObject(result);
					int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
					switch(resultCode){
					case HttpInvokerConst.SDK_RESULT_SUCCESS:
						LOG.d(TAG, "游戏卡充值成功");
						showMsg  = "游戏卡充值成功";
						TrdCardActivity.this.sendBroadcast(new Intent(PropertyField.BROADCAST_RECHARGE_SUCC));
						resultFlag = true;
						break;
					case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR:
						LOG.d(TAG, "签名验证失败");
						showMsg  = "签名验证失败";
					case HttpInvokerConst.PAY_RECHARGE_CARD_FAILED://支付失败
						LOG.d(TAG, "充值失败");
						showMsg  = "充值失败";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_ON_PAYING://等待付款中
						LOG.d(TAG, "等待付款中");
						showMsg  = "等待付款中";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_ORDER_OVER_TIME://订单过期
						LOG.d(TAG, "订单过期");
						showMsg  = "订单过期";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_ORDER_UNSAVE_PAYED://已支付风险订单
						LOG.d(TAG, "已支付风险订单");
						showMsg  = "已支付风险订单";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_ORDER_CHECKING://风控审核中
						LOG.d(TAG, "风控审核中");
						showMsg  = "风控审核中";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_ORDER_UNSAVE_REFUND://风险订单支付拒绝，退款中
						LOG.d(TAG, "风险订单支付拒绝，退款中");
						showMsg  = "风险订单支付拒绝，退款中";
						break;
					case HttpInvokerConst.PAY_RECHARGE_CARD_FAILED_UNKNOW://支付失败，原因未知
						LOG.d(TAG, "支付失败，原因未知");
						showMsg  = "支付异常";
						break;
					default:
						LOG.d(TAG, "游戏卡充值失败");
						showMsg  = "游戏卡充值失败";
						break;
					}
				} catch (Exception e) {
					LOG.w(TAG, "游戏卡充值失败 with Exception", e);
					ToastHelper.toast(TrdCardActivity.this, "游戏卡充值失败");
				} finally{
					if(resultFlag){
						showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_SUCC, showMsg);
					}else{
						showPayResultDialog(EmaConst.PAY_ACTION_TYPE_RECHARGE, EmaConst.PAY_RESULT_FAILED, showMsg);
					}
				}
			}
		});
	}
	
	/**
	 * 显示支付结束后的对话框
	 */
	private void showPayResultDialog(final int actionType, final int resultType, final String promptInfo){
		((Activity) Ema.getInstance().getContext()).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				new EmaDialogPayPromptResult(TrdCardActivity.this, actionType, resultType, promptInfo).show();
			}
		});
	}
	
	/**
	 * 卡类型适配器
	 * @author zhangyang
	 */
	private class MyListAdapter extends BaseAdapter{

		private LayoutInflater inflater;
		private List<PayTrdItemBean> list;
		
		public MyListAdapter(Context context, List<PayTrdItemBean> list){
			inflater = LayoutInflater.from(context);
			this.list = list;
		}
		
		@Override
		public int getCount() {
			if(list == null){
				return 0;
			}
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			if(list == null){
				return null;
			}
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View contentView, ViewGroup arg2) {
			TextView txtView = null;
			if(contentView == null){
				contentView = mResourceManager.getLayout("ema_pay_gcard_list_item");
				txtView = (TextView) contentView.findViewById(mResourceManager.getIdentifier("ema_pay_gcard_list_item_btn", "id"));
				contentView.setTag(txtView);
			}else{
				txtView = (TextView) contentView.getTag();
			}
			
			PayTrdItemBean bean = (PayTrdItemBean) getItem(position);
			txtView.setText(bean.getChannelCode());
			if(bean.isSelect()){
				txtView.setTextColor(Color.WHITE);
				txtView.setBackground(mResourceManager.getDrawable("ema_pay_gcard_list_itme_btn"));
			}else{
				txtView.setTextColor(Color.BLACK);
				txtView.getBackground().setAlpha(0);
			}
			
			return contentView;
		}
		
	}
	
	/**
	 * 面值金额列表适配器
	 * @author zhangyang
	 *
	 */
	private class MyGridAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		private List<PayTrdItemBean> list;
		
		public MyGridAdapter(Context context){
			inflater = LayoutInflater.from(context);
		}

		/**
		 * 设置数据
		 * @param list
		 */
		public void setData(List<PayTrdItemBean> list){
			this.list = list;
		}
		
		@Override
		public int getCount() {
			if(list == null){
				return 0;
			}
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			if(list == null)
				return null;
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View contentView, ViewGroup arg2) {
			TextView txtView = null;
			if(contentView == null){
				contentView = mResourceManager.getLayout("ema_pay_gcard_grid_item");
				txtView = (TextView) contentView.findViewById(mResourceManager.getIdentifier("ema_pay_gcard_grid_item_btn", "id"));
				contentView.setTag(txtView);
			}else{
				txtView = (TextView) contentView.getTag();
			}
			
			PayTrdItemBean bean = (PayTrdItemBean) getItem(position);
			txtView.setText((int)bean.getAmount().getPriceYuan() + "元");
			if(bean.isSelect()){
				txtView.setBackgroundColor(Color.rgb(253, 242, 219));
			}else{
				txtView.setBackgroundColor(Color.WHITE);
			}
			
			return contentView;
		}
		
	}
	
}
