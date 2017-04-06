package com.emagroup.sdk;

/**
 * EMA平台供外部调用的一些常量
 * @author zhangyang
 *
 */
public class EmaConst {

	//----------------------------------------------------------------------------------submitgamerole
	public static final String SUBMIT_ROLE_ID = "sm_roleId";//角色信息
	public static final String SUBMIT_ROLE_NAME = "sm_roleName";//角色名称
	public static final String SUBMIT_ROLE_LEVEL = "sm_roleLevel";//角色级别
	public static final String SUBMIT_ZONE_ID = "sm_zoneId";//服务器id
	public static final String SUBMIT_ZONE_NAME = "sm_zoneName";//服务器名称
	public static final String SUBMIT_ROLE_CT = "sm_roleCreateTime";//创角时间
	public static final String SUBMIT_DATA_TYPE = "sm_dataType";//创角时间
	public static final String SUBMIT_EXT = "sm_ext";//创角时间


	public static final int PAY_ACTION_TYPE_PAY = 100;//支付操作
	public static final int PAY_ACTION_TYPE_RECHARGE = 101;//充值操作
	
	public static final int PAY_RESULT_SUCC = 200;//支付或者充值成功
	public static final int PAY_RESULT_FAILED = 201;//支付或者充值失败
	public static final int PAY_RESULT_CANCEL = 202;//用户退出 支付或者充值
	public static final int PAY_RESULT_OTHERS = 203;//其他情况，统一视为支付失败
	public static final int PAY_RESULT_DELAYED = 204;   //发货有延迟

	public static final String SUPPORT_QQ_PAY = "support_qqpay";
	public static final String SUPPORT_WX_PAY = "support_wxpay";

	//------------------------------------------------------------------------------------
	public static final String EMA_PAYINFO_PRODUCT_ID = "product_id";  //商品id
	public static final String EMA_PAYINFO_PRODUCT_COUNT = "product_count"; //商品数量
	public static final String EMA_GAMETRANS_CODE="gameTransCode"; //游戏数据透传字段

}
