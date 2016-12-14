package com.emagroup.sdk;

public class PayTrdItemBean {

	private String thirdPayName;//第三方支付渠道的名称
	private int drawableId;//渠道图标资源ID
	private int channelId;//代表了第三方支付渠道编号
	private EmaPriceBean amount;//代表充值金额
	private boolean select;//是否选中
	private int discount;//折扣
	
	public PayTrdItemBean(){
	}
	
	public PayTrdItemBean(String thirdPayName, int id) {
		super();
		this.thirdPayName = thirdPayName;
		this.drawableId = id;
		this.discount = 100;
	}
	
	public PayTrdItemBean(String thirdPayName, int channelId, int discount){
		super();
		this.thirdPayName = thirdPayName;
		this.channelId = channelId;
		this.discount = discount;
	}

	public EmaPriceBean getAmount() {
		return amount;
	}

	public PayTrdItemBean setAmount(EmaPriceBean amount) {
		this.amount = amount;
		return this;
	}
	
	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public int getDiscount() {
		return discount;
	}

	public void setDiscount(int discount) {
		this.discount = discount;
	}

	public String get3rdPayName() {
		return thirdPayName;
	}

	public void set3rdPayName(String channelCode) {
		this.thirdPayName = channelCode;
	}

	public int getDrawableId() {
		return drawableId;
	}

	public void setDrawableId(int id) {
		this.drawableId = id;
	}

	public boolean isSelect() {
		return select;
	}

	public void setSelect(boolean select) {
		this.select = select;
	}

}
