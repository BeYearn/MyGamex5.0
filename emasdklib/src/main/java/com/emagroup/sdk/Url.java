package com.emagroup.sdk;

public class Url {

	public static final String WEB_URL = "https://wap.lemonade-game.com/";//外网
	public static final String WEB_URL_DEV = "https://wap.lemonade-game.com/";//内网暂时没有配好

	//所有请求url的域名
	private static String SERVER_URL_USE = "";

	private static String WEB_URL_USE = "";

	public static final String SERVER_URL = "http://api.emagroup.cn";//外网
//	public static final String SERVER_URL = "http://test.api.emagroup.cn";//测试环境
	public static final String SERVER_URL_ENV_DEV = "http://dev.api.emagroup.cn";//内部开发环境
//	public static final String SERVER_URL_ENV_DEV = "http://192.168.1.88";//内部开发环境
	public static final String SERVER_URL_TEST_DEV = "http://test.api.emagroup.cn";//内部测试环境
	public static final String SERVER_URL_ENV_ONLINE_TEST = "";//线上测试环境

//	//用户信息界面
//	public static final String WEB_URL_USERINFO = WEB_URL + "userinfo.html";
//	// 礼包界面
//	public static final String WEB_URL_GIFT = WEB_URL + "libao/libaolist.html";
//	// 帮助界面
//	public static final String WEB_URL_HELP = WEB_URL + "help/index.html";
//	//推广
//	public static final String WEB_URL_PROMOTION = WEB_URL + "bindtg.html";
//	//忘记支付密码界面
//	public static final String WEB_URL_FINDPASSW_WALLET = WEB_URL + "forgetwtpwd.html";
//	//忘记用户密码界面
//	public static final String WEB_URL_FINDPASSW_USER = WEB_URL + "forgetpwd.html";

	//预发环境
//	public static final String LINGYUANFU_IS_PAYMENT_ACTIVITY = "https://openapi.idc.nonobank.com/nono-web/creditPayment/isPaymentActivated";
//	public static final String LINGYUANFU_IS_PAYMENT_PASSWORD_EXIST  = "https://openapi.idc.nonobank.com/nono-web/creditAuth/isPaymentPasswordExist";
//	public static final String LINGYUANFU_UPDATE_PAYMENT_PASSWORD  = "https://openapi.idc.nonobank.com/nono-web/creditAuth/updatePaymentPassword";
//	public static final String LINGYUANFU_VERIFY_PAYMENT_PASSWORD  = "https://openapi.idc.nonobank.com/nono-web/creditAuth/verifyPaymentPassword";
//	public static final String LINGYUANFU_PAY  = "https://openapi.idc.nonobank.com/nono-web/creditPayment/pay";
//	public static final String URL_LINGYUANFU_PAYMENT_ACTIVITY = "https://openapi.idc.nonobank.com/h5/yima?openId=";

	//正式环境
	public static final String LINGYUANFU_IS_PAYMENT_ACTIVITY = "https://openapi.nonobank.com/nono-web/creditPayment/isPaymentActivated";
	public static final String LINGYUANFU_IS_PAYMENT_PASSWORD_EXIST  = "https://openapi.nonobank.com/nono-web/creditAuth/isPaymentPasswordExist";
	public static final String LINGYUANFU_UPDATE_PAYMENT_PASSWORD  = "https://openapi.nonobank.com/nono-web/creditAuth/updatePaymentPassword";
	public static final String LINGYUANFU_VERIFY_PAYMENT_PASSWORD  = "https://openapi.nonobank.com/nono-web/creditAuth/verifyPaymentPassword";
	public static final String LINGYUANFU_PAY  = "https://openapi.nonobank.com/nono-web/creditPayment/pay";
	public static final String URL_LINGYUANFU_PAYMENT_ACTIVITY = "https://openapi.nonobank.com/h5/yima?openId=";

	/**
	 * 获取礼包界面
	 * @return
	 */
	public static String getWebUrlGift(){
		return getWebUrl() + "libao/libaolist.html";
	}

	/**
	 * 获取帮助界面
	 * @return
	 */
	public static String getWebUrlHelp(){
		return getWebUrl() + "help/index.html";
	}
	/**
	 * 获取推广界面
	 * @return
	 */
	public static String getWebUrlPromotion(){
		return getWebUrl() + "bindtg.html";
	}

	/**
	 * 跳转成为推广员界面
	 * @return
	 */
	public static String getWeburlToBePromotion(){
		return getWebUrl() + "spread/login.html";
	}


	/**
	 * 收集创建的角色信息
	 * @return
	 */
	public static String getGatherInfoUrlGameRole(){
		return getServerUrl() + "/v1/gather/info/gamerole";
	}


	/**
	 * 收集游戏事件
	 * @return
	 */
	public static String getGatherInfoUrlGameEvent(){
		return getServerUrl() + "/v1/gather/info/gameevent";
	}

