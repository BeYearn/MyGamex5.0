package com.emagroup.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class USharedPerUtil {

	private static final String TAG = "USharedPerUtil";
	
	/**
	 * 保存用户登录信息，最多保存5个
	 */
	public static void saveUserLoginInfoList(Context context, List<UserLoginInfoBean> list){
		if(list != null){
			LOG.d(TAG, "save userinfo list size__:" + list.size());
		}
		if(list == null) return;
		SharedPreferences sp = context.getSharedPreferences(PropertyField.LOGING_INFO_SP, Context.MODE_PRIVATE);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(list);
			String contentBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
			Editor edit = sp.edit();
			edit.putString(PropertyField.LOGING_INFO_LIST, contentBase64);
			edit.commit();
		} catch (Exception e) {
			LOG.e(TAG, "saveUserLoginInfoList error", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<UserLoginInfoBean> getUserLoginInfoList(Context context){
		List<UserLoginInfoBean> list = null;
		SharedPreferences sp = context.getSharedPreferences(PropertyField.LOGING_INFO_SP, Context.MODE_PRIVATE);
		String contentBase64 = sp.getString(PropertyField.LOGING_INFO_LIST, "");
		if(UCommUtil.isStrEmpty(contentBase64)){
			return null;
		}
		try {
			byte[] base64 = Base64.decode(contentBase64, Base64.DEFAULT);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64);
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object obj = ois.readObject();
			if(obj instanceof List){
				list = (List<UserLoginInfoBean>) obj;
			}
		} catch (Exception e) {
			LOG.w(TAG, "getUserLoginInfoList error", e);
		}
		if(list != null){
			LOG.d(TAG, "get userinfo list size__:" + list.size());
		}
		return list;
	}
	
	/**
	 * 获取俺来也账号列表
	 * @param context
	 * @return
	 */
	public static List<UserLoginInfoBean> getUserLoginInfoListByAnlaiye(Context context){
		return getListByChannel(context, true);
	}
	
	/**
	 * 获取Ema账号列表
	 * @param context
	 * @return
	 */
	public static List<UserLoginInfoBean> getUserLoginInfoListByEma(Context context){
		return getListByChannel(context, false);
	}
	
	/**
	 * 获取不同渠道的列表
	 * @return
	 */
	private static List<UserLoginInfoBean> getListByChannel(Context context, boolean isAnlaiye){
		List<UserLoginInfoBean> list = getUserLoginInfoList(context);
		List<UserLoginInfoBean> _list = new ArrayList<UserLoginInfoBean>();
		if(list == null || list.size() == 0) return null;
		for(UserLoginInfoBean bean : list){
			if(isAnlaiye){
				if(bean.isAnlaiye()){
					_list.add(bean);
				}
			}else if(!bean.isAnlaiye()){
				_list.add(bean);
			}
		}
		return _list;
	}



	/**
	 * 保存在手机里面的文件名
	 */
	private static final String FILE_NAME = "emaSdkSpf";


	/**
	 * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
	 * @param context
	 * @param key
	 * @param object
	 */
	public static void setParam(Context context , String key, Object object){
		if(null==object){
			return;
		}
		String type = object.getClass().getSimpleName();
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();

		if("String".equals(type)){
			editor.putString(key, (String)object);
		}
		else if("Integer".equals(type)){
			editor.putInt(key, (Integer)object);
		}
		else if("Boolean".equals(type)){
			editor.putBoolean(key, (Boolean)object);
		}
		else if("Float".equals(type)){
			editor.putFloat(key, (Float)object);
		}
		else if("Long".equals(type)){
			editor.putLong(key, (Long)object);
		}

		editor.commit();
	}


	/**
	 * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
	 * @param context
	 * @param key
	 * @param defaultObject
	 * @return
	 */
	public static Object getParam(Context context , String key, Object defaultObject){
		String type = defaultObject.getClass().getSimpleName();
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

		if("String".equals(type)){
			return sp.getString(key, (String)defaultObject);
		}
		else if("Integer".equals(type)){
			return sp.getInt(key, (Integer)defaultObject);
		}
		else if("Boolean".equals(type)){
			return sp.getBoolean(key, (Boolean)defaultObject);
		}
		else if("Float".equals(type)){
			return sp.getFloat(key, (Float)defaultObject);
		}
		else if("Long".equals(type)){
			return sp.getLong(key, (Long)defaultObject);
		}

		return null;
	}

}
