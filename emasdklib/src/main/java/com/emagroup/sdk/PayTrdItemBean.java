package com.emagroup.sdk;

public class PayTrdItemBean {

	private String channel_code;//第三方支付渠道的名称
	private int drawableId;//渠道图标资源ID
	private int channelId;//代表了第三方支付渠道id
	private EmaPriceBean amount;//代表充值金额
	private boolean select;//是否选中
	private int discount;//折扣
	
	public PayTrdItemBean(){
	}
	
	public PayTrdItemBean(String channel_code, int id) {
		super();
		this.channel_code = channel_code;
		this.drawableId = id;
		this.discount = 100;
	}
	
	public PayTrdItemBean(String channel_code, int channelId, int discount){
		super();
		this.channel_code = channel_code;
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

	public String getChannelCode() {
		return channel_code;
	}

	public void setChannelCode(String channelCode) {
		this.channel_code = channelCode;
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
