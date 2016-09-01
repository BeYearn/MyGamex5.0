package cn.emagroup.sdk.comm;

import android.net.http.AndroidHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.emagroup.sdk.utils.LOG;
import cn.emagroup.sdk.utils.UCommUtil;

public class HttpInvoker {

	public static interface OnResponsetListener{
		public abstract void OnResponse(String result);
	}
	
	private static final String TAG = "HttpInvoker";
	private static final int TIME_OUT_SEC = 10000;
	
	private boolean mFlagIsContinue;//标记是否继续将数据传递回去
	
	public HttpInvoker() {
		mFlagIsContinue = true;
	}
	
	public void setContinue(boolean mFlagIsContinue) {
		this.mFlagIsContinue = mFlagIsContinue;
	}

	/**
	 * get方式请求网络数据
	 * @param url
	 * @param params
	 * @param listener
	 */
	public void get(String url, Map<String, String> params, OnResponsetListener listener){
		doGet(url, params, listener);
	}
	
	/**
	 * post方式请求网络数据
	 * @param url
	 * @param params
	 * @param listener
	 */
	public void post(String url, Map<String, String> params, OnResponsetListener listener){
		doPost(url, params, listener);
	}
	
	/**
	 * get方式异步线程请求网络
	 * @param url
	 * @param params
	 * @param listener
	 */
	public void getAsync(String url, Map<String, String> params, OnResponsetListener listener){
		final String _url = url;
		final Map<String, String> _params = params;
		final OnResponsetListener _listener = listener;
		new Thread(new Runnable() {
			@Override
			public void run() {
				doGet(_url, _params, _listener);
			}
		}).start();
	}
	
	/**
	 * post方式异步线程请求网络
	 * @param url
	 * @param params
	 * @param listener
	 */
	public void postAsync(String url, Map<String, String> params, OnResponsetListener listener){
		final String _url = url;
		final Map<String, String> _params = params;
		final OnResponsetListener _listener = listener;
		new Thread(new Runnable() {
			@Override
			public void run() {
				doPost(_url, _params, _listener);
			}
		}).start();
	}
	
	/**
	 * get方式请求数据(通过httpUrlConnection进行网络访问)
	 * @param url
	 * @param params
	 * @param listener
	 * @throws Exception
	 */
	private void doGet(String url, Map<String, String> params, OnResponsetListener listener){
		try {
			//返回值
			StringBuffer resultSb = new StringBuffer();
			// 拼凑get请求的URL字串，使用URLEncoder.encode对特殊和不可见字符进行编码
			url = buildUrl(url, params);
			LOG.d(TAG, "url_:" + url);
			URL getUrl = new URL(url);
			
			// 根据拼凑的URL，打开连接，URL.openConnection()函数会根据URL的类型，返回不同的URLConnection子类的对象，
			// 这里我们的URL是一个http，因此它实际返回的是HttpURLConnection
			HttpURLConnection httpCon = (HttpURLConnection)getUrl.openConnection();
			// 建立与服务器的连接，并未发送数据
			httpCon.connect();
			
			// 发送数据到服务器并使用Reader读取返回的数据
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
			
			String line;
			while((line = reader.readLine())!=null)
			{
				resultSb.append(line);
			}
			reader.close();
			LOG.d(TAG, "result__:" + resultSb.toString());
			//断开连接
			httpCon.disconnect();
			setOnresponse(listener, resultSb.toString());
		} catch (Exception e) {
			setOnresponse(listener, "网络请求数据失败");
			LOG.w(TAG, "doGet error ", e);
		}
	}

	/**
	 * post方式请求数据（通过HttpClient进行网络访问，Android在2.3版本之后推荐使用HttpUrlConnection进行网络访问，但是这里用HttpClient更方便）
	 * @param url
	 * @param params
	 * @param listener
	 */
	private void doPost(String url, Map<String, String> params, OnResponsetListener listener){
		if(UCommUtil.isStrEmpty(url)){
			LOG.w(TAG, "url is empty url_:" + url);
			try {
				JSONObject jo = new JSONObject();
				jo.put("errno", HttpInvokerConst.LOGIN_RESULT_URL_IS_NULL);
				listener.OnResponse(jo.toString());
			} catch (Exception e) {
			}
			return;
		}
		if(url.startsWith("https")){
			doHttpsPost(url, params, listener);
		}else{
			doHttpPost(url, params, listener);
		}
	}
	
