package com.example.martintest;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import cn.emagroup.sdk.Ema;
import cn.emagroup.sdk.pay.EmaPayInfoBean;
import cn.emagroup.sdk.pay.EmaPayListener;
import cn.emagroup.sdk.utils.LOG;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("ClickableViewAccessibility")
public class DemoPayDialog extends Dialog {

	protected EditText GoodsName;
	protected EditText product_id;
	protected EditText amount;
	protected EditText ext;
	protected Button pay,yifen;

	public DemoPayDialog(Context context) {
		super(context);
	}

	protected DemoPayDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public DemoPayDialog(Context context, int theme) {
		super(context, theme);
	}

	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏

		setContentView(R.layout.activity_demo_pay);

		GoodsName = (EditText) findViewById(R.id.GoodsName);
		product_id = (EditText) findViewById(R.id.product_id);
		amount = (EditText) findViewById(R.id.amount);
		ext = (EditText) findViewById(R.id.ext);
		pay = (Button) findViewById(R.id.pay);
		pay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pay();
			}
		});
		yifen = (Button) findViewById(R.id.payyifen);
		yifen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				payyifen();
			}
		});
	}

	protected void pay() {
		if (!amount.getText().toString().isEmpty()) {
			float amountNum = Float.valueOf(amount.getText().toString());
			if (0 != amountNum) {

				EmaPayInfoBean infoBean = new EmaPayInfoBean();
				infoBean.setOrder_amount(amountNum * 100);
				infoBean.setAmount(amountNum * 100);
				infoBean.setProduct_num(1);
				infoBean.setApp_order_id("order" + System.currentTimeMillis());
				infoBean.setProduct_name(GoodsName.getText().toString());
				infoBean.setProduct_id(product_id.getText().toString());
				infoBean.setExt(ext.getText().toString());
				Ema.getInstance().pay(infoBean, new EmaPayListener() {
					@Override
					public void onPayCallBack(Message msg) {
						if(msg != null){
							LOG.d(DemoPayDialog.class.toString(), "code__:" + msg.what);
							if(msg.obj != null){
								LOG.d(DemoPayDialog.class.toString(), "info__:" + msg.obj);
							}
						}
					}
				});
			}
		}
	}

	protected void payyifen() {
		EmaPayInfoBean infoBean = new EmaPayInfoBean();
		infoBean.setOrder_amount(1);
		infoBean.setAmount(1);
		infoBean.setProduct_num(1);
		infoBean.setApp_order_id("order" + System.currentTimeMillis());
		infoBean.setProduct_name(GoodsName.getText().toString());
		infoBean.setProduct_id(product_id.getText().toString());
		infoBean.setExt(ext.getText().toString());
		Ema.getInstance().pay(infoBean, new EmaPayListener() {
			@Override
			public void onPayCallBack(Message msg) {
				if(msg != null){
					LOG.d(DemoPayDialog.class.toString(), "code__:" + msg.what);
					if(msg.obj != null){
						LOG.d(DemoPayDialog.class.toString(), "info__:" + msg.obj);
					}
				}
			}
		});
	}
}
