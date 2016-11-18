package com.emagroup.sdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

class RegisterByPhoneDialog extends Dialog implements android.view.View.OnClickListener {

    private static final String TAG = "RegisterByPhoneDialog";

    private static final int CODE_GET_AUTH_CODE_SUCCESS = 100;//成功

    private static final int CODE_LOGIN_SUCC = 300;

    private static final int CODE_TIMER = 400;//发送定时器计数消息

    private static final int CODE_FAILED = 4;// 失败
    private static final int CODE_SUCCESS = 3;

    private static final int FIRST_STEP_LOGIN_SUCCESS = 32; // 第二步 第一步验证登陆成功

    private Activity mActivity;
    private ResourceManager mResourceManager;// 资源管理
    private DeviceInfoManager mDeviceInfoManager;// 设备信息管理
    private ConfigManager mConfigManager;// 配置项管理
    private EmaUser mEmaUser;// 当前登录用户信息
    private String accountType;

    private int mCountNum;//秒数
    private Timer mTimer;
    private TimerTask mTask;

    private LoginSuccDialog mLoginSuccDialog;// 登录成功后显示的对话框

    private Button mBtnGetAuthCode;//获取验证码
    private EditText mEdtContentView;//输入手机号码 或者 验证码

    private Map<String, Integer> mIDmap;

    //标记
    private boolean mFlagHasGetAuthCode;//标记当前状态是获取验证码之前(false)，还是获取了验证码之后(true)
    private String firstLoginResult;

