package com.emagroup.sdk;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OptionsAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<UserLoginInfoBean> mList;
	private ResourceManager mResourceManager;
	private Handler mHandler;
	private Context mContext;
	
	public OptionsAdapter(Context context, Handler handler, List<UserLoginInfoBean> list){
		mContext = context;
		inflater = LayoutInflater.from(mContext);
		mHandler = handler;
		mList = list;
		mResourceManager = ResourceManager.getInstance(mContext);
	}
	
	public void setData(List<UserLoginInfoBean> list){
		this.mList = list;
	}
	
	@Override
	public int getCount() {
		if(mList == null)
			return 0;
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		ViewHolder holder = null;
		if(convertView == null){
			convertView = inflater.inflate(mResourceManager.getIdentifier("ema_option_item", "layout"), null);
			holder = new ViewHolder();
			holder.txtView = (TextView) convertView.findViewById(mResourceManager.getIdentifier("ema_txt_content", "id"));
			holder.imgView = (ImageView) convertView.findViewById(mResourceManager.getIdentifier("ema_btn_del", "id"));
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		final UserLoginInfoBean bean = mList.get(position);
		holder.txtView.setText(bean.getUsername());

		//下拉框的账号文本设置监听事件
		holder.txtView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				msg.what = LoginDialog.CODE_SET_AUTO_LOGIN;
				msg.obj = bean;
				mHandler.sendMessage(msg);
			}
		});
		//为下拉框选项删除图标部分设置事件，最终效果是点击将该选项删除
		holder.imgView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mList.remove(position);
				//保存删除后的用户列表
				USharedPerUtil.saveUserLoginInfoList(mContext, mList);
				notifyDataSetChanged();
				mHandler.sendEmptyMessage(LoginDialog.CODE_DELETE_USERINFO);
			}
		});
		
		return convertView;
	}
	
	class ViewHolder{
		TextView txtView;
		ImageView imgView;
	}

}
