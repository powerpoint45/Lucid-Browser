package views;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

import com.powerpoint45.lucidbrowser.BookmarksActivity;
import com.powerpoint45.lucidbrowser.MainActivity;
import com.powerpoint45.lucidbrowser.Properties;
import com.powerpoint45.lucidbrowser.R;
import com.powerpoint45.lucidbrowser.Tools;
import com.powerpoint45.lucidbrowser.VideoEnabledWebChromeClient;
import com.powerpoint45.lucidbrowser.WebAddress;

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
import android.os.Handler;
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

public class CustomWebView extends WebView{

	private ProgressBar PB;
	private boolean videoPlaying;
	VideoEnabledWebChromeClient chromeClient;

	@SuppressWarnings("deprecation")
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	public CustomWebView(Context context, AttributeSet set, String url) {
		super(context, set);
		this.setId(R.id.browser_page);
		if (url == null)
			this.loadUrl(MainActivity.mPrefs.getString("browserhome",
					MainActivity.assetHomePage));
		else
			this.loadUrl(url);

		if (Properties.webpageProp.useDesktopView) {
			this.getSettings().setUserAgentString(
					createUserAgentString("desktop"));
			this.getSettings().setLoadWithOverviewMode(true);
		} else {
			this.getSettings().setUserAgentString(
					createUserAgentString("mobile"));
			this.getSettings().setLoadWithOverviewMode(false);
		}

		// Enable / Disable cookies
		if (!Properties.webpageProp.enablecookies) {
			CookieSyncManager.createInstance(context);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(false);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				cookieManager.setAcceptThirdPartyCookies(this, false);
		} else {
			CookieSyncManager.createInstance(context);
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

		this.getSettings().setPluginState(PluginState.ON);
		this.getSettings().setDomStorageEnabled(true);
		this.getSettings().setBuiltInZoomControls(true);
		this.getSettings().setDisplayZoomControls(false);
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


		((Activity) MainActivity.activity).registerForContextMenu(this);
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
					intent.putExtra("tabNumber", MainActivity.getTabNumber());
					try {
						MainActivity.activity.startActivity(intent);
						return true;
					}catch (Exception e){
						e.printStackTrace();
						return false;
					}
				}
				else if (url.startsWith("tel:")) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", MainActivity.getTabNumber());
					try{
						MainActivity.activity.startActivity(intent);
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
						intent.putExtra("tabNumber", MainActivity.getTabNumber());
						MainActivity.activity.startActivity(intent);
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
					intent.putExtra("tabNumber", MainActivity.getTabNumber());
					try {
						MainActivity.activity.startActivity(intent);
						return true;
					}catch(Exception e){
						e.printStackTrace();
						return false;
					}
				}else if (url.contains("youtube.com/")) {
					// Might be a bit too generic but saves a lot of comparisons

					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", MainActivity.getTabNumber());
					try {
						MainActivity.activity.startActivity(intent);
						return true;
					}catch(Exception e){
						e.printStackTrace();
						return false;
					}
				}
				return false;
			}


			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (PB == null)
					PB = (ProgressBar) MainActivity.webLayout
							.findViewById(R.id.webpgbar);
				if (MainActivity.browserListViewAdapter != null)
					MainActivity.browserListViewAdapter.notifyDataSetChanged();

				CustomWebView WV = (CustomWebView) MainActivity.webLayout
						.findViewById(R.id.browser_page);

				if (WV == CustomWebView.this) {// check if this webview is being
												// currently shown/used
					if (((EditText) ((Activity) MainActivity.activity)
							.findViewById(R.id.browser_searchbar)) != null)
						if (!((EditText) ((Activity) MainActivity.activity)
								.findViewById(R.id.browser_searchbar))
								.isFocused())
							if (view != null)
								if (view.getUrl() != null
										&& !view.getUrl().equals("about:blank")) {

									setUrlBarText(view.getUrl());

									if (view.getUrl().equals(MainActivity.assetHomePage)) {
										CustomWebView.this
												.loadUrl("javascript:(function() { "
														+ "document.getElementById('searchbtn').value = "
														+ "'"
														+ MainActivity.activity
																.getResources()
																.getString(
																		R.string.search)
														+ "';"
														+ "document.title = '"
														+ MainActivity.activity
																.getResources()
																.getString(
																		R.string.home)
														+ "';" + "})()");
										Handler handler = new Handler();
										Runnable r = new Runnable() {
											public void run() {
												MainActivity.browserListViewAdapter
														.notifyDataSetChanged();
											}
										};
										handler.postDelayed(r, 500);// allows to
																	// wait for
																	// js to
																	// take
																	// effect
									}
								}
					if (PB!=null)
						PB.setVisibility(ProgressBar.INVISIBLE);

					ImageButton IB = (ImageButton) MainActivity.bar
							.findViewById(R.id.browser_refresh);
					if (IB != null) {
						IB.setImageResource(R.drawable.btn_toolbar_reload_normal);
					}

					ImageButton BI = (ImageButton) MainActivity.bar
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
					CustomToolbar.colorizeToolbar(MainActivity.toolbar,
							Properties.appProp.primaryIntColor, MainActivity.activity);
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

				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
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

				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.activity);

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



