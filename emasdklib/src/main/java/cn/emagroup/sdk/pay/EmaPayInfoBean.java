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

	/*
	 * 支付 partition_id:分区ID false server_id:服务器ID false order_amount:单价 true
	 * amount:支付总金额 true point：积分 true product_num:商品数量 coupon:优惠券 false
	 * out_order_id:第三方订单号 false product_name:商品名字 product_id:商品ID rold_id：角色ID
	 * rold_name：角色名字 rold_level：角色等级 ext:附加信息
	 */
	// true 代表必需参数 false 代表可选参数
	private String app_order_id;//游戏订单id（cp_id）
	private int partition_id;// 分区ID false
	private int server_id;// 服务器ID false
	private float order_amount;// 订单金额	true
	private float amount;// 支付总金额 true
	private int point;// 积分 true
	private int product_num;// 商品数量 truen
	private String coupon;// 优惠券 false
	private String product_name;// 商品名称
	private String product_id;// 商品ID
	private String rold_id;// 角色ID
	private String rold_name;// 角色名称
	private String ext;// 附加信息
	private int rold_level;// 角色等级
	
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

	public int getPartition_id() {
		return partition_id;
	}

	public void setPartition_id(int partition_id) {
		this.partition_id = partition_id;
	}

	public int getServer_id() {
		return server_id;
	}

	public void setServer_id(int server_id) {
		this.server_id = server_id;
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

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public int getProduct_num() {
		return product_num;
	}

	public void setProduct_num(int product_num) {
		this.product_num = product_num;
	}

	public String getCoupon() {
		return coupon;
	}

	public void setCoupon(String coupon) {
		this.coupon = coupon;
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

	public String getRold_id() {
		return rold_id;
	}

	public void setRold_id(String rold_id) {
		this.rold_id = rold_id;
	}

	public String getRold_name() {
		return rold_name;
	}

	public void setRold_name(String rold_name) {
		this.rold_name = rold_name;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		if(ext.length() > 100){
			LOG.d(TAG, "附加信息超过限制长度100");
			mFLagCheckOK = false;
			setErrorInfo("附加信息超过限制长度100");
			return;
		}
		this.ext = ext;
	}

	public int getRold_level() {
		return rold_level;
	}

	public void setRold_level(int rold_level) {
		this.rold_level = rold_level;
	}
	
	private void setErrorInfo(String info){
		if(mErrorInfo == null){
			mErrorInfo = new StringBuffer();
		}
		mErrorInfo.append(info).append("\r\n");
	}

}