	/**
	 * 进行http post方式的网络请求
	 * @param url
	 * @param params
	 * @param listener
	 */
	private void doHttpPost(String url, Map<String, String> params, OnResponsetListener listener){
		AndroidHttpClient client = null;
		try {
			StringBuilder builer = new StringBuilder();
			for (Map.Entry<String,String> entry:params.entrySet()){
				builer.append(entry.getKey()+"="+entry.getValue()+"&");
			}
			LOG.e(TAG, "post url__:" + url);
			LOG.e(TAG,"params__"+builer.toString());
//			HttpClient client = new DefaultHttpClient();
			client = AndroidHttpClient.newInstance("");
			HttpPost post = new HttpPost(url);
			post.setEntity(buildEntry(params));

			HttpResponse response = client.execute(post);

			int resultCode = response.getStatusLine().getStatusCode();
			String result = EntityUtils.toString(response.getEntity());
			LOG.d(TAG, "return status code:" + resultCode);
			LOG.d(TAG, "return message:" + result);
			if(resultCode != HttpStatus.SC_OK){
				LOG.e(TAG, "服务器返回错误 !!!!!!");
				setOnresponse(listener,"{\"config\":{},\"data\":{},\"message\":\"请求超时\",\"status\":\"9\"}");
			}
			setOnresponse(listener, result);
		} catch (Exception e) {
			setOnresponse(listener,"{\"config\":{},\"data\":{},\"message\":\"请求失败,检查网络\",\"status\":\"9\"}");
			LOG.w(TAG, "doHttpsPost error", e);
		} finally{
			if(client != null){
				client.close();
			}
		}
	}
	
	/**
	 * 进行https post方式的网络请求
	 * @param url
	 * @param params
	 * @param listener
	 */
	private void doHttpsPost(String url, Map<String, String> params, OnResponsetListener listener){
		try {
			HttpParams httpParams = new BasicHttpParams();
			//设置http版本
			HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			//设置编码
			HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
			//设置连接超时
			HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT_SEC);
			//设置Socket超时
			HttpConnectionParams.setSoTimeout(httpParams, TIME_OUT_SEC);
			//获取添加了信任的HttpClient
			HttpClient client = getHttpClientFroHttps(httpParams);

			StringBuilder builer = new StringBuilder();
			for (Map.Entry<String,String> entry:params.entrySet()){
				builer.append(entry.getKey()+"="+entry.getValue()+"&");
			}
			LOG.e(TAG, "post url__:" + url);
			LOG.e(TAG,"params__"+builer.toString());
			HttpPost post = new HttpPost(url);
			post.setParams(httpParams);
			post.setEntity(buildEntry(params));
			HttpResponse response = client.execute(post);
			
			int resultCode = response.getStatusLine().getStatusCode();
			String result = EntityUtils.toString(response.getEntity());
			LOG.d(TAG, "return status code:" + resultCode);
			LOG.d(TAG, "return message:" + result);
			if(resultCode != HttpStatus.SC_OK){
				LOG.e(TAG, "服务器返回错误 !!!!!!");
				setOnresponse(listener,"{\"config\":{},\"data\":{},\"message\":\"请求超时\",\"status\":\"9\"}");
			}
			setOnresponse(listener, result);
		} catch (Exception e) {
			setOnresponse(listener,"{\"config\":{},\"data\":{},\"message\":\"请求失败,检查网络\",\"status\":\"9\"}");
			LOG.w(TAG, "doHttpsPost error", e);
		}
	}
	
	private void setOnresponse(OnResponsetListener listener, String result){
		if(mFlagIsContinue && listener != null){
			listener.OnResponse(result);
		}
	}
	
	/**
	 * 构建Entity进行参数传递
	 * @param params
	 * @return
	 */
	private UrlEncodedFormEntity buildEntry(Map<String, String> params){
		UrlEncodedFormEntity entry = null;
		List<NameValuePair> paramsPairs = new ArrayList<NameValuePair>();
		if(params != null && !params.isEmpty()){
			for(String key : params.keySet()){
				paramsPairs.add(new BasicNameValuePair(key, params.get(key)));
			}
			try {
				entry = new UrlEncodedFormEntity(paramsPairs, HTTP.UTF_8);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return entry;
	}
	
	/**
	 * 构建添加了信任的HttpClient（这里信任了所有的网站服务，不太安全）
	 * @param params
	 * @return
	 */
	private HttpClient getHttpClientFroHttps(HttpParams params){
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			SSLSocketFactory ssf = new SSLSocketFactoryImp(trustStore);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			//设置http和https的支持
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", ssf, 443));
			
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			LOG.e(TAG, "getHttpsHttpClient error");
			e.printStackTrace();
		}
		return new DefaultHttpClient(params);
	}
	
	/**
	 * 拼接url
	 * @param url
	 * @param map
	 * @return
	 */
	private String buildUrl(String url , Map<String, String> map){
		if(map == null || map.size() == 0){
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
		return sb.toString();
	}
	
}
