package com.emagroup.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.emagroup.sdk.R;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

public class EmaSendInfo {

    private static final String TAG = "EmaSendInfo";
    private static final String ONLINE_TIME = "online_time";
    private static String currentTaskTopName;

    /**
     * 发送心跳包
     */
    public static void sendOnlineAlive() {

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

                    //todo 之后收到什么特殊的result 发个通知
                    //showNotification();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private static void showNotification() {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Ema.getInstance().getContext());
        mBuilder.setPriority(PRIORITY_MAX);
        mBuilder.setSmallIcon(R.drawable.ema_bottom_promotion_checked);
        mBuilder.setContentTitle("My notification");
        mBuilder.setContentText("Hello World!");

        //Intent resultIntent = new Intent(this, MainActivity.class);
        //PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //mBuilder.setContentIntent(resultPendingIntent);

        Notification notification = mBuilder.build();

        //notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;

        notification.defaults = Notification.DEFAULT_SOUND;//通知带有系统默认声音

        NotificationManager mNotifyMgr = (NotificationManager) Ema.getInstance().getContext().getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(1, notification);
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
