package cn.emagroup.sdk.user;

import android.app.Activity;
import android.content.Context;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.emagroup.sdk.comm.ConfigManager;
import cn.emagroup.sdk.comm.EmaProgressDialog;
import cn.emagroup.sdk.comm.HttpInvoker;
import cn.emagroup.sdk.comm.HttpInvokerConst;
import cn.emagroup.sdk.comm.Url;
import cn.emagroup.sdk.utils.LOG;
import cn.emagroup.sdk.utils.UCommUtil;
import cn.emagroup.sdk.utils.USharedPerUtil;

public class EmaAutoLogin {

    private static final String TAG = EmaAutoLogin.class.toString();
    private static final int CODE_SUCCESS = 3;// 成功
    private static final int CODE_FAILED = 4;// 失败
    private static final int CODE_ACCESSID_LOST = 7;// SID过期
    private EmaUser mEmaUser;
    private static String token;

    /**
     * 判断是否可以自动登录
     *
     * @param context
     * @return
     */
    public static boolean isAutoLogin(Context context) {
        /*List<UserLoginInfoBean> list =  USharedPerUtil.getUserLoginInfoList(context);
        if(list != null && list.size() > 0){
			UserLoginInfoBean bean = list.get(0);
			if(!UCommUtil.isStrEmpty(bean.getSid())){
				return true;
			}
		}
		return false;*/
        ConfigManager mConfigManager = ConfigManager.getInstance(context);
        final boolean[] isAuto = {false};
        final EmaProgressDialog mProgress = new EmaProgressDialog(context);
        token = (String) USharedPerUtil.getParam(context, "token", "");

        if (!UCommUtil.isStrEmpty(token)) {
            mProgress.showProgress("登录中...");
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
                                LOG.d(TAG, "已登录！！");
                                isAuto[0] = true;
                                break;
                            default:
                                LOG.d(TAG, json.getString("message"));
                                break;
                        }
                        mProgress.closeProgress();
                    } catch (Exception e) {
                        LOG.w(TAG, "doSetPassw error", e);
                    }
                }
            });
        }
        return isAuto[0];
    }

    /**
     * 获取当前自动登陆的用户信息
     *
     * @return
     */
    private static UserLoginInfoBean getAutoLoginUser(Context context) {
        List<UserLoginInfoBean> list = USharedPerUtil.getUserLoginInfoList(context);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 自动登录
     *
     * @param context
     */
    public  void doLoginAuto(final Context context) {

        String uid = (String) USharedPerUtil.getParam(context, "uid", "");
        String nickname = (String) USharedPerUtil.getParam(context, "nickname", "");
        int accountType= (int) USharedPerUtil.getParam(context,"accountType",88);

        if (!UCommUtil.isStrEmpty(uid) && !UCommUtil.isStrEmpty(nickname)) {
            // 当前登录用户信息
            mEmaUser = EmaUser.getInstance();
            mEmaUser.setmUid(uid);
            mEmaUser.setNickName(nickname);
            mEmaUser.setmToken(token);
            mEmaUser.setAccountType(accountType);
            LOG.e("autologin",uid+"..."+nickname+"..."+accountType+"..."+token);
            // 显示登录成功后的对话框
            new LoginSuccDialog((Activity) context, true).start();
        } else {
            new RegisterByPhoneDialog(context).show();

        }


        /*// 显示登录进度条
        final EmaProgressDialog mProgress = new EmaProgressDialog(context);
        mProgress.showProgress("登录中...", false, false);

        DeviceInfoManager mDeviceInfoManager = DeviceInfoManager.getInstance(context);// 设备信息管理
        ConfigManager mConfigManager = ConfigManager.getInstance(context);// 配置项管理
        final EmaUser mEmaUser = EmaUser.getInstance();// 当前登录用户信息
        final UserLoginInfoBean mAutoUserInfoBean = getAutoLoginUser(context);

        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", mConfigManager.getAppId());
        params.put("sid", mAutoUserInfoBean.getSid());
        params.put("uuid", mAutoUserInfoBean.getUuid());
        params.put("channel", mConfigManager.getChannel());
        params.put("device_id", mDeviceInfoManager.getDEVICE_ID());
        long stamp = (int) (System.currentTimeMillis() / 1000);
        stamp = stamp - stamp % 600;
        String sign = mConfigManager.getAppId() + mAutoUserInfoBean.getSid()
                + mAutoUserInfoBean.getUuid() + stamp;
        sign = UCommUtil.MD5(sign);
        params.put("sign", sign);
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CODE_SUCCESS:
                        // 显示登录成功后的对话框
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new LoginSuccDialog(context, true).start();
                            }
                        });
                        break;
                    //失败调用登陆界面
                    case CODE_ACCESSID_LOST:
                    case CODE_FAILED:
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new LoginDialog(context).show();
                            }
                        });
                        break;
                    case EmaProgressDialog.CODE_LOADING_START://显示进度条
                        mProgress.showProgress((String) msg.obj);
                        break;
                    case EmaProgressDialog.CODE_LOADING_END://关闭进度条
                        mProgress.closeProgress();
                        break;
                }
            }
        };
        new HttpInvoker().postAsync(Url.getLoginUrlByCheckSid(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
                        try {
                            JSONObject json = new JSONObject(result);
                            int resultCode = json
                                    .getInt(HttpInvokerConst.RESULT_CODE);
                            Message msg = new Message();
                            switch (resultCode) {
                                case HttpInvokerConst.SDK_RESULT_SUCCESS:// 登录成功
                                    LOG.d(TAG, "自动登录成功");
                                    mEmaUser.setCode(json.getString("code"));
                                    mEmaUser.setNickName(mAutoUserInfoBean
                                            .getUsername());
                                    mEmaUser.setUUID(mAutoUserInfoBean.getUuid());
                                    mEmaUser.setSid(mAutoUserInfoBean.getSid());
                                    mEmaUser.setLoginType(UserConst.LOGIN_WITH_SID);
                                    msg.what = CODE_SUCCESS;
                                    break;
                                case HttpInvokerConst.LOGIN_CHECK_SID_RESULT_SID_OVER:
                                case HttpInvokerConst.LOGIN_CHECK_SID_RESULT_SID_OVER_1:// Sid过期
                                    LOG.w(TAG, "Sid过期");
                                    msg.what = CODE_ACCESSID_LOST;
                                    msg.obj = json.getString("errmsg");
                                    break;
                                case HttpInvokerConst.SDK_RESULT_FAILED_SIGIN_ERROR:// 签名验证失败
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
                });*/
    }
}
