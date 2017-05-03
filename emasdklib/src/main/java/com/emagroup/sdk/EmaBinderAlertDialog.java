package com.emagroup.sdk;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EmaBinderAlertDialog extends Dialog {


    public static final int WEAK_ALERT = 21;
    public static final int IDENTIFY_ALERT = 22;

    private final Context mContext;
    private final Ema.BindRemind mBindRemind;
    private final int mAlertType; // 标记是绑定弱账户还是实名认证
    private ResourceManager mResourceManager;// 资源管理

    //views
    private TextView mTxtPromptView;//提示语
    private ImageView mImgPromptView;//提示图片
    private TextView mBtnSure;
    private TextView mBtnCancle;
    private int mIdentifyLv;

    /**
     * @param context
     * @param bindRemind
     * @param type
     */
    public EmaBinderAlertDialog(Context context, Ema.BindRemind bindRemind, int type) {
        super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
        this.mContext = context;
        this.mBindRemind = bindRemind;
        this.mAlertType = type;
        mResourceManager = ResourceManager.getInstance(context);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIdentifyLv = (int) USharedPerUtil.getParam(mContext, EmaConst.IDENTIFY_LV, 0);

        initView();
        initData();
    }

    private void initView() {
        setContentView(mResourceManager.getIdentifier("ema_bind_alert_dialog", "layout"));

        mTxtPromptView = (TextView) findViewById(mResourceManager.getIdentifier("ema_txt_content", "id"));//dialog显示的内容

        mBtnSure = (TextView) findViewById(mResourceManager.getIdentifier("ema_tv_sure", "id"));
        mBtnCancle = (TextView) findViewById(mResourceManager.getIdentifier("ema_tv_cancel", "id"));

        mBtnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Ema.getInstance().getContext(), WebViewActivity.class);

                if (mAlertType == WEAK_ALERT) {
                    intent.putExtra(WebViewActivity.INTENT_TITLE, "绑定账号");
                    intent.putExtra(WebViewActivity.INTENT_URL, Url.getWebUrlBinder());
                    intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_BIND);
                } else if (mAlertType == IDENTIFY_ALERT) {
                    intent.putExtra(WebViewActivity.INTENT_TITLE, "实名认证");
                    intent.putExtra(WebViewActivity.INTENT_URL, Url.getWebUrlIdentity());
                    intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_IDENTIFY);
                }

                mContext.startActivity(intent);
                EmaBinderAlertDialog.this.dismiss();
            }
        });

        mBtnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAlertType == WEAK_ALERT) {
                    if (null != mBindRemind) {
                        mBindRemind.canelNext();
                    }
                } else if (mAlertType == IDENTIFY_ALERT) {
                    if (mIdentifyLv != 2 && null != mBindRemind) {
                        mBindRemind.canelNext();
                    }
                }
                EmaBinderAlertDialog.this.dismiss();
            }
        });

        mBtnSure.setVisibility(View.VISIBLE);
        mBtnCancle.setVisibility(View.VISIBLE);

    }

    private void initData() {
        if (mAlertType == WEAK_ALERT) {
            mTxtPromptView.setText("    您登录了游客账户，为了您的账户安全，避免数据丢失，请尽快绑定手机。");
        } else if (mAlertType == IDENTIFY_ALERT) {
            mTxtPromptView.setText("尊敬的用户:\n" +
                    "    根据国家规定,游戏用户需进行实名认证.\n" +
                    "-  信息仅用于认证且绝对保密\n" +
                    "-  未成年人不允许在游戏内支付\n" +
                    "-  认证信息可用于帐号找回");
            if (mIdentifyLv == 2) {   //2强制认证
                mBtnCancle.setText("关闭");    // 默认是下次再说
            }
        }
    }


}
