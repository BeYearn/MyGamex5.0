package com.emagroup.sdk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

public class EmaProgressDialog {

    public static final int CODE_LOADING_START = -100;//显示进度条
    public static final int CODE_LOADING_END = -101;//关闭进度条

    private Context mContext;
    private ProgressDialog mProgress;

    public EmaProgressDialog(Context context) {
        mContext = context;
    }

    /**
     * 显示进度条(默认可以返回退出进度条，不能点击外面区域退出)
     */
    public void showProgress(String msg) {
        showProgress(msg, true, false);
    }

    /**
     * 显示进度条
     *
     * @param msg
     * @param cancelable
     * @param outsideCancelAble
     */
    public void showProgress(final String msg, final boolean cancelable, final boolean outsideCancelAble) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mProgress == null) {
                    mProgress = new ProgressDialog(mContext);
                    mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    //mProgress.setTitle("提示");
                    Log.e("new showpro",mContext.toString());
                }
                mProgress.setCancelable(cancelable);
                mProgress.setCanceledOnTouchOutside(outsideCancelAble);
                mProgress.setMessage(msg);
                mProgress.show();

            }
        });
    }

    /**
     * 关闭进度条
     */
    public void closeProgress() {
        if (mProgress != null && mProgress.isShowing()) {
            mProgress.dismiss();
        }
    }

}