	/**
	 * 用户名密码进行登录的接口地址
	 * @return
	 */
	public static String getLoginUrlByPassw(){
		return getServerUrl() + "/oauth/authorize";
	}






// ------------------------------------------------------------------------------------------------------------------
	/**
	 * 获取创建弱账户的url  ----------不用这个接口了
	 * @return
	public static String getCreatWeakAcountUrl(){
		//return getServerUrl() + "";
		return "http://192.168.155.79:8080/member/createWeakAccount";
	}*/

	/**
	 * 获取绑定手机号信息界面
	 * @return
	 */
	public static String getWebUrlBinder(){
		//return getWebUrl() + "userinfo.html";
		return serverUrl+"/wap/bindphone.html";
	}

	/**
	 * 获取实名认证界面
	 * @return
	 */
	public static String getWebUrlIdentity(){
		return serverUrl+"/wap/certification.html";
	}

	/**
	 * 获取用户信息界面
	 * @return
	 */
	public static String getWebUrlUserInfo(){
		//return getWebUrl() + "userinfo.html";
		return serverUrl+"/wap/userinfo.html";
	}
	/**
	 * 忘记用户密码界面
	 * @return
	 */
	public static String getWebUrlFindpasswUser(){
		//return getWebUrl() + "forgetpwd.html";
		return serverUrl+"/wap/forgetpwd.html";
	}

	/**
	 * 忘记钱包支付密码界面
	 */
	public static String getWebUrlFindpasswWallet(){
		return serverUrl+"/wap/walletpwd.html";
	}

	//三个环境 常量
	public static final String PRODUCTION_SERVER_URL="https://platform.lemonade-game.com";
	public static final String STAGING_SERVER_URL="https://staging-platform.lemonade-game.com";
	public static final String TESTING_SERVER_URL="https://testing-platform.lemonade-game.com:8443";

	public static String serverUrl="https://platform.lemonade-game.com";

	public static void setServerUrl(String url){
		serverUrl=url;
	}

	public static String getUploadGameInfoUrl(){
		return serverUrl+"/ema-platform/member/uploadGameInfo";
	}

	/**
	 * 获取QQ登录接口
	 * @return
	 */
	public static String getQqLoginUrl(){

		return serverUrl+"/ema-platform/member/qqLogin";
	}

	/**
	 * 获取第一步登录请求接口
	 * @return
	 */
	public static String getFirstLoginUrl(){

		return serverUrl+"/ema-platform/member/pfLogin";
	}

	/**
	 * 获取验证码
	 * @return
	 */
	public static String getSmsUrl(){
		//return getServerUrl() + "";
		return serverUrl+"/ema-platform/notice/sendCaptcha";
	}

	/**
	 * 发送验证码邮件
	 * @return
	 */
	public static String getSendEmailUrl(){
		//return getServerUrl() + "";
		return serverUrl+"/ema-platform/notice/sendEmailCaptcha";
	}

	/**
	 * 发起购买接口
	 * @return
     */
	public static String getOrderStartUrl(){
		//return "http://192.168.155.79:8080/ema-platform/order/createOrder";
		return serverUrl+"/ema-platform/order/createOrder";
	}

	/**
	 * 取消订单接口
	 * @return
     */
	public static String getRejectOrderUrl(){
		return serverUrl+"/ema-platform/order/rejectOrder";
	}

	/**
	 * 设置支付密码
	 */
	public static String getSetWalletPwdUrl(){
		return serverUrl+"/ema-platform/billing/setChargePwd";
	}
    /**
     * 钱包确认支付
     */
    public static String getWalletPayUrl(){
		return serverUrl+"/ema-platform/order/confirmOrder";
	}
	/**
	 * 钱包确认支付，免密！！
	 */
	public static String getWalletPayNoPswUrl(){
		return serverUrl+"/ema-platform/order/confirmOrderSilient";
	}
	/**
	 * 验证登录状态
	 */
	public static  String getCheckLoginUrl(){
		return serverUrl+"/ema-platform/member/checkLogin";
	}


	/**
	 * 发送心跳包信息
	 * @return
	 */
	public static String getHeartbeatUrl(){
		//return serverUrl+"/ema-platform/member/heartbeat";
		return serverUrl+"/ema-platform/member/newheartbeat";
	}


	/**
	 * alipay:对支付信息进行签名
	 */
	public static String getSignedPayInfoUrl(){
		return serverUrl+"/ema-platform/sign/add";
	}
	/**
	 * alipay:支付宝的回调接口
	 */
	public static String getAliPayCallbackUrl(){

		return serverUrl+"/ema-platform/alipay/notifycallback";
	}

	/**
	 * 查询用户所有信息
	 */
	public static String getUserInfoUrl(){
		return serverUrl+"/ema-platform/member/getUserInfo";
	}

