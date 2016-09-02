package com.emagroup.sdk;

public class UserConst {
	// 记录登陆方式
	public static final int LOGIN_NULL = 0;//表明未登录，但最好不要靠这个来判断是否是登录状态
	public static final int LOGIN_DEFAULT = 100;// 未知登陆方式
	public static final int LOGIN_NORMAL = 101;// 普通登陆
	public static final int LOGIN_PHONE = 102;// 手机登陆
	public static final int LOGIN_WITH_SID = 103;//验证sid进行登录
	
	public static final int LOGIN_BY_EMA = 200;//Ema平台账号登陆
	public static final int LOGIN_BY_ANLAIYE = 201;//俺来也账号登陆
	
}
