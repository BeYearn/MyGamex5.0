package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PayMabiActivity extends Activity implements OnClickListener {

    private static final String TAG = "PayMabiActivity";

    public static final int CODE_SET_LIMIT_SUCC = 11;//密码验证成功

    private static final int CODE_PAY_SUCC = 0;//支付成功
    private static final int CODE_REFRESH_INFO = 10;//刷新信息显示

    private EmaUser mEmaUser;
    private ConfigManager mConfigManager;
    private ResourceManager mResourceManager;

    //views
    private Button mBtnBack;//返回按钮
    private ImageView mBtnReturnGame;//返回游戏
    private TextView mTxtProductName;//商品名称
    private TextView mTxtProductNumber;//商品数量
    private TextView mTxtTotalPrice;//商品总价
    private TextView mTxtAccount;//账户
    private TextView mTxtBalance;//余额
    private Button mBtnChongZhi;//充值按钮
    private TextView mTxtPayLimit;//支付限额
    private Button mBtnChangeLimit;//修改支付限额
    private EditText mEdtPasswView;//密码输入框
    private Button mBtnFindPassw;//忘记密码
    private Button mBtnStartPay;//支付

    private Map<String, Integer> mIDmap;

    private EmaReceiver mRechargeSuccReceiver;

    private EmaPayInfo mPayInfo;

    // 进度条
    private EmaProgressDialog mProgress;
    private EmaDialogSetPayPassw mSetPayPasswDialog;//设置密码对话框

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case PayConst.CODE_SET_PASSW_CANCEL://退出设置密码
                    dismissSetPasswDialog();
                    PayMabiActivity.this.finish();
                    break;
                case PayConst.CODE_SET_PASSW_FAILED://设置密码失败
                    dismissSetPasswDialog();
                    ToastHelper.toast(PayMabiActivity.this, "密码设置失败");
                    break;
                case PayConst.CODE_SET_PASSW_SUCC://设置密码成功
                    dismissSetPasswDialog();
                    ToastHelper.toast(PayMabiActivity.this, "密码设置成功");
                    break;
                case EmaProgressDialog.CODE_LOADING_END://进度条结束显示
                    mProgress.closeProgress();
                    break;
                case EmaProgressDialog.CODE_LOADING_START://开始显示进度条
                    mProgress.showProgress((String) msg.obj, false, false);
                    break;
                case CODE_SET_LIMIT_SUCC://设置支付额度成功
                    //mTxtPayLimit.setText(mEmaUser.getPayLimitPricebean().getPriceYuan() + "");
                    break;
                //--------------pay result -------
                case CODE_PAY_SUCC:
                    PayMabiActivity.this.finish();
                    ToolBar.getInstance(Ema.getInstance().getContext()).showToolBar();
                    break;
                case CODE_REFRESH_INFO:
                    setViews();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //将activity添加到列表，方便管理
        EmaPayProcessManager.getInstance().addPayActivity(this);

        mEmaUser = EmaUser.getInstance();
        mConfigManager = ConfigManager.getInstance(this);
        mResourceManager = ResourceManager.getInstance(this);
        mProgress = new EmaProgressDialog(this);

        //获得订单信息
        Intent intent = this.getIntent();
        mPayInfo = intent.getParcelableExtra("payInfo");

        registerBroadcast();

        initView();
        initData();
    }

    /**
     * 注册广播
     */
    private void registerBroadcast() {
        mRechargeSuccReceiver = new EmaReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                LOG.d(TAG, "");
                refreshWalletInfo();
            }
        };
        //注册充值成功的广播
        registerReceiver(mRechargeSuccReceiver, new IntentFilter(PropertyField.BROADCAST_RECHARGE_SUCC));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRechargeSuccReceiver != null) {
            unregisterReceiver(mRechargeSuccReceiver);
        }
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setContentView(mResourceManager.getIdentifier("ema_pay_mabi", "layout"));
        mBtnBack = (Button) findViewById(getId("ema_pay_mabi_back"));
        mBtnChangeLimit = (Button) findViewById(getId("ema_pay_mabi_pay_btn_limit_change"));
        mBtnChongZhi = (Button) findViewById(getId("ema_pay_mabi_congzhi"));
        mBtnFindPassw = (Button) findViewById(getId("ema_pay_mabi_find_passwd"));
        mBtnReturnGame = (ImageView) findViewById(getId("ema_pay_mabi_imageView_return"));
        mBtnStartPay = (Button) findViewById(getId("ema_pay_mabi_btn_pay"));

        mTxtProductName = (TextView) findViewById(getId("ema_pay_mabi_goodsName"));
        mTxtProductNumber = (TextView) findViewById(getId("ema_pay_mabi_GoodsNum"));
        mTxtTotalPrice = (TextView) findViewById(getId("ema_pay_mabi_totalPrice"));
        mTxtAccount = (TextView) findViewById(getId("ema_pay_mabi_account"));
        mTxtBalance = (TextView) findViewById(getId("ema_pay_mabi_balance_text"));
        mTxtPayLimit = (TextView) findViewById(getId("ema_pay_mabi_pay_limit"));
        mEdtPasswView = (EditText) findViewById(getId("ema_pay_mabi_edt_pay_passw"));

        mEdtPasswView.setKeyListener(new NumberKeyListener() {
            @Override
            public int getInputType() {
                return android.text.InputType.TYPE_CLASS_TEXT;
            }

            @Override
            protected char[] getAcceptedChars() {
                return PropertyField.PASSW_DIGITS;
            }
        });

        mBtnBack.setOnClickListener(this);
        mBtnChangeLimit.setOnClickListener(this);
        mBtnChongZhi.setOnClickListener(this);
        mBtnFindPassw.setOnClickListener(this);
        mBtnReturnGame.setOnClickListener(this);
        mBtnStartPay.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        setViews();

        //没有设置密码需要显示对话框输入支付密码
        if (!mEmaUser.getIsWalletHasPassw()) {
            mSetPayPasswDialog = new EmaDialogSetPayPassw(this, mHandler);
            mSetPayPasswDialog.show();
        }
    }

    /**
     * 设置控件的值
     */
    private void setViews() {
        mTxtProductName.setText(mPayInfo.getProductName());
        mTxtProductNumber.setText(mPayInfo.getProductNum());
        mTxtTotalPrice.setText(mPayInfo.getPrice() + "");
        mTxtAccount.setText(mEmaUser.getNickName());
    }

    /**
     * Click事件监听
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getId("ema_pay_mabi_back")) {//返回
            this.finish();
        } else if (id == getId("ema_pay_mabi_imageView_return")) {//返回游戏
            new EmaDialogPayPromptCancel(this).show();
        } else if (id == getId("ema_pay_mabi_pay_btn_limit_change")) {//修改支付限额
            doChangePayLimit();
        } else if (id == getId("ema_pay_mabi_congzhi")) {//充值
            doRecharge();
        } else if (id == getId("ema_pay_mabi_find_passwd")) {//忘记密码
            doFindPassw();
        } else if (id == getId("ema_pay_mabi_btn_pay")) {//支付
            doPay();
        }
    }

    /**
     * 监听返回按钮
     */
    @Override
    public void onBackPressed() {
        new EmaDialogPayPromptCancel(this).show();
    }

    /**
     * 修改支付额度完成后的回调
     *
     * @param msg
     */
    protected void onPayLimitCallBack(Message msg) {
        mHandler.sendMessage(msg);
    }

    /**
     * 从服务器获取最新的钱包信息
     */
    private void refreshWalletInfo() {
        // 重新从服务器获取余额
        /*PayUtil.getWalletSetting(mConfigManager.getAppId(), mEmaUser.getAccessSid(),
                mEmaUser.getUUID(), mConfigManager.getAppKEY(), new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        try {
                            JSONObject json = new JSONObject(result);
                            int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
                            switch (resultCode) {
                                case HttpInvokerConst.SDK_RESULT_SUCCESS:
                                    LOG.d(TAG, "获取钱包信息成功！");
                                    //mEmaUser.setPayLimit(json.getInt("pay_limit"));
                                    //mEmaUser.setIsWalletHasPassw(json.getString("is_wallet_pwd"));
                                    mHandler.sendEmptyMessage(CODE_REFRESH_INFO);
                                    break;
                                case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR://签名验证失败
                                    LOG.d(TAG, "签名验证失败");
                                default:
                                    LOG.w(TAG, "获取钱包信息失败");
                                    UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "获取钱包信息失败");
                                    break;
                            }
                        } catch (Exception e) {
                            LOG.w(TAG, "onActivityResult get wallet setting error", e);
                        }
                    }
                });*/
    }

    /**
     * 找回密码
     */
    private void doFindPassw() {
        LOG.d(TAG, "找回密码");
        Intent intent = new Intent(PayMabiActivity.this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_TITLE, "修改密码");
        intent.putExtra(WebViewActivity.INTENT_URL, Url.getWebUrlFindpasswWallet());
        intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_FIND_WALLET_PASSW);
        intent.putExtra(WebViewActivity.INTENT_INFORGAME, false);
        PayMabiActivity.this.startActivity(intent);
    }


    /**
     * 充值
     */
    private void doRecharge() {
        LOG.d(TAG, "充值...");
        Intent intent = new Intent(this, RechargeMabiActivity.class);
        startActivityForResult(intent, 0);
    }

    /**
     * 修改支付限额
     */
    private void doChangePayLimit() {
        LOG.d(TAG, "修改支付限额");
        new SetPayLimit(this).showPasswCheckDialog();
    }

    /**
     * 钱包支付，余额足够的情况下，显现钱包页面，输入钱包密码支付；
     */
    private void doPay() {
        String passw = mEdtPasswView.getText().toString();
        if (UCommUtil.isStrEmpty(passw)) {
            LOG.d(TAG, "输入密码为空");
            ToastHelper.toast(this, PropertyField.ERROR_PASSW_CAN_NOT_NULL);
            return;
        }
        if (passw.length() < 6 || passw.length() > 16) {
            LOG.d(TAG, "密码长度不符合(6-16位):" + passw.length());
            ToastHelper.toast(this, PropertyField.ERROR_PASSW_INCORRECT_LENGTH);
            return;
        }

        LOG.d(TAG, "支付中...");
        mProgress.showProgress("支付中", false, false);

        Map<String, String> params = new HashMap<>();
        params.put("orderId", mPayInfo.getOrderId());
        params.put("chargePwd", passw);
        params.put("token", mEmaUser.getToken());
        new HttpInvoker().postAsync(Url.getWalletPayUrl(), params, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
                try {
                    JSONObject json = new JSONObject(result);
                    int resultCode = json.getInt("status");

                    ToastHelper.toast(PayMabiActivity.this, json.getString("message"));
                    switch (resultCode) {
                        case HttpInvokerConst.SDK_RESULT_SUCCESS://支付成功,目前只知道这个case（0）和下面（1）这两种情况
                            LOG.d(TAG, "支付成功");
                            mHandler.sendEmptyMessage(CODE_PAY_SUCC);
                            UCommUtil.makePayCallBack(EmaCallBackConst.PAYSUCCESS, "支付成功");
                            break;
                        case HttpInvokerConst.SDK_RESULT_FAILED:
                            LOG.d(TAG, "支付失败");
                            UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, json.getString("message"));
                            break;
                        default:
                            LOG.d(TAG, "支付失败，失败原因未知");
                            ToastHelper.toast(PayMabiActivity.this, "支付失败");
                            UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "支付失败");
                            break;
                    }
                } catch (Exception e) {
                    LOG.w(TAG, "doPay error", e);
                    ToastHelper.toast(PayMabiActivity.this, "支付失败，可能为网络原因");
                    UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "支付失败");
                }
            }
        });
    }

    /**
     * 钱包支付：用户不可感知的，充值完后进行的钱包支付（无需密码！！）
     */
    public static void doPayNoKeyWord(EmaPayInfo payInfo){
        LOG.e(TAG, "充值成功后，继续支付中...");
        Map<String, String> params = new HashMap<>();
        params.put("orderId", payInfo.getOrderId());
        params.put("token", EmaUser.getInstance().getToken());
        new HttpInvoker().postAsync(Url.getWalletPayNoPswUrl(), params, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {
                    JSONObject json = new JSONObject(result);
                    int resultCode = json.getInt("status");
                    switch (resultCode) {
                        case HttpInvokerConst.SDK_RESULT_SUCCESS://支付成功,目前只知道这个case（0）和下面（1）这两种情况
                            LOG.d(TAG, "支付成功");
                            ToolBar.getInstance(Ema.getInstance().getContext()).showToolBar();
                            UCommUtil.makePayCallBack(EmaCallBackConst.PAYSUCCESS, "支付成功");
                            break;
                        case HttpInvokerConst.SDK_RESULT_FAILED:
                            LOG.d(TAG, "支付失败");
                            ToolBar.getInstance(Ema.getInstance().getContext()).showToolBar();
                            UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, json.getString("message"));
                            break;
                        default:
                            LOG.d(TAG, "支付失败，失败原因未知");
                            UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "支付失败");
                            break;
                    }
                } catch (Exception e) {
                    LOG.w(TAG, "doPay error", e);
                    UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "支付失败");
                }
            }
        });
    }

    /**
     * 关闭设置支付密码对话框
     */
    private void dismissSetPasswDialog() {
        if (mSetPayPasswDialog != null) {
            mSetPayPasswDialog.closeProgress();
            mSetPayPasswDialog.dismiss();
        }
    }

    /**
     * 方法目的（为了防止重复去获取资源ID）
     *
     * @param key
     * @return
     */
    private int getId(String key) {
        if (mIDmap == null) {
            mIDmap = new HashMap<String, Integer>();
        }
        if (!mIDmap.containsKey(key)) {
            mIDmap.put(key, mResourceManager.getIdentifier(key, "id"));
        }
        return mIDmap.get(key);
    }


    @Override
    protected void onStop() {
        ToolBar.getInstance(Ema.getInstance().getContext()).showToolBar();
        super.onStop();
    }
}
