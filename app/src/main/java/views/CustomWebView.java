package views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.powerpoint45.lucidbrowser.CustomWebViewClient;
import com.powerpoint45.lucidbrowser.MainActivity;
import com.powerpoint45.lucidbrowser.Properties;
import com.powerpoint45.lucidbrowser.R;
import com.powerpoint45.lucidbrowser.Tools;
import com.powerpoint45.lucidbrowser.VideoEnabledWebChromeClient;
import com.powerpoint45.lucidbrowser.WebAddress;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


@SuppressWarnings("deprecation")
public class CustomWebView extends WebView {

	private static Field sConfigCallback;
	protected WeakReference<MainActivity> activityRef;
	private static String origionalUserAgent;
	static DownloadManager.Request request;
	public CustomWebViewClient client;

	static {
		try {
			sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
			sConfigCallback.setAccessible(true);
		} catch (Exception e) {
			// ignored
		}

	}


	private boolean videoPlaying;
	VideoEnabledWebChromeClient chromeClient;


	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	public CustomWebView(MainActivity activity, String url) {
		super(activity);
		activityRef = new WeakReference<>(activity);

		//setWebContentsDebuggingEnabled(true);

		//setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		this.setId(R.id.browser_page);
		setDesktopMode(Properties.webpageProp.useDesktopView);

		// Enable / Disable cookies
		if (!Properties.webpageProp.enablecookies) {
			CookieSyncManager.createInstance(activity);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(false);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				cookieManager.setAcceptThirdPartyCookies(this, false);
		} else {
			CookieSyncManager.createInstance(activity);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(true);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				cookieManager.setAcceptThirdPartyCookies(this, true);
		}

		// Uncomment if wanted by users
		//
		// // Enable / Disable JavaScript
		// if (!Properties.webpageProp.enablejavascript) {
		// this.getSettings().setJavaScriptEnabled(false);
		// } else {
		this.getSettings().setJavaScriptEnabled(true);
		// }

		// Enable / Disable Images
		if (!Properties.webpageProp.enableimages) {
			this.getSettings().setLoadsImagesAutomatically(false);
		} else {
			this.getSettings().setLoadsImagesAutomatically(true);
		}

		this.getSettings().setSupportZoom(true);
		this.getSettings().setBuiltInZoomControls(true);
		this.getSettings().setDisplayZoomControls(false);
		this.getSettings().setPluginState(PluginState.ON);
		this.getSettings().setDomStorageEnabled(true);
		this.getSettings().setUseWideViewPort(true);
		this.getSettings().setSaveFormData(true);
		this.getSettings().setSavePassword(true);
		this.getSettings().setAllowFileAccess(true);
		methodInvoke(this.getSettings(), "setAllowUniversalAccessFromFileURLs", new Class[] { boolean.class }, new Object[] { true });
		methodInvoke(this.getSettings(), "setAllowFileAccessFromFileURLs", new Class[] { boolean.class }, new Object[] { true });

		if (Properties.webpageProp.fontSize==0)
			this.getSettings().setTextSize(WebSettings.TextSize.SMALLEST);
		if (Properties.webpageProp.fontSize==1)
			this.getSettings().setTextSize(WebSettings.TextSize.SMALLER);
		if (Properties.webpageProp.fontSize==2)
			this.getSettings().setTextSize(WebSettings.TextSize.NORMAL);
		if (Properties.webpageProp.fontSize==3)
			this.getSettings().setTextSize(WebSettings.TextSize.LARGER);
		if (Properties.webpageProp.fontSize==4)
			this.getSettings().setTextSize(WebSettings.TextSize.LARGEST);

		this.setLayoutParams(new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));


		activity.registerForContextMenu(this);
		client = new CustomWebViewClient(activity);
		this.setWebViewClient(client);

		chromeClient = new VideoEnabledWebChromeClient(CustomWebView.this, activity);
		this.setWebChromeClient(chromeClient);


		//Load initial page
		if (url!=null && url.equals("na")){
			//Do nothing. will load from instance
		}else {
			if (url == null) {
				String urlLoad = MainActivity.prefs.getString("setbrowserhome",
						Properties.webpageProp.assetHomePage);

				Log.d("LB","url load:" + urlLoad);
				this.loadUrl(urlLoad);
			}
			else
				this.loadUrl(url);
		}

