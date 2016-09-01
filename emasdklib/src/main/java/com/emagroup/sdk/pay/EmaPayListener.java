package com.emagroup.sdk.pay;

import android.os.Message;

public interface EmaPayListener {

	/**
	 * 支付回调函数
	 * @param result
	 */
	public void onPayCallBack(Message msg);
	
}
