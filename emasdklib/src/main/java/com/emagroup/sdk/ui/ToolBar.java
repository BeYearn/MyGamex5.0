package com.emagroup.sdk.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.emagroup.sdk.Ema;
import com.emagroup.sdk.comm.ResourceManager;
import com.emagroup.sdk.comm.Url;
import com.emagroup.sdk.utils.LOG;
import com.emagroup.sdk.utils.ToastHelper;
import com.emagroup.sdk.utils.UCommUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

@SuppressLint("NewApi")
public class ToolBar implements OnClickListener {

	public enum ToolbarState{
		mini,//小图标
		normal,//大图标
		move,//移动状态
		open//展开状态
	}
	
	private static final String TAG = "ToolBar";
	
	private Context mContext;
	private ResourceManager mResourceManager;
	
	private static ToolBar mInstance;
	private static final Object synchron = new Object();
	private ToolbarState mToolbarState = ToolbarState.normal;

	private WindowManager mWindowManager = null;
	private WindowManager.LayoutParams mWindowManagerParams = null;
	
	private float mTouchX;
	private float mTouchY;
	private float mX;
	private float mY;
	private float mStartX;
	private float mStartY;
	
	//Views
	private LinearLayout mToolView;//悬浮窗的容器
	private ImageView mBtnToolBarView;//悬浮窗的显示图标
	
	private LinearLayout mToolRightView;//悬浮窗图标右边的布局
	private Button mBtnAccount;
	private Button mBtnGift;
	private Button mBtnPromotion;//推广
	private Button mBtnHelp;
	
	private LinearLayout mToolLeftView;//悬浮窗图标 左边的布局
	private Button mBtnAccountLeft;
	private Button mBtnGiftLeft;
	private Button mBtnPromotionLeft;//推广
	private Button mBtnHelpLeft;
	
	//标记
	private boolean mFlagIsShowing;
	
	private Map<String, Integer> mIDMap;
	
	private ToolBar(Context context){
		mContext = context;
		mResourceManager = ResourceManager.getInstance(mContext);
		mFlagIsShowing = false;
		
		initToolbar();
	}
	
	public static ToolBar getInstance(Context context){
		if(mInstance == null){
			synchronized (synchron) {
				if(mInstance == null){
					mInstance = new ToolBar(context);
				}
			}
		}
		return mInstance;
	}
	
