package cn.emagroup.sdk.user;

import android.content.Context;
import cn.emagroup.sdk.Ema;
import cn.emagroup.sdk.comm.ConfigManager;
import cn.emagroup.sdk.utils.LOG;
import cn.emagroup.sdk.utils.ToastHelper;
import cn.emagroup.sdk.utils.UCommUtil;

public class UserUtil {

	private static final String TAG = "UserUtil";
	
	/**
	 * 获取是否是俺来也渠道
	 * @return
	 */
	public static boolean isAnlaiye(){
		return ConfigManager.getInstance(Ema.getInstance().getContext()).isAlyAccount();
	}
	
	/**
	 * 检查输入信息是否正常
	 * @return
	 */
	public static boolean checkLoginInputIsOk(Context context, String account, String passw){
		if(UCommUtil.isStrEmpty(account)){
			LOG.w(TAG, "输入账号为空....");
			ToastHelper.toast(context, "账号不能为空！！");
			return false;
		}
		if(!UCommUtil.isEmail(account)){//非邮箱账号
			if(account.length() > 16){
				LOG.w(TAG, "非邮箱账号长度应为5-16位！");
				ToastHelper.toast(context, "非邮箱账号长度应为5-16位！");
				return false;
			}else if(account.length() < 5){
				LOG.w(TAG, "非邮箱账号长度应为5-16位！");
				ToastHelper.toast(context, "非邮箱账号长度应为5-16位！");
				return false;
			}
		}
		if(UCommUtil.isStrEmpty(passw)){
			LOG.w(TAG, "输入密码为空...");
			ToastHelper.toast(context, "密码不能为空！！");
			return false;
		}
		if(passw.length() > 16 || passw.length() < 6){
			LOG.w(TAG, "密码长度不符合（6-16位）");
			ToastHelper.toast(context, "密码长度不符合（6-16位）");
			return false;
		}
		return true;
	}
	
	/**
	 * 检查电话号码输入是否正确 
	 * @param context
	 * @param phoneNum
	 * @return
	 */
	public static boolean checkPhoneInputIsOk(Context context, String phoneNum){
		if(UCommUtil.isStrEmpty(phoneNum)){
			LOG.w(TAG, "手机号码不能为空");
			ToastHelper.toast(context, "手机号码不能为空");
			return false;
		}
		
		if(!UCommUtil.isPhone(phoneNum)){
			LOG.w(TAG, "电话号码格式不正确");
			ToastHelper.toast(context, "手机号码不正确");
			return false;
		}
		
		return true;
	}

}