	/**
	 * 检查维护状态
	 */
	public static String getSDKStatusUrl(){

		//return serverUrl+"/ema-platform/admin/getSystemInfo";
		return serverUrl+"/ema-platform/admin/getSystemInfoEx";
	}

	/**
	 * 用来登录按钮打点的
	 * @return
     */
	public static String getUploadInfo(){
		return serverUrl+"/ema-platform/member/uploadEMAInfo";
	}

	/**
	 * 获取渠道key信息
     */
	public static String getChannelKayInfo(){
		return serverUrl+"/ema-platform/admin/channelKeyInfo";
	}

	/**
	 * qqwallet:创建预订单
	 */
	public static String getQQwalletPreOrder(){
		return serverUrl+"/ema-platform/order/createQQOrder";
	}
	/**
	 * weixinpay:创建预订单
	 */
	public static String getWeixinPayPreOrder(){
		return serverUrl+"/ema-platform/order/createWXOrder";
		//return "http://192.168.10.155:8080/ema-platform/order/createWXOrder";
	}

	/**
	 * 查询订单状态
	 */
	public static String checkOrderStatus(){
		return serverUrl+"/ema-platform/order/queryOrderStatusByOrderId";
	}
//-------------------------------------------------------------------------------------------------------------------------------------------



	/**
	 * 检查sid进行登录的接口地址
	 * @return
	 */
	public static String getLoginUrlByCheckSid(){
		return getServerUrl() + "/oauth/check_sid";
	}

	/**
	 * 手机登录（验证码）
	 * @return
	 */
	public static String getLoginUrlByPhone(){
		return getServerUrl() + "/v1/user/mobile_login";
	}

	/**
	 * 获取服务器默认分配的账户信息
	 * @return
	 */
	public static String getRegisterUrlAutoMakeAccount(){
		return getServerUrl() + "/v1/user/auto_make_account";
	}

	/**
	 * 普通注册
	 * @return
	 */
	public static String getRegisterUrlNomarl(){
		return getServerUrl() + "/v1/user/register";
	}

	/**
	 * 使用默认账户进行注册登录
	 * @return
	 */
	public static String getRegisterUrlAuto(){
		return getServerUrl() + "/v1/user/auto_register_account";
	}

	/**
	 * 获取验证码
	 * @return
	 */
	public static String getSendPhoneCodeUrl(){
		return getServerUrl() + "/v1/common/send_phone_code";
	}

	/**
	 * 获取用户信息（余额等。）
	 * @return
	 */
	public static String getPayUrlWalletsSeting(){
		return getServerUrl() + "/sdk/wallets/get_setting";
	}

	/**
	 * 设置或修改支付密码
	 * @return
	 */
	public static String getPayUrlSetPayPassw(){
		return getServerUrl() + "/sdk/wallets/change_pwd";
	}

	/**
	 * 检查密码是否正确
	 * @return
	 */
	public static String getPayUrlCheckPayPassw(){
		return getServerUrl() + "/sdk/wallets/check";
	}

	/**
	 * 修改支付限额
	 * @return
	 */
	public static String getPayUrlSetPayLimit(){
		return getServerUrl() + "/sdk/wallets/change_limit";
	}

	/**
	 * 支付 | 充值 的接口
	 * 接口判断操作类型的方法：
	 * 如果传入钱包余额参数 代表 【支付】操作
	 * 不传钱包余额参数 代表 【充值】操作
	 * @return
	 */
	public static String getPayUrlRecharge(){
		return getServerUrl() + "/sdk/pay/recharge";
//		return "http://192.168.1.88/sdk/pay/recharge";
	}

	/**
	 * 获取第三方支付列表
	 * @return
	 */
	public static String getPayTrdList(){
		return getServerUrl() + "/sdk/pay/get_charge_channel";
	}

	/**
	 * 初始化，发送服务器设备信息
	 * @return
	 */
	public static String getSendInfoUrlInitDeviceInfo(){
		return getServerUrl() + "/v1/gather/init";
	}

	/**
	 * 0元付支付成功后，通知服务器的接口
	 * @return
	 */
	public static String get0YuanFUNotifyUrl(){
		return getServerUrl() + "/sdk/pay/trade_query";
	}

	/**
	 * 登录成功后
	 * @return
	 */
	public static String getSendInfoUrlLoginSucc(){
		return getServerUrl() + "/v1/gather/login";
	}

	/**
	 * 注册成功后
	 * @return
	 */
	public static String getSendInfoUrlRegisterSucc(){
		return getServerUrl() + "/v1/gather/reg";
	}

	public static String getWebUrl(){
		return WEB_URL_USE;
	}

	/*public static void setWebUrl(String webUrl){
		WEB_URL_USE = webUrl;
	}*/

	public static String getServerUrl(){
		return SERVER_URL_USE;
	}

	/*public static void setServerUrl(String serverUrl){
		SERVER_URL_USE = serverUrl;
	}*/
}
