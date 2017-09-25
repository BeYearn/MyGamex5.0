package com.emagroup.sdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

class RegisterByPhoneDialog extends Dialog implements android.view.View.OnClickListener, ThirdLoginUtils.ThirdAllowAfter {

    private static final String TAG = "RegisterByPhoneDialog";

    private static final int CODE_GET_AUTH_CODE_SUCCESS = 100;//成功

    private static final int CODE_LOGIN_SUCC = 300;

    private static final int CODE_TIMER = 400;//发送定时器计数消息

    private static final int CODE_FAILED = 4;// 失败
    private static final int CODE_SUCCESS = 3;

    private static final int FIRST_STEP_LOGIN_SUCCESS = 32; // 第二步 第一步验证登陆成功

    private static Activity mActivity;
    private ResourceManager mResourceManager;// 资源管理
    private DeviceInfoManager mDeviceInfoManager;// 设备信息管理
    private ConfigManager mConfigManager;// 配置项管理
    private EmaUser mEmaUser;// 当前登录用户信息
    private String mAccountType;

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
    private RelativeLayout mWechatLogin;
    private RelativeLayout mQQLogin;

    // 进度条
    private EmaProgressDialog mProgress;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CODE_GET_AUTH_CODE_SUCCESS://成功
                    ToastHelper.toast(mActivity, "请查收验证码！");
                    mProgress.closeProgress();
                    setViewChange();
                    startTimeTask(60);
                    break;
                case CODE_TIMER://发送定时器消息，刷新定时器
                    updateTimeTask();
                    break;
                case CODE_LOGIN_SUCC://登录成功
                    ToastHelper.toast(mActivity, "登录成功");
                    if(mInstance!=null){
                        mInstance.stopTimeTask();
                        mInstance.dismiss();
                    }
                    UCommUtil.makeUserCallBack(EmaCallBackConst.REGISTERSUCCESS, "注册成功");
                    // 保存登录成功用户的信息
                    mEmaUser.saveLoginUserInfo(mActivity);
                    mLoginSuccDialog = new LoginSuccDialog(mActivity, false);
                    mLoginSuccDialog.start();
                    break;
                case CODE_SUCCESS:
                    doResultSuccFromServer((String) msg.obj);
                    if(mInstance!=null){
                        mInstance.dismiss();
                    }
                    mInstance = null;   //否则下次进入该界面还是验证码状态（未刷新）
                    mProgress.closeProgress();
                    break;
                case FIRST_STEP_LOGIN_SUCCESS:
                    LoginSecond();
                    break;
            }
        }

    };
    private static long firstGetTime = 0;
    private String uid;

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
        mAccountType = "0"; //0 弱帐号

        Map<String, String> params = new HashMap<>();
        params.put("accountType", mAccountType);
        params.put("deviceType", "android");
        params.put("allianceId", mConfigManager.getChannel());
        params.put("channelTag", mConfigManager.getChannelTag());
        params.put("appId", mConfigManager.getAppId());
        params.put("deviceKey", DeviceInfoManager.getInstance(mActivity).getDEVICE_ID());

        String sign = 0 + mConfigManager.getChannel() + mConfigManager.getAppId() + mConfigManager.getChannelTag() + mDeviceInfoManager.getDEVICE_ID() + "android" + EmaUser.getInstance().getAppKey();
        //LOG.e("rawSign",sign);
        sign = UCommUtil.MD5(sign);
        params.put("sign", sign);

        new HttpInvoker().postAsync(Url.getFirstLoginUrl(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        firstLoginResult(result, 0);
                    }
                });
    }

    /**
     * 手机或邮箱验证码登录的第一次登录验证
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
        params.put("accountType", mAccountType);
        params.put("captcha", captcha);
        params.put("appId", mConfigManager.getAppId());
        params.put("deviceType", "android");
        params.put("deviceKey", mDeviceInfoManager.getDEVICE_ID());
        params.put("allianceId", mConfigManager.getChannel());
        params.put("channelTag", mConfigManager.getChannelTag());
        String sign;
        if ("1".equals(mAccountType)) {
            params.put("mobile", mEmaUser.getMobile());
            sign = mAccountType + mConfigManager.getChannel() + mConfigManager.getAppId() + captcha + mConfigManager.getChannelTag() + mDeviceInfoManager.getDEVICE_ID() + "android" + mEmaUser.getMobile() + EmaUser.getInstance().getAppKey();
        } else {
            params.put("email", mEmaUser.getEmail());
            sign = mAccountType + mConfigManager.getChannel() + mConfigManager.getAppId() + captcha + mConfigManager.getChannelTag() + mDeviceInfoManager.getDEVICE_ID() + "android" + mEmaUser.getEmail() + EmaUser.getInstance().getAppKey();
        }
        //LOG.e("rawSign",sign);
        sign = UCommUtil.MD5(sign);
        params.put("sign", sign);

        new HttpInvoker().postAsync(Url.getFirstLoginUrl(), params, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                firstLoginResult(result, Integer.parseInt(mAccountType));
            }
        });

    }


    private void firstLoginResult(String result, int type) {
        try {
            JSONObject json = new JSONObject(result);
            firstLoginResult = json.getString("data");
            int resultCode = json.getInt("status");
            switch (resultCode) {
                case HttpInvokerConst.SDK_RESULT_SUCCESS://  第一步登录成功

                    mEmaUser.setAccountType(type);
                    JSONObject data = json.getJSONObject("data");

                    uid = data.getString("uid");
                    allianceUid = data.getString("allianceUid");

                    mEmaUser.setmUid(uid);
                    mEmaUser.setAllianceUid(allianceUid);

                    allianceId = data.getString("allianceId");
                    LOG.e("allianceId", allianceId);

                    authCode = data.getString("authCode");

                    callbackUrl = data.getString("callbackUrl");
                    LOG.e("callbackUrl", callbackUrl);

                    nickname = data.getString("nickname");
                    LOG.e("nickname", nickname);
                    mEmaUser.setNickName(nickname);

                    mHandler.sendEmptyMessage(FIRST_STEP_LOGIN_SUCCESS);
                    LOG.d(TAG, "第一步登录成功");
                    break;
                default:
                    Log.e("firstLoginResult",result);
                    //ToastHelper.toast(mActivity, json.getString("message"));
                    mProgress.closeProgress();
                    break;
            }
        } catch (Exception e) {
            LOG.w(TAG, "firstLoginResult error", e);
            mHandler.sendEmptyMessage(CODE_FAILED);
        }
    }

    /**
     * 第二步登录（各种登录方式的第二步登录一样）
     */
    private void LoginSecond() {
        Map<String, String> params = new HashMap<>();
        /*params.put("authCode", authCode);
        params.put("uid", allianceUid);*/
        params.put("data", firstLoginResult);
        new HttpInvoker().postAsync(callbackUrl, params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            int resultCode = jsonObject.getInt("status");
                            switch (resultCode) {
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
                            LOG.w(TAG, "LoginSecond error:", e);
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
        Ema.getInstance().setWechatCanLogin(mActivity, true);
        //ToastHelper.toast(mActivity, "登录成功");
        USharedPerUtil.setParam(mActivity, "token", token);
        USharedPerUtil.setParam(mActivity, "nickname", nickname);
        USharedPerUtil.setParam(mActivity, "uid", uid);
        USharedPerUtil.setParam(mActivity, "accountType", Integer.parseInt(mAccountType));  //记录账户类型
        mLoginSuccDialog = new LoginSuccDialog(mActivity, true);
        mLoginSuccDialog.start();
        EmaUser.getInstance().setIsLogin(true);
    }

    private String allianceId;
    private String authCode;
    private String callbackUrl;
    private String nickname;
    private static RegisterByPhoneDialog mInstance;
    private String allianceUid;

    public static RegisterByPhoneDialog getInstance(Context context) {
        if (mInstance == null || !mActivity.equals(context)) {
            mInstance = new RegisterByPhoneDialog(context);
        }
        return mInstance;
    }


    private RegisterByPhoneDialog(Context context) {
        super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
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
        //this.getWindow().getAttributes().alpha = 0.8F;

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setContentView(mResourceManager.getIdentifier("ema_register_by_phone", "layout"));
        Button mBtnStartWork = (Button) findViewById(getId("ema_btn_start_work"));
        ImageView mBtnReturnLogin = (ImageView) findViewById(getId("ema_btn_return_login"));
        ImageView mBtnReturnRegister = (ImageView) findViewById(getId("ema_btn_return_register"));
        mBtnGetAuthCode = (Button) findViewById(getId("ema_btn_get_auth_code"));

        String acNum = (String) USharedPerUtil.getParam(mActivity, "accountNum", "");
        mEdtContentView = (EditText) findViewById(getId("ema_phone_info_inputText"));

        if (!TextUtils.isEmpty(acNum)) {
            mEdtContentView.setText(acNum);
            mEdtContentView.setSelection(acNum.length());
        }

        mBtnGetAuthCode.setVisibility(View.GONE);
        mBtnGetAuthCode.setEnabled(false);

        mWechatLogin = (RelativeLayout) findViewById(getId("ema_wechat_login_rela"));

        mQQLogin = (RelativeLayout) findViewById(getId("ema_qq_login_rela"));

        if (Ema.getInstance().getWachatLoginVisibility()) {
            mWechatLogin.setOnClickListener(this);
        } else {
            mWechatLogin.setVisibility(View.GONE);
        }
        if (Ema.getInstance().getQQLoginVisibility()) {
            mQQLogin.setOnClickListener(this);
        } else {
            mQQLogin.setVisibility(View.GONE);
        }

      /*  mQQLogin.setOnClickListener(this);
        mWechatLogin.setOnClickListener(this);*/
        mBtnStartWork.setOnClickListener(this);
        mBtnReturnLogin.setOnClickListener(this);
        mBtnReturnRegister.setOnClickListener(this);
        mBtnGetAuthCode.setOnClickListener(this);
        Ema.getInstance().setWechatCanLogin(mActivity, true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getId("ema_btn_start_work")) {  //获取验证码，或者进入游戏
            doStartWork();
        } else if (id == getId("ema_btn_return_login")) {  //账号登录
            if(mInstance!=null){
                mInstance.dismiss();
            }
            mInstance = null; //用户如果切换为帐号登录，则置空这个，以便回来时重新走手机流程
            LoginDialog.getInstance(Ema.getInstance().getContext()).show();

            //发送一次打点deviceInfo
            EmaSendInfo.sendDeviceInfoJson("帐号登录", "1");

        } else if (id == getId("ema_btn_return_register")) {//游客登录
            doRegistByOneKey();

            //发送一次打点deviceInfo
            EmaSendInfo.sendDeviceInfoJson("游客登录", "1");

        } else if (id == getId("ema_btn_get_auth_code")) {   //重新获取验证码
            startTimeTask(60);
            if ("1".equals(mAccountType)) {
                doGetAuthCode(mEmaUser.getMobile());
            } else if ("2".equals(mAccountType)) {
                doSendEmail(mEmaUser.getEmail());
            }
        } else if (id == getId("ema_wechat_login_rela")) {   //微信登录
            // WeixinShareUtils.getInstance(mActivity).wachateLogin();
            wachateLogin();

            //发送一次打点deviceInfo
            EmaSendInfo.sendDeviceInfoJson("微信登录", "1");

        } else if (id == getId("ema_qq_login_rela")) {        //qq 登录
            //  Ema.getInstance().saveWachatLoginFlag(true);
            ThirdLoginUtils.getInstance(mActivity).qqLogin(this);

            //发送一次打点deviceInfo
            EmaSendInfo.sendDeviceInfoJson("QQ登录", "1");
        }
    }

    private void wachateLogin() {

        Ema.getInstance().setWechatCanLogin(mActivity, false);
        ThirdLoginUtils.getInstance(mActivity).wachateLogin(this);
    }


    /**
     * 获取验证码 / 进入游戏
     */
    private void doStartWork() {
        if (mFlagHasGetAuthCode) {//获取验证码之后，进行的是登录操作
            accountLoginFirst();
        } else {//还没有获取验证码，进行获取验证码操作

            String curAccountNum = mEdtContentView.getText().toString();//当前填入的帐号

            //记录最后一次帐号，以便下次登录填入
            USharedPerUtil.setParam(mActivity, "accountNum", curAccountNum);

            if (TextUtils.isEmpty(curAccountNum)) {
                ToastHelper.toast(mActivity, "帐号不能为空");
                return;
            }
            if (UCommUtil.isPhone(curAccountNum)) {
                mAccountType = "1";
                doGetAuthCode(curAccountNum);
            } else if (UCommUtil.isEmail(curAccountNum)) {
                mAccountType = "2";
                doSendEmail(curAccountNum);
            } else {
                ToastHelper.toast(mActivity, "请输入正确的帐号");
            }
        }
    }


    /**
     * 发送验证码邮件
     *
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

        Long hasTime = System.currentTimeMillis() - firstGetTime;
        boolean isTimeAll = hasTime > 60000;

        if (phoneNum.equals(mEmaUser.getMobile()) && !isTimeAll) {  //如果等于上次的号码并且没经过60s后  给他回到之前的页面，否则就是下面的流程

            setViewChange();
            startTimeTask((int) (60000 - hasTime) / 1000);

            return;
        }

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

                                    firstGetTime = System.currentTimeMillis();
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
    private void startTimeTask(int delayTime) {
        mCountNum = delayTime;
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

    @Override
    public void wachateAllowAfter(String result) {
        mProgress.showProgress("登录中...");
        mAccountType = "3"; //wx账号类型3

        Map<String, String> params = new HashMap();
        params.put("accountType", mAccountType);
        params.put("appId", mConfigManager.getAppId());
        params.put("channelTag", mConfigManager.getChannelTag());
        params.put("allianceId", ConfigManager.getInstance(Ema.getInstance().getContext()).getChannel());
        params.put("weixinCode", result);
        params.put("deviceKey", DeviceInfoManager.getInstance(mActivity).getDEVICE_ID());
        params.put("deviceType", "android");
        String sign = mAccountType + ConfigManager.getInstance(Ema.getInstance().getContext()).getChannel()
                + mConfigManager.getAppId() + mConfigManager.getChannelTag() + DeviceInfoManager.getInstance(mActivity).getDEVICE_ID()
                + params.get("deviceType") + result + EmaUser.getInstance().getAppKey();
        //LOG.e("rawSign",sign);
        sign = UCommUtil.MD5(sign);
        params.put("sign", sign);
        new HttpInvoker().postAsync(Url.getFirstLoginUrl(), params, new HttpInvoker.OnResponsetListener() {

            @Override
            public void OnResponse(String result) {
                Log.e("wachateAllowAfter",result);
                firstLoginResult(result, Integer.parseInt(mAccountType));
            }
        });
    }

    @Override
    public void qqAllowAfter(Map<String, String> param) {
        mProgress.showProgress("登录中...");
        mAccountType = "5"; //QQ账号类型5

        param.put("pfAppId", mConfigManager.getAppId());
        param.put("channelTag", mConfigManager.getChannelTag());
        param.put(/*"allianceId"*/"channelId", ConfigManager.getInstance(Ema.getInstance().getContext()).getChannel());
        param.put("deviceKey", DeviceInfoManager.getInstance(mActivity).getDEVICE_ID());
        param.put("deviceType", "android");
        String sign = param.get("accessToken") + ConfigManager.getInstance(Ema.getInstance().getContext()).getChannel() +
                mConfigManager.getChannelTag() + DeviceInfoManager.getInstance(mActivity).getDEVICE_ID()
                + param.get("deviceType") + param.get("openId") + param.get("pfAppId")
                + param.get("qqAppId") + EmaUser.getInstance().getAppKey();
        sign = UCommUtil.MD5(sign);
        param.put("sign", sign);
        new HttpInvoker().postAsync(Url.getQqLoginUrl(), param, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                firstLoginResult(result, Integer.parseInt(mAccountType));//QQ账号类型5
            }
        });

    }
}
