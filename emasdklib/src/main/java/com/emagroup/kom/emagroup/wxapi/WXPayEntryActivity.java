package com.emagroup.kom.emagroup.wxapi;
import java.util.HashMap;
import java.util.Map;

import com.emagroup.sdk.Ema;
import com.emagroup.sdk.comm.EmaCallBackConst;
import com.emagroup.sdk.comm.ResourceManager;
import com.emagroup.sdk.pay.EmaPayProcessManager;
import com.emagroup.sdk.utils.EmaConst;
import com.emagroup.sdk.utils.LOG;
import com.emagroup.sdk.utils.PropertyField;
import com.emagroup.sdk.utils.UCommUtil;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String TAG = WXPayEntryActivity.class.toString();
	private IWXAPI api;
	
	private ResourceManager mResourceManager;
	
	//views
	private ImageView mImgPromptView;
	private TextView mTxtPromptView;
	private TextView mBtnSureView;
	
	private Map<String, Integer> mIDmap;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mResourceManager = ResourceManager.getInstance(this);
		setContentView(mResourceManager.getIdentifier("ema_prompt_pay_result", "layout"));

		initView();
		
		api = WXAPIFactory.createWXAPI(this, EmaConst.EMA_WEIXIN_APPID);
		api.handleIntent(getIntent(), this);
		
	}
	
	private void initView(){
		mImgPromptView = (ImageView) findViewById(getId("ema_img_pay_result"));
		mTxtPromptView = (TextView) findViewById(getId("ema_txt_pay_result"));
		mBtnSureView = (TextView) findViewById(getId("ema_btn_pay_sure"));
		
		mBtnSureView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(EmaPayProcessManager.getInstance().getWeixinActionType() == EmaConst.PAY_ACTION_TYPE_PAY){
					EmaPayProcessManager.getInstance().closePay();
				}else if(EmaPayProcessManager.getInstance().getWeixinActionType() == EmaConst.PAY_ACTION_TYPE_RECHARGE){
					EmaPayProcessManager.getInstance().closeRecharge();
				}
				WXPayEntryActivity.this.finish();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq arg0) {

	}

	@Override
	public void onResp(BaseResp resp) {
		LOG.d(TAG, "resq code:" + resp.errCode);
		EmaPayProcessManager.getInstance().clearWeixinOrderInfo();
		boolean isPay = EmaPayProcessManager.getInstance().getWeixinActionType() == EmaConst.PAY_ACTION_TYPE_PAY;
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			switch(resp.errCode){
			case 0://支付成功
				mTxtPromptView.setText("微信支付成功");
				mImgPromptView.setImageResource(mResourceManager.getIdentifier("ema_prompt_paysucc", "drawable"));
				if(isPay){
					UCommUtil.makePayCallBack(EmaCallBackConst.PAYSUCCESS, "支付成功");
				}else{
					//发送充值成功的广播，刷新钱包余额
					Ema.getInstance().getContext().sendBroadcast(new Intent(PropertyField.BROADCAST_RECHARGE_SUCC));
				}
				LOG.d(TAG, "weixin pay succ");
				break;
			case -1://支付失败
				mTxtPromptView.setText("微信支付异常  errorCode:" + resp.errCode);
				mImgPromptView.setImageResource(mResourceManager.getIdentifier("ema_prompt_paywarn", "drawable"));
				if(isPay){
					UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "支付异常");
				}
				LOG.w(TAG, "weixin pay failed errorCode:" + resp.errCode + "  errorMsg:" + resp.errStr);
				break;
			case -2://支付取消
				mTxtPromptView.setText("微信支付取消");
				mImgPromptView.setImageResource(mResourceManager.getIdentifier("ema_prompt_paywarn", "drawable"));
				UCommUtil.makePayCallBack(EmaCallBackConst.PAYCANELI, "支付取消");
				LOG.d(TAG, "weixin pay failed");
				break;
			}
		}
	}
	
	/**
	 * 方法目的（为了防止重复去获取资源ID） 
	 * @param key
	 * @return
	 */
	private int getId(String key){
		if(mIDmap == null){
			mIDmap = new HashMap<String, Integer>();
		}
		if(!mIDmap.containsKey(key)){
			mIDmap.put(key, mResourceManager.getIdentifier(key, "id"));
		}
		return mIDmap.get(key);
	}

}
