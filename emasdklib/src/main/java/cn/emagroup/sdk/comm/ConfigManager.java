package cn.emagroup.sdk.comm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cn.emagroup.sdk.utils.LOG;

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
				InputStream input = mContext.getAssets()
						.open("emapayconfig");
				InputStreamReader isReader = new InputStreamReader(input,
						"UTF-8");
				BufferedReader bReader = new BufferedReader(isReader);
				String content = null;
				while ((content = bReader.readLine()) != null) {
					if(content.startsWith("#"))
						continue;
						mPayTrdList.add(content);
				}
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
			mAppid = getIntegerFromMetaData(mContext, "EMA_APP_ID") + "";
		}
		return mAppid;
	}

	/**
	 * 获取签名KEY
	 *
	 */
	public String getAppKEY() {
		if(mAppKey == null){
			mAppKey = getStringFromMetaData(mContext, "EMA_PRIVATE_KEY");
		}
		return mAppKey;
	}

	/**
	 * 获取登录回调地址
	 * @return
	 */
	public String getRedirectUri() {
		if(mRedirectUri == null){
			mRedirectUri = getStringFromMetaData(mContext, "EMA_REDIRECTURL"); 
		}
		return mRedirectUri;
	}
	
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
			mChannel=getIntegerFromMetaData(mContext,"EMA_CHANNEL")+"";
		}
		return mChannel;
	}
	
	/**
	 * 初始化服务器地址，在sdk初始化的时候做
	 */
	public void initServerUrl(){
		//线上正式环境
		Url.setServerUrl(Url.SERVER_URL);
		Url.setWebUrl(Url.WEB_URL);
		ApplicationInfo appinfo = mContext.getApplicationInfo();
		String sourceDir = appinfo.sourceDir;
		ZipFile zipfile = null;
		try {
			zipfile = new ZipFile(sourceDir);
			Enumeration<?> entries = zipfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = ((ZipEntry) entries.nextElement());
				String entryName = entry.getName();
				if(entryName.equals("META-INF/env_dev")){//内部开发环境
					Url.setServerUrl(Url.SERVER_URL_ENV_DEV);
					Url.setWebUrl(Url.WEB_URL_DEV);
					break;
				}else if(entryName.equals("META-INF/test_dev")){//内部测试环境
					Url.setServerUrl(Url.SERVER_URL_TEST_DEV);
					Url.setWebUrl(Url.WEB_URL_DEV);
					break;
				}else if(entryName.equals("META-INF/env_online_test")){//线上测试环境
					Url.setServerUrl(Url.SERVER_URL_ENV_ONLINE_TEST);
					Url.setWebUrl(Url.WEB_URL_DEV);
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
	private String getStringFromMetaData(Context context, String key) {
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

}
