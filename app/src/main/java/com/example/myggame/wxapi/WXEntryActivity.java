package com.example.myggame.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.emagroup.sdk.ConfigManager;
import com.emagroup.sdk.Ema;
import com.emagroup.sdk.EmaCallBackConst;
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
			 	if(Ema.getInstance().isWachatLoginFlag()){
					ThirdLoginUtils.getInstance(this).wechatLogin((SendAuth.Resp)resp);
				}else{
					WeixinShareUtils.mListener.onCallBack(EmaCallBackConst.EMA_SHARE_OK,"分享成功");

				}
			 	break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				WXEntryActivity.this.finish();
				if(!Ema.getInstance().isWachatLoginFlag()){
				WeixinShareUtils.mListener.onCallBack(EmaCallBackConst.EMA_SHARE_CANCLE,"分享取消");}
				//分享取消
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				//分享拒绝
				//ToastHelper.toast(this,"weixin share denied");
				if(!Ema.getInstance().isWachatLoginFlag()){
				WeixinShareUtils.mListener.onCallBack(EmaCallBackConst.EMA_SHARE_FAIL,"分享失败");};
				break;
		}
		finish();
	}


}
