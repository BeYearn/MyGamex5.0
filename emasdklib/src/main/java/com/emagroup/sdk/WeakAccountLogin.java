package com.emagroup.sdk;

import android.content.Context;
import android.os.Handler;


/**
 * Created by Administrator on 2016/8/18.
 *
 * 想把两个diglog里面的快速登录抽取到这一个里面
 *
 */
public class WeakAccountLogin {

    private static final int CODE_SUCCESS = 3;// 成功
    private static final int CODE_FAILED = 4;// 失败

    private static final int GET_UID_SUCCESS = 31;// 第一步 创建弱账户获得uid陈功
    private static final int FIRST_STEP_LOGIN_SUCCESS = 32; // 第二步 第一步验证登陆成功
    private Context context;
    private WeakAccountLogin instance;
    private String userid;
    //private static final int SECOND_STEP_LOGIN_SUCCESS=33; // 第三步 第二步验证登陆成功  就是最后的登陆成功

    private WeakAccountLogin() {
    }

    private WeakAccountLogin(Context context) {
        this.context = context;
    }

    public WeakAccountLogin getInstance(Context context) {
        if (instance == null) {
            instance = new WeakAccountLogin(context);
        }
        return instance;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {

                case GET_UID_SUCCESS:
                    //weakLoginFirst();
                    break;
                case FIRST_STEP_LOGIN_SUCCESS:
                    //weakLoginSecond();
                    break;

            }
        }

    };

    public String registAndloginByOneKey() {
        //createWeakAccount();
        return "";
    }


    /*private void createWeakAccount() {

        Map<String, String> params = new HashMap<>();
        params.put("deviceType", "android");
        params.put("deviceKey", DeviceInfoManager.getInstance(context).getDEVICE_ID());
        new HttpInvoker().postAsync(Url.getCreatWeakAcountUrl(), params,
                new HttpInvoker.OnResponsetListener() {
                    @Override
                    public void OnResponse(String result) {
                        mHandler.sendEmptyMessage(EmaProgressDialog.CODE_LOADING_END);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject data = jsonObject.getJSONObject("data");
                            userid = data.getString("userid");
                            LOG.e("uid", userid);

                            Message msg = new Message();
                            msg.what = GET_UID_SUCCESS;
                            mHandler.sendMessage(msg);
                        } catch (Exception e) {
                            LOG.w("-------", "login error", e);
                            mHandler.sendEmptyMessage(CODE_FAILED);
                        }
                    }
                });
    }*/

}
