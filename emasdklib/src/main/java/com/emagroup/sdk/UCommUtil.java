package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class UCommUtil {

	private static final String TAG = "UCommUtil";

	/**
	 * 发送信息
	 * @param handler
	 * @param code
	 * @param content
	 */
	public static void sendMesg(Handler handler, int code, Object content){
		if(content == null){
			handler.sendEmptyMessage(code);
			return;
		}
		if(content instanceof String){
			if(UCommUtil.isStrEmpty((String)content)){
				handler.sendEmptyMessage(code);
				return;
			}
		}
		Message msg = new Message();
		msg.what = code;
		msg.obj = content;
		handler.sendMessage(msg);
	}
	
	/**
	 * 在支付时，获取订单号发生订单号重复的问题时，显示系统繁忙
	 */
	public static void showSystemBusyDialog(final Context context) {
		((Activity) Ema.getInstance().getContext())
				.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						new EmaDialogSystemBusy(context).show();
					}
				});
	}

	/**
	 * Just For Test
	 * 
	 * @param map
	 */
	public static void testMapInfo(Map<String, String> map) {
		for (String key : map.keySet()) {
			LOG.d(TAG, "key_:" + key + "    value__:" + map.get(key));
		}
	}

	/**
	 * 拼接url
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	public static String buildUrl(String url, Map<String, String> map) {
		if (map == null || map.size() == 0) {
			return url;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(url);
		int i = 0;
		for (String key : map.keySet()) {
			if (i == 0) {
				sb.append("?");
			} else {
				sb.append("&");
			}
			sb.append(key).append("=").append(map.get(key));
			i++;
		}
		LOG.d(TAG, "test_url___:" + sb.toString());
		return sb.toString();
	}

	/**
	 * 获取指定长度的随机字符串(个数不超过32位)
	 * 
	 * @return
	 */
	public static String getRandomStr(int length) {
		Random random = new Random(System.currentTimeMillis());
		String str = MD5(random.toString());
		if (length >= 32)
			length = 32;
		if (str.length() >= length)
			str = str.substring(0, length);
		return str;
	}

	/**
	 * 获取32位的随机字符串
	 * 
	 * @return
	 */
	public static String getRandomStr() {
		return getRandomStr(32);
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 判断字符是否是中文
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isCN(String str) {
		try {
			byte[] bytes = str.getBytes("UTF-8");
			if (bytes.length == str.length()) {
				return false;
			} else {
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 过滤输入密码的特殊字符
	 * 
	 * @param str
	 * @return
	 * @throws PatternSyntaxException
	 */
	public static String loginPasswStringFilter(String str)
			throws PatternSyntaxException {
		String regEx = "[ ()\"']";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("");
	}

	/**
	 * 设置用户相关信息回调
	 * 
	 * @param msgCode
	 * @param msgObj
	 */
	public static void makeUserCallBack(int msgCode, String msgObj) {
		Ema.getInstance().makeCallBack(msgCode, msgObj);
	}

	/**
	 * 设置支付相关信息回调
	 * 
	 * @param msgCode
	 * @param msgObj
	 */
	public static void makePayCallBack(int msgCode, Object msgObj) {
		EmaPay.getInstance(Ema.getInstance().getContext()).makePayCallback(
				msgCode, msgObj);
	}

	/**
	 * 获取签名
	 */
	public static String getSign(String appid, String sid, String uuid,
			String appkey) {
		long stamp = getTimeStamp();
		String sign = appid + sid + uuid + stamp + appkey;
		sign = MD5(sign);
		return sign;
	}

	/**
	 * 获取当前时间（精确到二十分钟内），作为验证
	 * 
	 * @return
	 */
	public static long getTimeStamp() {
		long stamp = (int) (System.currentTimeMillis() / 1000);
		stamp = stamp - stamp % 1200;
		return stamp;
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isStrEmpty(String str) {
		if (str == null || str.trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是邮箱
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmail(String str) {
		if (isStrEmpty(str))
			return false;
		String mode = "";
		int position = str.indexOf('@');
		if (position == 1)
			mode = "^[a-z0-9A-Z]+\\@[a-z0-9A-Z]+[.]{1}[a-z0-9A-Z]+\\w*[.]*\\w*[a-zA-Z]+$";
		else
			mode = "^[a-z0-9A-Z]+[-+._a-z0-9A-Z]*[a-z0-9A-Z]+\\@[a-z0-9A-Z]+[.]{1}[a-z0-9A-Z]+\\w*[.]*\\w*[a-zA-Z]+$";
		Pattern pattern = Pattern.compile(mode);
		Matcher matcher = pattern.matcher(str);
		return matcher.find();
	}

	/**
	 * 判断是否是手机号码
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isPhone(String str) {
		if (isStrEmpty(str)) {
			return false;
		} else {
			Pattern pattern = Pattern
					.compile("^((13[0-9])|(14[0,9])|(15[0-9])|(16[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");
			Matcher matcher = pattern.matcher(str);
			return matcher.find();
		}
	}

	private static final String ALGORITHM = "RSA";

	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	private static final String DEFAULT_CHARSET = "UTF-8";

	public static String aliSign(String content, String privateKey) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature
					.getInstance(SIGN_ALGORITHMS);

			signature.initSign(priKey);
			signature.update(content.getBytes(DEFAULT_CHARSET));

			byte[] signed = signature.sign();
			String sign = Base64.encode(signed);
			LOG.d(TAG, "sign__:" + sign);
			return sign;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 返回md5加密后的字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String MD5(String str) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(str.getBytes("UTF-8"));
			byte bytes[] = messageDigest.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < bytes.length; i++)
				if (Integer.toHexString(0xff & bytes[i]).length() == 1)
					sb.append("0").append(Integer.toHexString(0xff & bytes[i]));
				else
					sb.append(Integer.toHexString(0xff & bytes[i]));
			return sb.toString().toUpperCase();
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * 返回格式化后的时间格式
	 * 
	 * @param content
	 * @return
	 */
	public static String DateFormat(String content) {
		return DateFormat(Long.valueOf(content));
	}

	/**
	 * 返回格式化后的时间格式
	 * 
	 * @return
	 */
	public static String DateFormat(Date date) {
		SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.US);
		return sfd.format(date);
	}

	/**
	 * 返回格式化后的时间格式
	 * 
	 * @param content
	 * @return
	 */
	public static String DateFormat(Long content) {
		String time = content.toString();
		if (time.length() == 10) {
			time += "000";
		}
		if (time.length() > 13) {
			time = time.substring(0, 13);
		}
		content = Long.valueOf(time);
		SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.US);
		return sfd.format(new Date(content));
	}

	/**
	 * 返回指定格式的时间
	 * 
	 * @param content
	 * @param format
	 * @return
	 */
	public static String DateFormat(String content, String format) {
		return DateFormat(Long.valueOf(content), format);
	}

	/**
	 * 返回指定格式的时间
	 * 
	 * @param content
	 * @param format
	 * @return
	 */
	public static String DateFormat(Long content, String format) {
		String time = content.toString();
		if (time.length() == 10) {
			time += "000";
		}
		if (time.length() > 13) {
			time = time.substring(0, 13);
		}
		content = Long.valueOf(time);
		SimpleDateFormat sfd = new SimpleDateFormat(format, Locale.US);
		return sfd.format(new Date(content));
	}

}
