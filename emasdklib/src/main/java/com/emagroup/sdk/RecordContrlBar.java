package com.emagroup.sdk;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import static com.emagroup.sdk.ActivityManager.synchron;

/**
 * Created by Administrator on 2017/2/10.
 */

public class RecordContrlBar {


    private static RecordContrlBar mInstance;
    private RecordScreenService mrecordScreenService;
    private ResourceManager mResourceManager;
    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowManagerParams;
    private float x ;
    private float y;
    private LinearLayout mControlView;
    private float mStartX;
    private float mStartY;

    private RecordContrlBar(Context context, RecordScreenService recordScreenService) {
        mContext = context;
        mrecordScreenService=recordScreenService;
        mResourceManager = ResourceManager.getInstance(context);
        initToolbar();
    }

    public static RecordContrlBar getInstance(Context context, RecordScreenService recordScreenService) {
        if (mInstance == null) {
            synchronized (synchron) {
                if (mInstance == null) {
                    mInstance = new RecordContrlBar(context,recordScreenService);
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化悬浮窗，只在第一次获取ToolBar的时候进行
     *
     */
    private void initToolbar() {

        mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        createWindowParams();

        creatBarView();
    }

    private void creatBarView() {
        mControlView = (LinearLayout) LayoutInflater.from(mContext).inflate(mResourceManager.getIdentifier("ema_record_control_floating", "layout"), null);

        mControlView.setOnTouchListener(new View.OnTouchListener() {
            float mTouchX;
            float mTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Rect frame = new Rect();
                mControlView.getWindowVisibleDisplayFrame(frame);
                int statusBarHeight = frame.top;

                // 获取相对屏幕的坐标，即以屏幕左上角为原点
                x = event.getRawX();
                y = event.getRawY() - statusBarHeight;
                Log.i("startP", "startX" + mTouchX + "====startY" + mTouchY);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 获取相对View的坐标，即以此View左上角为原点
                        mTouchX = event.getX();
                        mTouchY = event.getY();

                        mStartX=x;
                        mStartY=y;
                       /* long end = System.currentTimeMillis() - startTime;
                        // 双击的间隔在 300ms以下
                        if (end < 300) {
                            closeDesk();
                        }
                        startTime = System.currentTimeMillis();*/
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 更新浮动窗口位置参数
                        mWindowManagerParams.x = (int) (x - mTouchX);
                        mWindowManagerParams.y = (int) (y - mTouchY);
                        mWindowManager.updateViewLayout(v, mWindowManagerParams);
                        break;
                    case MotionEvent.ACTION_UP:

                        if((x - mStartX) < 5 && (y - mStartY) < 5 && (x - mStartX) > -5 && (y - mStartY) > -5){
                            doBarClick();
                        }
                        // 更新浮动窗口位置参数
                        mWindowManagerParams.x = (int) (x - mTouchX);
                        mWindowManagerParams.y = (int) (y - mTouchY);
                        mWindowManager.updateViewLayout(v, mWindowManagerParams);

                        // 可以在此记录最后一次的位置

                        mTouchX = mTouchY = 0;
                        break;
                }
                return true;
            }
        });
    }

    //点击bar后的效果
    private void doBarClick() {
        closeControlBar();
        mrecordScreenService.stopRecord();
    }

    /**
     * 显示
     */
    public void showControlBar() {
        mWindowManager.addView(mControlView, mWindowManagerParams);
    }

    /**
     * 关闭
     */
    public void closeControlBar() {
        mWindowManager.removeView(mControlView);
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
        mWindowManagerParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowManagerParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }
}
