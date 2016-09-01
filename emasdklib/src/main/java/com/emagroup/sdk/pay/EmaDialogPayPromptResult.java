package com.emagroup.sdk.pay;

import com.emagroup.sdk.comm.ResourceManager;
import com.emagroup.sdk.utils.EmaConst;
import com.emagroup.sdk.utils.UCommUtil;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EmaDialogPayPromptResult extends Dialog {
	
	private ResourceManager mResourceManager;// 资源管理
	
	private int mActionType;
	private int mResultType;
	private String mPromptInfo;
	
	//views
	private TextView mTxtPromptView;//提示语
	private ImageView mImgPromptView;//提示图片

	public EmaDialogPayPromptResult(Context context, int actionType, int resultType, String promptInfo) {
		super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
		mResourceManager = ResourceManager.getInstance(context);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		this.mActionType = actionType;
		this.mResultType = resultType;
		this.mPromptInfo = promptInfo;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}
	
	private void initView() {
		setContentView(mResourceManager.getIdentifier("ema_prompt_pay_result",
				"layout"));
		
		mTxtPromptView = (TextView) findViewById(mResourceManager.getIdentifier("ema_txt_pay_result", "id"));
		mImgPromptView = (ImageView) findViewById(mResourceManager.getIdentifier("ema_img_pay_result", "id"));
		
		//退出支付
		findViewById(mResourceManager.getIdentifier("ema_btn_pay_sure", "id")).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EmaDialogPayPromptResult.this.dismiss();
				if(mActionType == EmaConst.PAY_ACTION_TYPE_PAY){
					EmaPayProcessManager.getInstance().closePay();
				}else if(mActionType == EmaConst.PAY_ACTION_TYPE_RECHARGE){
					EmaPayProcessManager.getInstance().closeRecharge();
				}
			}
		});
	}

	private void initData(){
		String prompt = "";
		int drawableId = mResourceManager.getIdentifier("ema_prompt_paywarn", "drawable");
		switch(mResultType){
		case EmaConst.PAY_RESULT_SUCC://支付成功
			drawableId = mResourceManager.getIdentifier("ema_prompt_paysucc", "drawable");
			prompt = "支付成功";
			break;
		case EmaConst.PAY_RESULT_FAILED://支付失败
			prompt = "支付失败";
			break;
		case EmaConst.PAY_RESULT_CANCEL://支付退出
			prompt = "用户退出支付";
			break;
		case EmaConst.PAY_RESULT_OTHERS://其他情况，统一视为支付失败
			prompt = "支付失败";
			break;
		}
		if(!UCommUtil.isStrEmpty(mPromptInfo)){
			prompt = mPromptInfo;
		}
		mImgPromptView.setImageResource(drawableId);
		mTxtPromptView.setText(prompt);
	}
}
