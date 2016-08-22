package cn.emagroup.sdk.comm;

import android.os.Message;

public interface EmaSDKListener {
	/**
	 * 回调函数
	 * 
	 * @param eventId
	 *            回调事件id
	 */
	public void onCallBack(int resultCode,String decr);
}
