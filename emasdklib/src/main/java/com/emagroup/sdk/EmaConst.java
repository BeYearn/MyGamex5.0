package com.emagroup.sdk;

/**
 * EMA平台供外部调用的一些常量
 * @author zhangyang
 *
 */
public class EmaConst {
	
	public static final String EMA_SUBMIT_ROLE_ID = "role_id";//角色信息
	public static final String EMA_SUBMIT_ROLE_NAME = "role_name";//角色名称
	public static final String EMA_SUBMIT_ROLE_LEVEL = "role_level";//角色级别
	public static final String EMA_SUBMIT_SERVER_ID = "server_id";//服务器id
	public static final String EMA_SUBMIT_SERVER_NAME = "server_name";//服务器名称
	
	public static final int PAY_ACTION_TYPE_PAY = 100;//支付操作
	public static final int PAY_ACTION_TYPE_RECHARGE = 101;//充值操作
	
	public static final int PAY_RESULT_SUCC = 200;//支付或者充值成功
	public static final int PAY_RESULT_FAILED = 201;//支付或者充值失败
	public static final int PAY_RESULT_CANCEL = 202;//用户退出 支付或者充值
	public static final int PAY_RESULT_OTHERS = 203;//其他情况，统一视为支付失败
	public static final int PAY_RESULT_DELAYED = 204;   //发货有延迟
	

//------------------------------------------------------------------------------------
	public static final String EMA_PAYINFO_PRODUCT_ID = "product_id";  //商品id
	public static final String EMA_PAYINFO_PRODUCT_COUNT = "product_count"; //商品数量
	public static final String EMA_GAMETRANS_CODE="gameTransCode"; //游戏数据透传字段

}
