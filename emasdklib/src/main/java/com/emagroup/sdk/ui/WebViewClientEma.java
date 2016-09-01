package com.emagroup.sdk.ui;

import com.emagroup.sdk.Ema;
import com.emagroup.sdk.utils.LOG;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewClientEma extends WebViewClient {

	private static final String TAG = "WebViewClientEma";
	
	private Handler mHandler;
	
	public WebViewClientEma(Handler handler){
		mHandler = handler;
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		LOG.d(TAG, url);
		// 游戏退出
		if (-1 != url.indexOf("/login/logout")) {
			LOG.d(TAG, "WebViewClient:退出按钮");
			mHandler.sendEmptyMessage(WebViewActivity.CODE_LOGOUT);
		}else if (-1 != url.indexOf("/chargesucc")) {// 财付通支付成功  	http://192.168.10.180:8080/chargesucc.html
			LOG.d(TAG, "WebViewClient:财付通支付完成");
			mHandler.sendEmptyMessage(WebViewActivity.CODE_TENPAY_SUCC);
		}else if(-1 != url.indexOf("spread/login.html")){
			LOG.d(TAG, "点击成为推广员");
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	        Ema.getInstance().getContext().startActivity(intent);
			return true;
		}
		return false;
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		//主页删除历史
		if (-1 != url.indexOf("/userinfo")
				|| -1 != url.indexOf("/libao/libaolist")
				|| -1 != url.indexOf("/help/index")) {
			mHandler.sendEmptyMessage(WebViewActivity.CODE_CLEAR_HISTORY);
		}
	}
	
}
