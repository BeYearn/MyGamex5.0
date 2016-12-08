package com.emagroup.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ConfigManager {

	private static final String TAG = "UConfigUtil";

	private static ConfigManager mInstance;
	private Context mContext;

	private String mAppid;
	private String mAppKey;
	private String mChannel;
	private String mRedirectUri;
	private String mMerchant;
	private List<String> mPayTrdList;
	
	
	private static final Object synchron = new Object();
	private String channelTag;
	private String wechatAppId;
	private String weiBoAppId;
	private String qqAppId;


	public static ConfigManager getInstance(Context context){
		if(mInstance == null){
			synchronized (synchron) {
				if(mInstance == null){
					mInstance = new ConfigManager(context);
				}
			}
		}
		return mInstance;
	}

	private ConfigManager(Context context){
		mContext = context;
		mAppid = null;
		mAppKey = null;
		mChannel = null;
		mRedirectUri = null;
		mMerchant = null;
		mPayTrdList = null;
	}
	
	/**
	 * 清空配置信息
	 */
	public void clear(){
		mAppid = null;
		mAppKey= null;
		mChannel = null;
		mRedirectUri = null;
		mPayTrdList = null;
	}
	
	/**
	 * 标记是否需要写入日志到sdcard
	 * @return
	 */
	public boolean isNeedLogToSdcard(){
		return hasDebugFile(mContext);
	}

	/**
	 * 初始化获取是否是俺来也渠道
	 * @return
	 */
	public boolean isAlyAccount(){
		ApplicationInfo appinfo = mContext.getApplicationInfo();
		String sourceDir = appinfo.sourceDir;
		ZipFile zipfile = null;
		try {
			zipfile = new ZipFile(sourceDir);
			Enumeration<?> entries = zipfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = ((ZipEntry) entries.nextElement());
				String entryName = entry.getName();
				if (entryName.equals("META-INF/aly")) {
					LOG.d(TAG, "is aly account");
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (zipfile != null) {
				try {
					zipfile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		LOG.d(TAG, "is not aly account");
		return false;
	}
	
	/**
	 * 读取asset目录下的支付配置文件
	 * @return
	 */
	public List<String> getPayTrdListInfo(){
		if(mPayTrdList == null){
			mPayTrdList = new ArrayList<String>();
			try {
				//下面这一段是从app module的assets中读取的配置然后决定支付顺序的；现在直接写在代码里（依然为了两边统一），日后需要再改回
				/*InputStream input = mContext.getAssets()
						.open("emapayconfig");
				InputStreamReader isReader = new InputStreamReader(input,
						"UTF-8");
				BufferedReader bReader = new BufferedReader(isReader);
				String content = null;
				while ((content = bReader.readLine()) != null) {
					if(content.startsWith("#"))
						continue;
						mPayTrdList.add(content);
				}*/
				mPayTrdList.add("alipay_mobile");
				mPayTrdList.add("weixin");
				mPayTrdList.add("tenpay_wap_bank");
				mPayTrdList.add("wallet");
				mPayTrdList.add("sdopay_card");
				mPayTrdList.add("mobile");
				mPayTrdList.add("lingyuanfu");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mPayTrdList;
	}
	
	/**
	 * 获取是否需要将日志输出到sdcard上
	 * @param context
	 * @return
	 */
	private boolean hasDebugFile(Context context){
		ApplicationInfo appinfo = context.getApplicationInfo();
		String sourceDir = appinfo.sourceDir;
		ZipFile zipfile = null;
		try {
			zipfile = new ZipFile(sourceDir);
			Enumeration<?> entries = zipfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = ((ZipEntry) entries.nextElement());
				String entryName = entry.getName();
				if (entryName.equals("META-INF/log_to_file")) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (zipfile != null) {
				try {
					zipfile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	/**
	 * 获取appid
	 */
	public String getAppId() {
		if(mAppid == null){
			mAppid = getStringFromMetaData(mContext, "EMA_APP_ID").substring(1);
		}
		return mAppid;
	}

	/**
	 * 获取签名KEY
	 *
	 */
	public String getAppKEY() {
		if(mAppKey == null){
			//mAppKey = getStringFromMetaData(mContext, "EMA_APP_KEY");
			mAppKey = EmaUser.getInstance().getAppKey();  //改为从网络获取，不再写到本地
			//mAppKey ="800a924c499772bac7b76432803ea47a";
		}
		return mAppKey;
	}

	/**
	 * 获取登录回调地址
	 * @return
	public String getRedirectUri() {
		if(mRedirectUri == null){
			mRedirectUri = getStringFromMetaData(mContext, "EMA_REDIRECTURL"); 
		}
		return mRedirectUri;
	}*/
	
	/**
	 * 获取0元付  诺诺镑客提供给商户商户号
	 * @return
	 */
	public String getMerchant(){
		if(mMerchant == null){
			mMerchant = getIntegerFromMetaData(mContext, "0YUANFU_MERCHANT") + "";
		}
		return mMerchant;
	}
	
	/**
	 * 获取渠道号
	 * 
	 */
	public String getChannel() {
		if(mChannel == null){
			//mChannel = getChannelFromApk(mContext);  原来的意欲何为？？？？？？
			mChannel=getStringFromMetaData(mContext,"EMA_CHANNEL_ID").substring(1);
		}
		return mChannel;
	}
	/**
	 * 获取渠道号tag
	 */
	public String getChannelTag(){
		if(channelTag == null){
			channelTag = getStringFromMetaData(mContext,"EMA_CHANNEL_TAG").substring(1);
		}
		return channelTag;
	}


	/**
	 * 初始化服务器地址，在sdk初始化的时候做
	 */
	public void initServerUrl(){

		String emaEnvi = getStringFromMetaData(mContext,"EMA_WHICH_ENVI");
		if("staging".equals(emaEnvi)){
			Url.setServerUrl(Url.STAGING_SERVER_URL);
		}else if("testing".equals(emaEnvi)){
			Url.setServerUrl(Url.TESTING_SERVER_URL);
		}else{
			Url.setServerUrl(Url.PRODUCTION_SERVER_URL);
		}
	}

	/**
	 * 获取渠道号的实现方法
	 * @param context
	 */
	private String getChannelFromApk(Context context){
		ApplicationInfo appinfo = context.getApplicationInfo();
		String sourceDir = appinfo.sourceDir;
		String ret = "";
		ZipFile zipfile = null;
		try {
			zipfile = new ZipFile(sourceDir);
			Enumeration<?> entries = zipfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = ((ZipEntry) entries.nextElement());
				String entryName = entry.getName();
				if (entryName.startsWith("META-INF/ema_channel_")) {
					ret = entryName;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (zipfile != null) {
				try {
					zipfile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if ("" != ret) {
			return ret.substring(ret.lastIndexOf("_") + 1);
		} else {
			return "1001";
		}
	}
	
	/**
	 * 根据key获取metaData的Integer类型的数据
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	private int getIntegerFromMetaData(Context context, String key){
		ApplicationInfo ai;
		int value = 0;
		try {
			ai = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			value = bundle.getInt(key);
		} catch (Exception e) {
			LOG.e(TAG, "参数设置错误, 请检查！");
			e.printStackTrace();
		}
		return value;
	}
	
	/**
	 * 根据key获取metaData的string类型的数据
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	public String getStringFromMetaData(Context context, String key) {
		ApplicationInfo ai;
		String value = null;
		try {
			ai = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			value = bundle.getString(key);
		} catch (Exception e) {
			LOG.e(TAG, "参数设置错误, 请检查！");
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * 获取versioncode 整数
	 * @param context
     * @return
     */
	public int getVersionCode(Context context){
		PackageManager packageManager=context.getPackageManager();
		PackageInfo packageInfo;
		int versionCode=0;
		try {
			packageInfo=packageManager.getPackageInfo(context.getPackageName(),0);
			versionCode=packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 获取微信appId
	 *
	 */
	public String getWachatAppId() {
		if(wechatAppId == null){
			wechatAppId=getStringFromMetaData(mContext,"WECHAT_APP_ID");
		}
		return wechatAppId;
	}

	/**
	 * 获取微博AppId
	 *
	 */
	public String getWeiBoAppId() {
		if(weiBoAppId == null){
			weiBoAppId =getStringFromMetaData(mContext,"WEI_BO_APP_ID").substring(1);
		}
		return weiBoAppId;
	}

	public String getQQAppId() {
		if(qqAppId == null){
			qqAppId =getStringFromMetaData(mContext,"QQ_APP_ID").substring(1);
		}
		return qqAppId;
	}



}