		this.setDownloadListener(new DownloadListener() {

			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				Log.d("LB","onDownloadStart");
				Uri downloadUri = Uri.parse(url);

				// get file name. if filename exists in contentDisposition, use it. otherwise, use the last part of the url.
				String fileName = downloadUri.getLastPathSegment();

				String headerFileName = Tools.getFileNameFromHeader(contentDisposition);
				if (headerFileName!=null)
					fileName = headerFileName;

				onDownloadStartNoStream(activityRef.get(), url, userAgent, contentDisposition, mimetype, fileName, false);
			}
		});


	}




	public void notifyVideoEnded(){
		Log.d("LB","notifyVideoEnded");
	}



	//Only Enable Swipe refresh when top of page is reeched
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {

		boolean enableSwipeRefresh = this.getScrollY()==0 && activityRef.get().webLayout.getY()==Tools.getStatusMargine(getContext());
		if (activityRef.get().findViewById(R.id.swipe_refresh).isEnabled()!= enableSwipeRefresh)
			activityRef.get().findViewById(R.id.swipe_refresh).setEnabled(enableSwipeRefresh);
		super.onScrollChanged(l, t, oldl, oldt);
	}



	public CustomWebView(Context context) {
		super(context);
	}

	public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean isVideoPlaying() {
		return videoPlaying;
	}



	public void setDesktopMode(final boolean enabled) {
		final WebSettings webSettings = getSettings();

		if (origionalUserAgent==null) {
			origionalUserAgent = webSettings.getUserAgentString();
			Log.d("LB", "Your user agent is:"+origionalUserAgent);
		}

		String newUserAgent = origionalUserAgent;
		if (enabled) {
			try {
				String ua = webSettings.getUserAgentString();
				String androidOSString = webSettings.getUserAgentString().substring(ua.indexOf("("), ua.indexOf(")") + 1);
				newUserAgent = origionalUserAgent.replace(androidOSString,"(X11; Linux x86_64)");
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		else {
			newUserAgent = origionalUserAgent;
		}

		webSettings.setUserAgentString(newUserAgent);
		webSettings.setUseWideViewPort(enabled);
		webSettings.setLoadWithOverviewMode(enabled);
	}

	boolean justRestoredState;
	@Override
	public WebBackForwardList restoreState(Bundle inState) {
		justRestoredState = true;
		return super.restoreState(inState);

	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		Log.d("LB", "onWindowVisibilityChanged");
		if (justRestoredState){
			justRestoredState = false;
			reload();
		}
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		Log.d("LB", "vis:"+visibility);
	}

	public void setVideoPlaying(boolean b) {
		videoPlaying = b;
	}

	public VideoEnabledWebChromeClient getChromeClient() {
		return chromeClient;
	}

	public void setUrlBarText(String url){
		if (url!=null){
			CustomWebView WV = activityRef.get().webLayout.findViewById(R.id.browser_page);
			if (WV!=null && this!=null && WV.equals(this)){
				if ((activityRef.get().findViewById(R.id.browser_searchbar))!=null && !activityRef.get().findViewById(R.id.browser_searchbar).isFocused()){
					if (url.startsWith("file:///android_asset/")) {
						((EditText) activityRef.get().findViewById(R.id.browser_searchbar)).setText("");
						((EditText) activityRef.get().findViewById(R.id.browser_searchbar)).setHint(R.string.urlbardefault);
					}else{
						((EditText) activityRef.get().findViewById(R.id.browser_searchbar))
								.setText(url
										.replace("http://", "")
										.replace("https://", ""));
					}
				}
			}
		}
	}



	/**
	 * Notify the host application a download should be done, even if there
	 * is a streaming viewer available for thise type.
	 * @param activity Activity requesting the download.
	 * @param url The full url to the content that should be downloaded
	 * @param userAgent User agent of the downloading application.
	 * @param contentDisposition Content-disposition http header, if present.
	 * @param mimetype The mimetype of the content reported by the server
	 * @param fileName The referer associated with the downloaded url
	 * @param privateBrowsing If the request is coming from a private browsing tab.
	 */
	public static void onDownloadStartNoStream(final Activity activity,
											   String url, String userAgent, String contentDisposition,
											   String mimetype, String fileName, boolean privateBrowsing) {

		Log.d("LB", "onDownloadStartNoStream " + url);
		// java.net.URI is a lot stricter than KURL so we have to encode some
		// extra characters. Fix for b 2538060 and b 1634719
		WebAddress webAddress;
		try {
			webAddress = new WebAddress(url);
			webAddress.setPath(encodePath(webAddress.getPath()));
		} catch (Exception e) {
			// This only happens for very bad urls, we want to chatch the
			// exception here
			Log.e("browser", "Exception trying to parse url:" + url);
			return;
		}
		String addressString = webAddress.toString();
		Uri uri = Uri.parse(addressString);
		try {
			request = new DownloadManager.Request(uri);
		} catch (IllegalArgumentException e) {
			Log.e("LB", "IllegalArgumentException:" + url);
			e.printStackTrace();
			return;
		}

		// Ensure the fileName is not null or empty.
		if (fileName == null || fileName.isEmpty()) {
			fileName = uri.getLastPathSegment();
			if (fileName == null || fileName.isEmpty()) {
				fileName = "default_download_file"; // Fallback to a default file name.
			}
		}


		request.setTitle(fileName);

		request.setMimeType(mimetype);
		// set downloaded file destination to /sdcard/Download.
		// or, should it be set to one of several Environment.DIRECTORY* dirs depending on mimetype?
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
		// let this downloaded file be scanned by MediaScanner - so that it can
		// show up in Gallery app, for example.
		request.allowScanningByMediaScanner();
		request.setDescription(webAddress.getHost());
		// XXX: Have to use the old url since the cookies were stored using the
		// old percent-encoded url.
		String cookies = CookieManager.getInstance().getCookie(url);
		request.addRequestHeader("cookie", cookies);
		request.addRequestHeader("User-Agent", userAgent);
		//request.addRequestHeader("Referer", referer);
		request.setNotificationVisibility(
				DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		if (mimetype == null) {
			if (TextUtils.isEmpty(addressString)) {
				return;
			}
			// We must have long pressed on a link or image to download it. We
			// are not sure of the mimetype in this case, so do a head request
			mimetype = "";
		}
		new Thread("Browser download") {
			public void run() {
				enqueDownload(activity);
			}
		}.start();
	}

	public static boolean enqueDownload(final Activity c){
			Log.d("LB", "enqueDownload");
			DownloadManager manager
					= (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);

			if (manager != null) {
				c.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Tools.toastString(R.string.download_started, c);
					}
				});

				manager.enqueue(request);
			}

			return true;
	}

	// This is to work around the fact that java.net.URI throws Exceptions
	// instead of just encoding URL's properly
	// Helper method for onDownloadStartNoStream
	private static String encodePath(String path) {
		char[] chars = path.toCharArray();
		boolean needed = false;
		for (char c : chars) {
			if (c == '[' || c == ']' || c == '|') {
				needed = true;
				break;
			}
		}
		if (needed == false) {
			return path;
		}
		StringBuilder sb = new StringBuilder();
		for (char c : chars) {
			if (c == '[' || c == ']' || c == '|') {
				sb.append('%');
				sb.append(Integer.toHexString(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private final static Object methodInvoke(Object obj, String method, Class<?>[] parameterTypes, Object[] args) {
		try {
			Method m = obj.getClass().getMethod(method, boolean.class);
			m.invoke(obj, args);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void destroy() {
		super.destroy();
		Log.d("LL","called destroy on CustomWebView");
		removeAllViews();
		setWebChromeClient(null);
		setWebViewClient(null);
		setDownloadListener(null);

		chromeClient = null;
		activityRef = null;

		try {
			if( sConfigCallback!=null )
				sConfigCallback.set(null, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	protected void finalize() throws Throwable {
		super.finalize();

		Log.d("LL","CustomWebView disposed");
	}


}
