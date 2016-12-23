package com.emagroup.sdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginDialog extends Dialog implements
        android.view.View.OnClickListener {

    private static final String TAG = "LoginDialog";

    public static final int CODE_SET_AUTO_LOGIN = 8;// 设置自动登录用户
    public static final int CODE_DELETE_USERINFO = 9;//下拉框删除用户

    private static final int CODE_SUCCESS = 3;// 成功
    private static final int CODE_FAILED = 4;// 失败
    private static final int CODE_FAILED_NO_ACCOUNT = 5;// 用户名不存在
    private static final int CODE_FAILED_ERROR_PASSW = 6;// 密码错误
    private static final int CODE_ACCESSID_LOST = 7;// SID过期


    private static final int GET_UID_SUCCESS = 31;// 第一步 创建弱账户获得uid陈功
    private static final int FIRST_STEP_LOGIN_SUCCESS = 32; // 第二步 第一步验证登陆成功
    //private static final int SECOND_STEP_LOGIN_SUCCESS=33; // 第三步 第二步验证登陆成功  就是最后的登陆成功


    private static final String mPasswShowStr = "............";//密码显示

    private static Activity mActivity;
    private ResourceManager mResourceManager;// 资源管理
    private DeviceInfoManager mDeviceInfoManager;// 设备信息管理
    private ConfigManager mConfigManager;// 配置项管理
    private EmaUser mEmaUser;// 当前登录用户信息

    private List<UserLoginInfoBean> mUserInfoList;

    // views
    private Button mBtnLogin;
    private ImageView mImageLogoView;
    private Button mBtnRegistByPhone;
    private Button mBtnRegistByOneKey;
    //private TextView mBtnLoginByAnlaiye;//俺来也账号登陆
    private Button mBtnLoginByEma;//柠檬水账号登陆
    private EditText mEdtPasswView;
    private EditText mEdtNameView;
    private Button mBtnFindPasswView;
    private View mLayoutSelectView;
    private View mLayoutPoupParentView;

    private PopupWindow mSelectPoupWindow;// 选择下拉框
    private OptionsAdapter mOptionAdapter;// 下拉框选择的显示适配器

    private LoginSuccDialog mLoginSuccDialog;// 登录成功后显示的对话框


    private String firstLoginResult;

    private Map<String, Integer> mIDmap;

   // private ImageView mWechatLogin,mQQLogin;

    // 进度条
    private EmaProgressDialog mProgress;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CODE_SUCCESS:// 登录成功，并保存用户的信息(不要保存密码)
                    LoginDialog.this.dismiss();
                    doResultSuccFromServer((String) msg.obj);
                    mProgress.closeProgress();
                    break;
                case CODE_FAILED:// 登录失败（原因异常）
                    ToastHelper.toast(mActivity, "登录失败");
                    UCommUtil.makeUserCallBack(EmaCallBackConst.LOGINFALIED, "登录失败");
                    mEmaUser.clearUserInfo();
                    break;
                case CODE_FAILED_ERROR_PASSW:// 登录失败（密码错误）
                    ToastHelper.toast(mActivity, "密码错误");
                    UCommUtil.makeUserCallBack(EmaCallBackConst.LOGINFALIED, (String) msg.obj);
                    mEmaUser.clearUserInfo();
                    break;
                case CODE_FAILED_NO_ACCOUNT:// 登录失败（用户名不存在）
                    ToastHelper.toast(mActivity, "用户名不存在");
                    UCommUtil.makeUserCallBack(EmaCallBackConst.LOGINFALIED, (String) msg.obj);
                    mEmaUser.clearUserInfo();
                    break;
                case CODE_ACCESSID_LOST:// sid过期
                    doResultSidIsOutOfDate();
                    break;
                case CODE_SET_AUTO_LOGIN:// 在下拉框选择自动登录的用户
                    doResultSelectAccount(msg);
                    break;
                case CODE_DELETE_USERINFO://下拉框删除用户
                    doResultDeleteUser();
                    break;
                case FIRST_STEP_LOGIN_SUCCESS:
                    LoginSecond();
                    break;
                default:
                    break;
            }
        }
    };

    // 弱账户第一次登录请求
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

                                    USharedPerUtil.setParam(mActivity, "accountType", 0);//记录账户类型

                                    JSONObject data = json.getJSONObject("data");
                                    userid = data.getString("allianceUid");
                                    LOG.e("allianceUid", userid);
                                    mEmaUser.setmUid(userid);
                                    mEmaUser.setAllianceUid(userid);
                                    allianceId = data.getString("allianceId");
                                    LOG.e("allianceId", allianceId);
                                    authCode = data.getString("authCode");
                                    callbackUrl = data.getString("callbackUrl");
                                    LOG.e("callbackUrl", callbackUrl);
                                    nickname = data.getString("nickname");
                                    LOG.e("nickname", nickname);
                                    mEmaUser.setNickName(nickname);

                                    Message msg = new Message();
                                    msg.what = FIRST_STEP_LOGIN_SUCCESS;
                                    mHandler.sendMessage(msg);
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
                            LOG.w(TAG, "AccountLoginFirst error", e);
                            mHandler.sendEmptyMessage(CODE_FAILED);
                        }
                    }
                });
    }

    /**
     * 用户名和密码验证登录   第一步验证登录
     */
    private void AccountLoginFirst() {
        String account = mEdtNameView.getText().toString();
        //记录最后一次帐号，以便下次登录填入
        USharedPerUtil.setParam(mActivity,"accountNum",account);

        String passw = mEdtPasswView.getText().toString();
        String accountType="";
        if(TextUtils.isEmpty(account)){
            ToastHelper.toast(mActivity, "帐号不能为空");
            return;
        }
        if(UCommUtil.isPhone(account)){
            accountType="1"; // youxiang 2
        }else if(UCommUtil.isEmail(account)){
            accountType="2";
        }else {
            ToastHelper.toast(mActivity,"请输入正确的帐号");
        }
        if (UCommUtil.isStrEmpty(passw)) {
            ToastHelper.toast(mActivity, "密码不能为空");
            return;
        }
        mProgress.showProgress("登录中...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("accountType", accountType);
        params.put("password", passw);
        params.put("appId", mConfigManager.getAppId());
        params.put("deviceType", "android");
        params.put("deviceKey", mDeviceInfoManager.getDEVICE_ID());
        params.put("allianceId", mConfigManager.getChannel());
        params.put("channelTag", mConfigManager.getChannelTag());
        String sign="";
        if("1".equals(accountType)){
            params.put("mobile", account);
            sign = accountType+mConfigManager.getChannel()+mConfigManager.getAppId()+mConfigManager.getChannelTag()+mDeviceInfoManager.getDEVICE_ID()+"android"+account+passw+EmaUser.getInstance().getAppKey();
        }else {
            params.put("email", account);
            sign = accountType+mConfigManager.getChannel()+mConfigManager.getAppId()+mConfigManager.getChannelTag()+mDeviceInfoManager.getDEVICE_ID()+"android"+account+passw+EmaUser.getInstance().getAppKey();
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
                            USharedPerUtil.setParam(mActivity, "accountType", 1);  //记录账户类型

                            JSONObject data = json.getJSONObject("data");
                            userid = data.getString("allianceUid");
                            LOG.e("allianceUid", userid);
                            mEmaUser.setmUid(userid);
                            mEmaUser.setAllianceUid(userid);
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
                    mHandler.sendEmptyMessage(CODE_FAILED);
                    LOG.w(TAG, "doLogin error", e);
                }
            }
        });
    }

    /**
     * 第二步登录验证 都是一样的
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
                                case HttpInvokerConst.SDK_RESULT_SUCCESS:// 成功
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
                            LOG.w(TAG, "AccountLoginFirst error", e);
                            mHandler.sendEmptyMessage(CODE_FAILED);
                        }
                    }
                });
    }

    /**
     * 从服务器接受到了登录成功的返回 后 的处理
     */
    private void doResultSuccFromServer(String token) {
        // 显示登录成功后的对话框
        mLoginSuccDialog = new LoginSuccDialog(mActivity, true);
        mLoginSuccDialog.start();
        //ToastHelper.toast(mActivity, "登录成功");
        USharedPerUtil.setParam(mActivity, "token", token);
        USharedPerUtil.setParam(mActivity, "nickname", nickname);
        USharedPerUtil.setParam(mActivity, "uid", userid);
        EmaUser.getInstance().setIsLogin(true);
    }

    private String allianceId;
    private String authCode;
    private String callbackUrl;
    private String nickname;

    private UserLoginInfoBean mAutoUserInfoBean;// 自动登录的时候需要使用的用户信息
    // 标记
    private boolean mFlagIsPoupInit;// 判断下拉框控件是否进行了初始化
    private boolean mFlagIsLoginByAnlaiye;//标记是否是用俺来也账号登陆

    private String userid;//弱账户创建出的uid

    private static LoginDialog mInstance;


    public static LoginDialog getInstance(Context context){
        if(null==mInstance||!mActivity.equals(context)){
            mInstance=new LoginDialog(context);
        }
        return mInstance;
    }

    private LoginDialog(Context context) {
        super(context,ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
        mActivity = (Activity) context;
        mResourceManager = ResourceManager.getInstance(mActivity);
        mDeviceInfoManager = DeviceInfoManager.getInstance(mActivity);
        mConfigManager = ConfigManager.getInstance(mActivity);
        mEmaUser = EmaUser.getInstance();
        mProgress = new EmaProgressDialog(mActivity);
        mFlagIsPoupInit = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        this.getWindow().getAttributes().alpha=0.8F;
        this.setCanceledOnTouchOutside(false);

        initView();

        initData();
    }

    /**
     * 获取配置文件内保存的用户列表，并显示最近登录的用户作为默认登录用户
     */
    private void initData() {
        //如果是俺来也账号，需要显示俺来也的图标，用来切换登陆
        if (UserUtil.isAnlaiye()) {
          //  mBtnLoginByAnlaiye.setVisibility(View.VISIBLE);
        }
        //进入登陆界面默认显示Ema账号列表
        mUserInfoList = USharedPerUtil.getUserLoginInfoList(mActivity);
        if (mUserInfoList != null && mUserInfoList.size() > 0) {
            UserLoginInfoBean bean = mUserInfoList.get(0);
            if (UserUtil.isAnlaiye()) {
                doChangeLoginSource(bean.isAnlaiye());
            } else {
                setDefaultAccount(false);
            }
        }
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setContentView(mResourceManager.getIdentifier("ema_login_normal", "layout"));

        mBtnLogin = (Button) findViewById(getId("ema_normalLogin_enterGame"));
        mImageLogoView = (ImageView) findViewById(getId("ema_login_image_logo"));
      //  mBtnLoginByAnlaiye = (TextView) findViewById(getId("ema_login_btn_login_by_anlaiye"));
        mBtnLoginByEma = (Button) findViewById(getId("ema_normallogin_change_emalogin"));
        mBtnRegistByPhone = (Button) findViewById(getId("ema_normalLogin_phoneLogin"));
        mBtnRegistByOneKey = (Button) findViewById(getId("ema_normalLogin_oneKeyReg"));

        String acNum = (String) USharedPerUtil.getParam(mActivity, "accountNum", "");
        mEdtNameView = (EditText) findViewById(getId("ema_normal_name_editText"));
        if(!TextUtils.isEmpty(acNum)){
            mEdtNameView.setText(acNum);
            mEdtNameView.setSelection(acNum.length());
        }

        mEdtPasswView = (EditText) findViewById(getId("ema_normal_pass_editText"));
        mBtnFindPasswView = (Button) findViewById(getId("ema_normal_find_passwd"));
        mLayoutSelectView = findViewById(getId("ema_account_go_list"));
        mLayoutPoupParentView = findViewById(getId("ema_login_normal_RelativeLayout1"));
       /* mWechatLogin= (ImageView) findViewById(getId("ema_wachate_login"));
        mQQLogin= (ImageView) findViewById(getId("ema_qq_login"));

        mWechatLogin.setOnClickListener(this);
        mQQLogin.setOnClickListener(this);*/
        mBtnLogin.setOnClickListener(this);
        mBtnRegistByPhone.setOnClickListener(this);
        mBtnRegistByOneKey.setOnClickListener(this);
        mLayoutSelectView.setOnClickListener(this);
        mBtnFindPasswView.setOnClickListener(this);
        //mBtnLoginByAnlaiye.setOnClickListener(this);
        mBtnLoginByEma.setOnClickListener(this);

        mEdtNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                mEdtPasswView.setText("");
            }
        });

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

    }

    /**
     * 生命周期的这个方法内，才能获取控件的位置参数(width, height)等
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!mFlagIsPoupInit) {
            initSelectPoupWindow();
            mFlagIsPoupInit = true;
        }
    }

    /**
     * 初始化下拉框控件
     */
    private void initSelectPoupWindow() {
        int pWidth = mEdtNameView.getWidth();
        View windowView = mResourceManager.getLayout("ema_options");
        ListView listView = (ListView) windowView.findViewById(mResourceManager
                .getIdentifier("ema_list", "id"));
        mOptionAdapter = new OptionsAdapter(mActivity, mHandler, mUserInfoList);
        listView.setAdapter(mOptionAdapter);

        mSelectPoupWindow = new PopupWindow(windowView, pWidth,
                LayoutParams.WRAP_CONTENT, true);
        mSelectPoupWindow.setTouchable(true);
        mSelectPoupWindow.setOutsideTouchable(true);
        mSelectPoupWindow.setBackgroundDrawable(new ColorDrawable(0));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getId("ema_normalLogin_enterGame")) {// 登录
            doLogin();
        } else if (id == getId("ema_normalLogin_phoneLogin")) {// 手机注册
            doRegistByPhone();
        } else if (id == getId("ema_normalLogin_oneKeyReg")) {// 一键注册
            doRegistByOneKey();
        } else if (id == getId("ema_account_go_list")) {// 下拉选择账号
            doSelectAccount();
        } else if (id == getId("ema_normal_find_passwd")) {//找回密码
            doFindPassw();
        } else if (id == getId("ema_login_btn_login_by_anlaiye")) {//俺来也账号登陆
            doChangeLoginSource(true);
        } else if (id == getId("ema_normallogin_change_emalogin")) {//柠檬水账号登陆
            doChangeLoginSource(false);
        }/*else if(id==getId("ema_wachate_login")){
                WeixinShareUtils.getInstance(mActivity).login();
        }*/
    }

    /**
     * 切换登陆账号来源时的改变
     *
     * @param isAnlaiye
     */
    private void doChangeLoginSource(boolean isAnlaiye) {
        mFlagIsLoginByAnlaiye = isAnlaiye;
      //  mBtnLoginByAnlaiye.setVisibility(isAnlaiye ? View.INVISIBLE : View.VISIBLE);
        mBtnRegistByOneKey.setVisibility(isAnlaiye ? View.GONE : View.VISIBLE);
        mBtnRegistByPhone.setVisibility(isAnlaiye ? View.GONE : View.VISIBLE);
        mBtnLoginByEma.setVisibility(isAnlaiye ? View.VISIBLE : View.GONE);
        mBtnFindPasswView.setVisibility(isAnlaiye ? View.GONE : View.VISIBLE);
        setDefaultAccount(isAnlaiye);
    }

    /**
     * 设置默认账号
     */
    private void setDefaultAccount(boolean isAnlaiye) {
        //刷新账号列表
        if (isAnlaiye) {
            mUserInfoList = USharedPerUtil.getUserLoginInfoListByAnlaiye(mActivity);
            mImageLogoView.setImageDrawable(mResourceManager.getDrawable(("ema_logo_anlaiye")));
            mEdtNameView.setHint("请输入账号");
        } else {
            mUserInfoList = USharedPerUtil.getUserLoginInfoListByEma(mActivity);
            mImageLogoView.setImageDrawable(mResourceManager.getDrawable(("ema_title")));
            mEdtNameView.setHint("请输入账号/手机/邮箱");
        }
        if (mOptionAdapter == null) {
            mOptionAdapter = new OptionsAdapter(mActivity, mHandler, mUserInfoList);
        }
        mOptionAdapter.setData(mUserInfoList);
        mOptionAdapter.notifyDataSetChanged();
        if (mUserInfoList != null && mUserInfoList.size() > 0) {
            UserLoginInfoBean bean = mUserInfoList.get(0);
            setViews(bean);
            mAutoUserInfoBean = bean;// 设定为自动登录的用户
        } else {
            setViews(null);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ToolBar.getInstance(mActivity).showToolBar();
        UCommUtil.makeUserCallBack(EmaCallBackConst.LOGINCANELL, "登陆取消！");
    }

    /**
     * 找回密码
     */
    private void doFindPassw() {
        EmaUser.getInstance().setNickName(mEdtNameView.getText().toString());
        Intent intent = new Intent(Ema.getInstance().getContext(), WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_TITLE, "找回密码");
        intent.putExtra(WebViewActivity.INTENT_URL, Url.getWebUrlFindpasswUser());
        intent.putExtra(WebViewActivity.INTENT_TYPE, WebViewActivity.TYPE_FIND_LOGIN_PASSW);
        mActivity.startActivity(intent);
    }

    /**
     * 选择账户后的处理
     */
    private void doResultSelectAccount(Message msg) {
        mAutoUserInfoBean = (UserLoginInfoBean) msg.obj;
        //设置用户名为选中的用户
        setViews(mAutoUserInfoBean);
        if (mSelectPoupWindow != null) {
            mSelectPoupWindow.dismiss();
        }
    }

    /**
     * 如果删除的用户是当前显示的用户，则需要替换掉当前用户
     */
    private void doResultDeleteUser() {
        if (mUserInfoList != null && mUserInfoList.size() > 0) {
            if (!mUserInfoList.contains(mAutoUserInfoBean)) {
                mAutoUserInfoBean = mUserInfoList.get(0);
                setViews(mAutoUserInfoBean);
            }
        } else {
            setViews(null);
            if (mSelectPoupWindow != null) {
                mSelectPoupWindow.dismiss();
            }
        }
    }

    /**
     * 以检查sid的方式自动登录失败的处理
     */
    private void doResultSidIsOutOfDate() {
        mEdtPasswView.setText("");
        LOG.d(TAG, "sid 过期，需要重新输入密码进行登录");
        ToastHelper.toast(mActivity, "请重新输入密码登录");
        if (mUserInfoList != null && mUserInfoList.contains(mAutoUserInfoBean)) {
            mUserInfoList.remove(mAutoUserInfoBean);
        }
        // 登录失败，当前用户为空，清空当前用户的所有信息
        mEmaUser.clearUserInfo();
        // 自动登录失败，需要转化为正常的账号，密码登录
    }

    /**
     * 下拉选择账号
     */
    private void doSelectAccount() {
        if (mOptionAdapter.getCount() > 0) {
            mSelectPoupWindow.showAsDropDown(mLayoutPoupParentView, 0, -3);
        }
    }

    /**
     * 设置控件
     *
     * @param bean
     */
    private void setViews(UserLoginInfoBean bean) {
        if (bean == null) {
            mEdtNameView.setText("");
            mEdtPasswView.setText("");

        } else {
            mEdtNameView.setText(bean.getUsername());
            mEdtPasswView.setText(mPasswShowStr);
        }
    }

    /**
     * 登录
     */
    private void doLogin() {
        /*if (mEdtPasswView.getText().toString().equals(mPasswShowStr)) {
            loginAutoLogin();
        } else {}*/
            AccountLoginFirst();
    }

    /**
     * 手机注册
     */
    private void doRegistByPhone() {
        this.dismiss();
        RegisterByPhoneDialog.getInstance(Ema.getInstance().getContext()).show();
    }

    /**
     * 一键注册
     */
    private void doRegistByOneKey() {
        this.dismiss();
        //new RegisterDialog(Ema.getInstance().getContext()).show();   这是原来的：一键注册弹出一键注册的框，以及后续逻辑； 现改为以下新逻辑
        weakLoginFirst();
    }

    /**
     * 自动登录
     */
    private void loginAutoLogin() {
        // 显示登录进度条
        mProgress.showProgress("登录中...", false, false);

        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", mConfigManager.getAppId());
        params.put("sid", mAutoUserInfoBean.getSid());
        params.put("uuid", mAutoUserInfoBean.getUuid());
        params.put("channel", mConfigManager.getChannel());
        params.put("channelTag", mConfigManager.getChannelTag());
        params.put("device_id", mDeviceInfoManager.getDEVICE_ID());
        if (mFlagIsLoginByAnlaiye) {
            params.put("paltform", "aly");
        }
        LOG.d(TAG, "当前时间：" + UCommUtil.DateFormat(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
        long stamp = (int) (System.currentTimeMillis() / 1000);
        stamp = stamp - stamp % 600;
        String sign = mConfigManager.getAppId() + mAutoUserInfoBean.getSid()
                + mAutoUserInfoBean.getUuid() + stamp;
        sign = UCommUtil.MD5(sign);
        params.put("sign", sign);

        UCommUtil.testMapInfo(params);

        new HttpInvoker().postAsync(Url.getLoginUrlByCheckSid(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
                        try {
                            JSONObject json = new JSONObject(result);
                            int resultCode = json.getInt(HttpInvokerConst.RESULT_CODE);
                            Message msg = new Message();
                            switch (resultCode) {
                                case HttpInvokerConst.SDK_RESULT_SUCCESS:// 登录成功
                                    LOG.d(TAG, "自动登录成功");
                                    mEmaUser.setNickName(mAutoUserInfoBean.getUsername());
                                    //mEmaUser.setUUID(mAutoUserInfoBean.getUuid());
                                    //mEmaUser.setSid(mAutoUserInfoBean.getSid());
                                    mEmaUser.setLoginType(UserConst.LOGIN_WITH_SID);
                                    msg.what = CODE_SUCCESS;
                                    break;
                                case HttpInvokerConst.LOGIN_CHECK_SID_RESULT_SID_OVER:
                                case HttpInvokerConst.LOGIN_CHECK_SID_RESULT_SID_OVER_1:// Sid过期
                                    LOG.w(TAG, "Sid过期");
                                    msg.what = CODE_ACCESSID_LOST;
                                    msg.obj = json.getString("errmsg");
                                    break;
                                case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR://签名验证失败
                                    LOG.w(TAG, "签名验证失败");
                                    msg.what = CODE_FAILED;
                                    break;
                                case HttpInvokerConst.LOGIN_RESULT_URL_IS_NULL:// URL为空
                                    LOG.w(TAG, "url为空");
                                    msg.what = CODE_FAILED;
                                    break;
                                default:
                                    LOG.w(TAG, "登录失败");
                                    msg.what = CODE_FAILED;
                                    break;
                            }
                            mHandler.sendMessage(msg);
                        } catch (Exception e) {
                            mHandler.sendEmptyMessage(CODE_FAILED);
                            LOG.w(TAG, "loginAutoLogin error", e);
                        }
                    }
                });
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
}
