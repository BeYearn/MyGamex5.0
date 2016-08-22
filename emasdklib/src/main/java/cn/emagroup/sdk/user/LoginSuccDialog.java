package cn.emagroup.sdk.user;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import cn.emagroup.sdk.Ema;
import cn.emagroup.sdk.comm.EmaCallBackConst;
import cn.emagroup.sdk.comm.ResourceManager;
import cn.emagroup.sdk.ui.ToolBar;
import cn.emagroup.sdk.utils.LOG;
import cn.emagroup.sdk.utils.UCommUtil;

public class LoginSuccDialog extends Dialog {

	private static final String TAG = "LoginSuccDialog";
	
	private static final int CODE_LOGIN_SUCC = 0;//登录成功
	private static final int CODE_LOGIN_CANCEL = 1;//点击了切换账号，退出登录
	
	private ResourceManager mResourceManager;
	private EmaUser mEmaUser;
	private Timer mTimer;
	private boolean mFlagIsBtnShow;//标记是否显示按钮（登录的时候显示按钮，注册的时候不显示）
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case CODE_LOGIN_SUCC://登录成功
				LOG.d(TAG, "登录成功");
				LoginSuccDialog.this.dismiss();
				mEmaUser.setIsLogin(true);
				UCommUtil.makeUserCallBack(EmaCallBackConst.LOGINSUCCESS, "登录成功");
				ToolBar.getInstance(Ema.getInstance().getContext()).showToolBar();
				break;
			case CODE_LOGIN_CANCEL://退出登录
				LOG.d(TAG, "切换账号，退出登录");
				LoginSuccDialog.this.dismiss();
				mEmaUser.clearUserInfo();
				new LoginDialog(Ema.getInstance().getContext()).show();
				break;
			}
		};
	};
	
	public LoginSuccDialog(Context context, boolean isBtnShow) {
		super(context);
		mResourceManager = ResourceManager.getInstance(context);
		mEmaUser = EmaUser.getInstance();
		mFlagIsBtnShow = isBtnShow;
		//NOTE It's not good. 但为测试方便，需要一直显示切换按钮
		mFlagIsBtnShow = true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏

		initView();
		
		setLayoutParams();
		this.setCancelable(false);
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		setContentView(mResourceManager.getLayout("ema_login_succdialog"));
		TextView nameView = (TextView) findViewById(mResourceManager.getIdentifier("ema_login_succ_txtView", "id"));
		nameView.setText(mEmaUser.getNickName() + "正在登录...");
		Button btnSwitchAccount = (Button) findViewById(mResourceManager.getIdentifier("ema_login_succ_buttonView", "id"));
		if(mFlagIsBtnShow){
			btnSwitchAccount.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(mTimer != null){
						mTimer.cancel();
					}
					mHandler.sendEmptyMessage(CODE_LOGIN_CANCEL);
				}
			});
		}else{
			btnSwitchAccount.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 初始化位置
	 */
	private void setLayoutParams() {
		Window window = getWindow();
		window.setGravity(Gravity.TOP);
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.5f;
		window.setAttributes(lp);
	}

	/**
	 * 开始显示对话框
	 * 在没有点击 切换账号 按钮的情况下，2秒钟后对话框会自动消失
	 * 在2秒钟内点击了 切换账号 按钮的情况下，对话框消失并重新显示登录对话框
	 */
	public void start(){
		this.show();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(CODE_LOGIN_SUCC);
			}
		}, 2000);
	}
}
