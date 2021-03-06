package com.emagroup.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewActivity extends Activity implements OnClickListener, EmaSDKListener {

    private static final String TAG = "WebViewActivity";

    public static final String INTENT_TITLE = "webview_title";//webView显示的标题
    public static final String INTENT_URL = "url";//webView显示的url
    public static final String INTENT_TYPE = "type";//调用webView的类别
    public static final String INTENT_INFORGAME = "inforgame";//webView的结果是否需要通知游戏

    public static final int CODE_LOGOUT = 10;//退出
    private static final int CODE_CHANGE_TITLE = 11;//修改标题
    public static final int CODE_CLEAR_HISTORY = 40;//清楚浏览记录
    public static final int CODE_TENPAY_SUCC = 30;//财付通充值成功

    public static final int TYPE_EMAACCOUNT = 1;//账号中心
    public static final int TYPE_GIFT = 2;
    public static final int TYPE_HELP = 3;
    public static final int TYPE_TENPAY = 4;//财付通支付  ***** 目前支付只有财付通一个 ******
    public static final int TYPE_FIND_LOGIN_PASSW = 5;//找回用户密码
    public static final int TYPE_FIND_WALLET_PASSW = 7;//找回钱包密码
    public static final int TYPE_PROMOTION = 6;//推广
    public static final int TYPE_BIND = 8;//弱帐号绑定提醒
    public static final int TYPE_IDENTIFY = 9; //实名认证

    private int mType;//标记打开网页的类型（即从哪个入口进来的）
    //标记
    private boolean mFlagIsNeedInforGame = false;//是否需要回调游戏

    private EmaUser mEmaUser;
    private ConfigManager mConfigManager;
    private DeviceInfoManager mDeviceInfoManager;
    private ResourceManager mResourceManager;

    //views
    private ImageView mBtnBack;
    private ImageView mBtnReturnGame;
    private TextView mTxtTitle;
    private WebView mWebView;
    private ProgressBar mProgressBar;


    private Map<String, Integer> mIDMap;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CODE_TENPAY_SUCC://财付通支付成功
                    doTenpaySucc();
                    break;
                case CODE_LOGOUT://退出
                    break;
                case CODE_CLEAR_HISTORY:
                    mWebView.clearHistory();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏

        mEmaUser = EmaUser.getInstance();
        mConfigManager = ConfigManager.getInstance(this);
        mDeviceInfoManager = DeviceInfoManager.getInstance(this);
        mResourceManager = ResourceManager.getInstance(this);

        initView();

        initData();

    }

    @Override
    public void onCallBack(int resultCode, String decr) {
        switch (resultCode) {
            case EmaCallBackConst.EMA_SHARE_OK:
                Toast.makeText(WebViewActivity.this, decr, Toast.LENGTH_SHORT).show();
                //分享成功
                break;
            case EmaCallBackConst.EMA_SHARE_CANCLE:
                Toast.makeText(WebViewActivity.this, decr, Toast.LENGTH_SHORT).show();
                //分享取消
                break;
            case EmaCallBackConst.EMA_SHARE_FAIL:
                //分享拒绝
                Toast.makeText(WebViewActivity.this, decr, Toast.LENGTH_SHORT).show();
                break;
        }
    }


    /**
     * 与js交互时用到的方法对象，在js里直接调用
     */
    public class JavaScriptinterface {
        Context context;

        public JavaScriptinterface(Context c) {
            context = c;
        }

        /**
         * wap上修改密码完成后关闭那个页面
         *
         * @param num
         */
        @JavascriptInterface
        public void close(String num) {
            WebViewActivity.this.finish();
            ToolBar.getInstance(WebViewActivity.this).showToolBar();
            ToastHelper.toast(context, "密码修改成功");
        }

        /**
         * wap页面上登出
         */
        @JavascriptInterface
        public void logout() {
            WebViewActivity.this.finish();
            ToolBar.getInstance(WebViewActivity.this).hideToolBar();
            EmaSDK.getInstance().doLogout();
        }

        /**
         * 复制wap页面上的string
         */
        @JavascriptInterface
        public void copyString(String str) {
            try {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", str);
                cm.setPrimaryClip(mClipData);
                Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * 获取分享描述，URL，图片地址
         */
        @JavascriptInterface
        public void getShareInfo(final String desc, final String imageUrl, final String url, final String title) {
            try {
                Log.i(TAG, desc + imageUrl + url);

                new AsyncTask<String, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(String... strings) {

                        return getBitmap(imageUrl);
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                       /*  WebViewActivity.this.shareBitmap=bitmap;
                        mHandler.sendEmptyMessage(0);*/
                        EmaSDK.getInstance().doShareWebPage((Activity) context, WebViewActivity.this, url, title, desc, bitmap);
                    }
                }.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化界面
     */
    @SuppressLint("NewApi")
    private void initView() {
        setContentView(mResourceManager.getIdentifier("ema_webview", "layout"));
        mBtnBack = (ImageView) findViewById(getID("ema_webview_back"));
        mBtnReturnGame = (ImageView) findViewById(getID("ema_webview_imageView_return"));
        mTxtTitle = (TextView) findViewById(getID("ema_webView_title"));
        mProgressBar = (ProgressBar) findViewById(getID("ema_webview_ProgressBar"));

        mWebView = (WebView) findViewById(getID("ema_webview_url"));

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavaScriptinterface(this), "webview");
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setDefaultZoom(ZoomDensity.MEDIUM);
        mWebView.getSettings().setBuiltInZoomControls(true);
        //mWebView.getSettings().setTextZoom(75);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);//设置是否允许通过file url加载的Javascript可以访问其他的源，包括其他的文件和http,https等其他的源
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClientEma(mHandler));
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    if (View.INVISIBLE == mProgressBar.getVisibility()) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        mBtnReturnGame.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
        mBtnBack.setVisibility(View.GONE);

        /*mRadioGroupView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int arg1) {
                if (!EmaUser.getInstance().getIsLogin()) {
                    ToastHelper.toast(WebViewActivity.this, "请先登录");
                    return;
                }
                int checkedId = group.getCheckedRadioButtonId();
                if (checkedId == getID("ema_webview_account")) {
                    mWebView.loadUrl(Url.getWebUrlUserInfo());
                    doSetTitle("个人中心");
                } else if (checkedId == getID("ema_webview_gift")) {
                    *//*mWebView.loadUrl(Url.getWebUrlGift());
                    doSetTitle("礼包列表");*//*
                    ToastHelper.toast(WebViewActivity.this, "礼包暂未开放");
                } else if (checkedId == getID("ema_webview_help")) {
                    *//*mWebView.loadUrl(Url.getWebUrlHelp());
                    doSetTitle("帮助中心");*//*
                    ToastHelper.toast(WebViewActivity.this, "帮助暂未开放");
                } else if (checkedId == getID("ema_webview_promotion")) {
					mWebView.loadUrl(Url.getWebUrlPromotion());
					doSetTitle("推广");
                    ToastHelper.toast(WebViewActivity.this, "推广暂未开放");
                }
            }
        });*/
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Intent intent = getIntent();
        String url = intent.getStringExtra(INTENT_URL);
        mType = intent.getIntExtra(INTENT_TYPE, TYPE_EMAACCOUNT);
        mTxtTitle.setText(intent.getStringExtra(INTENT_TITLE));

        mFlagIsNeedInforGame = intent.getBooleanExtra(INTENT_INFORGAME, false);

        if (mType == TYPE_TENPAY || mType == TYPE_FIND_WALLET_PASSW) {
            if (mFlagIsNeedInforGame) {
                EmaPayProcessManager.getInstance().addPayActivity(this);
            } else {
                EmaPayProcessManager.getInstance().addRechargeActivity(this);
            }
        }

        doSetCookies(url);

        /*switch (mType) {
            case TYPE_EMAACCOUNT:
                //	doSetCookies(url);
                mBtnAccount.setChecked(true);
                break;
            case TYPE_GIFT:
                //doSetCookies(url);
                mBtnGift.setChecked(true);
			ToastHelper.toast(WebViewActivity.this, "礼包暂未开放");
                break;
        }*/

        LOG.d(TAG, "url__:" + url);
        mWebView.loadUrl(url);
    }

    /**
     * Click监听事件
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getID("ema_webview_imageView_return")) {//返回游戏
            if (mType == TYPE_TENPAY || mType == TYPE_FIND_WALLET_PASSW) {//支付或者充值
                new EmaDialogPayPromptCancel(this).show();
                return;
            } else if (mType != TYPE_FIND_LOGIN_PASSW) {
                ToolBar.getInstance(this).showToolBar();
            }
            this.finish();
        } else if (id == getID("ema_webview_back")) {//返回上一层界面 NOTE:目前这个按钮应该是出不来了，隐掉了
        }
    }

    /**
     * 财付通支付成功后的操作，需要关闭页面
     */
    private void doTenpaySucc() {
        if (mFlagIsNeedInforGame) {
            UCommUtil.makePayCallBack(EmaCallBackConst.PAYSUCCESS, "财付通支付成功！");
        } else {
            sendBroadcast(new Intent(PropertyField.BROADCAST_RECHARGE_SUCC));
        }
        new EmaDialogPayPromptResult(this,
                mFlagIsNeedInforGame ? EmaConst.PAY_ACTION_TYPE_PAY : EmaConst.PAY_ACTION_TYPE_RECHARGE,
                EmaConst.PAY_RESULT_SUCC,
                mFlagIsNeedInforGame ? "支付成功" : "充值成功").show();
    }

    /**
     * 账号中心需要设置cookie
     *
     * @param url
     */
    private void doSetCookies(String url) {
        //CookieSyncManager.createInstance(this); 如今WebView已经可以在需要的时候自动同步cookie了，所以不再需要
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookie();

        cookieManager.setCookie(url, getCookie(url, "appid", mConfigManager.getAppId()));
        cookieManager.setCookie(url, getCookie(url, "channelId", mConfigManager.getChannel()));
        cookieManager.setCookie(url, getCookie(url, "channelTag", mConfigManager.getChannelTag()));

        cookieManager.setCookie(url, getCookie(url, "token", mEmaUser.getToken()));
        cookieManager.setCookie(url, getCookie(url, "uid", mEmaUser.getmUid()));
        cookieManager.setCookie(url, getCookie(url, "nickname", mEmaUser.getNickName()));
        cookieManager.setCookie(url, getCookie(url, "deviceType", "android"));
        cookieManager.setCookie(url, getCookie(url, "deviceKey", mDeviceInfoManager.getDEVICE_ID()));
        cookieManager.setCookie(url, getCookie(url, "accountType", mEmaUser.getAccountType() + ""));

        //String gameInfoJson = Ema.getInstance().getGameInfoJson();
        String gameInfoJson = EmaUser.getInstance().getGameRoleInfo();
        cookieManager.setCookie(url, getCookie(url, "gameRoleInfo", gameInfoJson));
        try {
            JSONObject jsonObject = new JSONObject(gameInfoJson);
            cookieManager.setCookie(url, getCookie(url, "zoneId", jsonObject.getString("zoneId")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //CookieSyncManager.getInstance().sync();同上

        String str = cookieManager.getCookie(url);
        LOG.d(TAG, "cookStr__:" + str);
    }

    private String getCookie(String str, String key, String value) {
        URL url = null;
        String domain;
        String cookieValue = null;
        try {
            url = new URL(str);
            domain = url.getHost();

            cookieValue = key + "=" + value + ";domain=" + domain + ";path=/";
            Log.e("cookieValue", cookieValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cookieValue;
    }

    /**
     * 为不同的界面需要作出不同的改变
     *
     * @param url
     */
    private void doUrlChange(String url) {
        // 个人中心
        if (-1 != url.indexOf("/userinfo")) {
            doSetTitle("个人中心");
        }
        // 修改密码
        if (-1 != url.indexOf("/modifypwd")) {
            doSetTitle("修改密码");
        }
        // 忘记密码
        if (-1 != url.indexOf("/forgetpwd")) {
            doSetTitle("忘记密码");
        }
        // bindphone.html 手机绑定
        if (-1 != url.indexOf("/bindphone")) {
            doSetTitle("手机绑定");
        }
        // isbind.html 更换绑定
        if (-1 != url.indexOf("/isbind")) {
            doSetTitle("更换绑定");
        }
        // libao/libaolist.html 礼包列表
        if (-1 != url.indexOf("/libao/libaolist")) {
            doSetTitle("礼包列表");
        }
        // libao/lingqu.html 我的礼包（我领取的号）
        if (-1 != url.indexOf("/libao/lingqu")) {
            doSetTitle("我的礼包");
        }
        // libao/libaodetail.html 礼包详情
        if (-1 != url.indexOf("/libao/libaodetail")) {
            doSetTitle("礼包详情");
        }
        // chargerecord.html 充值记录
        if (-1 != url.indexOf("/chargerecord")) {
            doSetTitle("充值记录");
        }
        // forgetwtpwd.html 钱包密码
        if (-1 != url.indexOf("/forgetwtpwd")) {
            doSetTitle("找回钱包密码");
        }
        // chargesucc.html 支付成功页面
        if (-1 != url.indexOf("/chargesucc")) {
            doSetTitle("支付成功");
        }
    }

    /**
     * 设置标题
     *
     * @param title
     */
    private void doSetTitle(String title) {
        mTxtTitle.setText(title);
    }

    /**
     * 监听返回按钮
     */
    @Override
    public void onBackPressed() {
        //如果有上层界面，则返回到上层界面，没有的话结束页面（是否需要提示？？？？）
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            if (mType == TYPE_TENPAY && mFlagIsNeedInforGame) {
                new EmaDialogPayPromptCancel(this).show();
            } else {
                closeWebView();
            }
        }
    }

    /**
     * 关闭WebViewActivity，之后要显示toolbar(支付页面，不需要显示)
     * so stupid ！！！！！ can you fix me ....
     */
    private void closeWebView() {
        this.finish();
        if (mType != TYPE_TENPAY && mType != TYPE_FIND_LOGIN_PASSW && mType != TYPE_FIND_WALLET_PASSW) {
            Ema.getInstance().showToolBar();
        }
    }

    /**
     * 获取资源ID
     *
     * @param key
     * @return
     */
    private int getID(String key) {
        if (mIDMap == null) {
            mIDMap = new HashMap<String, Integer>();
        }
        if (!mIDMap.containsKey(key)) {
            int id = mResourceManager.getIdentifier(key, "id");
            mIDMap.put(key, id);
        }
        return mIDMap.get(key);
    }

    public Bitmap getBitmap(String url) {
        /*Bitmap bitmap = null;
        try {
            URL picurl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) picurl.openConnection(); // 获得连接
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false);//不缓存
            conn.connect();
            Bitmap bmp = BitmapFactory.decodeStream(conn.getInputStream());

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
            int options = 100;
            while (output.toByteArray().length > 9000 && options != 10) {
                output.reset(); //清空baos
                bmp.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到baos中
                options -= 10;
            }
            bmp.recycle();
            byte[] result = output.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);  //通过此方法的存储在file文件下图片为设定值以下，会导致失真的现象（每个像素的透明度会变化）。注意：以jpeg格式压缩后, 原来图片中透明的元素将消失。但是尺寸不变，即长宽的比例不变。所以，通过BitmapFactory.decodeFile（）后得到的bitmap所占用内存的大小不变。
            Log.e("sharebitmapsize", bitmap.getByteCount() + "bytes");
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;*/

        Bitmap bitmap = null;
        try {
            URL iconUrl = new URL(url);
            URLConnection conn = iconUrl.openConnection();
            HttpURLConnection http = (HttpURLConnection) conn;
            int length = http.getContentLength();
            conn.connect();
            InputStream is = conn.getInputStream();// 获得图像的字符流
            BufferedInputStream bis = new BufferedInputStream(is, length);

            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inJustDecodeBounds=true;

            //BitmapFactory.decodeStream(bis,null,options);

            options.inSampleSize = calculateInSampleSize(options, 80, 80);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            bitmap = BitmapFactory.decodeStream(bis, null, options);

            Log.e("sharebitmapsize", bitmap.getByteCount() + "bytes");

            bis.close();
            is.close();// 关闭流
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;

        /*URL imageURL = null;
        Bitmap bitmap = null;
        try {
            imageURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) imageURL.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
            Log.e("sharebitmapsize", bitmap.getByteCount() + "bytes");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;*/
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {// 原始图片的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // 在保证解析出的bitmap宽高分别大于目标尺寸宽高的前提下，取可能的inSampleSize的最大值
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    @Override
    protected void onPause() {
        Log.e("webview", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.e("webview", "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.e("webview", "onDestroy");

        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().removeAllCookie();

        super.onDestroy();
    }
}
