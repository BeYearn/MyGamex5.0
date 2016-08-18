package cn.emagroup.sdk.comm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import cn.emagroup.sdk.utils.LOG;
import cn.emagroup.sdk.utils.UCommUtil;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/*
 * 用于获取手机设备信息
 * 
 * 
 * */
public class DeviceInfoManager {

	private static final String TAG = "DeviceInfoManager";
	
	/** 没有网络 */
	public static final int NETWORKTYPE_INVALID = 0;
	/** wap网络 */
	public static final int NETWORKTYPE_WAP = 1;
	/** 2G网络 */
	public static final int NETWORKTYPE_2G = 2;
	/** 3G和3G以上网络，或统称为快速网络 */
	public static final int NETWORKTYPE_3G = 3;
	/** wifi网络 */
	public static final int NETWORKTYPE_WIFI = 4;
	
	/*******设备信息*******/
	//设备ID
	private String DEVICE_ID = "";
	//网络类型
	private int NETWORKTYPE=0;
	//游戏包名
	private String GAME_PACKAGENAME="";
	//游戏版本
	private int GAME_VERSIONCODE=0;
	//系统版本
	private int ANDROID_VERSIONCODE=0;
	//机型
	private String MODELS = "";
	//cpu 
	private String CPU ="";
	//总内存
	private long ALL_MEMORY=0;
	//可用内存
	private long FREE_MEMORY=0;
	//屏幕分辨率
	private String SCREENINFO=""; 
	//运营商类型
	private String OPERATOR="";
	//手机号
	private String PHONENUM="";
	//IP地址
	private String IP="";
	
	private Context mContext;
	private static final Object synchron = new Object();

	private static DeviceInfoManager mInstance;

