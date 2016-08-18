package cn.emagroup.sdk.pay;

public class PayConst {
	
	//支付限额的最大值
	public static final int PAY_MAX_LIMIT = 100000;

	//支付 | 充值 的方法渠道 ID
	public static final int PAY_CHARGE_CHANNEL = 100;// 柠檬水支付
	public static final int PAY_CHARGE_CHANNEL_GAMECARD = 9000;//游戏卡 支付|充值
	//目前除了盛大卡，其他卡都是不支持一张卡多次充值的
	public static final int PAY_CHARGE_CHANNEL_GAMECARD_SHENGDA = 9001;//盛大卡
	public static final int PAY_CHARGE_CHANNEL_GAMECARD_JUNWANG = 9003;//骏网一卡通
	public static final int PAY_CHARGE_CHANNEL_GAMECARD_WANMEI = 9004;//完美一卡通
	public static final int PAY_CHARGE_CHANNEL_GAMECARD_ZONGYOU = 9005;//纵游一卡通
	public static final int PAY_CHARGE_CHANNEL_GAMECARD_SOHU = 9006;//搜狐一卡通
	public static final int PAY_CHARGE_CHANNEL_GAMECARD_ZHENGTU = 9007;//征途一卡通
	public static final int PAY_CHARGE_CHANNEL_GAMECARD_WANGYI = 9008;//网易一卡通
	public static final int PAY_CHARGE_CHANNEL_GAMECARD_TENCENT = 9009;//腾讯Q币卡
	public static final int PAY_CHARGE_CHANNEL_GAMECARD_JIUYOU = 9010;//久游一卡通
	//手机卡支付
	public static final int PAY_CHARGE_CHANNEL_PHONECARD = 4000;//游戏卡 支付|充值
	public static final int PAY_CHARGE_CHANNEL_PHONECARD_LIANTONG = 4001;//联通
	public static final int PAY_CHARGE_CHANNEL_PHONECARD_DIANXIN = 4002;//电信
	public static final int PAY_CHARGE_CHANNEL_PHONECARD_YIDONG = 4004;//移动
	
	public static final int PAY_CHARGE_CHANNEL_TENPAY = 9028;//财付通充值方式ID
	
	public static final int PAY_CHARGE_CHANNEL_ALIPAY = 30008;//支付宝充值方式ID
	
	public static final int PAY_CHARGE_CHANNEL_WEIXINPAY = 40001;//微信支付方式ID

	public static final int PAY_CHARGE_CHANNEL_0YUANFU = 10003;//0元付支付方式ID
	
	//设置或修改密码(自定义跟服务器没有联系)
	public static final int CODE_SET_PASSW_SUCC = 1001;//成功
	public static final int CODE_SET_PASSW_CANCEL = 1002;//取消
	public static final int CODE_SET_PASSW_FAILED = 1003;//失败
	
	public static final String PAY_TRD_QIANBAO = "wallet";//代表钱包
	public static final String PAY_TRD_TENPAY = "tenpay_wap_bank";//代表财付通
	public static final String PAY_TRD_GAMECARD = "sdopay_card";//代表游戏卡
	public static final String PAY_TRD_PHONE_CARD = "mobile";//代表手机卡
	public static final String PAY_TRD_ALIPAY = "alipay_mobile";//支付宝支付
	public static final String PAY_TRD_WEIXIN = "weixin";//微信支付
	public static final String PAY_TRD_0YUANFU = "lingyuanfu";//0元付
	
	//跳转WebView的类别
	public static final int WEBVIEW_TYPE_TENPAY = 20;//财付通url请求的webView
	
	public static final int CODE_PAY_ALI_RESULT = 60;//支付宝的支付结果
	public static final int CODE_PAY_0YUANFU_RESULT = 70;//0元付的支付结果
	public static final int CODE_PAY_GET_TRD_PAY_LIST = 1000;//获取到了支付列表
	
}
