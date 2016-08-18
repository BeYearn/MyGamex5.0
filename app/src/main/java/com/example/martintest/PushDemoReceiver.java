package com.example.martintest;

import cn.emagroup.sdk.utils.LOG;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PushDemoReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		LOG.d("PushDemoReceiver", "push test get info ");
	}

}