	public static DeviceInfoManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (synchron) {
				if(mInstance == null){
					mInstance = new DeviceInfoManager(context);
				}
			}
		}
		return mInstance;
	}

	private DeviceInfoManager(Context context){
		mContext = context;
	}
	
	// 收集手机信息
	public Map<String, String> deviceInfoGather() {
		Map<String, String> parameter = new HashMap<String, String>();
		
		parameter.put("app_id", ConfigManager.getInstance(mContext).getAppId());//appId 
		parameter.put("devid", this.getDEVICE_ID());		//设备ID
		parameter.put("chid", ConfigManager.getInstance(mContext).getChannel());//渠道ID
		parameter.put("appname", this.getPACKAGENAME());		//包名
		parameter.put("appver", String.valueOf(this.getGAMEVERSIONCODE()));//游戏版本号
		parameter.put("dev_mod", this.getMODELS());					//机型
		parameter.put("cpu", this.getCpuInfo());					//CPU信息
		parameter.put("mem", String.valueOf(this.getTotalMemory()));//总内存
		parameter.put("ava_mem", String.valueOf(this.getAvailMemory()));//可用内存
		parameter.put("sys_mod", "1");									//系统类型，Android OR iOS
		parameter.put("sys_ver",String.valueOf(this.getANDROIDVERSIONCODE()));//系统版本
		parameter.put("pb", "0");											//越狱
		parameter.put("reso", this.getScreenInfo());						//屏幕信息
		parameter.put("net_mod", String.valueOf(this.getNetworkType()));	//网络信息
		parameter.put("operator", this.getOPERATOR());						//运营商信息
		parameter.put("phone", this.getPHONENUM());							//手机号
		
		return parameter;
	}
	
	
	
	//设备ID
	public String getDEVICE_ID()
	{
		if(UCommUtil.isStrEmpty(DEVICE_ID)){
			TelephonyManager tm = (TelephonyManager) mContext.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			DEVICE_ID = tm.getDeviceId();
		}
		return DEVICE_ID;
	}

	// 获取CPU信息，返回CPU型号，频率
	public String getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2 = "";
		String[] cpuInfo = { "", "" }; // 1-cpu型号 //2-cpu频率
		String[] arrayOfString;
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			for (int i = 2; i < arrayOfString.length; i++) {
				cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
			}
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			cpuInfo[1] += arrayOfString[2];
			localBufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		CPU = "{"+ cpuInfo[0] + "," + cpuInfo[1]+"}";
		return CPU;
	}

	//获取网络类型
	@SuppressWarnings("deprecation")
	public int getNetworkType() {
		NETWORKTYPE = NETWORKTYPE_INVALID;
		Context context = mContext.getApplicationContext();
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			String type = networkInfo.getTypeName();
			if (type.equalsIgnoreCase("WIFI")) {
				NETWORKTYPE = NETWORKTYPE_WIFI;
			} else if (type.equalsIgnoreCase("MOBILE")) {
				String proxyHost = android.net.Proxy.getDefaultHost();
				NETWORKTYPE = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(context) ? NETWORKTYPE_3G
						: NETWORKTYPE_2G)
						: NETWORKTYPE_WAP;
			}
		}

		return NETWORKTYPE;
	}

	// 获取手机版本信息
	public String[] getVersion() {
		String[] version = { "", "", "", "", "" };
		String str1 = "/proc/version";
		String str2;
		String[] arrayOfString;
		version[0] = Build.MODEL;// model
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			version[1] = arrayOfString[2];// KernelVersion
			localBufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		version[2] = Build.VERSION.RELEASE;// firmware version
		version[3] = Build.DISPLAY;// system version
		version[4] = String.valueOf(Build.VERSION.SDK_INT); // android sdk
															// version
		return version;
	}

	// 获取android当前可用内存大小（Byte）
	public long getAvailMemory() {
		ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		FREE_MEMORY = mi.availMem;
		return FREE_MEMORY;
	}

	// 读取系统内存信息文件: meminfo第一行，获取系统总内存大小（Byte）
	public long getTotalMemory() {
		if(ALL_MEMORY == 0){
			String str1 = "/proc/meminfo";
			String str2;
			String[] arrayOfString;
			try {
				FileReader localFileReader = new FileReader(str1);
				BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
				str2 = localBufferedReader.readLine();
				
				arrayOfString = str2.split("\\s+");
				ALL_MEMORY = Long.valueOf(arrayOfString[1]) * 1024;
				localBufferedReader.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ALL_MEMORY;
	}

	// 获取屏幕分辨率, 返回屏幕宽，高，密度，密度dpi
	public String getScreenInfo() {
		// 这种方式在service中无法使用
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mWindowManager.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels; // 宽
		int height = dm.heightPixels; // 高
		float density = dm.density; // 屏幕密度（0.75 / 1.0 / 1.5）
		int densityDpi = dm.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
		SCREENINFO = "{"+width + "," + height + "," + density + "," + densityDpi+"}";
		return SCREENINFO;
	}

	//判断网络类型
	public boolean isFastMobileNetwork(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		switch (telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return false; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return false; // ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return false; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return true; // ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return true; // ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return false; // ~ 100 kbps
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return true; // ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return true; // ~ 700-1700 kbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return true; // ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return true; // ~ 400-7000 kbps
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			return true; // ~ 1-2 Mbps
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			return true; // ~ 5 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return true; // ~ 10-20 Mbps
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return false; // ~25 kbps
		case TelephonyManager.NETWORK_TYPE_LTE:
			return true; // ~ 10+ Mbps
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return false;
		default:
			return false;
		}
	}
	
	//游戏包名
	public String getPACKAGENAME()
	{
		GAME_PACKAGENAME = mContext.getPackageName();
		return GAME_PACKAGENAME;
	}
	
	//游戏版本
	public int getGAMEVERSIONCODE()
	{
		try {
			GAME_VERSIONCODE = mContext.getPackageManager().getPackageInfo(getPACKAGENAME(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return GAME_VERSIONCODE;
	}
	
	//系统版本
	public int getANDROIDVERSIONCODE()
	{
		ANDROID_VERSIONCODE = Build.VERSION.SDK_INT;
		return ANDROID_VERSIONCODE;
	}
	////机型
	public String getMODELS()
	{
		MODELS = Build.MODEL;
		return MODELS;
	}
	//运营商类型
	public String getOPERATOR()
	{
		TelephonyManager telManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		OPERATOR = telManager.getSimOperator();
		
		if(OPERATOR!=null){

			if(OPERATOR.equals("46000") || OPERATOR.equals("46002")|| OPERATOR.equals("46007")){
			//中国移动
				LOG.d(TAG, "运营商类型:中国移动");;
			}else if(OPERATOR.equals("46001")){
			//中国联通
				LOG.d(TAG, "运营商类型:中国联通");;
			}else if(OPERATOR.equals("46003")){
			//中国电信
				LOG.d(TAG, "运营商类型:中国电信");;
			}
			else
			{
				//未知运营商
				LOG.d(TAG, "运营商类型:未知运营商");;
			}
		}
		return OPERATOR;
	}
	
	
	
	//手机号
	public String getPHONENUM()
	{
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		PHONENUM = tm.getLine1Number();
		LOG.d(TAG, "手机号：" + PHONENUM);
		return PHONENUM;
	}
	
	//获取IP地址
	public String getIP()
	{
		if(IP == null){
			try {  
				for (Enumeration<NetworkInterface> en = NetworkInterface  
						.getNetworkInterfaces(); en.hasMoreElements();) {  
					NetworkInterface intf = en.nextElement();  
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {  
						InetAddress inetAddress = enumIpAddr.nextElement();  
						if (!inetAddress.isLoopbackAddress()) {
							IP = inetAddress.getHostAddress().toString();
							return IP;  
						}  
					}  
				}  
			} catch (SocketException ex) {  
				LOG.d(TAG, "WifiPreference IpAddress"+ex.toString());
			}
		}
			
		return IP;
	}
}
