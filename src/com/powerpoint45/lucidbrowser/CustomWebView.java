package com.powerpoint45.lucidbrowser;

import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
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

public class CustomWebView extends WebView {

	private ProgressBar PB;
	private boolean videoPlaying;
	VideoEnabledWebChromeClient chromeClient;

	public CustomWebView(Context context, AttributeSet set, String url) {
		super(context, set);
		this.setId(R.id.browser_page);
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		if (url == null)
			this.loadUrl(MainActivity.mPrefs.getString("browserhome",
					MainActivity.assetHomePage));
		else
			this.loadUrl(url);

		if (Properties.webpageProp.useDesktopView) {
			this.getSettings().setUserAgentString(
					createUserAgentString( "desktop"));
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
		} else {
			CookieSyncManager.createInstance(context);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(true);
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

		this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		this.getSettings().setPluginState(PluginState.ON);
		this.getSettings().setDomStorageEnabled(true);
		this.getSettings().setBuiltInZoomControls(true);
		this.getSettings().setDisplayZoomControls(false);
		this.getSettings().setUseWideViewPort(true);
		this.getSettings().setSaveFormData(true);
		
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
		this.setLayoutParams(new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((Activity) MainActivity.activity).registerForContextMenu(this);
		this.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if (url.startsWith("https://play.google.com/store/")
						|| url.startsWith("market://")) {

					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", MainActivity.getTabNumber());
					MainActivity.activity.startActivity(intent);
					return true;
				}

				else if (url.startsWith("https://maps.google.")
						|| url.startsWith("intent://maps.google.")) {

					// Convert maps intent to normal http link
					if (url.contains("intent://")) {
						url = url.replace("intent://", "https://");
						url = url.substring(0, url.indexOf("#Intent;"));

					}
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", MainActivity.getTabNumber());
					MainActivity.activity.startActivity(intent);
					return true;
				}

				else if (url.contains("youtube.com/")) {
					// Might be a bit too generic but saves a lot of comparisons

					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", MainActivity.getTabNumber());
					MainActivity.activity.startActivity(intent);
					return true;
				} else if (url.startsWith("intent://")) {

					Intent intent;
					try {
						intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
					} catch (URISyntaxException e) {
						e.printStackTrace();
						System.out.println("INVALID INTENT URI");
						return false;
					}
					intent.putExtra("tabNumber", MainActivity.getTabNumber());
					MainActivity.activity.startActivity(intent);
					return true;
				} else if (url.startsWith("mailto:")) {

					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", MainActivity.getTabNumber());
					MainActivity.activity.startActivity(intent);
					return true;
				}else if (url.startsWith("tel:")) {

					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					intent.putExtra("tabNumber", MainActivity.getTabNumber());
					MainActivity.activity.startActivity(intent);
					return true;
				}
				return false;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (PB == null)
					try {
						PB = (ProgressBar) MainActivity.webLayout
								.findViewById(R.id.webpgbar);
					} catch (Exception e) {
					}
				;
				if (view.getVisibility() == View.VISIBLE)
					if (PB != null && PB.getVisibility() != View.VISIBLE
							&& url.compareTo("about:blank") != 0)
						PB.setVisibility(ProgressBar.VISIBLE);
				ImageButton IB = (ImageButton) MainActivity.bar
						.findViewById(R.id.browser_refresh);
				if (IB != null) {
					IB.setImageResource(R.drawable.btn_toolbar_stop_loading_normal);
				}
			}

			public void onPageFinished(WebView view, String url) {
				if (Properties.appProp.transparentNav)
		        	CustomWebView.this.loadUrl("javascript:(function() { " +  
								 "document.body.style.paddingBottom='+"+MainActivity.NavMargine+"px';"+
								 "document.querySelector('footer').style.paddingBottom='"+MainActivity.NavMargine+"px';"+
								 "document.getElementById('footer').style.paddingBottom='"+MainActivity.NavMargine+"px';"+
			                    "})()");

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
										&& view.getUrl().compareTo(
												"about:blank") != 0) {
									if (view.getUrl().compareTo(
											MainActivity.assetHomePage) == 0) {
										((EditText) ((Activity) MainActivity.activity)
												.findViewById(R.id.browser_searchbar))
												.setText(MainActivity.activity
														.getResources()
														.getString(
																R.string.urlbardefault));
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
									} else
										((EditText) ((Activity) MainActivity.activity)
												.findViewById(R.id.browser_searchbar))
												.setText(view
														.getUrl()
														.replace("http://", "")
														.replace("https://", ""));
								}
					PB.setVisibility(ProgressBar.INVISIBLE);

					ImageButton IB = (ImageButton) MainActivity.bar
							.findViewById(R.id.browser_refresh);
					if (IB != null) {
						IB.setImageResource(R.drawable.btn_toolbar_reload_normal);
					}

					ImageButton BI = (ImageButton) MainActivity.bar
							.findViewById(R.id.browser_bookmark);
					if (BI != null) {
						int numBooks = MainActivity.mPrefs.getInt(
								"numbookmarkedpages", 0);
						boolean isBook = false;
						for (int i = 0; i < numBooks; i++) {
							if (CustomWebView.this != null)
								if (CustomWebView.this.getUrl() != null)
									if (MainActivity.mPrefs.getString(
											"bookmark" + i, "").compareTo(
											CustomWebView.this.getUrl()) == 0) {
										BI.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
										isBook = true;
										break;
									}
						}
						if (!isBook)
							BI.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
					}
				}

			}

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {

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

				MainActivity.dialog = builder.create();
				MainActivity.dialog.setCancelable(false);
				MainActivity.dialog.setCanceledOnTouchOutside(false);
				MainActivity.dialog.show();

			}

		});

		chromeClient = new VideoEnabledWebChromeClient(this);
		this.setWebChromeClient(chromeClient);

		this.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {

				if (MainActivity.isDownloadManagerAvailable(MainActivity.ctxt)) {
					DownloadManager.Request request = new DownloadManager.Request(
							Uri.parse(url));

					// TODO Check if necessary
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
						request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
					else
						request.setShowRunningNotification(true);

					if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
						request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
					else
						request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);

					request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
					request.allowScanningByMediaScanner();
					request.setDestinationInExternalPublicDir(
							Environment.DIRECTORY_DOWNLOADS,
							url.substring(url.lastIndexOf('/') + 1,
									url.length()));
					DownloadManager manager = (DownloadManager) MainActivity.ctxt
							.getSystemService(Context.DOWNLOAD_SERVICE);
					manager.enqueue(request);
				}
			}
		});

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

	String createUserAgentString(String mode) {
		String ua = "";

		// TODO Test with different user agents
		// Stopped mimicking Chrome to be better safe than sorry
		if (mode.equals("mobile")) {

			ua = "Mozilla/5.0 (" + System.getProperty("os.name", "Linux")
					+ "; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL
					+ "; Build/" + Build.ID
					+ ") AppleWebKit/537.36 (KHTML, like Gecko) "
					+ "Chrome/30.0.0.0 Mobile Safari/537.36";
			// + "Chrome/34.0.1847.114 Mobile Safari/537.36";

		} else if (mode.equals("desktop")) {
			ua = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.0.0 Mobile Safari/537.36";

		}
		return ua;
	}

	public void setVideoPlaying(boolean b) {
		videoPlaying = b;
	}

	public VideoEnabledWebChromeClient getChromeClient() {
		return chromeClient;
	}

	@SuppressLint("InlinedApi")
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if (Properties.appProp.transparentNav){
			if ((getContentHeight() -(t+getHeight()))<Properties.numtodp(30)){
				if (Properties.controls.navBarHidden==false){
					View decorView = MainActivity.activity.getWindow().getDecorView();
					decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
	                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
					Properties.controls.navBarHidden=true;
				}
			}else{
				View decorView = MainActivity.activity.getWindow().getDecorView();
				decorView.setSystemUiVisibility(
			            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
				Properties.controls.navBarHidden=false;
			}
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

}