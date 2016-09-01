package com.emagroup.sdk.pay;

import java.io.Serializable;

public class EmaPriceBean implements Serializable {

	public static final int TYPE_FEN = 0;//以分做单位
	public static final int TYPE_YUAN = 1;//以元做单位
	
	private int priceFen;//用来传递参数
	private float priceYuan;//用来显示呈现给用户
	
	public EmaPriceBean(float price, int type){
		if(type == TYPE_FEN){
			this.priceFen = (int) price;
			this.priceYuan = price / 100;
		}else if(type == TYPE_YUAN){
			this.priceFen = (int) (price * 100);
			this.priceYuan = price;
		}
	}
	
	public int getPriceFen() {
		return priceFen;
	}

	public void setPriceByFen(int priceFen) {
		this.priceFen = priceFen;
		this.priceYuan = priceFen / 100;
	}

	public float getPriceYuan() {
		return priceYuan;
	}

	public void setPriceByYuan(float priceYuan) {
		this.priceYuan = priceYuan;
		this.priceFen = (int) (this.priceYuan * 100);
	}

}
