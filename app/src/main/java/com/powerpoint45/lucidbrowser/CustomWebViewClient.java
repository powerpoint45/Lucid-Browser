package com.powerpoint45.lucidbrowser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import database.AppDatabase;
import views.CustomWebView;

import static views.CustomWebView.onDownloadStartNoStream;

public class CustomWebViewClient extends WebViewClient implements DownloadListener {

    protected WeakReference<MainActivity> activityRef;
    private ProgressBar PB;


    public CustomWebViewClient(Activity activity){
        activityRef = new WeakReference<MainActivity>((MainActivity) activity);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("intent://")) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                if (intent != null) {
                    view.stopLoading();

                    PackageManager packageManager = activityRef.get().getPackageManager();
                    ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (info != null) {
                        activityRef.get().startActivity(intent);
                    } else {
                        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                        if (fallbackUrl != null)
                            view.loadUrl(fallbackUrl);
                        else return false;
                    }
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else if (url.startsWith("mailto:")) {

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                    .parse(url));
            intent.putExtra("tabNumber", activityRef.get().getTabNumber());
            try {
                activityRef.get().startActivity(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else if (url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                    .parse(url));
            intent.putExtra("tabNumber", activityRef.get().getTabNumber());
            try {
                activityRef.get().startActivity(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else if (url.startsWith("https://play.google.com/store/")
                || url.startsWith("market://")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                        .parse(url));
                intent.putExtra("tabNumber", activityRef.get().getTabNumber());
                activityRef.get().startActivity(intent);
                System.out.println("Play Store!!");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else if (url.startsWith("https://maps.google.")
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
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Log.d("LB", "SPECIAL " + url);
            Context context = view.getContext();
            Intent intent = null;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            if (intent != null) {
                view.stopLoading();

                PackageManager packageManager = context.getPackageManager();
                ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (info != null) {
                    context.startActivity(intent);
                } else {
                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                    if (fallbackUrl != null)
                        view.loadUrl(fallbackUrl);
                    else return false;
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d("LB","started loading "+url);
        startRotatingRefreshView();
        if (activityRef.get() != null && activityRef.get().webLayout != null) {
            CustomWebView WV = activityRef.get().webLayout
                    .findViewById(R.id.browser_page);

            if (WV != null && view == WV) {
                if (PB == null)
                    try {
                        PB = activityRef.get().webLayout
                                .findViewById(R.id.webpgbar);
                    } catch (Exception e) {
                    }
                if (view.getVisibility() == View.VISIBLE)
                    if (PB != null && PB.getVisibility() != View.VISIBLE
                            && url.compareTo("about:blank") != 0)
                        PB.setVisibility(ProgressBar.VISIBLE);
                ImageButton IB = activityRef.get().barHolder.findViewById(R.id.browser_refresh);
                if (IB != null) {
                    IB.setImageResource(R.drawable.btn_toolbar_stop_loading_normal);
                }
                ((CustomWebView)view).setUrlBarText(url);

                //force colorize toolbar
                activityRef.get().toolbar.requestLayout();
            }
        }
    }

    public void startRotatingRefreshView(){
               // keep rotation after animation

        // Aply animation to image view
        //activityRef.get().barHolder.findViewById(R.id.browser_refresh).setAnimation(an);
        activityRef.get().barHolder.findViewById(R.id.browser_refresh).startAnimation(AnimationUtils.loadAnimation(activityRef.get(), R.anim.rotate_anim));
    }

    public void endRefreshAnimation(){
        activityRef.get().barHolder.findViewById(R.id.browser_refresh).clearAnimation();
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        endRefreshAnimation();
        if (PB == null)
            PB = activityRef.get().webLayout
                    .findViewById(R.id.webpgbar);

        if (activityRef.get().browserListViewAdapter != null)
            activityRef.get().browserListViewAdapter.notifyDataSetChanged();

        final String urlFinal = view.getUrl();
        final String titleFinal = view.getTitle();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!Properties.appProp.historyPaused)
                    AppDatabase.getDb(view.getContext().getApplicationContext()).addURL(urlFinal, titleFinal);
            }
        }).start();


        Log.d("LB", "pageFinished");
        if (view.getUrl() != null && view.getUrl().equals(Properties.webpageProp.assetHomePage)) {
            //Replace the Search text on the document with properly localized string
            String js = "javascript:(function() { ";
            js += "document.getElementById('search').placeholder = '" + activityRef.get().getString(R.string.search) + "';";
            if (Properties.webpageProp.engine.contains("ecosia.org/search?tt=lucid&q=")) {
                js += "document.getElementById('add').name = 'tt';";
                js += "document.getElementById('add').value = 'lucid';";
            }
            js += "})()";

            view.loadUrl(js);
            Log.d("LL", "fixing placeholder");
        }

        CustomWebView WV = activityRef.get().webLayout
                .findViewById(R.id.browser_page);

        if (WV == view) {// check if this webview is being currently shown/used
            if (activityRef.get().findViewById(R.id.browser_searchbar) != null)
                if (activityRef.get().findViewById(R.id.browser_searchbar).isFocused())
                    if (view != null)
                        if (view.getUrl() != null && !view.getUrl().equals("about:blank")) {
                            ((CustomWebView)view).setUrlBarText(view.getUrl());
                        }
            if (PB != null)
                PB.setVisibility(ProgressBar.INVISIBLE);

            ImageButton IB = activityRef.get().barHolder
                    .findViewById(R.id.browser_refresh);
            if (IB != null) {
                IB.setImageResource(R.drawable.btn_toolbar_reload_normal);
            }

            ImageButton BI = activityRef.get().barHolder
                    .findViewById(R.id.browser_bookmark);
            if (BI != null) {
                String bookmarkName = null;
                if (view.getUrl() != null) {
                    bookmarkName = BookmarksActivity.bookmarksMgr.root.containsBookmarkDeep(view.getUrl());
                }

                if (bookmarkName != null) {
                    BI.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
                } else {
                    BI.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
                }
            }
            //force colorize toolbar
            activityRef.get().toolbar.requestLayout();
        }

        addJavascriptInterface((CustomWebView) view);

    }

    private void addJavascriptInterface(CustomWebView webView)
    {
        {
            // Add javascript interface to be called when the video ends (must be done before page load)
            webView.addJavascriptInterface(new Object()
            {
                @JavascriptInterface
                @SuppressWarnings("unused")
                public void notifyVideoEnd() // Must match Javascript interface method of VideoEnabledWebChromeClient
                {
                    // This code is not executed in the UI thread, so we must force it to happen
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            webView.notifyVideoEnded();
                        }
                    });
                }


                @JavascriptInterface
                @SuppressWarnings("unused")
                public void back() // Must match Javascript interface method of VideoEnabledWebChromeClient
                {
                    Log.d("LB", "notify can't go back");
                    // This code is not executed in the UI thread, so we must force it to happen
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(webView.canGoBack())
                                webView.goBack();
                            else if (activityRef.get().drawerLayout.isDrawerOpen(activityRef.get().browserListView)){
                                activityRef.get().finish();
                            }else {
                                activityRef.get().drawerLayout.openDrawer(activityRef.get().browserListView);
                            }
                        }
                    });
                }
            }, "_CustomWebView"); // Must match Javascript interface name of VideoEnabledWebChromeClient

        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);

        int errorCode = error.getPrimaryError();
        System.out.println("SSL ERROR " + errorCode + " DETECTED");

        sslCertificateErrorDialog(view, handler, error, errorCode);
    }

    public boolean errorPage = false;
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        // For API 23 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (error.getErrorCode() == WebViewClient.ERROR_CONNECT) {
                // Handle ERR_CONNECTION_REFUSED
                String url = request.getUrl().toString();
                Log.d("CustomWebViewClient", "Received ERR_CONNECTION_REFUSED for URL: " + url);

                if (url.startsWith("http://")) {
                    String newUrl = "https://" + url.substring(7); // Replace http:// with https://
                    view.loadUrl(newUrl);
                }
            }
        } else {
            // Handle older API levels if needed
            super.onReceivedError(view, request, error);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        errorPage = true;
        Log.d("LB", "error page");
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);

    }

    @SuppressLint({"NewApi", "StringFormatInvalid", "StringFormatMatches"})
    // Is surpressed as the code will only be executed on the correct platform
    private void sslCertificateErrorDialog(WebView view, SslErrorHandler handler, SslError error, int errorCode)
            throws Resources.NotFoundException {

            if (!isSSLSnackShowing()) {
                String title = "SSL Error detected";
                String msg = "";
                String url = "";

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    url = error.getUrl();
                } else {
                    url = error.toString();
                    url = url.substring(url.lastIndexOf(" on URL: ") + 9);
                }

                String host = null;
                try {
                    host = new URL(url).getHost();
                } catch (Exception ignored) {
                }


                final SharedPreferences sslPreferences = activityRef.get().getSharedPreferences("sslurls", 0);

                if (host != null && !sslPreferences.contains(host)) {


                    String sslWarning = activityRef.get().getResources().getString(
                            R.string.sslWebsiteWarning);
                    String proceedQuestion = activityRef.get().getResources().getString(
                            R.string.sslProceedQuestion);

                    String urlDisplay = url;
                    if (urlDisplay.length()>500)
                        urlDisplay=urlDisplay.substring(0,500)+"...";

                    if (errorCode == SslError.SSL_UNTRUSTED) {
                        msg = String.format(
                                activityRef.get().getResources().getString(
                                        R.string.sslUntrustedMessage), urlDisplay);

                        title = String
                                .format(activityRef.get().getResources().getString(
                                        R.string.sslUntrustedTitle), urlDisplay);
                    } else if (errorCode == SslError.SSL_IDMISMATCH) {
                        String issuedTo = error.getCertificate().getIssuedTo()
                                .getCName();
                        msg = String.format(
                                activityRef.get().getResources().getString(
                                        R.string.sslIdMismatchMessage), urlDisplay,
                                issuedTo);

                        title = String.format(
                                activityRef.get().getResources().getString(
                                        R.string.sslIdMismatchTitle), urlDisplay);
                    } else if (errorCode == SslError.SSL_DATE_INVALID) {

                        Date currentDate = Calendar.getInstance().getTime();
                        Date expiredOn = error.getCertificate()
                                .getValidNotAfterDate();

                        if (currentDate.after(expiredOn)) {

                            msg = String.format(
                                    activityRef.get().getResources().getString(
                                            R.string.sslExpiredMessage), urlDisplay,
                                    expiredOn.toString());

                            title = String.format(
                                    activityRef.get().getResources().getString(
                                            R.string.sslExpiredTitle), urlDisplay);
                        } else {
                            Date validFrom = error.getCertificate()
                                    .getValidNotBeforeDate();
                            msg = String.format(
                                    activityRef.get().getResources().getString(
                                            R.string.sslNotYetValidMessage), urlDisplay,
                                    validFrom.toString());

                            title = String.format(
                                    activityRef.get().getResources().getString(
                                            R.string.sslNotYetValidTitle), urlDisplay);

                        }

                    }


                    showSSLSnack(msg + " " + sslWarning + "\n\n" + proceedQuestion, title, handler, host);
                }
            }
        }


        @Override
        public void onDownloadStart (String url, String userAgent, String contentDisposition, String
        mimetype,long contentLength){
            Log.i("LB", "Download: " + url);
            Log.i("LB", "Length: " + contentLength);
            Log.d("LB","onDownloadStart");
            Uri downloadUri = Uri.parse(url);

            // get file name. if filename exists in contentDisposition, use it. otherwise, use the last part of the url.
            String fileName = downloadUri.getLastPathSegment();

            String headerFileName = Tools.getFileNameFromHeader(contentDisposition);
            if (headerFileName!=null)
                fileName = headerFileName;

            onDownloadStartNoStream(activityRef.get(), url, userAgent, contentDisposition, mimetype, fileName, false);
        }

        public boolean isSSLSnackShowing(){
            return activityRef.get().findViewById(R.id.ssl_snack) !=null;
        }

        public void dismissSSLSnack(){
            if (isSSLSnackShowing()) {
                ((FrameLayout) activityRef.get().findViewById(R.id.content_frame)).removeView(activityRef.get().findViewById(R.id.ssl_snack));
            }
        }

    /**
     * Displays a message at bottom of screen with an SSL Error
     * @param message ssl error message
     * @param title ssl error title
     * @param handler SSL handler
     * @param host ssl host source of error
     */
        public void showSSLSnack(String message, String title, final SslErrorHandler handler, final String host){
            RelativeLayout rl = (RelativeLayout) activityRef.get().getLayoutInflater().inflate(R.layout.ssl_snack, null);
            ((TextView)rl.findViewById(R.id.title)).setText(title);

            ((TextView)rl.findViewById(R.id.message)).setText(message);
            rl.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.cancel();
                    dismissSSLSnack();
                }
            });
            rl.findViewById(R.id.accept_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.proceed();
                    SharedPreferences sslPreferences = activityRef.get().getSharedPreferences("sslurls", 0);
                    sslPreferences.edit().putBoolean(host, true).apply();
                    dismissSSLSnack();
                }
            });

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM;
            ((FrameLayout)activityRef.get().findViewById(R.id.content_frame)).addView(rl, lp);
        }

}
