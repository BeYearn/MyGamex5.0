package com.emagroup.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.igexin.sdk.PushConsts;

public class PushDemoReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.d("PushDemoReceiver", "push test get info ");
        Bundle bundle = intent.getExtras();

        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_CLIENTID:

                String cid = bundle.getString("clientid");
                // 处理cid返回

                break;
            case PushConsts.GET_MSG_DATA:
                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null) {
                    String data = new String(payload);
                    LOG.e("个推透传数据",data);
                    //接收处理透传（payload）数据
                    EmaSDK.getInstance().makeCallBack(EmaCallBackConst.RECIVEMSG_MSG,data);
                }
                break;
            default:
                break;
        }
    }
}
