package cn.emagroup.sdk.pay;


import java.util.List;
import java.util.Map;

import cn.emagroup.sdk.comm.ResourceManager;
import cn.emagroup.sdk.utils.LOG;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PayTrdListAdapter extends BaseAdapter {

	private static final String TAG = PayTrdListAdapter.class.toString();
	
	private LayoutInflater inflater;
	private ResourceManager mResourceManager;
	private Context mContext;
	private List<PayTrdItemBean> mList;
	
	public PayTrdListAdapter(Context context, List<PayTrdItemBean> list){
		mContext = context;
		inflater = LayoutInflater.from(mContext);
		mResourceManager = ResourceManager.getInstance(mContext);
		mList = list;
	}
	
	@Override
	public int getCount() {
		if(mList == null)
			return 0;
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder = null;
		if(convertView == null){
			convertView = inflater.inflate(mResourceManager.getIdentifier("ema_pay_trd_item", "layout"), null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(mResourceManager.getIdentifier("ema_pay_trd_itemImage", "id"));
			holder.discountView = (TextView) convertView.findViewById(mResourceManager.getIdentifier("ema_pay_trd_discount", "id"));
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		PayTrdItemBean bean = (PayTrdItemBean) getItem(position);
		int drawableId = bean.getDrawableId();
		holder.imageView.setBackgroundResource(drawableId);
		if(bean.getDiscount() != 100){
			holder.discountView.setVisibility(View.VISIBLE);
			holder.discountView.setText(bean.getDiscount() / 10 + "折");
		}else{
			holder.discountView.setVisibility(View.GONE);
		}
		return convertView;
	}

	class ViewHolder{
		ImageView imageView;
		TextView discountView;
	}
	
}
