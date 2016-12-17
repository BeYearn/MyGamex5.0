package com.emagroup.sdk;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EmaBinderAlertDialog extends Dialog {


	private final Context mContext;
	private ResourceManager mResourceManager;// 资源管理

	//views
	private TextView mTxtPromptView;//提示语
	private ImageView mImgPromptView;//提示图片
	private TextView mBtnSure;
	private TextView mBtnCancle;
	private View.OnClickListener canelClickListener;

	//private boolean isUpdateVersion;

	/**
	 *
	 * @param context
     */
	public EmaBinderAlertDialog(Context context) {
		super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
		this.mContext=context;
		mResourceManager = ResourceManager.getInstance(context);
		setCancelable(false);
		setCanceledOnTouchOutside(false);

	}

	public void setCanelClickListener(View.OnClickListener canelClickListener) {
		this.canelClickListener = canelClickListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}
	
	private void initView() {
		setContentView(mResourceManager.getIdentifier("ema_bind_alert_dialog", "layout"));

		 mTxtPromptView = (TextView) findViewById(mResourceManager.getIdentifier("ema_txt_content", "id"));//dialog显示的内容

		mBtnSure= (TextView) findViewById(mResourceManager.getIdentifier("ema_tv_sure", "id"));
		mBtnCancle= (TextView) findViewById(mResourceManager.getIdentifier("ema_tv_cancel", "id"));

		mBtnSure.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Ema.getInstance().getContext(), WebViewActivity.class);
				intent.putExtra(WebViewActivity.INTENT_TITLE, "绑定账号");
				intent.putExtra(WebViewActivity.INTENT_URL,Url.getWebUrlBinder());
				intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_BIND);
				mContext.startActivity(intent);
				EmaBinderAlertDialog.this.dismiss();
			}
		});

		/*mBtnCancle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			 EmaBinderAlertDialog.this.dismiss();

			}
		});*/

		mBtnCancle.setOnClickListener(canelClickListener);


			mBtnSure.setVisibility(View.VISIBLE);
			mBtnCancle.setVisibility(View.VISIBLE);

	}

	private void initData(){

		mTxtPromptView.setText("您登录了游客账户，为了您的账户安全，避免数据丢失，请尽快绑定手机。");
	}


}
