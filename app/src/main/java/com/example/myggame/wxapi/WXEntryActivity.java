package com.example.myggame.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.emagroup.sdk.ConfigManager;
import com.emagroup.sdk.ConfigManager;
import com.emagroup.sdk.ThirdLoginUtils;
import com.emagroup.sdk.WeixinShareUtils;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	// IWXAPI 是第三方app和微信通信的openapi接口
	private IWXAPI api;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		api = WXAPIFactory.createWXAPI(this, ConfigManager.getInstance(this).getWachatAppId()/*"wx3b310a6bcccbd788"*/, false);
		api.handleIntent(getIntent(), this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);
		api.handleIntent(intent, this);
	}

	// 微信发送请求到第三方应用时，会回调到该方法
	@Override
	public void onReq(BaseReq arg0) { }

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				//ToastHelper.toast(this,"weixin share successful");
			//	WeixinShareUtils.mListener.onCallBack(BaseResp.ErrCode.ERR_OK,"weixin share successful");
				//与微信登录冲突，等完整分享做完一起解决
			//	ThirdLoginUtils.getInstance(this).wechatLogin((SendAuth.Resp)resp);
			//	WeixinShareUtils.mListener.onCallBack(BaseResp.ErrCode.ERR_OK,"weixin share successful");
				//与微信登录冲突，等完整分享做完一起解决
				ThirdLoginUtils.getInstance(this).wechatLogin((SendAuth.Resp)resp);
				//分享成功
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				//ToastHelper.toast(this,"weixin share cancle");
				//与微信登录冲突，等完整分享做完一起解决
				//与微信登录冲突，等完整分享做完一起解决
				WXEntryActivity.this.finish();
				WeixinShareUtils.mListener.onCallBack(BaseResp.ErrCode.ERR_USER_CANCEL,"weixin share cancle");
				//分享取消
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				//分享拒绝
				//ToastHelper.toast(this,"weixin share denied");
				WeixinShareUtils.mListener.onCallBack(BaseResp.ErrCode.ERR_AUTH_DENIED,"weixin share failed");
				break;
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(this.getClass().getName(),this.getClass().getName()+"-----onDestroy");
	}
}
