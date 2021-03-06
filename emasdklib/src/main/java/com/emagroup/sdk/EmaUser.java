package com.emagroup.sdk;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class EmaUser {

    //太不专业啦，但是还是暂时先把appkey放到这里
    private static String appKey;
    public String getAppKey() {
        return appKey;
    }
    public void setAppKey(String appKey) {
        EmaUser.appKey = appKey;
    }

    //记录角色区服信息
    private static String gameRoleInfo;
    public String getGameRoleInfo(){
        return gameRoleInfo;
    }
    public void setGameRoleInfo(String info){
        EmaUser.gameRoleInfo = info;
    }

    private static EmaUser mInstance;

    private static String mNickName;//昵称
    private static String mUid;
    private static String mAlienceUid; //官网渠道AllianceUid和UId相同
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
        this.accountType = accountType;
    }


    public String getAllianceUid() {
        return mAlienceUid;
    }
    public void setAllianceUid(String aUid){
        this.mAlienceUid=aUid;
    }

    public String getmUid(){
        return mUid;
    }
    public void setmUid(String mUid) {
        this.mUid = mUid;
    }

    public String getToken() {
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
        mInstance=null;  //这样再getInstance就得到的是一个空的实例 妙   个蛋，上面字段都是static的！
        gameRoleInfo=null;
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
