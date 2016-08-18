package cn.emagroup.sdk.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import cn.emagroup.sdk.user.UserLoginInfoBean;

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
}
