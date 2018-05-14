package com.emagroup.sdk;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class EmaSendInfo {

    private static final String TAG = "EmaSendInfo";
    private static String HEART_CODE = "";         //用来避免code重复通知

    public static void heartBeat(long delay){

        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                sendOnlineAlive();
            }
        };

        timer.schedule(timerTask,delay);
    }

    /**
     * 发送心跳包
     */
    private static void sendOnlineAlive() {

        final Context context = Ema.getInstance().getContext();
        LOG.d(TAG, "sendOnlineAlive");
        try {
            Map<String, String> params = new HashMap<String, String>();
            //获取位置信息
            LocationBean location = DeviceInfoManager.getInstance(context).getLocation();

            params.put("token", EmaUser.getInstance().getToken());
            params.put("uid", EmaUser.getInstance().getmUid());
            params.put("appId", ConfigManager.getInstance(Ema.getInstance().getContext()).getAppId());
            params.put("allianceId", ConfigManager.getInstance(Ema.getInstance().getContext()).getChannel());
            params.put("channelTag", ConfigManager.getInstance(Ema.getInstance().getContext()).getChannelTag());

            JSONObject extraJson = new JSONObject();
            extraJson.put("altitude", location.getAltitude() + "");
            extraJson.put("longitude", location.getLongitude() + "");
            extraJson.put("latitude", location.getLatitude() + "");
            extraJson.put("location", location.getCountry() + " " + location.getCity());
            extraJson.put("isBackgroud", "0");
            params.put("extra", extraJson.toString());
            /*currentTaskTopName = UCommUtil.getTopApp(context);
            if(TextUtils.isEmpty(currentTaskTopName)){
				extraJson.put("isBackgroud","0");
			}else{
				if(currentTaskTopName.equals(context.getPackageName())){
				extraJson.put("isBackgroud","0");
			}else{
				extraJson.put("isBackgroud","1");
			}}*/
            new HttpInvoker().postAsync(Url.getHeartbeatUrl(), params, new HttpInvoker.OnResponsetListener() {
                @Override
                public void OnResponse(String result) {
                    LOG.d(TAG, "heartbeat result__:" + result);

                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        int status = jsonObject.getInt("status");
                        if (0 == status) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            if (data.toString().contains("code")) {
                                String code = data.getString("code");

                                if (!HEART_CODE.equals(code)) {
                                    String applicationName = UCommUtil.getApplicationName(context);
                                    UCommUtil.showNotification(context,applicationName + " 通知", "您的验证码为：" + code);
                                    HEART_CODE = code;
                                    ToastHelper.toast(context,"请于通知栏查收您的验证码~");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param inMessage 游客登录
     */
    public static String getInfoJsonStr(String inMessage){
        Context context = Ema.getInstance().getContext();
        Map<String, String> deviceInfoMap = DeviceInfoManager.getInstance(context).deviceInfoGather();
        deviceInfoMap.put("message",inMessage);

        JSONObject deviceInfojson = new JSONObject(deviceInfoMap);
        String InfoJsonStr = deviceInfojson.toString();

        return InfoJsonStr;
    }

    /**
     * @param inMessage 0：getSystemInfo    1：点击

     * @param type 0 -初始化，1-点击数据
     */
    public static void sendDeviceInfoJson(String inMessage,String type){
        Context context = Ema.getInstance().getContext();

        HashMap<String,String> param = new HashMap<>();

        param.put("infoJson",getInfoJsonStr(inMessage));
        param.put("allianceId",ConfigManager.getInstance(context).getChannel());
        param.put("type",type);
        param.put("channelTag",ConfigManager.getInstance(Ema.getInstance().getContext()).getChannelTag());
        param.put("appId",ConfigManager.getInstance(Ema.getInstance().getContext()).getAppId());

        String sign = ConfigManager.getInstance(context).getChannel()+ConfigManager.getInstance(context).getAppId()
                + ConfigManager.getInstance(context).getChannelTag() +type+ EmaUser.getInstance().getAppKey();
        sign = UCommUtil.MD5(sign);
        param.put("sign", sign);

        new HttpInvoker().postAsync(Url.getUploadInfo(), param, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                Log.e("upLoadEmaInfo",result);
            }
        });
    }


    /**
     * 创建角色
     */
    public static void createRole() {
        LOG.d(TAG, "createRole");
        Context context = Ema.getInstance().getContext();
        Map<String, String> params = new HashMap<String, String>();
        params.put(PropertyField.APP_ID, ConfigManager.getInstance(context).getAppId());
        params.put(PropertyField.SEND_CHANNEL_ID, ConfigManager.getInstance(context).getChannel());
        //params.put(PropertyField.UUID, EmaUser.getInstance().getUUID());
        params.put(PropertyField.IP, DeviceInfoManager.getInstance(context).getIP());
        params.put(PropertyField.SEND_DEVICE_ID, DeviceInfoManager.getInstance(context).getDEVICE_ID());
        params.put(PropertyField.GAME_SERVER_ID, "");
        params.put(PropertyField.ROLE_ID, "");
        params.put(PropertyField.ROLE_NAME, "");
        params.put(PropertyField.ROLE_SEX, "");
        params.put(PropertyField.ROLE_TYPE, "");
        params.put(PropertyField.ROLE_CAMP, "");

        new HttpInvoker().postAsync(Url.getGatherInfoUrlGameRole(), params, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                LOG.d(TAG, "createRole result__:" + result);
            }
        });
    }

    /**
     * 初始化，向服务器发送设备信息
     */
    public static void sendInitDeviceInfo() {
        LOG.d(TAG, "sendInitDeviceInfo");
        Map<String, String> params = DeviceInfoManager.getInstance(Ema.getInstance().getContext()).deviceInfoGather();
        new HttpInvoker().postAsync(Url.getSendInfoUrlInitDeviceInfo(), params, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                LOG.d(TAG, result);
            }
        });
    }

    /**
     * 登录成功，发送用户信息
     */
    public static void sendLoginSuccInfo() {
        LOG.d(TAG, "sendLoginSuccInfo");
        Map<String, String> params = new HashMap<String, String>();
        ConfigManager configManager = ConfigManager.getInstance(Ema.getInstance().getContext());
        params.put(PropertyField.APP_ID, configManager.getAppId());
        params.put(PropertyField.SEND_CHANNEL_ID, configManager.getChannel());
        params.put(PropertyField.SEND_DEVICE_ID, DeviceInfoManager.getInstance(Ema.getInstance().getContext()).getDEVICE_ID());

        new HttpInvoker().postAsync(Url.getSendInfoUrlLoginSucc(), params, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                LOG.d(TAG, result);
            }
        });
    }

    /**
     * 注册，发送设备信息
     */
    public static void sendRegisterInfo() {
        LOG.d(TAG, "sendRegisterDeviceInfo");
        Map<String, String> params = new HashMap<String, String>();
        ConfigManager configManager = ConfigManager.getInstance(Ema.getInstance().getContext());
        params.put(PropertyField.APP_ID, configManager.getAppId());
        params.put(PropertyField.SEND_CHANNEL_ID, configManager.getChannel());
        params.put(PropertyField.SEND_DEVICE_ID, DeviceInfoManager.getInstance(Ema.getInstance().getContext()).getDEVICE_ID());
        new HttpInvoker().postAsync(Url.getSendInfoUrlRegisterSucc(), params, new HttpInvoker.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                LOG.d(TAG, result);
            }
        });
    }


}
