package com.emagroup.sdk;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class EmaDialogPayPromptCancel extends Dialog {
	
	private ResourceManager mResourceManager;// 资源管理

	public EmaDialogPayPromptCancel(Context context) {
		super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
		mResourceManager = ResourceManager.getInstance(context);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(mResourceManager.getIdentifier("ema_prompt_pay_cancel","layout"));
		//继续支付
		findViewById(mResourceManager.getIdentifier("ema_btn_pay_continue", "id")).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EmaDialogPayPromptCancel.this.dismiss();
			}
			
		});
		//退出支付
		findViewById(mResourceManager.getIdentifier("ema_btn_pay_exit", "id")).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EmaDialogPayPromptCancel.this.dismiss();
				EmaPayProcessManager.getInstance().closeAll();
				
				UCommUtil.makePayCallBack(EmaCallBackConst.PAYCANELI, "取消支付");
				//call一次取消订单
				EmaPay.getInstance(Ema.getInstance().getContext()).cancelOrder();
			}
		});
	}
}
