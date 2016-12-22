package com.emagroup.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.api.IOpenApiListener;
import com.tencent.mobileqq.openpay.api.OpenApiFactory;
import com.tencent.mobileqq.openpay.data.base.BaseResponse;
import com.tencent.mobileqq.openpay.data.pay.PayResponse;

import org.json.JSONObject;

import java.util.List;

public class PayTrdActivity extends Activity implements OnClickListener, IOpenApiListener {

    private static final String TAG = "PayTrdActivity";

    public static final int PAY_ACTIVITY_CLOSE=11;   //本页面关闭
    public static final int PAY_ACTIVITY_DIALOG_CANLE=12;   //本页面弹窗 支付取消
    public static final int PAY_ACTIVITY_DIALOG_FAIL=13;   //本页面弹窗  支付失败

    private EmaUser mEmaUser;
    private ResourceManager mResourceManager;

    //views
    private ImageView mBtnBackToGameView;
    private TextView mTxtTotalPrice;
    private TextView mTxtUserName;
    private TextView mTxtProductName;
    private GridView mGridView;

    private PayTrdListAdapter mAdapter;

    private EmaPayInfo mPayInfo;

    //保存按钮的资源ID
    private int mIDBtnBackToGameView;

    // 进度条
    private EmaProgressDialog mProgress;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PayConst.CODE_PAY_GET_TRD_PAY_LIST://获取第三方支付渠道完成
                    if (msg.obj instanceof List) {
                        mAdapter = new PayTrdListAdapter(PayTrdActivity.this, (List<PayTrdItemBean>) msg.obj);
                        mGridView.setAdapter(mAdapter);
                    }
                    break;
                case PayConst.CODE_PAY_ALI_RESULT://支付宝支付结果（此处也就是充值结果）
                    if (msg.obj != null && msg.obj instanceof String) {
                        doResultAlipay((String) msg.obj);
                    }
                    break;
                case PayConst.CODE_PAY_0YUANFU_RESULT://0元付支付结果
                    if (msg.obj != null && msg.obj instanceof String) {
                        doResult0YuanFu((String) msg.obj);
                    }
                    break;
                case EmaProgressDialog.CODE_LOADING_START:
                    mProgress.showProgress((String) msg.obj);
                    break;
                case EmaProgressDialog.CODE_LOADING_END:
                    mProgress.closeProgress();
                    break;
                case EmaConst.PAY_RESULT_DELAYED:  // 延迟发货的提醒
                    new EmaDialogPayPromptResult(PayTrdActivity.this, 0,EmaConst.PAY_RESULT_DELAYED,"发货有延迟").show();
                    break;
                case PayTrdActivity.PAY_ACTIVITY_CLOSE:  //关闭支付订单页面（本页面）
                    PayTrdActivity.this.finish();
                    break;
                case PayTrdActivity.PAY_ACTIVITY_DIALOG_CANLE:
                    showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_CANCEL, "");
                    break;
                case PayTrdActivity.PAY_ACTIVITY_DIALOG_FAIL:
                    showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "");
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
        EmaPayProcessManager.getInstance().clearWeixinOrderInfo();

        mEmaUser = EmaUser.getInstance();
        mResourceManager = ResourceManager.getInstance(this);
        mProgress = new EmaProgressDialog(this);
        mIDBtnBackToGameView = 0;

        //获得订单信息
        Intent intent = this.getIntent();
        mPayInfo = intent.getParcelableExtra("payInfo");

        initView();
        initData();
        qqWalletonCreat();
    }

    private void qqWalletonCreat() {
        IOpenApi openApi = OpenApiFactory.getInstance(this, ConfigManager.getInstance(this).getQQAppId());
        openApi.handleIntent(getIntent(), this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mTxtProductName.setText(mPayInfo.getProductName());
        mTxtTotalPrice.setText(mPayInfo.getPrice() + "");
        mTxtUserName.setText(mEmaUser.getNickName());

        PayUtil.getRechargeList(this, mHandler);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setContentView(mResourceManager.getIdentifier("ema_pay_thrdpay", "layout"));
        mBtnBackToGameView = (ImageView) findViewById(getID_BtnBackToGame());
        mTxtTotalPrice = (TextView) findViewById(mResourceManager.getIdentifier("ema_pay_thrd_total_price_textview", "id"));
        mTxtUserName = (TextView) findViewById(mResourceManager.getIdentifier("ema_pay_thrd_username_textview", "id"));
        mTxtProductName = (TextView) findViewById(mResourceManager.getIdentifier("ema_pay_thrd_goodsname_textview", "id"));
        mGridView = (GridView) findViewById(mResourceManager.getIdentifier("ema_pay_thrd_PayList", "id"));

        mBtnBackToGameView.setOnClickListener(this);

        //为三方支付准备的一个payInfo
        final EmaPayInfo rechargePayInfo = new EmaPayInfo();
        rechargePayInfo.setOrderId(mPayInfo.getOrderId());
        rechargePayInfo.setProductName(mPayInfo.getProductName());
        rechargePayInfo.setPrice(mPayInfo.getPrice());
        rechargePayInfo.setDescription(mPayInfo.getDescription());

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position,
                                    long arg3) {
                PayTrdItemBean bean = (PayTrdItemBean) mAdapter.getItem(position);
                String payName = bean.get3rdPayName();
                if (payName.equals(PayConst.PAY_TRD_QIANBAO)) {//钱包支付

                    //PayUtil.GoPayByMabi(PayTrdActivity.this, REQUEST_CODE_PAY_MABI);
                    ToastHelper.toast(PayTrdActivity.this, "暂不支持");

                } else if (payName.equals(PayConst.PAY_TRD_QQWALLET)) {//qq钱包

                    //PayUtil.GoRechargeByQQwallet(PayTrdActivity.this, rechargePayInfo, mHandler);   参数下来后开放
                    ToastHelper.toast(PayTrdActivity.this,"暂不支持");
                } else if (payName.equals(PayConst.PAY_TRD_GAMECARD)) {//游戏卡支付

                    //PayUtil.GoPayByGameCardpay(PayTrdActivity.this, REQUEST_CODE_PAY_GAMECARD);
                    ToastHelper.toast(PayTrdActivity.this, "暂不支持");
                } else if (payName.equals(PayConst.PAY_TRD_PHONE_CARD)) {//手机卡支付

                    //PayUtil.GoPayByPhoneCardpay(PayTrdActivity.this, REQUEST_CODE_PAY_PHONECARD);
                    ToastHelper.toast(PayTrdActivity.this, "暂不支持");
                } else if (payName.equals(PayConst.PAY_TRD_ALIPAY)) {//支付宝支付

                    //PayUtil.GoPayByAlipay(PayTrdActivity.this, mHandler);   现在如下：钱不够先走充值完了再用余额支付（用户感知为直接用钱买）
                    //把上面得到的那个mPayInfo拿好，充值ok后还得用它钱包支付。
                    PayUtil.GoRecharegeByAlipay(PayTrdActivity.this, rechargePayInfo, mHandler);

                } else if (payName.equals(PayConst.PAY_TRD_WEIXIN)) {//微信支付

                    PayUtil.GoRechargeByWeixin(PayTrdActivity.this, rechargePayInfo, mHandler);

                    //ToastHelper.toast(PayTrdActivity.this,"暂不支持");

                } else if (payName.equals(PayConst.PAY_TRD_0YUANFU)) {//0元付
                    ToastHelper.toast(PayTrdActivity.this, "暂不支持");
                    //PayUtil.GoPayBy0yuanfu(PayTrdActivity.this, bean,  mHandler);

                }
            }
        });
    }

    /**
     * Click监听事件
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getID_BtnBackToGame()) {//返回游戏
            showCancelPromptDialog();
        }
    }

    /**
     * 0元付支付结果处理
     *
     * @param result
     */
    private void doResult0YuanFu(String result) {
        try {
            JSONObject json = new JSONObject(result);
            int resultCode = json.getInt("retcode");
            switch (resultCode) {
                case HttpInvokerConst.SDK_RESULT_SUCCESS://获取订单号成功
                    LOG.d(TAG, "查询订单成功");
                    UCommUtil.makePayCallBack(EmaCallBackConst.PAYSUCCESS, "查询订单成功，支付成功");
                    showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_SUCC, "");
                    break;
                case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR://签名验证失败
                    LOG.d(TAG, "签名验证失败，查询订单失败");
                    UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "签名验证失败，查询订单失败");
                    showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "签名验证失败");
                    break;
                default:
                    LOG.d(TAG, "查询订单失败，原因未知");
                    UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "查询订单失败，原因未知");
                    showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "");
                    break;
            }
        } catch (Exception e) {
            LOG.w(TAG, "pay failed", e);
            UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "查询订单失败，原因未知");
            showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "");
        }
    }

    /**
     * 支付宝充值结果
     */
    private void doResultAlipay(String result) {
        TrdAliPayResult data = new TrdAliPayResult(result);
        if (data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_SUCC)) {//支付 充值成功

            //充值成功，此时再重新走一边支付(下面三行已废，但emapay那边的逻辑还保留着，充值然后再买应该认为是一次订单，一个orderid)
//			LOG.e("paythirdActivity","充值完毕再走一次钱包支付");
//			mPayInfo.setReChargePay(true);
//			EmaPay.getInstance(Ema.getInstance().getContext()).pay(mPayInfo,EmaPay.getInstance(Ema.getInstance().getContext()).mPayListener);

            //PayMabiActivity.doPayNoKeyWord(mPayInfo);  现在这个操作放在服务端做了 12/10
            //UCommUtil.makePayCallBack(EmaCallBackConst.PAYSUCCESS, "支付成功");  现在需要查询订单状态  12/14
            PayUtil.doCheckOrderStatus(mHandler);

        } else if (data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_ON_PAYING)) {//正在处理中

            UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "正在处理中");
            showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_OTHERS, "正在处理中");

        } else if (data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_PAY_CANCEL)) {//用户中途取消

            UCommUtil.makePayCallBack(EmaCallBackConst.PAYCANELI, "用户中途取消");
            showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_CANCEL, "");
        } else if (data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_FAILED)) {//订单支付失败
            UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "订单支付失败");
            showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "");
        } else if (data.getResultStatus().equals(TrdAliPay.RESULT_STATUS_NETWORK_ERROR)) {//网络连接出错
            UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "网络连接出错");
            showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "请检查网络连接是否正常");
        }
    }

    /**
     * 显示支付结束后的对话框
     */
    public void showPayResultDialog(int actionType, int resultType, String promptInfo) {
        new EmaDialogPayPromptResult(this, actionType, resultType, promptInfo).show();
    }

    /**
     * 获取支付url后的回调
     */
    public void onPayCallBack(Message msg) {
        mHandler.sendMessage(msg);
    }


    /**
     * 监听返回按钮
     * 所有成功的就直接finish了，有各种情况的会走这里，里面会取消订单
     */
    @Override
    public void onBackPressed() {
        showCancelPromptDialog();
    }

    /**
     * 显示对话框提示用户是否确认退出支付
     */
    private void showCancelPromptDialog() {
        new EmaDialogPayPromptCancel(this).show();
    }

    /**
     * 获取返回游戏按钮的资源ID
     *
     * @return
     */
    private int getID_BtnBackToGame() {
        if (mIDBtnBackToGameView == 0) {
            mIDBtnBackToGameView = mResourceManager.getIdentifier("ema_pay_thrd_return_game", "id");
        }
        return mIDBtnBackToGameView;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //qq钱包
        IOpenApi openApi = OpenApiFactory.getInstance(this, ConfigManager.getInstance(this).getQQAppId());
        openApi.handleIntent(intent, this);
    }

    //这个是实现了qq钱包的回调接口IOpenApiListener的重写方法
    @Override
    public void onOpenResponse(BaseResponse baseResponse) {
        if (baseResponse == null) {
            return;// 不能识别的intent
        } else {
            if (baseResponse instanceof PayResponse) {
                // 支付回调响应
                PayResponse payResponse = (PayResponse) baseResponse;

                switch (payResponse.retCode) {
                    case 0:     //成功
                        PayUtil.doCheckOrderStatus(mHandler);
                        break;
                    case -1:     //用户取消
                        mHandler.sendEmptyMessage(PayTrdActivity.PAY_ACTIVITY_DIALOG_CANLE);
                        UCommUtil.makePayCallBack(EmaCallBackConst.PAYCANELI, "订单取消");
                        break;
                    default:    //失败
                        mHandler.sendEmptyMessage(PayTrdActivity.PAY_ACTIVITY_DIALOG_FAIL);
                        UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "订单支付失败");
                        break;
                }

            } else {
                // 不能识别的响应
                UCommUtil.makePayCallBack(EmaCallBackConst.PAYFALIED, "订单支付失败");
                showPayResultDialog(EmaConst.PAY_ACTION_TYPE_PAY, EmaConst.PAY_RESULT_FAILED, "");
            }
        }
    }



}
