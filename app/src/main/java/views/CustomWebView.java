package views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.powerpoint45.lucidbrowser.BookmarksActivity;
import com.powerpoint45.lucidbrowser.MainActivity;
import com.powerpoint45.lucidbrowser.Properties;
import com.powerpoint45.lucidbrowser.R;
import com.powerpoint45.lucidbrowser.Tools;
import com.powerpoint45.lucidbrowser.VideoEnabledWebChromeClient;
import com.powerpoint45.lucidbrowser.WebAddress;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

public class CustomWebView extends WebView{

	private ProgressBar PB;
	private boolean videoPlaying;
	VideoEnabledWebChromeClient chromeClient;

	private static Field sConfigCallback;
	protected WeakReference<MainActivity> activityRef;
    private static String origionalUserAgent;

	static {
		try {
			sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
			sConfigCallback.setAccessible(true);
		} catch (Exception e) {
			// ignored
		}

	}

	@SuppressWarnings("deprecation")
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	public CustomWebView(MainActivity activity, AttributeSet set, String url) {
		super(activity, set);
		activityRef = new WeakReference<MainActivity>(activity);
		this.setId(R.id.browser_page);

		if (url!=null && url.equals("na")){
			//Do nothing. will load from instance
		}else {
			if (url == null)
				this.loadUrl(MainActivity.mPrefs.getString("browserhome",
						Properties.webpageProp.assetHomePage));
			else
				this.loadUrl(url);
		}

        setDesktopMode(Properties.webpageProp.useDesktopView);

		// Enable / Disable cookies
		if (!Properties.webpageProp.enablecookies) {
			CookieSyncManager.createInstance(activity.getApplicationContext());
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(false);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				cookieManager.setAcceptThirdPartyCookies(this, false);
		} else {
			CookieSyncManager.createInstance(activity.getApplicationContext());
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
		this.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if (url.startsWith("intent://")) {
					try {
						Context context = view.getContext();
						Intent intent = new Intent().parseUri(url, Intent.URI_INTENT_SCHEME);

						if (intent != null) {
							view.stopLoading();

							PackageManager packageManager = context.getPackageManager();
							ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
							if (info != null) {
								context.startActivity(intent);
							} else {
								String fallbackUrl = intent.getStringExtra("browser_fallback_url");
								if (fallbackUrl!=null)
									view.loadUrl(fallbackUrl);
								else return false;
							}
							return true;
						}
					} catch (URISyntaxException e) {
						e.printStackTrace();
						return false;
					}
				}
				else if (url.startsWith("mailto:")) {

					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", activityRef.get().getTabNumber());
					try {
						activityRef.get().startActivity(intent);
						return true;
					}catch (Exception e){
						e.printStackTrace();
						return false;
					}
				}
				else if (url.startsWith("tel:")) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", activityRef.get().getTabNumber());
					try{
						activityRef.get().startActivity(intent);
						return true;
					}catch (Exception e){
						e.printStackTrace();
						return false;
					}
				}else if (url.startsWith("https://play.google.com/store/")
						|| url.startsWith("market://")) {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri
								.parse(url));
						intent.putExtra("tabNumber", activityRef.get().getTabNumber());
						activityRef.get().startActivity(intent);
						System.out.println("Play Store!!");
						return true;
					}catch(Exception e){
						e.printStackTrace();
						return false;
					}
				}else if (url.startsWith("https://maps.google.")
						|| url.startsWith("intent://maps.google.")) {

					// Convert maps intent to normal http link
					if (url.contains("intent://")) {
						url = url.replace("intent://", "https://");
						url = url.substring(0, url.indexOf("#Intent;"));

					}
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", activityRef.get().getTabNumber());
					try {
						activityRef.get().startActivity(intent);
						return true;
					}catch(Exception e){
						e.printStackTrace();
						return false;
					}
				}else if (url.contains("youtube.com/watch")) {
					// Might be a bit too generic but saves a lot of comparisons

					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", activityRef.get().getTabNumber());
					try {
						activityRef.get().startActivity(intent);
						return true;
					}catch(Exception e){
						e.printStackTrace();
						return false;
					}
				}
				return false;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(CustomWebView.this, url, favicon);
				if (PB == null)
					try {
						PB = (ProgressBar) activityRef.get().webLayout
								.findViewById(R.id.webpgbar);
					} catch (Exception e) {
					}
				;
				if (view.getVisibility() == View.VISIBLE)
					if (PB != null && PB.getVisibility() != View.VISIBLE
							&& url.compareTo("about:blank") != 0)
						PB.setVisibility(ProgressBar.VISIBLE);
				ImageButton IB = (ImageButton) activityRef.get().bar
						.findViewById(R.id.browser_refresh);
				if (IB != null) {
					IB.setImageResource(R.drawable.btn_toolbar_stop_loading_normal);
					CustomToolbar.colorizeToolbar(activityRef.get().toolbar,
							Properties.appProp.primaryIntColor, activityRef.get().activity);
				}
				CustomToolbar.colorizeToolbar(activityRef.get().toolbar,
						Properties.appProp.primaryIntColor, activityRef.get());
				setUrlBarText(url);
			}


			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (PB == null)
					PB = (ProgressBar) activityRef.get().webLayout
							.findViewById(R.id.webpgbar);

				if (MainActivity.browserListViewAdapter != null)
					MainActivity.browserListViewAdapter.notifyDataSetChanged();

				CustomWebView WV = (CustomWebView) activityRef.get().webLayout
						.findViewById(R.id.browser_page);

                if (view.getUrl()!=null && view.getUrl().equals(Properties.webpageProp.assetHomePage)) {
                    //Replace the Search text on the document with properly localized string
                    String js = "javascript:(function() { ";
                    js += "document.getElementById('search').placeholder = '"+getResources().getString(R.string.search)+"';";
                    if (Properties.webpageProp.engine.contains("ecosia.org/search?tt=lucid&q=")) {
                        js += "document.getElementById('add').name = 'tt';";
                        js += "document.getElementById('add').value = 'lucid';";
                    }
                    js+="})()";

                    view.loadUrl(js);
                    Log.d("LL","fixing placeholder");
                }

				if (WV == CustomWebView.this) {// check if this webview is being
					// currently shown/used
					if (((EditText) activityRef.get().findViewById(R.id.browser_searchbar)) != null) {
                        if (!((EditText) activityRef.get()
                                .findViewById(R.id.browser_searchbar))
                                .isFocused()) {
                            if (view != null) {
                                if (view.getUrl() != null && !view.getUrl().equals("about:blank")) {
                                    setUrlBarText(view.getUrl());
                                }
                            }
                        }
                    }

					if (PB!=null)
						PB.setVisibility(ProgressBar.INVISIBLE);

					ImageButton IB = (ImageButton) activityRef.get().bar
							.findViewById(R.id.browser_refresh);
					if (IB != null) {
						IB.setImageResource(R.drawable.btn_toolbar_reload_normal);
					}

					ImageButton BI = (ImageButton) activityRef.get().bar
							.findViewById(R.id.browser_bookmark);
					if (BI != null) {
						String bookmarkName = null;
						if (CustomWebView.this != null && CustomWebView.this.getUrl() != null){
							bookmarkName = BookmarksActivity.bookmarksMgr.root.containsBookmarkDeep(CustomWebView.this.getUrl());
						}

						if (bookmarkName != null){
							BI.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
						} else {
							BI.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
						}
					}
					CustomToolbar.colorizeToolbar(activityRef.get().toolbar,
							Properties.appProp.primaryIntColor, activityRef.get().activity);
				}
			}

			@Override
			public void onReceivedSslError(WebView view,
										   SslErrorHandler handler, SslError error) {
				super.onReceivedSslError(view, handler, error);
				int errorCode = error.getPrimaryError();
				System.out.println("SSL ERROR " + errorCode + " DETECTED");

				sslCertificateErrorDialog(view, handler, error, errorCode);
			}

			@SuppressLint("NewApi")
			// Is surpressed as the code will only be executed on the correct platform
			private void sslCertificateErrorDialog(WebView view,
												   final SslErrorHandler handler, SslError error, int errorCode)
					throws NotFoundException {

				String title = "SSL Error detected";
				String msg = "";
				String url = "";

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					url = error.getUrl();
				} else {
					url = error.toString();
					url = url.substring(url.lastIndexOf(" on URL: ") + 9);
				}

				String sslWarning = getResources().getString(
						R.string.sslWebsiteWarning);
				String proceedQuestion = getResources().getString(
						R.string.sslProceedQuestion);

				if (errorCode == SslError.SSL_UNTRUSTED) {
					msg = String.format(
							getResources().getString(
									R.string.sslUntrustedMessage), url);

					title = String
							.format(getResources().getString(
									R.string.sslUntrustedTitle), url);
				} else if (errorCode == SslError.SSL_IDMISMATCH) {
					String issuedTo = error.getCertificate().getIssuedTo()
							.getCName();
					msg = String.format(
							getResources().getString(
									R.string.sslIdMismatchMessage), url,
							issuedTo);

					title = String.format(
							getResources().getString(
									R.string.sslIdMismatchTitle), url);
				} else if (errorCode == SslError.SSL_DATE_INVALID) {

					Date currentDate = Calendar.getInstance().getTime();
					Date expiredOn = error.getCertificate()
							.getValidNotAfterDate();

					if (currentDate.after(expiredOn)) {

						msg = String.format(
								getResources().getString(
										R.string.sslExpiredMessage), url,
								expiredOn.toString());

						title = String.format(
								getResources().getString(
										R.string.sslExpiredTitle), url);
					} else {
						Date validFrom = error.getCertificate()
								.getValidNotBeforeDate();
						msg = String.format(
								getResources().getString(
										R.string.sslNotYetValidMessage), url,
								validFrom.toString());

						title = String.format(
								getResources().getString(
										R.string.sslNotYetValidTitle), url);

					}

				}

				AlertDialog.Builder builder = new AlertDialog.Builder(activityRef.get());

				builder.setMessage(
						msg + " " + sslWarning + "\n\n" + proceedQuestion)
						.setTitle(title)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
										handler.proceed();
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
										handler.cancel();

									}
								});
				Dialog dialog;
				dialog = builder.create();
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
				dialog.show();

			}
		});



		chromeClient = new VideoEnabledWebChromeClient(CustomWebView.this,activity);
		this.setWebChromeClient(chromeClient);

		this.setDownloadListener(new DownloadListener() {

			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
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

	private final static Object methodInvoke(Object obj, String method, Class<?>[] parameterTypes, Object[] args) {
		try {
			Method m = obj.getClass().getMethod(method, new Class[] { boolean.class });
			m.invoke(obj, args);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
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


	public void setVideoPlaying(boolean b) {
		videoPlaying = b;
	}

	public VideoEnabledWebChromeClient getChromeClient() {
		return chromeClient;
	}

	public void setUrlBarText(String url){
		if (url!=null){
			CustomWebView WV = (CustomWebView) activityRef.get().webLayout.findViewById(R.id.browser_page);
			if (WV!=null && this!=null && WV.equals(this)){
				if ((activityRef.get().findViewById(R.id.browser_searchbar))!=null && !((EditText) ((Activity) activityRef.get()).findViewById(R.id.browser_searchbar)).isFocused()){
					if (url.startsWith("file:///android_asset/")) {
						((EditText) ((Activity) activityRef.get()).findViewById(R.id.browser_searchbar)).setText("");
						((EditText) ((Activity) activityRef.get()).findViewById(R.id.browser_searchbar)).setHint(R.string.urlbardefault);

//						((EditText) ((Activity) MainActivity.activity).findViewById(R.id.browser_searchbar)).setText(MainActivity.activity
//							.getResources()
//							.getString(R.string.urlbardefault));
					}else{
						((EditText) ((Activity) activityRef.get()).findViewById(R.id.browser_searchbar))
								.setText(url
										.replace("http://", "")
										.replace("https://", ""));
					}
				}
			}
		}
	}


//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//
//        String js = "javascript:";
//        js+= "var posX = " + event.getX() +";";
//        js+= "var posY = " + event.getY()+";";
//        js+= "var wvWidth = " + getWidth()+";";
//        js+= "var wvHeight = " + getHeight()+";";
//        js+= "var fixedX = posX * (window.innerWidth / wvWidth);";
//        js+= "var fixedY = posY * (window.innerHeight / wvHeight);";
//        js +="var focused = document.elementFromPoint(fixedX,fixedY);";
//        //js +="console.log(focused.tagName);";
//        js += "console.log(focused.className);";
//        loadUrl(js);
//
//        return super.onTouchEvent(event);
//    }








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
	public static void onDownloadStartNoStream(Activity activity,
											   String url, String userAgent, String contentDisposition,
											   String mimetype, String fileName, boolean privateBrowsing) {

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
		final DownloadManager.Request request;
		try {
			request = new DownloadManager.Request(uri);
		} catch (IllegalArgumentException e) {
			return;
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
		} else {
			final DownloadManager manager
					= (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
			new Thread("Browser download") {
				public void run() {
					manager.enqueue(request);
				}
			}.start();
		}

		Tools.toastString(R.string.download_started, activity);

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
		StringBuilder sb = new StringBuilder("");
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

}
