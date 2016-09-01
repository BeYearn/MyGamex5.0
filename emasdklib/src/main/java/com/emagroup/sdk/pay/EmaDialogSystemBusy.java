package com.emagroup.sdk.pay;

import com.emagroup.sdk.comm.ResourceManager;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class EmaDialogSystemBusy extends Dialog {
	
	private ResourceManager mResourceManager;// 资源管理

	public EmaDialogSystemBusy(Context context) {
		super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
		mResourceManager = ResourceManager.getInstance(context);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mResourceManager.getIdentifier("ema_dialog_system_busy",
				"layout"));
		findViewById(mResourceManager.getIdentifier("ema_btn_pay_sure", "id")).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EmaDialogSystemBusy.this.dismiss();
				EmaPayProcessManager.getInstance().closeAll();
			}
		});
	}
}
