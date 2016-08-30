package cn.emagroup.sdk.comm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.igexin.sdk.PushConsts;

import cn.emagroup.sdk.utils.LOG;
import cn.emagroup.sdk.wrapper.EmaSDK;

public class PushDemoReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.d("PushDemoReceiver", "push test get info ");
        Bundle bundle = intent.getExtras();

        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_CLIENTID:

                String cid = bundle.getString("clientid");
                // TODO:处理cid返回

                break;
            case PushConsts.GET_MSG_DATA:
                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null) {
                    String data = new String(payload);
                    LOG.e("个推透传数据",data);
                    // TODO:接收处理透传（payload）数据

                    EmaSDK.getInstance().makeCallBack(EmaCallBackConst.RECIVEMSG_MSG,data);
                }
                break;
            default:
                break;
        }
    }
}
