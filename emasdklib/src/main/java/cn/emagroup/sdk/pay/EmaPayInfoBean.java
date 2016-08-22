package cn.emagroup.sdk.pay;

import cn.emagroup.sdk.utils.LOG;

/*
 * 支付参数类
 * 
 * 方便参数合法性的判断
 * 
 * */

public class EmaPayInfoBean {

	private static final String TAG = "EmaPayInfo";

	private String app_order_id;//游戏订单id（cp_id）
	private float order_amount;// 订单金额	true
	private float amount;// 支付总金额 true
	private int product_num;// 商品数量 truen
	private String product_name;// 商品名称
	private String product_id;// 商品ID

	private EmaPriceBean order_amount_pricebean;//对应order_amount
	private EmaPriceBean amount_pricebean;//对应amount
	
	private boolean mFLagCheckOK = true;//检查某些输入字段信息是否符合规则
	private StringBuffer mErrorInfo;
	
	protected boolean getFlagCheckOk() {
		return mFLagCheckOK;
	}
	
	protected String getErrorInfo() {
		if(mErrorInfo == null)
			return null;
		return mErrorInfo.toString();
	}
	
	public EmaPriceBean getOrder_amount_pricebean() {
		return order_amount_pricebean;
	}

	public EmaPriceBean getAmount_pricebean() {
		return amount_pricebean;
	}

	public String getApp_order_id() {
		return app_order_id;
	}

	public void setApp_order_id(String app_order_id) {
		this.app_order_id = app_order_id;
	}


	public float getOrder_amount() {
		return order_amount;
	}

	/**
	 * 精确到分
	 * @param order_amount
	 */
	public void setOrder_amount(float order_amount) {
		this.order_amount = order_amount;
		this.order_amount_pricebean = new EmaPriceBean(order_amount, EmaPriceBean.TYPE_FEN);
	}

	public float getAmount() {
		return amount;
	}

	/**
	 * 精确到分
	 * @param amount
	 */
	public void setAmount(float amount) {
		if(amount > 9999999){
			LOG.d(TAG, "总金额不能超过9999999");
			mFLagCheckOK = false;
			setErrorInfo("总金额不能超过99999");
			return;
		}
		this.amount = amount;
		this.amount_pricebean = new EmaPriceBean(amount, EmaPriceBean.TYPE_FEN);
	}


	public int getProduct_num() {
		return product_num;
	}

	public void setProduct_num(int product_num) {
		this.product_num = product_num;
	}


	public String getProduct_name() {
		return product_name;
	}

	public void setProduct_name(String product_name) {
		if(product_name.length() > 50){
			LOG.d(TAG, "商品名字超过限制长度50");
			mFLagCheckOK = false;
			setErrorInfo("商品名字超过限制长度50");
			return;
		}
		this.product_name = product_name;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		if(product_id.length() > 25){
			LOG.d(TAG, "商品ID超过限制长度25");
			mFLagCheckOK = false;
			setErrorInfo("商品ID超过限制长度25");
			return;
		}
		this.product_id = product_id;
	}


	public void setExt(String ext) {
		if(ext.length() > 100){
			LOG.d(TAG, "附加信息超过限制长度100");
			mFLagCheckOK = false;
			setErrorInfo("附加信息超过限制长度100");
			return;
		}
		//this.ext = ext;
	}

	private void setErrorInfo(String info){
		if(mErrorInfo == null){
			mErrorInfo = new StringBuffer();
		}
		mErrorInfo.append(info).append("\r\n");
	}

}