	/**
	 * 显示悬浮窗
	 */
	public void showToolBar(){
		((Activity)Ema.getInstance().getContext()).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LOG.d(TAG, "isShowing_:" + mFlagIsShowing);
				if(mFlagIsShowing == true){
					return;
				}
				if(mWindowManager == null){
					mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
				}
				if(mToolView == null){
					initToolbarView();
				}
				if(mWindowManagerParams == null){
					createWindowParams();
				}
				mFlagIsShowing = true;
				mToolLeftView.setVisibility(View.GONE);
				mToolRightView.setVisibility(View.GONE);
				mToolView.setBackgroundColor(0);
				try {
					LOG.d(TAG, "xxxxxxxxxxxxxxxx    add toolview to window     xxxxxxxxxxxxxxxx");
					mWindowManager.addView(mToolView, mWindowManagerParams);
				} catch (Exception e) {
					LOG.w(TAG, "showToolBar", e);
					e.printStackTrace();
				}
				refreshToolbarState(ToolbarState.normal);
			}
		});
	}
	
	/**
	 * 隐藏悬浮框
	 */
	public void hideToolBar(){
		if(mFlagIsShowing == true){
			mFlagIsShowing = false;
			((Activity)Ema.getInstance().getContext()).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						LOG.d(TAG, "xxxxxxxxxxxxxxxx    remove toolview from window     xxxxxxxxxxxxxxxx");
						mWindowManager.removeView(mToolView);
					} catch (Exception e) {
						LOG.w(TAG, "hideToolBar", e);
					}
				}
			});
		}
	}
	
	/**
	 * 判断悬浮框是否正在显示
	 * @return
	 */
	public boolean isToolbarShowing(){
		return mFlagIsShowing;
	}
	
	/**
	 * 初始化悬浮窗，只在第一次获取ToolBar的时候进行
	 * @param context
	 */
	private void initToolbar(){
		mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		initToolbarView();
		createWindowParams();
	}
	
	/**
	 * 初始化界面
	 */
	private void initToolbarView(){
		mToolView = (LinearLayout) LayoutInflater.from(mContext).inflate(
				mResourceManager.getIdentifier("ema_floating", "layout"), null);
		mBtnToolBarView = (ImageView) mToolView.findViewById(getId("ema_btn_toolbar"));
		mToolRightView = (LinearLayout) mToolView.findViewById(getId("ema_toolbar_right"));
		mToolLeftView = (LinearLayout) mToolView.findViewById(getId("ema_toolbar_left"));
		mBtnAccount = (Button) mToolView.findViewById(getId("ema_btn_account"));
		mBtnAccountLeft = (Button) mToolView.findViewById(getId("ema_btn_account_left"));
		mBtnGift = (Button) mToolView.findViewById(getId("ema_btn_gift"));
		mBtnGiftLeft = (Button) mToolView.findViewById(getId("ema_btn_gift_left"));
		mBtnPromotion = (Button) mToolView.findViewById(getId("ema_btn_promotion"));
		mBtnPromotionLeft = (Button) mToolView.findViewById(getId("ema_btn_promotion_left"));
		mBtnHelp = (Button) mToolView.findViewById(getId("ema_btn_help"));
		mBtnHelpLeft = (Button) mToolView.findViewById(getId("ema_btn_help_left"));
		
		mBtnAccount.setOnClickListener(this);
		mBtnAccountLeft.setOnClickListener(this);
		mBtnGift.setOnClickListener(this);
		mBtnGiftLeft.setOnClickListener(this);
		mBtnPromotion.setOnClickListener(this);
		mBtnPromotionLeft.setOnClickListener(this);
		mBtnHelp.setOnClickListener(this);
		mBtnHelpLeft.setOnClickListener(this);
		
		//获取屏幕的高宽
		Display display = mWindowManager.getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		final int screenWidth = point.x;
		int screenHeight = point.y;
		
		LOG.d(TAG, "screenWidth=" + screenWidth + " screenHeight=" + screenHeight);
		
		mBtnToolBarView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				Rect frame = new Rect();
				mBtnToolBarView.getWindowVisibleDisplayFrame(frame);
				int statusBarHeight = frame.top;
				mX = event.getRawX();
				mY = event.getRawY() - statusBarHeight;
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					mTouchX = event.getX();
					mTouchY = event.getY();
					mStartX = mX;
					mStartY = mY;
//					LOG.d(TAG, "ACTION_DOWN ,mTouchX=" + mTouchX + " mTouchY="
//							+ mTouchY);
					break;
				case MotionEvent.ACTION_MOVE:
//					LOG.d(TAG, "ACTION_DOWN ,mTouchX=" + mTouchX + " mTouchY="
//							+ mTouchY);
					refreshToolbarState(ToolbarState.move);
					updateViewPostion();
					break;
				case MotionEvent.ACTION_UP:
//					LOG.d(TAG, "ACTION_DOWN ,mTouchX=" + mTouchX + " mTouchY="
//							+ mTouchY);
					if((mX - mStartX) < 5 && (mY - mStartY) < 5 
							&& (mX - mStartX) > -5 && (mY - mStartY) > -5){
						onToolBarClick(v);
					}else{
						
						refreshToolbarState(ToolbarState.normal);
					}
					float left = mX - mTouchX;
					if(left <= screenWidth/2){//图标icon吸附在左边
						mX = mTouchX;
					}else{//图标icon吸附在右边
						mX = screenWidth;
					}
					updateViewPostion();
					//移动重点的坐标，重置为0
					mTouchX = mTouchY = 0;
					break;
				}
				return true;
			}
		});
	}
	
	
	private Timer mTimer;
	/**
	 * 根据不同的情况刷新图标的显示
	 */
	private void refreshToolbarState(ToolbarState state){
		LOG.d(TAG, "refreshToolbarState__:" + state);
		if(mToolbarState == ToolbarState.move && state == ToolbarState.move) return;
		
		mToolbarState = state;
		
		android.view.ViewGroup.LayoutParams layoutParams = mBtnToolBarView.getLayoutParams();
		layoutParams.width = UCommUtil.dip2px(mContext, 50);
		layoutParams.height = UCommUtil.dip2px(mContext, 50);
		
		switch (mToolbarState) {
		case mini:
			layoutParams.width = UCommUtil.dip2px(mContext, 30);
			layoutParams.height = UCommUtil.dip2px(mContext, 30);
			break;
		case normal://这个状态下。开始倒计时 图标变小
			if(mTimer != null){
				mTimer.cancel();
			}
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					((Activity) mContext).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							refreshToolbarState(ToolbarState.mini);
						}
					});
				}
			}, 2000);
			break;
		case move:
		case open:
			if(mTimer != null){
				mTimer.cancel();
			}
			break;
		}
		LOG.d(TAG, "toobar set layout  width:" + layoutParams.width + "   height__:" + layoutParams.height);
		mBtnToolBarView.setLayoutParams(layoutParams);
	}
	
	/**
	 * 点击悬浮窗
	 * @param v
	 */
	private void onToolBarClick(View v){
		Log.d(TAG, "点击悬浮窗");
		final Point point = new Point();
		mWindowManager.getDefaultDisplay().getSize(point);
		if(mToolLeftView.getVisibility() == View.VISIBLE 
				|| mToolRightView.getVisibility() == View.VISIBLE){
			mToolView.setBackgroundColor(0);
			mToolLeftView.setVisibility(View.GONE);
			mToolRightView.setVisibility(View.GONE);
			refreshToolbarState(ToolbarState.normal);
		}else{
			mToolView.setBackground(mResourceManager.getDrawable("ema_toolbar_bg"));
			if(mX > point.x / 2){
				mToolLeftView.setVisibility(View.VISIBLE);
				mToolView.setGravity(Gravity.RIGHT);
			}else{
				mToolRightView.setVisibility(View.VISIBLE);
				mToolView.setGravity(Gravity.LEFT);
			}
			refreshToolbarState(ToolbarState.open);
		}
	}
	  
	/**
	 * Click事件监听
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == getId("ema_btn_account") || id == getId("ema_btn_account_left")){
			toAccount();
		}else if(id == getId("ema_btn_gift") || id == getId("ema_btn_gift_left")){
			toGift();
		}else if(id == getId("ema_btn_help") || id == getId("ema_btn_help_left")){
			toHelp();
		}else if(id == getId("ema_btn_promotion") || id == getId("ema_btn_promotion_left")){
			toPromotion();
		}
	}
	
	/**
	 * 跳转到账号界面
	 */
	private void toAccount(){
		if(!Ema.getInstance().isLogin()){
			LOG.d(TAG, "未登录状态");
			ToastHelper.toast(mContext, "请先登录");
		}else{
			LOG.d(TAG, "跳转个人中心");
			Intent intent = new Intent(mContext, WebViewActivity.class);
			intent.putExtra(WebViewActivity.INTENT_TITLE, "账户中心");
			intent.putExtra(WebViewActivity.INTENT_URL, Url.getWebUrlUserInfo());
			intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_EMAACCOUNT);
			mContext.startActivity(intent);
			hideToolBar();
		}
	}
	
	/**
	 * 跳转礼包界面
	 */
	private void toGift(){
		/*if(!Ema.getInstance().isLogin()){
			LOG.d(TAG, "未登录状态");
			ToastHelper.toast(mContext, "请先登录");
		}else{
			LOG.d(TAG, "跳转礼包");
			Intent intent = new Intent(mContext, WebViewActivity.class);
			intent.putExtra(WebViewActivity.INTENT_TITLE, "礼包");
			intent.putExtra(WebViewActivity.INTENT_URL, Url.getWebUrlGift());
			intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_GIFT);
			mContext.startActivity(intent);
			hideToolBar();
		}*/
		ToastHelper.toast(mContext, "暂未开放");
	}
	
	/**
	 * 跳转推广界面
	 */
	private void toPromotion(){
		/*if(!Ema.getInstance().isLogin()){
			LOG.d(TAG, "未登录状态");
			ToastHelper.toast(mContext, "请先登录");
		}else{
			LOG.d(TAG, "跳转推广");
			Intent intent = new Intent(mContext, WebViewActivity.class);
			intent.putExtra(WebViewActivity.INTENT_TITLE, "推广");
			intent.putExtra(WebViewActivity.INTENT_URL, Url.getWebUrlPromotion());
			intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_PROMOTION);
			mContext.startActivity(intent);
			hideToolBar();
		}*/
		ToastHelper.toast(mContext, "暂未开放");
	}
	
	/**
	 * 跳转帮助界面
	 */
	private void toHelp(){
		/*if(!Ema.getInstance().isLogin()){
			LOG.d(TAG, "未登录状态");
			ToastHelper.toast(mContext, "请先登录");
		}else{
			LOG.d(TAG, "跳转帮助");
			Intent intent = new Intent(mContext, WebViewActivity.class);
			intent.putExtra(WebViewActivity.INTENT_TITLE, "帮助");
			intent.putExtra(WebViewActivity.INTENT_URL, Url.getWebUrlHelp());
			intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_HELP);
			mContext.startActivity(intent);
		}*/
		ToastHelper.toast(mContext, "暂未开放");
	}
	   
	/**
	 * 更新悬浮窗参数
	 */
	private void updateViewPostion(){
		if(mToolLeftView.getVisibility() == View.GONE 
				&& mToolRightView.getVisibility() == View.GONE){
			mWindowManagerParams.x = (int) (mX - mTouchX);
			mWindowManagerParams.y = (int) (mY - mTouchY);
			Log.d(TAG, "wp.x=" + mWindowManagerParams.x + " wp.y="+ mWindowManagerParams.y);
			mWindowManager.updateViewLayout(mToolView, mWindowManagerParams);
		}
	}
	
	/**
	 * 设置悬浮窗参数
	 */
	private void createWindowParams(){
		mWindowManagerParams = new WindowManager.LayoutParams();
		//设置相关的窗口布局参数(悬浮窗口效果)
		mWindowManagerParams.type = 2007;
		mWindowManagerParams.format = 1;// 设置图片格式，效果为背景透明
		//设置window flag  不影响后面的事件 和 不可聚焦
		/*
		 * 注意，flag的值可以为： LayoutParams.FLAG_NOT_TOUCH_MODAL 不影响后面的事件
		 * LayoutParams.FLAG_NOT_FOCUSABLE 不可聚焦 LayoutParams.FLAG_NOT_TOUCHABLE
		 * 不可触摸
		 */
		mWindowManagerParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		//调整悬浮窗口至左上角，便于调整坐标
		mWindowManagerParams.gravity = Gravity.LEFT | Gravity.TOP;
		//已品目左上角为原点，设置 x，y的初始值
		mWindowManagerParams.x = 0;
		mWindowManagerParams.y = 80;
		//设置悬浮窗口的长宽
		mWindowManagerParams.width = LayoutParams.WRAP_CONTENT;
		mWindowManagerParams.height = LayoutParams.WRAP_CONTENT;
	}
	
	/**
	 * 获取资源ID(类型为 "id"的才行)
	 * @param key
	 * @return
	 */
	private int getId(String key){
		if(mIDMap == null){
			mIDMap = new HashMap<String, Integer>();
		}
		if(!mIDMap.containsKey(key)){
			mIDMap.put(key, mResourceManager.getIdentifier(key, "id"));
		}
		return mIDMap.get(key);
	}
	
}
