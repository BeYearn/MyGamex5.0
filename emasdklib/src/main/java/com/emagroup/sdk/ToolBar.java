package com.emagroup.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
public class ToolBar implements OnClickListener {

    private boolean isCanShow;  // 1可以显示  0的话toobar失效
    private ArrayList<BarInfo> barInfoList;

    public enum ToolbarState {
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
    private Button mToolBarBtn1;
    private Button mToolBarBtn2;
    private Button mToolBarBtn3;
    private Button mToolBarBtn4;
    private Button mToolBarBtn5;

    private LinearLayout mToolLeftView;//悬浮窗图标 左边的布局
    private Button mToolBarBtn1Left;
    private Button mToolBarBtn2Left;
    private Button mToolBarBtn3Left;
    private Button mToolBarBtn4Left;
    private Button mToolBarBtn5Left;

    //标记
    private boolean mFlagIsShowing;

    private Map<String, Integer> mIDMap;

    class BarInfo{
        String id;
        String name;
        String icon;
        String url;
    }


    private ToolBar(Context context) {
        mContext = context;
        mResourceManager = ResourceManager.getInstance(mContext);
        mFlagIsShowing = false;

        initToolbar();
    }

    public static ToolBar getInstance(Context context) {
        if (mInstance == null) {
            synchronized (synchron) {
                if (mInstance == null) {
                    mInstance = new ToolBar(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 显示悬浮窗
     */
    public void showToolBar() {

        if(!isCanShow){
            return;
        }

        ((Activity) Ema.getInstance().getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LOG.d(TAG, "isShowing_:" + mFlagIsShowing);
                if (mFlagIsShowing == true) {
                    return;
                }
                if (mWindowManager == null) {
                    mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                }
                if (mToolView == null) {
                    initToolbarView();
                }
                if (mWindowManagerParams == null) {
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
    public void hideToolBar() {
        if (mFlagIsShowing == true) {
            mFlagIsShowing = false;
            ((Activity) Ema.getInstance().getContext()).runOnUiThread(new Runnable() {
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
     *
     * @return
     */
    public boolean isToolbarShowing() {
        return mFlagIsShowing;
    }

    /**
     * 初始化悬浮窗，只在第一次获取ToolBar的时候进行
     *
     */
    private void initToolbar() {
        mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        initToolbarView();
        createWindowParams();
    }

    /**
     * 初始化界面
     */
    private void initToolbarView() {

        //获取bar的显示信息
        String toolBarInfoStr = (String) USharedPerUtil.getParam(mContext, "menuBarInfo", "");
        barInfoList = new ArrayList<>();
        try {
            if(TextUtils.isEmpty(toolBarInfoStr)){
                toolBarInfoStr=null;
            }
            JSONObject toolBarInfo = new JSONObject(toolBarInfoStr);

            //boolean canShow1 = toolBarInfo.getInt("show_float") == 1;
            String hide_channel_id = toolBarInfo.getString("hide_channel_id");
            String[] split = hide_channel_id.split("|");
            boolean canShow2=true;
            for (String s:split) {
                if(s.contains(EmaSDK.getInstance().getChannelId())){
                    canShow2=false;
                }
            }
            isCanShow=canShow2;

            JSONArray details = toolBarInfo.getJSONArray("details");
            int detailsLen=details.length();
            for (int i=0;i<detailsLen;i++){
                JSONObject barObject = details.getJSONObject(i);
                int type = barObject.getInt("type");
                if(type==0||type==1){  //0通用 1android
                    BarInfo barInfo = new BarInfo();
                    barInfo.id=barObject.getString("id");
                    barInfo.name=barObject.getString("name");
                    barInfo.icon=barObject.getString("icon");
                    barInfo.url=barObject.getString("url");
                    barInfoList.add(barInfo);
                }
            }

        } catch (JSONException e) {
            isCanShow=false;
            e.printStackTrace();
        }



        mToolView = (LinearLayout) LayoutInflater.from(mContext).inflate(
                mResourceManager.getIdentifier("ema_floating", "layout"), null);

        mBtnToolBarView = (ImageView) mToolView.findViewById(getId("ema_img_toolbar"));

        mToolRightView = (LinearLayout) mToolView.findViewById(getId("ema_toolbar_right"));
        mToolLeftView = (LinearLayout) mToolView.findViewById(getId("ema_toolbar_left"));

        mToolBarBtn1 = (Button) mToolView.findViewById(getId("ema_btn_1"));
        mToolBarBtn1Left = (Button) mToolView.findViewById(getId("ema_btn_1_left"));
        mToolBarBtn2 = (Button) mToolView.findViewById(getId("ema_btn_2"));
        mToolBarBtn2Left = (Button) mToolView.findViewById(getId("ema_btn_2_left"));
        mToolBarBtn3 = (Button) mToolView.findViewById(getId("ema_btn_3"));
        mToolBarBtn3Left = (Button) mToolView.findViewById(getId("ema_btn_3_left"));
        mToolBarBtn4 = (Button) mToolView.findViewById(getId("ema_btn_4"));
        mToolBarBtn4Left = (Button) mToolView.findViewById(getId("ema_btn_4_left"));
        mToolBarBtn5 = (Button) mToolView.findViewById(getId("ema_btn_5"));
        mToolBarBtn5Left = (Button) mToolView.findViewById(getId("ema_btn_5_left"));

        mToolBarBtn1.setOnClickListener(this);
        mToolBarBtn1Left.setOnClickListener(this);
        mToolBarBtn2.setOnClickListener(this);
        mToolBarBtn2Left.setOnClickListener(this);
        mToolBarBtn3.setOnClickListener(this);
        mToolBarBtn3Left.setOnClickListener(this);
        mToolBarBtn4.setOnClickListener(this);
        mToolBarBtn4Left.setOnClickListener(this);
        mToolBarBtn5.setOnClickListener(this);
        mToolBarBtn5Left.setOnClickListener(this);

        ArrayList<Button> buttonList = new ArrayList<>();
        buttonList.add(mToolBarBtn1);
        buttonList.add(mToolBarBtn1Left);
        buttonList.add(mToolBarBtn2);
        buttonList.add(mToolBarBtn2Left);
        buttonList.add(mToolBarBtn3);
        buttonList.add(mToolBarBtn3Left);
        buttonList.add(mToolBarBtn4);
        buttonList.add(mToolBarBtn4Left);
        buttonList.add(mToolBarBtn5);
        buttonList.add(mToolBarBtn5Left);

        //将barinfo设置到btn上
        for (int i=0;i<barInfoList.size();i++){
            BarInfo barInfo = barInfoList.get(i);

            buttonList.get(2*i).setVisibility(View.VISIBLE);
            buttonList.get(2*i+1).setVisibility(View.VISIBLE);

            buttonList.get(2*i).setText(barInfo.name);
            buttonList.get(2*i+1).setText(barInfo.name);
            if(barInfo.icon.contains("http")){
                new MyImageLoad().setPicture(barInfo.icon,buttonList.get(2*i));
                new MyImageLoad().setPicture(barInfo.icon,buttonList.get(2*i+1));
            }else {
                int drawable = mResourceManager.getIdentifier(barInfo.icon, "drawable");
                buttonList.get(2*i).setCompoundDrawablesRelativeWithIntrinsicBounds(0,drawable,0,0);
                buttonList.get(2*i+1).setCompoundDrawablesRelativeWithIntrinsicBounds(0,drawable,0,0);
            }
        }




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
                switch (event.getAction()) {
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
                        if ((mX - mStartX) < 5 && (mY - mStartY) < 5
                                && (mX - mStartX) > -5 && (mY - mStartY) > -5) {
                            onToolBarClick(v);
                        } else {

                            refreshToolbarState(ToolbarState.normal);
                        }
                        float left = mX - mTouchX;
                        if (left <= screenWidth / 2) {//图标icon吸附在左边
                            mX = mTouchX;
                        } else {//图标icon吸附在右边
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
    private void refreshToolbarState(ToolbarState state) {
        LOG.d(TAG, "refreshToolbarState__:" + state);
        if (mToolbarState == ToolbarState.move && state == ToolbarState.move) return;

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
                if (mTimer != null) {
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
                if (mTimer != null) {
                    mTimer.cancel();
                }
                break;
        }
        LOG.d(TAG, "toobar set layout  width:" + layoutParams.width + "   height__:" + layoutParams.height);
        mBtnToolBarView.setLayoutParams(layoutParams);
    }

    /**
     * 点击悬浮窗
     *
     * @param v
     */
    private void onToolBarClick(View v) {
        Log.d(TAG, "点击悬浮窗");
        final Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        if (mToolLeftView.getVisibility() == View.VISIBLE
                || mToolRightView.getVisibility() == View.VISIBLE) {
            mToolView.setBackgroundColor(0);
            mToolLeftView.setVisibility(View.GONE);
            mToolRightView.setVisibility(View.GONE);
            refreshToolbarState(ToolbarState.normal);
        } else {
            mToolView.setBackground(mResourceManager.getDrawable("ema_toolbar_bg"));
            if (mX > point.x / 2) {
                mToolLeftView.setVisibility(View.VISIBLE);
                mToolView.setGravity(Gravity.RIGHT);
            } else {
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
        if (id == getId("ema_btn_1") || id == getId("ema_btn_1_left")) {
            //toAccount();
            doToolBarClick(barInfoList.get(0));
        } else if (id == getId("ema_btn_2") || id == getId("ema_btn_2_left")) {
            //toGift();
            doToolBarClick(barInfoList.get(1));
        } else if (id == getId("ema_btn_3") || id == getId("ema_btn_3_left")) {
            //toHelp();
            doToolBarClick(barInfoList.get(2));
        } else if (id == getId("ema_btn_4") || id == getId("ema_btn_4_left")) {
            //toPromotion();
            doToolBarClick(barInfoList.get(3));
        } else if (id == getId("ema_btn_5") || id == getId("ema_btn_5_left")) {
            //toRecharge();
            doToolBarClick(barInfoList.get(4));   // 后面的如果barInfoList里面没有这么多，前面就不会让它显示，不必担心nullpoint
        }
    }

    public  void doToolBarClick(BarInfo barInfo){
        if(!TextUtils.isEmpty(barInfo.url)){
            toWebView(barInfo.name,barInfo.url);
        }else {
            ToastHelper.toast(mContext,"暂未开放");
        }
    }

    /**
     * 跳转webview页面
     * @param tab 名字
     * @param url webview的url
     */
    private void toWebView(String tab,String url){
        if (!EmaUser.getInstance().getIsLogin()) {
            LOG.d(TAG, "未登录状态");
            ToastHelper.toast(mContext, "请先登录");
        }else if(url.contains("charge")){  // 特殊 android专有,该url是假的
          toRecharge();
        } else {
            LOG.d(TAG, "跳转"+tab);
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra(WebViewActivity.INTENT_TITLE, tab);
            intent.putExtra(WebViewActivity.INTENT_URL, url);
            intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_EMAACCOUNT);
            mContext.startActivity(intent);
            hideToolBar();
        }
    }

    /**
     * 充值
     */
    private void toRecharge() {
        LOG.d(TAG, "跳转充值页面...");
        Intent intent = new Intent(mContext, RechargeMabiActivity.class);
        mContext.startActivity(intent);
        hideToolBar();
    }

    /**
     * 跳转到账号界面
     */
    private void toAccount() {
        if (!EmaUser.getInstance().getIsLogin()) {
            LOG.d(TAG, "未登录状态");
            ToastHelper.toast(mContext, "请先登录");
        } else {
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
    private void toGift() {
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
    private void toPromotion() {
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
    private void toHelp() {
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
    private void updateViewPostion() {
        if (mToolLeftView.getVisibility() == View.GONE
                && mToolRightView.getVisibility() == View.GONE) {
            mWindowManagerParams.x = (int) (mX - mTouchX);
            mWindowManagerParams.y = (int) (mY - mTouchY);
            Log.d(TAG, "wp.x=" + mWindowManagerParams.x + " wp.y=" + mWindowManagerParams.y);
            mWindowManager.updateViewLayout(mToolView, mWindowManagerParams);
        }
    }

    /**
     * 设置悬浮窗参数
     */
    private void createWindowParams() {
        mWindowManagerParams = new WindowManager.LayoutParams();
        //设置相关的窗口布局参数(悬浮窗口效果)
        mWindowManagerParams.type =  WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
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
     *
     * @param key
     * @return
     */
    private int getId(String key) {
        if (mIDMap == null) {
            mIDMap = new HashMap<String, Integer>();
        }
        if (!mIDMap.containsKey(key)) {
            mIDMap.put(key, mResourceManager.getIdentifier(key, "id"));
        }
        return mIDMap.get(key);
    }

}
