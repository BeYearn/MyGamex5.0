package com.emagroup.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.sina.weibo.sdk.api.share.BaseResponse;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class UCommUtil {

    private static final String TAG = "UCommUtil";

    /**
     * 获取应用名称
     *
     * @param context
     * @return
     */
    public static String getApplicationName(Context context) {
        PackageManager pm = context.getPackageManager();
        String appName = context.getApplicationInfo().loadLabel(pm).toString();
        return appName;
    }

    /**
     * 获得appicon 的 id
     * @param context
     * @return
     */
    public static int getAppIconId(Context context) {

        PackageManager pm = context.getPackageManager();
        String packageName = context.getApplicationInfo().packageName;

        Intent intent = pm.getLaunchIntentForPackage(packageName);
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null) {
            return resolveInfo.getIconResource();
        } else {
            return 0;
        }
    }

    /**
     * 发送信息
     *
     * @param handler
     * @param code
     * @param content
     */
    public static void sendMesg(Handler handler, int code, Object content) {
        if (content == null) {
            handler.sendEmptyMessage(code);
            return;
        }
        if (content instanceof String) {
            if (UCommUtil.isStrEmpty((String) content)) {
                handler.sendEmptyMessage(code);
                return;
            }
        }
        Message msg = new Message();
        msg.what = code;
        msg.obj = content;
        handler.sendMessage(msg);
    }

    /**
     * 在支付时，获取订单号发生订单号重复的问题时，显示系统繁忙
     */
    public static void showSystemBusyDialog(final Context context) {
        ((Activity) Ema.getInstance().getContext())
                .runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new EmaDialogSystemBusy(context).show();
                    }
                });
    }

    /**
     * Just For Test
     *
     * @param map
     */
    public static void testMapInfo(Map<String, String> map) {
        for (String key : map.keySet()) {
            LOG.d(TAG, "key_:" + key + "    value__:" + map.get(key));
        }
    }

    /**
     * 拼接url
     *
     * @param url
     * @param map
     * @return
     */
    public static String buildUrl(String url, Map<String, String> map) {
        if (map == null || map.size() == 0) {
            return url;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        int i = 0;
        for (String key : map.keySet()) {
            if (i == 0) {
                sb.append("?");
            } else {
                sb.append("&");
            }
            sb.append(key).append("=").append(map.get(key));
            i++;
        }
        LOG.d(TAG, "test_url___:" + sb.toString());
        return sb.toString();
    }

    /**
     * 获取指定长度的随机字符串(个数不超过32位)
     *
     * @return
     */
    public static String getRandomStr(int length) {
        Random random = new Random(System.currentTimeMillis());
        String str = MD5(random.toString());
        if (length >= 32)
            length = 32;
        if (str.length() >= length)
            str = str.substring(0, length);
        return str;
    }

    /**
     * 获取32位的随机字符串
     *
     * @return
     */
    public static String getRandomStr() {
        return getRandomStr(32);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 判断字符是否是中文
     *
     * @param str
     * @return
     */
    public static boolean isCN(String str) {
        try {
            byte[] bytes = str.getBytes("UTF-8");
            if (bytes.length == str.length()) {
                return false;
            } else {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 过滤输入密码的特殊字符
     *
     * @param str
     * @return
     * @throws PatternSyntaxException
     */
    public static String loginPasswStringFilter(String str)
            throws PatternSyntaxException {
        String regEx = "[ ()\"']";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("");
    }

    /**
     * 设置用户相关信息回调
     *
     * @param msgCode
     * @param msgObj
     */
    public static void makeUserCallBack(int msgCode, String msgObj) {
        Ema.getInstance().makeCallBack(msgCode, msgObj);
    }

    /**
     * 设置支付相关信息回调
     *
     * @param msgCode
     * @param msgObj
     */
    public static void makePayCallBack(int msgCode, Object msgObj) {
        EmaPay.getInstance(Ema.getInstance().getContext()).makePayCallback(
                msgCode, msgObj);
    }

    /**
     * 获取签名
     */
    public static String getSign(String appid, String sid, String uuid,
                                 String appkey) {
        long stamp = getTimeStamp();
        String sign = appid + sid + uuid + stamp + appkey;
        sign = MD5(sign);
        return sign;
    }

    /**
     * 获取当前时间（精确到二十分钟内），作为验证
     *
     * @return
     */
    public static long getTimeStamp() {
        long stamp = (int) (System.currentTimeMillis() / 1000);
        stamp = stamp - stamp % 1200;
        return stamp;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isStrEmpty(String str) {
        if (str == null || str.trim().equals("")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是邮箱
     *
     * @param str
     * @return
     */
    public static boolean isEmail(String str) {
        if (isStrEmpty(str))
            return false;
        String mode = "";
        int position = str.indexOf('@');
        if (position == 1)
            mode = "^[a-z0-9A-Z]+\\@[a-z0-9A-Z]+[.]{1}[a-z0-9A-Z]+\\w*[.]*\\w*[a-zA-Z]+$";
        else
            mode = "^[a-z0-9A-Z]+[-+._a-z0-9A-Z]*[a-z0-9A-Z]+\\@[a-z0-9A-Z]+[.]{1}[a-z0-9A-Z]+\\w*[.]*\\w*[a-zA-Z]+$";
        Pattern pattern = Pattern.compile(mode);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    /**
     * 判断是否是手机号码
     *
     * @param str
     * @return
     */
    public static boolean isPhone(String str) {
        if (isStrEmpty(str)) {
            return false;
        } else {
            Pattern pattern = Pattern.compile("^1\\d{10}$");
            Matcher matcher = pattern.matcher(str);
            return matcher.find();
        }
    }

    private static final String ALGORITHM = "RSA";

    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    private static final String DEFAULT_CHARSET = "UTF-8";

    public static String aliSign(String content, String privateKey) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
                    Base64.decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = java.security.Signature
                    .getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update(content.getBytes(DEFAULT_CHARSET));

            byte[] signed = signature.sign();
            String sign = Base64.encode(signed);
            LOG.d(TAG, "sign__:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 返回md5加密后的字符串
     *
     * @param str
     * @return
     */
    public static String MD5(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
            byte bytes[] = messageDigest.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++)
                if (Integer.toHexString(0xff & bytes[i]).length() == 1)
                    sb.append("0").append(Integer.toHexString(0xff & bytes[i]));
                else
                    sb.append(Integer.toHexString(0xff & bytes[i]));
            return sb.toString().toUpperCase();
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 返回格式化后的时间格式
     *
     * @param content
     * @return
     */
    public static String DateFormat(String content) {
        return DateFormat(Long.valueOf(content));
    }

    /**
     * 返回格式化后的时间格式
     *
     * @return
     */
    public static String DateFormat(Date date) {
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.US);
        return sfd.format(date);
    }

    /**
     * 返回格式化后的时间格式
     *
     * @param content
     * @return
     */
    public static String DateFormat(Long content) {
        String time = content.toString();
        if (time.length() == 10) {
            time += "000";
        }
        if (time.length() > 13) {
            time = time.substring(0, 13);
        }
        content = Long.valueOf(time);
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.US);
        return sfd.format(new Date(content));
    }

    /**
     * 返回指定格式的时间
     *
     * @param content
     * @param format
     * @return
     */
    public static String DateFormat(String content, String format) {
        return DateFormat(Long.valueOf(content), format);
    }

    /**
     * 返回指定格式的时间
     *
     * @param content
     * @param format
     * @return
     */
    public static String DateFormat(Long content, String format) {
        String time = content.toString();
        if (time.length() == 10) {
            time += "000";
        }
        if (time.length() > 13) {
            time = time.substring(0, 13);
        }
        content = Long.valueOf(time);
        SimpleDateFormat sfd = new SimpleDateFormat(format, Locale.US);
        return sfd.format(new Date(content));
    }

    /**
     * 处理分享回调 (此处目前是微博的)
     *
     * @param activity
     * @param baseResponse
     */
    public static void shareCallback(Activity activity, BaseResponse baseResponse) {
        switch (baseResponse.errCode) {
            case EmaCallBackConst.EMA_SHARE_OK:
                WeiboShareUtils.getInstance(activity).mListener.onCallBack(EmaCallBackConst.EMA_SHARE_OK, "分享成功");
                //  ToastHelper.toast(MainActivity.this, "share successful");
                break;
            case EmaCallBackConst.EMA_SHARE_CANCLE:
                WeiboShareUtils.getInstance(activity).mListener.onCallBack(EmaCallBackConst.EMA_SHARE_CANCLE, "分享取消");
                //   ToastHelper.toast(MainActivity.this, "share cancel");
                break;
            case EmaCallBackConst.EMA_SHARE_FAIL:
                WeiboShareUtils.getInstance(activity).mListener.onCallBack(EmaCallBackConst.EMA_SHARE_FAIL, "分享失败");
                //  ToastHelper.toast(MainActivity.this, baseResponse.errMsg*//*"share fail"*//*);
                break;
        }
    }

    /**
     * 处理分享回调 (此处是微信的)
     *
     * @param activity
     * @param resp
     */
    public static void shareCallback(Activity activity, BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if (Ema.getInstance().isWachatLoginFlag()) {
                    ThirdLoginUtils.getInstance(activity).wechatLogin((SendAuth.Resp) resp);
                } else {
                    WeixinShareUtils.mListener.onCallBack(EmaCallBackConst.EMA_SHARE_OK, "分享成功");
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                activity.finish();
                if (!Ema.getInstance().isWachatLoginFlag()) {
                    WeixinShareUtils.mListener.onCallBack(EmaCallBackConst.EMA_SHARE_CANCLE, "分享取消");
                }
                //分享取消
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                //分享拒绝
                if (!Ema.getInstance().isWachatLoginFlag()) {
                    WeixinShareUtils.mListener.onCallBack(EmaCallBackConst.EMA_SHARE_FAIL, "分享失败");
                }
                break;
        }
    }

    /**
     * 分享弹窗，进一步选择哪个渠道分享
     */
    public static void doShare(Activity activity, ShareDialog.OnBtnListener listener) {
        ShareDialog shareDialog = ShareDialog.create(activity);
        shareDialog.setOnBtnListener(listener);
        shareDialog.showDialog();
    }

    //读取指定目录下的TXT文件的第一行内容
    protected static String getFileContent(File file) {
        String content = "";
        if (file.getName().endsWith(".txt")) {//文件格式为txt文件
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader
                            = new InputStreamReader(instream, "GBK");
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line = "";
                    //分行读取
                       /* while (( line = buffreader.readLine()) != null) {
                            content += line + "\n";
                        }*/
                    content = buffreader.readLine();
                    instream.close();        //关闭输入流
                }
            } catch (java.io.FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    public static String getTopApp(Context context) {
        List<PackageInfo> packages = context.getPackageManager()
                .getInstalledPackages(0);
        android.app.ActivityManager mActivityManager;
        mActivityManager = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        String packageName;
        if (Build.VERSION.SDK_INT > 20) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getApplicationContext()
                    .getSystemService("usagestats");

            long ts = System.currentTimeMillis();
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);

            UsageStats recentStats = null;
            for (UsageStats usageStats : queryUsageStats) {
                if (recentStats == null || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                    recentStats = usageStats;
                }
            }
            packageName = recentStats != null ? recentStats.getPackageName() : null;
        } else {
            // 5.0之前
            // 获取正在运行的任务栈(一个应用程序占用一个任务栈) 最近使用的任务栈会在最前面
            // 1表示给集合设置的最大容量 List<RunningTaskInfo> infos = am.getRunningTasks(1);
            // 获取最近运行的任务栈中的栈顶Activity(即用户当前操作的activity)的包名
            packageName = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
            //Log.i(TAG,packageName);
        }
        Log.i(TAG, "getTopApp  packageName==" + packageName);
        return packageName;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isNoSwitch(Context context) {
        long ts = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) context
                .getSystemService("usagestats");
        List queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, 0, ts);
        return !(queryUsageStats == null || queryUsageStats.isEmpty());
    }

    public static void showUsagestatsDialog(final Context context) {
        new AlertDialog.Builder(context).
                setTitle("设置").
                setMessage("开启usagestats权限")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        context.startActivity(intent);
                        //finish();
                    }
                }).show();
    }
}
