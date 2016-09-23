package com.emagroup.sdk;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class EmaUser {

    private static EmaUser mInstance;

    private static String mNickName;//昵称
    private static String mUid;
    private static String mToken;
    private static int accountType;  //0 弱帐号 1 手机 2邮箱 3渠道
    private static String mBalance;//余额
    private static String email;
    private static String mobile;
    private static boolean mIsWalletHasPassw;//是否有支付密码

    private int mLoginType;//登录的方式（普通登录，手机登录）

    private boolean mFlagIsLogin;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        EmaUser.accountType = accountType;
    }

    public String getmUid() {
        return mUid;
    }

    public void setmUid(String mUid) {
        this.mUid = mUid;
    }

    public String getmToken() {
        return mToken;
    }

    public void setmToken(String mToken) {
        this.mToken = mToken;
    }

    private EmaUser() {}

    public static EmaUser getInstance() {
        if (mInstance == null) {
            mInstance = new EmaUser();
        }
        return mInstance;
    }

    /**
     * 退出登录后，清空所有用户信息
     */
    public void clearUserInfo() {
        mInstance=null;
    }

    /**
     * 情况用户的支付信息
     */
    public void clearPayInfo() {

    }

    /**
     * 保存当前登录用户的信息，最多保存5个
     *
     * @param context
     */
    protected void saveLoginUserInfo(Context context) {
        List<UserLoginInfoBean> list = USharedPerUtil.getUserLoginInfoList(context);

        UserLoginInfoBean bean = new UserLoginInfoBean();
        bean.setUsername(getNickName());
        bean.setLastLoginTime(System.currentTimeMillis());

        if (list == null) {
            list = new ArrayList<UserLoginInfoBean>();
        }

        int index = checkUserInList(bean, list);

        if (index == -1) {//当前用户 不在保存的用户列表中
            if (list.size() >= 5) {
                list.remove(4);
            }
        } else {
            list.remove(index);
        }
        list.add(0, bean);

        USharedPerUtil.saveUserLoginInfoList(context, list);
    }

    /**
     * 检查当前用户是否在 配置文件保存的登录用户列表中
     * 如果在返回用户所在列表的index
     * 否认返回 -1
     *
     * @param bean
     * @param list
     * @return
     */
    private int checkUserInList(UserLoginInfoBean bean, List<UserLoginInfoBean> list) {
        for (int i = 0; i < list.size(); i++) {
            if (bean.getUsername().equals(list.get(i).getUsername())) {
                return i;
            }
        }
        return -1;
    }


    protected void setLoginType(int loginType) {
        mLoginType = loginType;
    }

    public int getLoginType() {
        return mLoginType;
    }

    protected void setNickName(String nickName) {
        mNickName = nickName;
    }

    public String getNickName() {
        return mNickName;
    }

    protected void setIsLogin(boolean isLogin) {
        mFlagIsLogin = isLogin;
    }

    public boolean getIsLogin() {
        return mFlagIsLogin;
    }

    public void setBalance(String balance) {
        mBalance = balance;
    }

    public String getBalance() {
        return mBalance;
    }

    public void setIsWalletHasPassw(boolean hasWalletHasPassw) {
        mIsWalletHasPassw = hasWalletHasPassw;
    }

    public boolean getIsWalletHasPassw() {
        return mIsWalletHasPassw;
    }

}
