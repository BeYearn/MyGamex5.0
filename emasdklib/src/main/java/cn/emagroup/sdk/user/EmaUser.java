package cn.emagroup.sdk.user;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.emagroup.sdk.pay.EmaPriceBean;
import cn.emagroup.sdk.utils.USharedPerUtil;

public class  EmaUser {

	private static EmaUser mInstance;
	private static final Object synchron = new Object();

	private String mNickName;//昵称
	private String mUid;
	private String mToken;

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

	private String mAccessSid;
	private String mUUID;

	private String mPhoneNum;
	private boolean mFlagIsLogin;
	private int mLoginType;//登录的方式（普通登录，手机登录）
	private boolean mFlagIsAnlaiye;//标记是否是俺来也账号
	
	//支付相关信息
	private int mBalance;//余额
	private boolean mIsWalletHasPassw;//是否有支付密码
	private EmaPriceBean mBalancePricebean;//跟余额关联

	private EmaUser(){
		mAccessSid = null;
		mUUID = null;
		mFlagIsLogin = false;
		mLoginType = UserConst.LOGIN_NULL;
		mBalance = 0;
		mBalancePricebean = new EmaPriceBean(0, EmaPriceBean.TYPE_FEN);
		mIsWalletHasPassw = false;
	}

	public static EmaUser getInstance(){
		if(mInstance == null){
			synchronized (synchron) {
				if(mInstance == null){
					mInstance = new EmaUser();

				}
			}
		}
		return mInstance;
	}

	/**
	 * 退出登录后，清空所有用户信息
	 */
	public void clearUserInfo() {
		mAccessSid = null;
		mUUID = null;
		mPhoneNum = null;
		mLoginType = UserConst.LOGIN_NULL;

		mFlagIsLogin = false;

		mUid=null;
		mNickName=null;
		mToken=null;
	}
	
	/**
	 * 情况用户的支付信息
	 */
	public void clearPayInfo(){
		mBalance = 0;
		mBalancePricebean.setPriceByFen(0);
		mIsWalletHasPassw = false;
	}
	
	/**
	 * 保存当前登录用户的信息，最多保存5个
	 * @param context
	 */
	protected void saveLoginUserInfo(Context context){
		List<UserLoginInfoBean> list = USharedPerUtil.getUserLoginInfoList(context);
		
		UserLoginInfoBean bean = new UserLoginInfoBean();
		bean.setUsername(getNickName());
		bean.setSid(getAccessSid());
		bean.setUuid(getUUID());
		bean.setLastLoginTime(System.currentTimeMillis());
		
		if(list == null){
			list = new ArrayList<UserLoginInfoBean>();
		}
		
		int index = checkUserInList(bean, list);
		
		if(index == -1){//当前用户 不在保存的用户列表中
			if(list.size() >= 5){
				list.remove(4);
			}
		}else{
			list.remove(index);
		}
		list.add(0, bean);
		
		USharedPerUtil.saveUserLoginInfoList(context, list);
	}
	
	/**
	 * 检查当前用户是否在 配置文件保存的登录用户列表中
	 * 如果在返回用户所在列表的index
	 * 否认返回 -1
	 * @param bean
	 * @param list
	 * @return
	 */
	private int checkUserInList(UserLoginInfoBean bean, List<UserLoginInfoBean> list){
		for(int i=0; i<list.size(); i++){
			if(bean.getUsername().equals(list.get(i).getUsername())){
				return i;
			}
		}
		return -1;
	}


	protected void setLoginType(int loginType) {
		mLoginType = loginType;
	}
	
	public int getLoginType(){
		return mLoginType;
	}

	
	protected void setUUID(String uuid) {
		mUUID = uuid;
	}
	
	public String getUUID() {
		return mUUID;
	}
	
	protected void setSid(String sid) {
		mAccessSid = sid;
	}
	
	public String getAccessSid() {
		return mAccessSid;
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
	
	public boolean getIsLogin(){
		return mFlagIsLogin;
	}

	protected void setPhoneNum(String phoneNum) {
		mPhoneNum = phoneNum;
	}
	
	public String getPhoneNum() {
		return mPhoneNum;
	}
	
	public EmaPriceBean getBalancePricebean(){
		return mBalancePricebean;
	}
	
	public void setBalance(int balance) {
		mBalance = balance;
		mBalancePricebean.setPriceByFen(balance);
	}
	
	public int getBalance(){
		return mBalance;
	}

	public void setIsWalletHasPassw(boolean hasWalletHasPassw){
		mIsWalletHasPassw = hasWalletHasPassw;
	}
	
	public boolean getIsWalletHasPassw(){
		return mIsWalletHasPassw;
	}
	
}