		chromeClient = new VideoEnabledWebChromeClient(this);
		this.setWebChromeClient(chromeClient);

		this.setDownloadListener(new DownloadListener() {

			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		        Uri downloadUri = Uri.parse(url);

		        // get file name. if filename exists in contentDisposition, use it. otherwise, use the last part of the url.
		        String fileName = downloadUri.getLastPathSegment();

		        String headerFileName = Tools.getFileNameFromHeader(contentDisposition);
		        if (headerFileName!=null)
		        	fileName = headerFileName;

				onDownloadStartNoStream(MainActivity.activity, url, userAgent, contentDisposition, mimetype, fileName, false);
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

		// TODO Auto-generated constructor stub
	}

	public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean isVideoPlaying() {
		return videoPlaying;
	}

	public String createUserAgentString(String mode) {
		String ua = "";

		// TODO Test with different user agents
		// For now copied Chrome user agents and adapt them to the user's device
		if (mode.equals("mobile")) {

			ua = "Mozilla/5.0 (" + System.getProperty("os.name", "Linux")
					+ "; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL
					+ "; Build/" + Build.ID
					+ ") AppleWebKit/537.36 (KHTML, like Gecko) "
					//+ "Chrome/34.0.1847.114 Mobile Safari/537.36"
					+ "Chrome/51.0.2704.106 Mobile Safari/537.36";

		} else if (mode.equals("desktop")) {
			ua = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36";
		}
		return ua;
	}

	public void setVideoPlaying(boolean b) {
		videoPlaying = b;
	}

	public VideoEnabledWebChromeClient getChromeClient() {
		return chromeClient;
	}

	public void setUrlBarText(String url){
		if (url!=null){
			CustomWebView WV = (CustomWebView) MainActivity.webLayout.findViewById(R.id.browser_page);
			if (WV!=null && this!=null && WV.equals(this)){
				if ((MainActivity.activity.findViewById(R.id.browser_searchbar))!=null && !((EditText) ((Activity) MainActivity.activity).findViewById(R.id.browser_searchbar)).isFocused()){
					if (url.equals(MainActivity.assetHomePage)) {
						((EditText) ((Activity) MainActivity.activity).findViewById(R.id.browser_searchbar)).setText("");
						((EditText) ((Activity) MainActivity.activity).findViewById(R.id.browser_searchbar)).setHint(R.string.urlbardefault);

//						((EditText) ((Activity) MainActivity.activity).findViewById(R.id.browser_searchbar)).setText(MainActivity.activity
//							.getResources()
//							.getString(R.string.urlbardefault));
					}else{
						((EditText) ((Activity) MainActivity.activity).findViewById(R.id.browser_searchbar))
							.setText(url
							.replace("http://", "")
							.replace("https://", ""));
					}
				}
			}
		}
	}

//	float downY;
//	float upY;
//	float actualY;
//
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		switch (event.getAction()){
//
//		case MotionEvent.ACTION_DOWN:
//			downY = event.getY();
//			actualY = event.getRawY();
//			break;
//
//		case MotionEvent.ACTION_MOVE:
//			MainActivity.actionBarControls.move(event.getY()-downY);
//
//			if (Math.abs(event.getY()-downY)>Properties.numtodp(5)){
//				cancelLongPress();
//				clearFocus();
//			}
//
//			if (Math.abs(event.getRawY()-actualY)>Properties.numtodp(5)){
//				cancelLongPress();
//				clearFocus();
//			}
//
//			break;
//
//		case MotionEvent.ACTION_UP:
//			MainActivity.actionBarControls.showOrHide();
//			break;
//
//		case MotionEvent.ACTION_CANCEL:
//			MainActivity.actionBarControls.actionCanceled();
//			break;
//
//		}
//	    return super.onTouchEvent(event);
//	}








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