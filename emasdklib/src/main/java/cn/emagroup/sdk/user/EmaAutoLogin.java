package cn.emagroup.sdk.user;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.emagroup.sdk.Ema;
import cn.emagroup.sdk.comm.ConfigManager;
import cn.emagroup.sdk.comm.EmaCallBackConst;
import cn.emagroup.sdk.comm.EmaProgressDialog;
import cn.emagroup.sdk.comm.HttpInvoker;
import cn.emagroup.sdk.comm.HttpInvokerConst;
import cn.emagroup.sdk.comm.Url;
import cn.emagroup.sdk.ui.ToolBar;
import cn.emagroup.sdk.utils.LOG;
import cn.emagroup.sdk.utils.ToastHelper;
import cn.emagroup.sdk.utils.UCommUtil;
import cn.emagroup.sdk.utils.USharedPerUtil;

public class EmaAutoLogin {

    private static final String TAG = EmaAutoLogin.class.toString();
    private static final int CODE_SUCCESS = 3;// 成功
    private static final int CODE_FAILED = 4;// 失败
    private static final int CODE_ACCESSID_LOST = 7;// SID过期
    private static EmaAutoLogin mInstance;
    private final Context context;
    private EmaUser mEmaUser;
    private static String token;
    private final ConfigManager mConfigManager;
    private final EmaProgressDialog mProgress;


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case HttpInvokerConst.SDK_RESULT_SUCCESS:// 登录成功，并保存用户的信息
                    mProgress.closeProgress();
                    doSetUser();
                    break;
                case HttpInvokerConst.SDK_RESULT_FAILED:// 登录失败（原因异常）
                    UCommUtil.makeUserCallBack(EmaCallBackConst.LOGINFALIED, "请重新登录");
                    mProgress.closeProgress();
                    new RegisterByPhoneDialog(context).show();
                    break;
            }
        }
    };

    private void doSetUser() {
        String uid = (String) USharedPerUtil.getParam(context, "uid", "");
        String nickname = (String) USharedPerUtil.getParam(context, "nickname", "");
        int accountType = (int) USharedPerUtil.getParam(context, "accountType", 88);

        if (!UCommUtil.isStrEmpty(uid) && !UCommUtil.isStrEmpty(nickname)) {
            // 当前登录用户信息
            mEmaUser = EmaUser.getInstance();
            mEmaUser.setmUid(uid);
            mEmaUser.setNickName(nickname);
            mEmaUser.setmToken(token);
            mEmaUser.setAccountType(accountType);
            LOG.e("autologin", uid + "..." + nickname + "..." + accountType + "..." + token);

            // 显示登录成功后的对话框
            if(!((Activity) context).isFinishing()) {
                new LoginSuccDialog(context, true).start();
            }else {  // 这个是个临时解决办法--------就是这个context被回收的问题
                ToastHelper.toast(context,"已登录成功");
                UCommUtil.makeUserCallBack(EmaCallBackConst.LOGINSUCCESS, "登录成功");
                ToolBar.getInstance(Ema.getInstance().getContext()).showToolBar();
            }
        } else {
            new RegisterByPhoneDialog(context).show();
            UCommUtil.makeUserCallBack(EmaCallBackConst.LOGINFALIED, "自动登录失败");
        }
    }


    public static EmaAutoLogin getInstance(Context context){
        if(mInstance == null){
            synchronized (new Object()) {
                if(mInstance == null){
                    mInstance = new EmaAutoLogin(context);

                }
            }
        }
        return mInstance;
    }
    private EmaAutoLogin(Context context){
        this.context=context;
        mConfigManager = ConfigManager.getInstance(context);
        mProgress = new EmaProgressDialog(context);
    }
    /**
     * 判断是否可以自动登录
     *
     * @param
     * @return
     */
    public  boolean isAutoLogin() {
        token = (String) USharedPerUtil.getParam(context, "token", "");

        if (!UCommUtil.isStrEmpty(token)) {
            return true;
        }
        return false;
    }

    /**
     * 获取当前自动登陆的用户信息
     *
     * @return
     */
    private  UserLoginInfoBean getAutoLoginUser(Context context) {
        List<UserLoginInfoBean> list = USharedPerUtil.getUserLoginInfoList(context);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 自动登录
     *
     */
    public void doLoginAuto() {
        if(!((Activity) context).isFinishing()) {
            //show dialog
            mProgress.showProgress("登录中...");
        }
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("appKey", mConfigManager.getAppKEY());
        new HttpInvoker().postAsync(Url.getCheckLoginUrl(), params, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {
                    JSONObject json = new JSONObject(result);
                    int resultCode = json.getInt("status");
                    switch (resultCode) {
                        case HttpInvokerConst.SDK_RESULT_SUCCESS:// 已登录
                            LOG.d(TAG, "自动登录成功！！");
                            mHandler.sendEmptyMessage(HttpInvokerConst.SDK_RESULT_SUCCESS);
                            break;
                        default:
                            LOG.d(TAG, json.getString("message"));
                            mHandler.sendEmptyMessage(HttpInvokerConst.SDK_RESULT_FAILED);
                            break;
                    }
                } catch (Exception e) {
                    LOG.w(TAG, "doSetPassw error", e);
                }
            }
        });
    }
}