    // 进度条
    private EmaProgressDialog mProgress;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CODE_GET_AUTH_CODE_SUCCESS://成功
                    ToastHelper.toast(mActivity, "请查收验证码！");
                    mProgress.closeProgress();
                    setViewChange();
                    startTimeTask();
                    break;
                case CODE_TIMER://发送定时器消息，刷新定时器
                    updateTimeTask();
                    break;
                case CODE_LOGIN_SUCC://登录成功
                    ToastHelper.toast(mActivity, "登录成功");
                    RegisterByPhoneDialog.this.stopTimeTask();
                    RegisterByPhoneDialog.this.dismiss();
                    UCommUtil.makeUserCallBack(EmaCallBackConst.REGISTERSUCCESS, "注册成功");
                    // 保存登录成功用户的信息
                    mEmaUser.saveLoginUserInfo(mActivity);
                    mLoginSuccDialog = new LoginSuccDialog(mActivity, false);
                    mLoginSuccDialog.start();
                    break;
                case CODE_SUCCESS:
                    doResultSuccFromServer((String) msg.obj);
                    RegisterByPhoneDialog.this.dismiss();
                    mProgress.closeProgress();
                    break;
                case FIRST_STEP_LOGIN_SUCCESS:
                    LoginSecond();
                    break;
            }
        }

    };

    /**
     * 一键登录的开始
     * 从这里开始后面的流程，和loginDialog里界面上所显示的快速登录的逻辑一样了
     */
    private void doRegistByOneKey() {
        //new RegisterDialog(Ema.getInstance().getContext()).show();   这是原来的：一键注册弹出一键注册的框，以及后续逻辑； 和loginDialog里面那段一样，ok后抽取
        weakLoginFirst();
    }

    /**
     * 弱账户的第一步登录
     */
    private void weakLoginFirst() {
        mProgress.showProgress("注册登录中...");

        Map<String, String> params = new HashMap<>();
        params.put("accountType", "0");
        params.put("deviceType", "android");
        params.put("allianceId", mConfigManager.getChannel());
        params.put("channelTag", mConfigManager.getChannelTag());
        params.put("appId", mConfigManager.getAppId());
        params.put("deviceKey", DeviceInfoManager.getInstance(mActivity).getDEVICE_ID());

        String sign = 0+mConfigManager.getChannel()+mConfigManager.getAppId()+mConfigManager.getChannelTag()+mDeviceInfoManager.getDEVICE_ID()+"android"+EmaUser.getInstance().getAppKey();
        //LOG.e("rawSign",sign);
        sign = UCommUtil.MD5(sign);
        params.put("sign", sign);

        new HttpInvoker().postAsync(Url.getFirstLoginUrl(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        try {
                            JSONObject json = new JSONObject(result);
                            firstLoginResult=json.getString("data");
                            int resultCode = json.getInt("status");
                            switch (resultCode) {
                                case HttpInvokerConst.SDK_RESULT_SUCCESS://  第一步登录成功

                                    USharedPerUtil.setParam(mActivity, "accountType", 0);  //记录账户类型

                                    JSONObject data = json.getJSONObject("data");
                                    userid = data.getString("allianceUid");
                                    LOG.e("allianceUid", userid);
                                    mEmaUser.setmUid(userid);
                                    allianceId = data.getString("allianceId");
                                    LOG.e("allianceId", allianceId);
                                    authCode = data.getString("authCode");
                                    LOG.e("authCode", authCode);
                                    callbackUrl = data.getString("callbackUrl");
                                    LOG.e("callbackUrl", callbackUrl);
                                    nickname = data.getString("nickname");
                                    LOG.e("nickname", nickname);
                                    mEmaUser.setNickName(nickname);
                                    mHandler.sendEmptyMessage(FIRST_STEP_LOGIN_SUCCESS);
                                    LOG.d(TAG, "第一步登录成功");
                                    break;
                                case HttpInvokerConst.SDK_RESULT_FAILED:
                                    ToastHelper.toast(mActivity, json.getString("message"));
                                    mProgress.closeProgress();
                                    break;
                                default:
                                    ToastHelper.toast(mActivity, json.getString("message"));
                                    mProgress.closeProgress();
                                    break;
                            }
                        } catch (Exception e) {
                            LOG.w(TAG, "login error", e);
                            mHandler.sendEmptyMessage(CODE_FAILED);
                        }
                    }
                });
    }

    /**
     * 手机验证码登录的第一次登录验证
     */
    private void accountLoginFirst() {
        final String captcha = mEdtContentView.getText().toString();
        if (UCommUtil.isStrEmpty(captcha)) {
            LOG.d(TAG, "验证码为空");
            ToastHelper.toast(mActivity, "验证码不能为空");
            return;
        }
        mProgress.showProgress("登录中...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("accountType", accountType);
        params.put("captcha", captcha);
        params.put("appId", mConfigManager.getAppId());
        params.put("deviceType", "android");
        params.put("deviceKey", mDeviceInfoManager.getDEVICE_ID());
        params.put("allianceId", mConfigManager.getChannel());
        params.put("channelTag", mConfigManager.getChannelTag());
        String sign="";
        if("1".equals(accountType)){
            params.put("mobile", mEmaUser.getMobile());
            sign = accountType+mConfigManager.getChannel()+mConfigManager.getAppId()+captcha+mConfigManager.getChannelTag()+mDeviceInfoManager.getDEVICE_ID()+"android"+mEmaUser.getMobile()+EmaUser.getInstance().getAppKey();
        }else {
            params.put("email", mEmaUser.getEmail());
            sign = accountType+mConfigManager.getChannel()+mConfigManager.getAppId()+captcha+mConfigManager.getChannelTag()+mDeviceInfoManager.getDEVICE_ID()+"android"+mEmaUser.getEmail()+EmaUser.getInstance().getAppKey();
        }
        //LOG.e("rawSign",sign);
        sign = UCommUtil.MD5(sign);
        params.put("sign", sign);

        new HttpInvoker().postAsync(Url.getFirstLoginUrl(), params, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {
                    JSONObject json = new JSONObject(result);
                    firstLoginResult=json.getString("data");
                    int resultCode = json.getInt("status");
                    switch (resultCode) {
                        case HttpInvokerConst.SDK_RESULT_SUCCESS://  第一步登录成功

                            USharedPerUtil.setParam(mActivity, "accountType", Integer.parseInt(accountType));  //记录账户类型

                            JSONObject data = json.getJSONObject("data");
                            userid = data.getString("allianceUid");
                            LOG.e("allianceUid", userid);
                            mEmaUser.setmUid(userid);
                            allianceId = data.getString("allianceId");
                            LOG.e("allianceId", allianceId);
                            authCode = data.getString("authCode");
                            LOG.e("authCode", authCode);
                            callbackUrl = data.getString("callbackUrl");
                            LOG.e("callbackUrl", callbackUrl);
                            nickname = data.getString("nickname");
                            LOG.e("nickname", nickname);
                            mEmaUser.setNickName(nickname);
                            mHandler.sendEmptyMessage(FIRST_STEP_LOGIN_SUCCESS);
                            LOG.d(TAG, "第一步登录成功");
                            break;
                        case HttpInvokerConst.SDK_RESULT_FAILED:
                            ToastHelper.toast(mActivity, json.getString("message"));
                            mProgress.closeProgress();
                            break;
                        default:
                            ToastHelper.toast(mActivity, json.getString("message"));
                            mProgress.closeProgress();
                            break;
                    }
                } catch (Exception e) {
                    ToastHelper.toast(mActivity, "登录失败");
                    mProgress.closeProgress();
                    LOG.w(TAG, "accountLoginFirst error:"+e);
                }
            }
        });

    }

    /**
     * 第二步登录（各种登录方式的第二步登录一样）
     */
    private void LoginSecond() {
        Map<String, String> params = new HashMap<>();
        /*params.put("authCode", authCode);
        params.put("uid", userid);*/
        params.put("data",firstLoginResult);
        new HttpInvoker().postAsync(callbackUrl, params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            int resultCode = jsonObject.getInt("status");
                            switch (resultCode){
                                case HttpInvokerConst.SDK_RESULT_SUCCESS:
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    String token = data.getString("token");
                                    LOG.e("token", token);
                                    mEmaUser.setmToken(token);

                                    Message msg = new Message();
                                    msg.what = CODE_SUCCESS;
                                    msg.obj = token;
                                    mHandler.sendMessage(msg);
                                    break;
                                case HttpInvokerConst.SDK_RESULT_FAILED:
                                    ToastHelper.toast(mActivity, jsonObject.getString("message"));
                                    mProgress.closeProgress();
                                    break;
                                default:
                                    ToastHelper.toast(mActivity, jsonObject.getString("message"));
                                    mProgress.closeProgress();
                                    break;
                            }

                        } catch (Exception e) {
                            ToastHelper.toast(mActivity, "登录失败");
                            mProgress.closeProgress();
                            LOG.w(TAG, "accountLoginsecond error:"+e);
                        }
                    }
                });
    }

    /**
     * 登录成功后做的操作
     *
     * @param token
     */
    private void doResultSuccFromServer(String token) {

        // 显示登录成功后的对话框
        mLoginSuccDialog = new LoginSuccDialog(mActivity, true);
        mLoginSuccDialog.start();
        //ToastHelper.toast(mActivity, "登录成功");
        USharedPerUtil.setParam(mActivity, "token", token);
        USharedPerUtil.setParam(mActivity,"nickname",nickname);
        USharedPerUtil.setParam(mActivity,"uid",userid);
        EmaUser.getInstance().setIsLogin(true);
    }

    private String allianceId;
    private String authCode;
    private String callbackUrl;
    private String nickname;
    private static RegisterByPhoneDialog mInstance;
    private String userid;

    public static RegisterByPhoneDialog getInstance(Context context){
        if(mInstance==null){
            mInstance=new RegisterByPhoneDialog(context);
        }
        return mInstance;
    }


    private RegisterByPhoneDialog(Context context) {
        super(context,ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
        mActivity = (Activity) context;
        mResourceManager = ResourceManager.getInstance(mActivity);
        mDeviceInfoManager = DeviceInfoManager.getInstance(mActivity);
        mConfigManager = ConfigManager.getInstance(mActivity);
        mEmaUser = EmaUser.getInstance();
        mProgress = new EmaProgressDialog(mActivity);
        mFlagHasGetAuthCode = false;
        mTimer = null;
        mTask = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setCanceledOnTouchOutside(false);


        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setContentView(mResourceManager.getIdentifier("ema_register_by_phone", "layout"));
        Button mBtnStartWork = (Button) findViewById(getId("ema_btn_start_work"));
        Button mBtnReturnLogin = (Button) findViewById(getId("ema_btn_return_login"));
        Button mBtnReturnRegister = (Button) findViewById(getId("ema_btn_return_register"));
        mBtnGetAuthCode = (Button) findViewById(getId("ema_btn_get_auth_code"));
        mEdtContentView = (EditText) findViewById(getId("ema_phone_info_inputText"));
        mBtnGetAuthCode.setVisibility(View.GONE);

        mBtnStartWork.setOnClickListener(this);
        mBtnReturnLogin.setOnClickListener(this);
        mBtnReturnRegister.setOnClickListener(this);
        mBtnGetAuthCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getId("ema_btn_start_work")) {//获取验证码，或者进入游戏
            doStartWork();
        } else if (id == getId("ema_btn_return_login")) {//账号登录
            this.dismiss();
            LoginDialog.getInstance(Ema.getInstance().getContext()).show();
        } else if (id == getId("ema_btn_return_register")) {//快速注册
            doRegistByOneKey();
        } else if (id == getId("ema_btn_get_auth_code")) {//重新获取验证码
            startTimeTask();
            if("1"==accountType){
                doGetAuthCode(mEmaUser.getMobile());
            }else if("2"==accountType){
                doSendEmail(mEmaUser.getEmail());
            }
        }
    }


    /**
     * 获取验证码 / 进入游戏
     */
    private void doStartWork() {
        if (mFlagHasGetAuthCode) {//获取验证码之后，进行的是登录操作
            accountLoginFirst();
        } else {//还没有获取验证码，进行获取验证码操作
            String accountNum = mEdtContentView.getText().toString();

            if(TextUtils.isEmpty(accountNum)){
                ToastHelper.toast(mActivity, "帐号不能为空");
                return;
            }
            if(UCommUtil.isPhone(accountNum)){
                accountType="1";
                doGetAuthCode(accountNum);
            }else if(UCommUtil.isEmail(accountNum)){
                accountType="2";
                doSendEmail(accountNum);
            }else {
                ToastHelper.toast(mActivity,"请输入正确的帐号");
            }
        }
    }


    /**
     * 发送验证码邮件
     * @param accountNum
     */
    private void doSendEmail(String accountNum) {
        mProgress.showProgress("发送验证邮件...");
        //设置用户的电话号码信息
        mEmaUser.setEmail(accountNum);
        Map<String, String> params = new HashMap<>();
        params.put("email", accountNum);
        UCommUtil.testMapInfo(params);

        new HttpInvoker().postAsync(Url.getSendEmailUrl(), params, new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        try {
                            JSONObject json = new JSONObject(result);
                            int resultCode = json.getInt("status");
                            switch (resultCode) {
                                case HttpInvokerConst.SDK_RESULT_SUCCESS:// 成功
                                    mHandler.sendEmptyMessage(CODE_GET_AUTH_CODE_SUCCESS);
                                    break;
                                case HttpInvokerConst.SDK_RESULT_FAILED:// 失败
                                    ToastHelper.toast(mActivity, json.getString("message"));
                                    mProgress.closeProgress();
                                    break;
                                default:
                                    ToastHelper.toast(mActivity, json.getString("message"));
                                    mProgress.closeProgress();
                                    break;
                            }
                        } catch (Exception e) {
                            LOG.w(TAG, "doSendEmail error", e);
                            ToastHelper.toast(mActivity, "请求失败");
                            mProgress.closeProgress();
                        }
                    }
                });
    }

    /**
     * 获取验证码
     */
    private void doGetAuthCode(String phoneNum) {
        mProgress.showProgress("获取验证码...");
        //设置用户的电话号码信息
        mEmaUser.setMobile(phoneNum);
        Map<String, String> params = new HashMap<>();
        params.put("mobile", phoneNum);
        UCommUtil.testMapInfo(params);

        new HttpInvoker().postAsync(Url.getSmsUrl(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        try {
                            JSONObject json = new JSONObject(result);
                            int resultCode = json.getInt("status");
                            switch (resultCode) {
                                case HttpInvokerConst.SDK_RESULT_SUCCESS:// 成功
                                    mHandler.sendEmptyMessage(CODE_GET_AUTH_CODE_SUCCESS);
                                    break;
                                case HttpInvokerConst.SDK_RESULT_FAILED:// 失败
                                    ToastHelper.toast(mActivity, json.getString("message"));
                                    mProgress.closeProgress();
                                    break;
                                default:
                                    ToastHelper.toast(mActivity, json.getString("message"));
                                    mProgress.closeProgress();
                                    break;
                            }
                        } catch (Exception e) {
                            LOG.w(TAG, "doGetAuthCode error", e);
                            ToastHelper.toast(mActivity, "请求失败");
                            mProgress.closeProgress();
                        }
                    }
                });
    }


    /**
     * 刷新定时器
     */
    private void updateTimeTask() {
        mCountNum--;
        if (0 < mCountNum) {
            mBtnGetAuthCode.setText("重新获取(" + mCountNum + ")");
        } else {
            mBtnGetAuthCode.setText("重新获取");
            stopTimeTask();
        }
    }

    /**
     * 开启定时器
     */
    private void startTimeTask() {
        mCountNum = 60;
        mBtnGetAuthCode.setEnabled(false);
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTask != null) {
            mTask.cancel();
        }
        mTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(CODE_TIMER);
            }
        };
        mTimer.schedule(mTask, 1000, 1000);
    }

    /**
     * 暂停定时器
     */
    public void stopTimeTask() {
        mBtnGetAuthCode.setEnabled(true);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    /**
     * 控制界面的转化（在获取验证码之后  转化为 输入验证码进行登录的界面）
     */
    private void setViewChange() {
        mEdtContentView.setText("");
        mEdtContentView.setHint("请输入验证码");
        mBtnGetAuthCode.setVisibility(View.VISIBLE);
        mFlagHasGetAuthCode = true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        EmaUser.getInstance().clearUserInfo();
        UCommUtil.makeUserCallBack(EmaCallBackConst.LOGINCANELL, "取消登录");
    }

    /**
     * 方法目的（为了防止重复去获取资源ID）
     *
     * @param key
     * @return
     */
    private int getId(String key) {
        if (mIDmap == null) {
            mIDmap = new HashMap<>();
        }
        if (!mIDmap.containsKey(key)) {
            mIDmap.put(key, mResourceManager.getIdentifier(key, "id"));
        }
        return mIDmap.get(key);
    }

}
