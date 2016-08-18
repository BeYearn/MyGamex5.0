package cn.emagroup.sdk.ui;

import java.util.Timer;
import java.util.TimerTask;

import cn.emagroup.sdk.comm.ResourceManager;
import cn.emagroup.sdk.utils.LOG;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class SplashDialog extends Dialog {

	private static final String TAG = "SplashDialog";
	private Activity mActivity;
	private ResourceManager mResourceManager;
	
	private Timer mTimer;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			SplashDialog.this.dismiss();
		};
	};
	
	public SplashDialog(Context context) {
		super(context, ResourceManager.getInstance(context).getIdentifier("ema_dialog", "style"));
		mActivity = (Activity)context;
		mResourceManager = ResourceManager.getInstance(mActivity);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		initView();
		
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
		this.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	/**
	 * 开始显示闪屏，并在3秒后关闭闪屏
	 */
	public void start(){
		if(this.isShowing()){
			return;
		}
		this.show();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(0);
			}
		}, 3000);
	}
	
	/**
	 * 初始化界面
	 */
	private void initView() {
		int type = mActivity.getResources().getConfiguration().orientation;
		View view = mResourceManager.getLayout("ema_splash");
		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		int drawableId = 0;
		if(type == Configuration.ORIENTATION_LANDSCAPE){
			LOG.d(TAG, "landscape");
			drawableId = mResourceManager.getIdentifier("ema_init_bg", "drawable");
		}else if(type == Configuration.ORIENTATION_PORTRAIT){
			LOG.d(TAG, "portrait");
			drawableId = mResourceManager.getIdentifier("ema_init_bg_vertical", "drawable");
		}
		ImageView imageView = (ImageView) view.findViewById(mResourceManager.getIdentifier("ema_splash_imageview", "id"));
		
		imageView.setImageResource(drawableId);
		this.setContentView(view);
	}
	
}
