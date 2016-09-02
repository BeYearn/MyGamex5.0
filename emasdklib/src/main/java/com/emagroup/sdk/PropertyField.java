package com.emagroup.sdk;

public class PropertyField {

	//游戏钱币名称
	public static final String EMA_COIN_UNIT = "柠檬";
	
	//保存自动登录列表的配置文件
	public static final String LOGING_INFO_SP = "login_info";
	public static final String LOGING_INFO_LIST = "login_info_list";
	
	public static final String APP_ID = "app_id";
	public static final String SEND_CHANNEL_ID = "chid";
	public static final String SEND_DEVICE_ID = "devid";
	public static final String UUID = "uuid";
	public static final String IP = "ip";
	public static final String GAME_SERVER_ID = "serverid";
	public static final String ROLE_ID = "roleid";
	public static final String ROLE_NAME = "rolename";
	public static final String ROLE_SEX = "rolesex";
	public static final String ROLE_TYPE = "roletype";//角色类别
	public static final String ROLE_CAMP = "";//角色阵营
	public static final String MEMO = "memo";//备注
	
	public static final String ERROR_PASSW_INCORRECT_LENGTH = "密码长度不符合(6-16位)";
	public static final String ERROR_PASSW_CAN_NOT_NULL = "密码不能为空";
	public static final String ERROR_PASSW_ERROR = "密码错误";
	public static final String ERROR_PASSW_CHECK_FAILED = "验证失败";
	
	//指定密码输入的字符
	public static final char[] PASSW_DIGITS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','_','.','(',')','{','}','-','+','=','~','`','!','@','#','$','%','^','&','*','?','/','\\','|','[',']','<','>'};
	
	//广播
	public static final String BROADCAST_RECHARGE_SUCC = "recharge_succ";//充值成功后的广播
	
}
