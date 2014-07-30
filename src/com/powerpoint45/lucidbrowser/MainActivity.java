package com.powerpoint45.lucidbrowser;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BrowserHandler {
	public static Activity           activity;
	public static Context            ctxt;
	public static SharedPreferences  mPrefs;
	public static SharedPreferences  mGlobalPrefs;
	static LayoutInflater            inflater;
	public static InputMethodManager imm;
	NotificationManager              mNotificationManager;
	
	public static RelativeLayout       bar;
	public static ActionBar            actionBar;
	static FrameLayout                 contentView;
	public static DrawerLayout         mainView; 
	
	public static LinearLayout        webLayout;
	public static ListView            browserListView;
	public static BrowserImageAdapter browserListViewAdapter;
	static Vector <CustomWebView>     webWindows;
	static public int NavMargine;   //used in CustomWebView
	public static int StatusMargine;//used in SetupLayouts
	public static List<String> responses;
	static BrowserBarAdapter suggestionsAdapter;
	SystemBarTintManager tintManager;
	
	static Dialog dialog;
	
	public static String assetHomePage; //string for default startPage

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity     = this;
		ctxt         = getApplicationContext();
		mPrefs       = getSharedPreferences("pref",0);
		mGlobalPrefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
		inflater     = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		imm          = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		
		bar                       = new RelativeLayout(this);
		actionBar                 = getActionBar();
		
		
		webLayout                 = (LinearLayout) inflater.inflate(R.layout.page_web, null);
		browserListViewAdapter    = new BrowserImageAdapter(this);
		webWindows                = new Vector<CustomWebView>();
		
		
		Point screenSize = new Point();
		screenSize.x=getWindow().getWindowManager().getDefaultDisplay().getWidth();
		screenSize.y=getWindow().getWindowManager().getDefaultDisplay().getHeight();
		
		if (Math.min(screenSize.x, screenSize.y)<510)//px  //may need to be adjusted
			assetHomePage = "file:///android_asset/home_small.html";
		else
			assetHomePage = "file:///android_asset/home.html";
		
		
		
		Properties.update_preferences();
		
		if (Properties.sidebarProp.swapLayout)
			mainView              = (DrawerLayout) inflater.inflate(R.layout.main_swapped, null);
		else
			mainView              = (DrawerLayout) inflater.inflate(R.layout.main, null);
		
		contentView               = ((FrameLayout)mainView.findViewById(R.id.content_frame));
		
		browserListView           = (ListView) mainView.findViewById(R.id.right_drawer);
		
		
		if (Properties.appProp.fullscreen)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus)
				if (id == 0) {//transparency is not supported
					Properties.appProp.transparentNav   =false;
					Properties.appProp.TransparentStatus=false;
				}
		
		Tools tools = new Tools();
		tintManager = new SystemBarTintManager(activity);
		
		if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus)
			if (id != 0) {
 		        if (Properties.appProp.transparentNav)
		        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		        if (Properties.appProp.TransparentStatus)
		        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		        if (Properties.appProp.fullscreen && Properties.appProp.transparentNav){
		        	if (tools.hasSoftNavigation(activity))
		        		NavMargine= tools.getNavBarHeight(getResources());
		        }else if (Properties.appProp.fullscreen){
		        	StatusMargine=Properties.ActionbarSize;
		        }else if (Properties.appProp.transparentNav){
		        	if (tools.hasSoftNavigation(activity))
		        		NavMargine= tools.getNavBarHeight(getResources());
		        	StatusMargine=Properties.ActionbarSize+tools.getStatusBarHeight(getResources());
		        }else
		        	StatusMargine=Properties.ActionbarSize+tools.getStatusBarHeight(getResources());
		        if (Properties.appProp.TransparentStatus){
		        	tintManager = new SystemBarTintManager(this);
		        	tintManager.setStatusBarTintEnabled(true);
		        	tintManager.setStatusBarTintColor(Properties.appProp.actionBarColor);
		        	if (Properties.appProp.fullscreen)
		        		tintManager.setTintAlpha(0.0f);
		        	
		        }
		        
		        if (Properties.appProp.TransparentStatus&&Properties.appProp.fullscreen)
		        	StatusMargine=Properties.ActionbarSize;
		    }
		
		
		SetupLayouts.setuplayouts();
		
		Intent intent = getIntent();
		SharedPreferences savedInstancePreferences = getSharedPreferences("state",0);
		int numSavedTabs =savedInstancePreferences.getInt("numtabs", 0);
		
		if (numSavedTabs>0){
			System.out.println("RESTORING STATE");
			String [] urls = new String[numSavedTabs];
			for (int I = 0;I<numSavedTabs;I++)
				urls[I]=savedInstancePreferences.getString("URL"+I, "http://www.google.com/");
			 
			int tabNumber = savedInstancePreferences.getInt("tabNumber",0);
			for (int I=0;I<urls.length;I++){
				webWindows.add(new CustomWebView(MainActivity.this,null,urls[I]));
				browserListViewAdapter.notifyDataSetChanged();
			}
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(tabNumber));
			savedInstancePreferences.edit().clear().commit();
			savedInstancePreferences=null;
		}
		else{//If no InstanceState is found, just add a single page
			if (intent.getAction()!=Intent.ACTION_WEB_SEARCH &&intent.getAction()!=Intent.ACTION_VIEW){//if page was requested from a different app, do not load home page
				webWindows.add(new CustomWebView(MainActivity.this,null,null));
				((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(0));
				browserListViewAdapter.notifyDataSetChanged();
			}
		}
		
		//detect if app was opened from a different app to open a site
        if (intent.getAction()==Intent.ACTION_WEB_SEARCH ||intent.getAction()==Intent.ACTION_VIEW){
        	if (intent.getDataString()!=null){
    	    	webWindows.add(new CustomWebView(MainActivity.this,null,intent.getDataString()));
    	    	((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
    	    	((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(webWindows.size()-1));
    	    	((EditText) bar.findViewById(R.id.browser_searchbar)).setText(intent.getDataString());
    	    	browserListViewAdapter.notifyDataSetChanged();
        	}
        }
		
		
		
		
		
		
		
		
		if (Properties.appProp.systemPersistent){
			Intent notificationIntent = new Intent(this, MainActivity.class);  
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,   
			            PendingIntent.FLAG_UPDATE_CURRENT);
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_stat_location_web_site)
					.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
					.setOngoing(true)
					.setContentIntent(contentIntent)
					.setPriority(2)
			        .setContentTitle(getResources().getString(R.string.label));
			mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(1, mBuilder.build());
		}
		
		
		
		
		contentView.addView(webLayout);
		setContentView(mainView);

		View decorView = getWindow().getDecorView();

		decorView.setOnSystemUiVisibilityChangeListener
		        (new View.OnSystemUiVisibilityChangeListener() {
		    @Override
		    public void onSystemUiVisibilityChange(int visibility) {
		        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
		        	if (tintManager!=null && Properties.appProp.TransparentStatus)
		        		tintManager.setStatusBarTintEnabled(true);
		        	Properties.appProp.fullscreen = true;
		        } else {
		        	if (tintManager!=null && Properties.appProp.TransparentStatus)
		        		tintManager.setStatusBarTintEnabled(false);
		        	Properties.appProp.fullscreen = false;
		        }
		    }
		});
		
	}
	
	
	public static boolean isDownloadManagerAvailable(Context context) {
	    try {
	        Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.addCategory(Intent.CATEGORY_LAUNCHER);
	        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
	        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
	                PackageManager.MATCH_DEFAULT_ONLY);
	        return list.size() > 0;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	public static void browserSearch(){
		if (webWindows.size()==0){
			webWindows.add(new CustomWebView(MainActivity.activity,null,null));
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(0));
		}
		
		CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
		
		WV.stopLoading();
		if (SetupLayouts.actionBarNum==2)
			((EditText)bar.findViewById(R.id.browser_searchbar)).clearFocus();
		String q = ((EditText)bar.findViewById(R.id.browser_searchbar)).getText().toString();
		if (q.contains(".") && !q.contains(" ")){
			if (q.startsWith("http://")||q.startsWith("https://"))
				WV.loadUrl(q);
			else if (q.startsWith("www."))
				WV.loadUrl("http://"+q);
			else
				WV.loadUrl("http://"+q);
		}
		else if (q.startsWith("about:home"))
				WV.loadUrl(assetHomePage);
		else if (q.startsWith("about:")||q.startsWith("file:"))
			WV.loadUrl(q);
		else
			WV.loadUrl("http://www.google.com/search?q="+q.replace(" ", "+"));
	}
	
	public void browserActionClicked(View v){
		Handler handler=new Handler();
 		Runnable r=new Runnable(){
 		    public void run() {
 		    	mainView.closeDrawers();
 		    }
 		};
 		if (v.getId() != R.id.browser_bookmark)
 			handler.postDelayed(r, 500);	
		
		if (webWindows.size()==0){
			webWindows.add(new CustomWebView(MainActivity.this,null,null));
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(0));
		}
		
		Message msg = new Message();
		final CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
		switch (v.getId()){
		case R.id.browser_home:
			WV.loadUrl(mPrefs.getString("browserhome", assetHomePage));
			WV.clearHistory();
			break;
		case R.id.browser_share:
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND); 
		    sharingIntent.setType("text/plain");
		    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Link");
		    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, WV.getUrl());
		    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));
			break;
		case R.id.browser_back:
			WV.goBack();
			break;
		case R.id.browser_forward:
			WV.goForward();
			break;
		case R.id.browser_refresh:
			if (WV.getProgress()!=100)
				WV.stopLoading();
			else
				WV.reload();
			break;
		case R.id.browser_bookmark:
			SharedPreferences.Editor ed = mPrefs.edit();
			ImageButton BI = (ImageButton) MainActivity.bar.findViewById(R.id.browser_bookmark);
			int numBooks=MainActivity.mPrefs.getInt("numbookmarkedpages", 0);
			boolean isBook = false;
			int markedBook = 0;
			for (int i=0;i<numBooks;i++){
				if (WV!=null)
					if (WV.getUrl()!=null)
	    				if (MainActivity.mPrefs.getString("bookmark"+i, "").compareTo(WV.getUrl())==0){
	    					BI.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
	    					isBook=true;
	    					markedBook = i;
	    					break;
	    				}
			}
			if (isBook){
				BI.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
				removeBookmark(markedBook);
				
			    if (BI!=null)
	    			BI.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
			}else{
				try{
					
					Bitmap b = WV.getFavicon();
					if (b.getRowBytes()>1){
						new File(getApplicationInfo().dataDir+"/icons/").mkdirs();
						URL url = new URL(WV.getUrl());
						FileOutputStream out = new FileOutputStream(getApplicationInfo().dataDir+"/icons/" + url.getHost());
						WV.getFavicon().compress(Bitmap.CompressFormat.PNG, 100, out);
						out.flush();
					    out.close();
					}else{
						
					}
				}catch(Exception e){e.printStackTrace();};
				
				ed.putString("bookmark"+numBooks,WV.getUrl());
			    ed.putString("bookmarktitle"+numBooks,WV.getTitle());
			    ed.putInt("numbookmarkedpages",numBooks+1);
			    ed.commit();
			    
			    if (BI!=null)
	    			BI.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
			}
            break;
		case R.id.browser_find_on_page:
			SetupLayouts.setUpFindBar();
			setUpFindBarListeners();
			suggestionsAdapter = null;
			// Focus on Find Bar
			TextView findText = (TextView) bar.findViewById(R.id.find_searchbar);
			findText.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(findText, InputMethodManager.SHOW_IMPLICIT);
			break;
		case R.id.browser_open_bookmarks:
            startActivity(new Intent(ctxt,BookmarksActivity.class));
			break;
		case R.id.browser_set_home:
			mPrefs.edit().putString("browserhome", WV.getUrl()).commit();
			msg.obj=WV.getTitle()+" set";
			msg.what = 1;
            messageHandler.sendMessage(msg);
	    	 break;
	    	 
		case R.id.browser_settings:
			if (mNotificationManager!=null)
				mNotificationManager.cancel(1);
			startActivity(new Intent(ctxt,SettingsV2.class));
			onStop();
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		case R.id.browser_exit:
			doExiting();
			break;
		case R.id.browser_toggle_desktop:
			mGlobalPrefs.edit().putBoolean("usedesktopview", !Properties.webpageProp.useDesktopView).commit();
			Properties.webpageProp.useDesktopView = !Properties.webpageProp.useDesktopView;
			
			for (int I = 0;I<webWindows.size();I++){
					if (Properties.webpageProp.useDesktopView) {
						webWindows.get(I).getSettings().setUserAgentString(
								webWindows.get(I).createUserAgentString("desktop"));
						webWindows.get(I).getSettings().setLoadWithOverviewMode(true);
					} else {
						webWindows.get(I).getSettings().setUserAgentString(
								webWindows.get(I).createUserAgentString("mobile"));
						webWindows.get(I).getSettings().setLoadWithOverviewMode(false);
					}
				webWindows.get(I).reload();
			}
			break;
		}
	}
	
	private void setUpFindBarListeners() {

		final CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);

		// Setup Button Listeners
		((ImageView)bar.findViewById(R.id.find_exit)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SetupLayouts.dismissFindBar();
			}
		});	

		((ImageView)bar.findViewById(R.id.find_back)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WV.findNext(false);
			}
		});

		((ImageView)bar.findViewById(R.id.find_forward)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WV.findNext(true);
			}
		});

		((EditText)bar.findViewById(R.id.find_searchbar)).addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				WV.findAll(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
	}
	
	private void exitBrowser(){
		if (Properties.webpageProp.clearonexit){
			clearTraces();
		}
		finish();
	}

	private void doExiting() {
		if (Properties.webpageProp.exitconfirmation){
			exitBrowserWithConfirmation();
		} else {
			exitBrowser();
		}
	}
	
	private void exitBrowserWithConfirmation() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.activity);
		builder.setMessage(R.string.confirm_exit_text)
		       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   exitBrowser();
		           }
		       })
		       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		           }
		       });
		Dialog d = builder.create();
		d.show();
	}
	
	public void removeBookmark(int pos){
		int numBooks=MainActivity.mPrefs.getInt("numbookmarkedpages", 0);
		SharedPreferences.Editor ed = MainActivity.mPrefs.edit();
		ed.putString("bookmark"+pos,MainActivity.mPrefs.getString("bookmark"+(numBooks-1), "null"));
	    ed.putString("bookmarktitle"+pos,MainActivity.mPrefs.getString("bookmarktitle"+(numBooks-1), "null"));
		
	    ed.remove("bookmark"+numBooks);
	    ed.remove("bookmarktitle"+numBooks);
	    ed.putInt("numbookmarkedpages",numBooks-1);
	    ed.commit();
	}
	
	public void closeCurrentTab(View v){
		int pos = (Integer) v.getTag();
		ProgressBar PB = (ProgressBar) MainActivity.webLayout.findViewById(R.id.webpgbar);
		ImageButton BookmarkButton = (ImageButton) MainActivity.bar.findViewById(R.id.browser_bookmark);
		ImageButton refreshButton = (ImageButton) MainActivity.bar.findViewById(R.id.browser_refresh);
		
		if ((pos)<=(webWindows.size()-1)){
			webWindows.get(pos).loadUrl("about:blank");
			
			if (webLayout.findViewById(R.id.browser_page)==webWindows.get(pos)){
				if ((pos-1)>=0){
					((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
					((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(pos-1));
					if (((TextView) bar.findViewById(R.id.browser_searchbar))!=null && webWindows.get(pos-1).getUrl()!=null)
						((TextView) bar.findViewById(R.id.browser_searchbar)).setText(webWindows.get(pos-1).getUrl().replace("http://", "").replace("https://", ""));
					if (webWindows.get(pos-1).getProgress()<100){
						PB.setVisibility(View.VISIBLE);
						refreshButton.setImageResource(R.drawable.btn_toolbar_stop_loading_normal);
					}
					else{
						PB.setVisibility(View.INVISIBLE);
						refreshButton.setImageResource(R.drawable.btn_toolbar_reload_normal);
					}
					System.out.println("CLOSED"+ webWindows.get(pos-1).getProgress());
					
					
					int numBooks=MainActivity.mPrefs.getInt("numbookmarkedpages", 0);
					boolean isBook = false;
					for (int i=0;i<numBooks;i++){
						if (webWindows.get(pos-1)!=null)
							if (webWindows.get(pos-1).getUrl()!=null)
			    				if (MainActivity.mPrefs.getString("bookmark"+i, "").compareTo(webWindows.get(pos-1).getUrl())==0){
			    					BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
			    					isBook=true;
			    					break;
			    				}
					}
					if (!isBook){
						BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
					}
					
				}
				else{
					((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
					if (((TextView) bar.findViewById(R.id.browser_searchbar))!=null)
						((TextView) bar.findViewById(R.id.browser_searchbar)).setText("");
					ImageView IV = new ImageView(ctxt);
					IV.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
					IV.setScaleType(ImageView.ScaleType.CENTER);
					IV.setImageResource(R.drawable.web_logo);
					((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(IV);
					PB.setVisibility(View.INVISIBLE);
					BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
					refreshButton.setImageResource(R.drawable.btn_toolbar_reload_normal);
				}
			}
			
			webWindows.remove(pos);
		}
		browserListViewAdapter.notifyDataSetChanged();
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) !=
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
        	mainView.closeDrawers();
        }
        
        if (intent.getAction()==Intent.ACTION_WEB_SEARCH ||intent.getAction()==Intent.ACTION_VIEW){
        		if (intent.getDataString()!=null){
        			int tabNumber = intent.getIntExtra("tabNumber", -1); //used if intent is coming from Lucid Browser
        			
        			if (tabNumber!=-1 && tabNumber < webWindows.size()){
        				webWindows.get(tabNumber).loadUrl(intent.getDataString());
        			}else
        				tabNumber=-1;
        				
        			if (tabNumber==-1){
	    	    		openURLInNewTab(intent.getDataString());
        			}
        			
        		}
        }
	}
	
	public static void openURLInNewTab(String url){
		if (url!=null){
			webWindows.add(new CustomWebView(MainActivity.activity,null,url));
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(webWindows.size()-1));
			((EditText) bar.findViewById(R.id.browser_searchbar)).setText(url);
			browserListViewAdapter.notifyDataSetChanged();
		}
	}
	
	public static void closeVideoViewIfOpen(){
		try{
			CustomWebView WV = (CustomWebView) mainView.findViewById(R.id.web_holder).findViewById(R.id.browser_page);
			if (WV!=null)
				if (WV.isVideoPlaying())
					WV.getChromeClient().onHideCustomView();
		}catch(Exception e){};
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 	    // Confirm the view is a webview
 	    if (v instanceof WebView) {
 	        WebView.HitTestResult result = ((WebView) v).getHitTestResult();

 	        if (result != null) {
 	            int type = result.getType();

 	            if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
	                LinearLayout inflateView = ((LinearLayout) MainActivity.inflater.inflate(R.layout.web_menu_popup, null));
	                
	           if (type != WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE){
	        	   inflateView.findViewById(R.id.saveimage).setVisibility(View.GONE);	        	   
	           }
	                inflateView.setTag(result.getExtra());
	                MainActivity.dialog = new Dialog(MainActivity.activity);
					MainActivity.dialog.setTitle(R.string.wallpaper_instructions);
					MainActivity.dialog.setContentView(inflateView);
					MainActivity.dialog.show();
	            }
 	           else if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
 	                LinearLayout inflateView = ((LinearLayout) MainActivity.inflater.inflate(R.layout.web_menu_popup, null));
 	                
 	                if (type==WebView.HitTestResult.IMAGE_TYPE){
 	                	inflateView.findViewById(R.id.copyurl).setVisibility(View.GONE); 	                	
 	                }
 	                
 	                inflateView.setTag(result.getExtra());
 	                MainActivity.dialog = new Dialog(MainActivity.activity);
					MainActivity.dialog.setTitle(R.string.wallpaper_instructions);
					MainActivity.dialog.setContentView(inflateView);
					MainActivity.dialog.show();
 	            }
 	           
 	        }
 	    }
 	}
	
	//used in the web dialog popup /res/layout/web_menu_popup.xml
	public void webviewActionClicked(View v){
		switch(v.getId()){
		case R.id.saveimage:
			dismissDialog();
			try {
				dlImage(new URL(((LinearLayout) v.getParent()).getTag().toString()));//method in handler
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			break;
		case R.id.openinnewtab:
			dismissDialog();
			webWindows.add(new CustomWebView(MainActivity.this,null,null));
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
			webWindows.get(webWindows.size()-1).loadUrl(((LinearLayout) v.getParent()).getTag().toString());
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(webWindows.size()-1));
			((EditText) bar.findViewById(R.id.browser_searchbar)).setText("");
			browserListViewAdapter.notifyDataSetChanged();
			break;
		case R.id.copyurl:
			dismissDialog();
			String url = ((LinearLayout) v.getParent()).getTag().toString();
			
			// Gets a handle to the Clipboard Manager
		    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		    ClipData clip = ClipData.newPlainText("Copied URL", url);
		    clipboard.setPrimaryClip(clip);

			
			
		}	
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			if (!mainView.isDrawerOpen(browserListView))
				mainView.openDrawer(browserListView);
			else
				mainView.closeDrawer(browserListView);
			return true;
        }
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			//Finder is active, close it then
			 if (bar.findViewById(R.id.finder)!=null){
			    SetupLayouts.dismissFindBar();
			 	return true;
			 }else{
			
				CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
				
				if (WV!=null){
					if(WV.canGoBack())
		            {
		            	if (!MainActivity.mainView.isDrawerOpen(MainActivity.browserListView))
		            		WV.goBack();
		                return true;
		            }
				}
				if ((WV!=null && WV.canGoBack()==false) || webWindows.size()==0){
					doExiting();
					
				}
			}
				return true;
        }
	    return false;
	};
	
	@Override
	public void onUserLeaveHint(){
		// TODO Check: Should tabs be closed too?
		if (Properties.webpageProp.clearonexit){
			clearTraces();
			
		}
		
	}
	
    static void dismissDialog(){
   	 if (dialog!=null){
   		 dialog.dismiss();
   		 dialog=null;
   	 }
    }
    
    protected static void clearTraces(){
    	CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
    	if (WV!=null){
			WV.clearHistory();
			WV.clearCache(true);
    	}
		
		WebViewDatabase wDB = WebViewDatabase.getInstance(activity);
		wDB.clearFormData();
		
		CookieSyncManager.createInstance(activity);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		// Usefull for future commits:
//			cookieManager.setAcceptCookie(false)
//
//			WebView webview = new WebView(this);
//			WebSettings ws = webview.getSettings();
//			ws.setSaveFormData(false);
//			ws.setSavePassword(false); // Not needed for API level 18 or greater (deprecat
    }
    
	protected static Handler messageHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
        	if (msg.what == 1) {//toast
                String message = (String)msg.obj;
                Toast.makeText(activity, message , Toast.LENGTH_LONG).show();
            }
            if (msg.what == 3) {//bookmark dialog
            	dialog = new Dialog(activity);
				dialog.setTitle(R.string.bookmarks);
				int numBooks=MainActivity.mPrefs.getInt("numbookmarkedpages", 0);
				
				if (numBooks==0){
					RelativeLayout noBooks = (RelativeLayout) MainActivity.inflater.inflate(R.layout.empty_bookmarks_item, null);					
					dialog.setContentView(noBooks);
				} else {
					ListView lv = new ListView(activity);
					lv.setAdapter(new BookmarksListAdapter());
					dialog.setContentView(lv);					
				}
				
				dialog.show();
            }
        }
 };
 
 @Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
	 
    super.onSaveInstanceState(savedInstanceState);
    saveState();
 }
 
 @Override
 	public void onStop(){
	super.onStop();
	saveState();
	if (isFinishing())
		clearAllTabsForExit();
 }
 
 	void saveState(){
	 SharedPreferences savedInstancePreferences = getSharedPreferences("state",0);
	 if (Properties.webpageProp.closetabsonexit && isFinishing()){
		 savedInstancePreferences.edit().putInt("numtabs", 0).commit();
	 }
	 else{
		   CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
		   int tabNumber = getTabNumber();
		   savedInstancePreferences.edit().putInt("numtabs", webWindows.size()).commit();
		   
		   if (WV!=null)
			   for (int I=0;I<webWindows.size();I++){
				   savedInstancePreferences.edit().putString("URL"+I, webWindows.get(I).getUrl()).commit();
			   }
		   if (tabNumber==-1)
			   tabNumber = 0;
		   savedInstancePreferences.edit().putInt("tabNumber", tabNumber).commit();
	 }
 }
 
 	public static int getTabNumber(){
	 int tabNumber = -1;
	 CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
	 if (WV!=null)
		 for (int I=0;I<webWindows.size();I++){
			  if (webWindows.get(I)==WV)
				  tabNumber=I;
		 }
	 return tabNumber;
 }
 
 	void clearAllTabsForExit(){
	 for (int i =0; i<webWindows.size();i++){
		 webWindows.get(i).loadUrl("about:blank");
	 } 
 }
 
 	public void copyURLButtonClicked(View v){
 	ClipboardManager clipboard = (ClipboardManager)
 	        getSystemService(Context.CLIPBOARD_SERVICE);
 	
 	if (v.getId() == R.id.copyurlbutton){
	 	ClipData clip = null;
	 	if (((EditText) bar.findViewById(R.id.browser_searchbar))!=null)
	 		 clip = ClipData.newPlainText("",((EditText) bar.findViewById(R.id.browser_searchbar)).getText());
	 	
	 	if (clip!=null)
	 		clipboard.setPrimaryClip(clip);
 	}
 	
 	if (v.getId() == R.id.pastebutton){
	 	if (clipboard.hasPrimaryClip())
	 		if (((EditText) bar.findViewById(R.id.browser_searchbar))!=null)
	 			((EditText) bar.findViewById(R.id.browser_searchbar)).setText(clipboard.getText().toString());
 	}
 	
 	SetupLayouts.popup.dismiss();
 	if (((EditText) bar.findViewById(R.id.browser_searchbar))!=null)
 		((EditText) bar.findViewById(R.id.browser_searchbar)).setFocusableInTouchMode(true);
 }
}
