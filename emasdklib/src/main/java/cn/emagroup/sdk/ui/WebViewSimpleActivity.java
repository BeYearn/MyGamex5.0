package cn.emagroup.sdk.ui;

import cn.emagroup.sdk.comm.ResourceManager;
import cn.emagroup.sdk.utils.LOG;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class WebViewSimpleActivity extends Activity {

	private static final String TAG = WebViewSimpleActivity.class.toString();
	
	public static final String INTENT_URL = "url";//webView显示的url
	public static final String INTENT_TITLE = "webview_title";//webView显示的标题
	
	private WebView mWebView;
	private TextView mTitleView;
	
	private ResourceManager mResourceManager;
	
	private int mIDWebView;
	private int mIdTitleView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
		
		mResourceManager = ResourceManager.getInstance(this);

		initView();
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		
		initData();
	}

	private void initView() {
		setContentView(mResourceManager.getIdentifier("ema_webview_simple", "layout"));
		mWebView = (WebView) findViewById(getIDWebView());
		mTitleView = (TextView) findViewById(getIDTiteView());
	}

	private void initData() {
		Intent intent = getIntent();
		if(intent.hasExtra(INTENT_URL)){
			String url = getIntent().getStringExtra(INTENT_URL);
			LOG.d(TAG, "load url__:" + url);
			mWebView.loadUrl(url);
		}
		mTitleView.setText(mResourceManager.getString("ema_name"));
		if(intent.hasExtra(INTENT_TITLE)){
			mTitleView.setText(intent.getStringExtra(INTENT_TITLE));
		}
	}

	private int getIDWebView(){
		if(mIDWebView == 0){
			mIDWebView = mResourceManager.getIdentifier("ema_webview", "id");
		}
		return mIDWebView;
	}
	
	private int getIDTiteView(){
		if(mIdTitleView == 0){
			mIdTitleView = mResourceManager.getIdentifier("ema_txt_title", "id");
		}
		return mIdTitleView;
	}	
	
}

