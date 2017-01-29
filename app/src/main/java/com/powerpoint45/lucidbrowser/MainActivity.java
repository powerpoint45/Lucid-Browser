package com.powerpoint45.lucidbrowser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Vector;

import bookmarkModel.Bookmark;
import bookmarkModel.BookmarksManager;
import views.CustomToolbar;
import views.CustomWebView;
import views.WebLayoutView;

public class MainActivity extends BrowserHandler {
	public MainActivity activity;
	public Context            ctxt;
	public static SharedPreferences  mPrefs;
	public static SharedPreferences  mGlobalPrefs;
	static LayoutInflater            inflater;
	public static InputMethodManager imm;
	public NotificationManager              mNotificationManager;
	
	public RelativeLayout       bar;
	public static ActionBar            actionBar;
	public static ActionBarControls actionBarControls;
	public Toolbar                     toolbar;
	FrameLayout                 contentFrame;
	public DrawerLayout         contentView;
	
	public WebLayoutView webLayout;
	public ListView            browserListView;
	public static BrowserImageAdapter browserListViewAdapter;
	static Vector <CustomWebView>     webWindows;
	static public int NavMargine;   //used in CustomWebView
	public static List<String> responses;
	static BrowserBarAdapter suggestionsAdapter;
	SystemBarTintManager tintManager;
	
	static Dialog dialog;
	

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
		
		webLayout                 = (WebLayoutView) inflater.inflate(R.layout.page_web, null);
		browserListViewAdapter    = new BrowserImageAdapter(this);
		webWindows                = new Vector<CustomWebView>();
		
		
		//Initialize BookmarksManager
		if (BookmarksActivity.bookmarksMgr ==  null){
			BookmarksManager loadedBookmarksMgr = BookmarksManager.loadBookmarksManager(activity);
			if (loadedBookmarksMgr == null){
				Log.d("LB","BookmarksActivity.bookmarksMgr is null. Making new one");
				BookmarksActivity.bookmarksMgr = new BookmarksManager();
			}else {
				Log.d("LB","BookmarksActivity.bookmarksMgr loaded");
				BookmarksActivity.bookmarksMgr = loadedBookmarksMgr;
			}
		} else {
			Log.d("LB","BookmarksActivity.bookmarksMgr is not null");
		}
		
		
		Point screenSize = new Point();
		screenSize.x=getWindow().getWindowManager().getDefaultDisplay().getWidth();
		screenSize.y=getWindow().getWindowManager().getDefaultDisplay().getHeight();
		
		
		
		Properties.update_preferences(activity);
		
		if (Properties.sidebarProp.swapLayout)
			contentView              = (DrawerLayout) inflater.inflate(R.layout.main_swapped, null);
		else
			contentView              = (DrawerLayout) inflater.inflate(R.layout.main, null);
		
		contentFrame               = ((FrameLayout)contentView.findViewById(R.id.content_frame));
		browserListView           = (ListView) contentView.findViewById(R.id.right_drawer);
		toolbar = (Toolbar) contentView.findViewById(R.id.toolbar);
		
		contentFrame.addView(webLayout,0);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar(); 
		actionBarControls = new ActionBarControls(actionBar,activity);
		setContentView(contentView);
		
		
		if (Properties.appProp.fullscreen)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus)
				if (id == 0) {//transparency is not supported
					Properties.appProp.transparentNav   =false;
					Properties.appProp.TransparentStatus=false;
				}
		
		tintManager = new SystemBarTintManager(activity);
		
//		if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus)
//			if (id != 0) {
// 		        if (Properties.appProp.transparentNav)
//		        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//		        if (Properties.appProp.TransparentStatus)
//		        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//		        if (Properties.appProp.fullscreen && Properties.appProp.transparentNav){
//		        	if (Tools.hasSoftNavigation(activity))
//		        		NavMargine= Tools.getNavBarHeight(getResources());
//		        }else if (Properties.appProp.transparentNav){
//		        	if (Tools.hasSoftNavigation(activity))
//		        		NavMargine= Tools.getNavBarHeight(getResources());
//		        }
//		        
//		        if (Properties.appProp.TransparentStatus){
//		        	tintManager = new SystemBarTintManager(this);
//		        	tintManager.setStatusBarTintEnabled(true);
//		        	tintManager.setStatusBarTintColor(Properties.appProp.actionBarColor);
//		        	if (Properties.appProp.fullscreen)
//		        		tintManager.setTintAlpha(0.0f);
//		        	
//		        }
//		   }
				
		SetupLayouts.setuplayouts(activity);
		
		Intent intent = getIntent();
        restoreInstance();
		
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


		mNotificationManager = Tools.setUpSystemPersistence(this);
		Log.d("LB", "NOT:"+mNotificationManager);
		View decorView = getWindow().getDecorView();

		decorView.setOnSystemUiVisibilityChangeListener
		        (new View.OnSystemUiVisibilityChangeListener() {
		    @Override
		    public void onSystemUiVisibilityChange(int visibility) {
		        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
		        	if (tintManager!=null && Properties.appProp.TransparentStatus)
		        		tintManager.setStatusBarTintEnabled(true);
		        	//Properties.appProp.fullscreen = true;
		        } else {
		        	if (tintManager!=null && Properties.appProp.TransparentStatus)
		        		tintManager.setStatusBarTintEnabled(false);
		        	//Properties.appProp.fullscreen = false;
		        }
		    }
		});

        if (!mGlobalPrefs.getBoolean("showcased",false))
            new Showcaser(activity, Showcaser.STEP_BROWSER_SIDEBAR);
		
	}
	
	
	
	
	public void browserSearch(){
		if (webWindows.size()==0){
			webWindows.add(new CustomWebView(activity,null,null));
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(0));
		}
		
		CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
		
		WV.stopLoading();
		if (SetupLayouts.actionBarNum==2)
			((EditText)bar.findViewById(R.id.browser_searchbar)).clearFocus();
		String q = ((EditText)bar.findViewById(R.id.browser_searchbar)).getText().toString();
		WV.loadUrl(Tools.fixURL(q));
	}
	
	public void browserActionClicked(View v){
		Handler handler=new Handler();
 		Runnable r=new Runnable(){
 		    public void run() {
 		    	contentView.closeDrawers();
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
			WV.loadUrl(mPrefs.getString("setbrowserhome", Properties.webpageProp.assetHomePage));
			WV.clearHistory();
			break;
		case R.id.browser_share:
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		    sharingIntent.setType("text/plain");
		    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Link");
		    sharingIntent.putExtra(Intent.EXTRA_TEXT, WV.getUrl());
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
			ImageButton BI = (ImageButton) bar.findViewById(R.id.browser_bookmark);
			// Find out if already a bookmark
			String url = WV.getUrl();
			if (WV!=null && url!=null){
				String bookmarkName = BookmarksActivity.bookmarksMgr.root.containsBookmarkDeep(url);

				if (bookmarkName != null){
					BookmarksActivity.bookmarksMgr.root.removeBookmarkDeep(bookmarkName);
					BI.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
					System.out.println("BOOKMARK REMOVED!!");
				} else {
					Bookmark newBookmark = new Bookmark(url,WV.getTitle());
					newBookmark.setFavIcon(activity,WV.getFavicon());

					BookmarksActivity.bookmarksMgr.root.addBookmark(newBookmark);
					BI.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
					System.out.println("BOOKMARK SET!!");
				}

				BookmarksActivity.bookmarksMgr.saveBookmarksManager(activity);
				CustomToolbar.colorizeToolbar(toolbar, Properties.appProp.primaryIntColor, activity);

			}
            break;
		case R.id.browser_find_on_page:
			actionBarControls.show();
			actionBarControls.lock(true);
			SetupLayouts.setUpFindBar(activity);
			setUpFindBarListeners();
			suggestionsAdapter = null;
			// Focus on Find Bar
			TextView findText = (TextView) bar.findViewById(R.id.find_searchbar);
			findText.requestFocus();
			imm.showSoftInput(findText, InputMethodManager.SHOW_IMPLICIT);
			break;
		case R.id.browser_open_bookmarks:
            startActivityForResult(new Intent(ctxt,BookmarksActivity.class), ActivityIds.REQUEST_PICK_BOOKMARK);
			break;
		case R.id.browser_set_home:
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle(R.string.set_home);
			adb.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String url = ((EditText) MainActivity.dialog.findViewById(R.id.homepage_url)).getText().toString();
					if (url.equals(""))
						mPrefs.edit().remove("setbrowserhome").apply();
					else
						mPrefs.edit().putString("setbrowserhome", Tools.fixURL(url)).apply();
				}
			});
			adb.setNegativeButton(android.R.string.cancel,null);
			adb.setView(inflater.inflate(R.layout.homepage_enter,null));
			dialog = adb.create();
			dialog.show();

            ((ImageButton)dialog.findViewById(R.id.reset_homepage)).setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
			if (!WV.getUrl().equals(Properties.webpageProp.assetHomePage))
				((EditText) MainActivity.dialog.findViewById(R.id.homepage_url)).setText(WV.getUrl());
	    	 break;
		case R.id.reset_homepage:
			((EditText) MainActivity.dialog.findViewById(R.id.homepage_url)).setText("");
			break;
		case R.id.browser_settings:
			if (mNotificationManager!=null)
				mNotificationManager.cancel(8);

			startActivityForResult(new Intent(ctxt,SettingsV2.class), ActivityIds.REQUEST_OPEN_SETTINGS);
			break;
		case R.id.browser_exit:
			doExiting();
			break;
		case R.id.browser_toggle_desktop:
			mGlobalPrefs.edit().putBoolean("usedesktopview", !Properties.webpageProp.useDesktopView).commit();
			Properties.webpageProp.useDesktopView = !Properties.webpageProp.useDesktopView;

			for (int I = 0;I<webWindows.size();I++){
				webWindows.get(I).setDesktopMode(Properties.webpageProp.useDesktopView);
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
				SetupLayouts.dismissFindBar(activity);
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

		((EditText)bar.findViewById(R.id.find_searchbar)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				contentView.closeDrawers();
			}
		});
	}

	private void exitBrowser(){
		if (Properties.webpageProp.clearonexit)
			clearTraces();

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
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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


	public void closeCurrentTab(View v){
		int pos = (Integer) v.getTag();
		ProgressBar PB = (ProgressBar) webLayout.findViewById(R.id.webpgbar);
		ImageButton BookmarkButton = (ImageButton) bar.findViewById(R.id.browser_bookmark);
		ImageButton refreshButton = (ImageButton) bar.findViewById(R.id.browser_refresh);

		if ((pos)<=(webWindows.size()-1)){
			webWindows.get(pos).loadUrl("about:blank");

			if (webLayout.findViewById(R.id.browser_page)==webWindows.get(pos)){
				if ((pos-1)>=0){
					((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
					((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(pos-1));
					if (((TextView) bar.findViewById(R.id.browser_searchbar))!=null && webWindows.get(pos-1).getUrl()!=null){
						webWindows.get(pos-1).setUrlBarText(webWindows.get(pos-1).getUrl());
					}
					if (webWindows.get(pos-1).getProgress()<100){
						PB.setVisibility(View.VISIBLE);
						refreshButton.setImageResource(R.drawable.btn_toolbar_stop_loading_normal);
					}
					else{
						PB.setVisibility(View.INVISIBLE);
						refreshButton.setImageResource(R.drawable.btn_toolbar_reload_normal);
					}
					System.out.println("CLOSED"+ webWindows.get(pos-1).getProgress());

					// Find out if already a bookmark
					String bookmarkName = null;
					if (webWindows.get(pos-1)!=null && webWindows.get(pos-1).getUrl()!=null){
						bookmarkName = BookmarksActivity.bookmarksMgr.root.containsBookmarkDeep(webWindows.get(pos-1).getUrl());
					}

					if (bookmarkName!=null){
						BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
					} else {
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
			activity.unregisterForContextMenu(webWindows.get(pos));
			webWindows.remove(pos);
		}
		browserListViewAdapter.notifyDataSetChanged();
	}

	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) !=
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
        	contentView.closeDrawers();
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

	public void openURLInNewTab(String url){
		if (url!=null){
			webWindows.add(new CustomWebView(activity,null,url));
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(webWindows.size()-1));
			((EditText) bar.findViewById(R.id.browser_searchbar)).setText(url);
			browserListViewAdapter.notifyDataSetChanged();
		}
	}

	public void closeVideoViewIfOpen(){
		try{
			CustomWebView WV = (CustomWebView) contentView.findViewById(R.id.web_holder).findViewById(R.id.browser_page);
			if (WV!=null)
				if (WV.isVideoPlaying())
					WV.getChromeClient().onHideCustomView();
		}catch(Exception e){};
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		Handler mHandler = new Handler() {

	    @Override
	        public void handleMessage(Message msg) {
	            // Get link-URL.
	            final String url = (String) msg.getData().get("url");

	            // Do something with it.
	            if (url != null){
	            	runOnUiThread(new Runnable() {

						@Override
						public void run() {
							LinearLayout inflateView = ((LinearLayout) MainActivity.inflater.inflate(R.layout.web_menu_popup, null));
			            	inflateView.findViewById(R.id.saveimage).setVisibility(View.GONE);
			            	inflateView.setTag(url);
			                MainActivity.dialog = new Dialog(activity);
						    MainActivity.dialog.setTitle(R.string.wallpaper_instructions);
							MainActivity.dialog.setContentView(inflateView);
						    MainActivity.dialog.show();
						}
					});

	            }
	        }
	    };

 	    // Confirm the view is a webview
 	    if (v instanceof WebView) {
 	        WebView.HitTestResult result = ((WebView) v).getHitTestResult();
 	       if (result != null && result.getExtra()!=null && !result.getExtra().startsWith("file:")) {
 	            int type = result.getType();

 	            if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE || result.getExtra().startsWith("data:")) {
	                LinearLayout inflateView = ((LinearLayout) MainActivity.inflater.inflate(R.layout.web_menu_popup, null));

		           if (type != WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE || result.getExtra().startsWith("data:")){
		        	   inflateView.findViewById(R.id.saveimage).setVisibility(View.GONE);
		           }


	                if (type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE){
		                Message msg = mHandler.obtainMessage();
		                webWindows.get(getTabNumber()).requestFocusNodeHref(msg);
	                }else{
	                	inflateView.setTag(result.getExtra());
	                	MainActivity.dialog = new Dialog(activity);
						MainActivity.dialog.setTitle(R.string.wallpaper_instructions);
						MainActivity.dialog.setContentView(inflateView);
						MainActivity.dialog.show();
	                }

	           }
 	           else if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
 	                LinearLayout inflateView = ((LinearLayout) MainActivity.inflater.inflate(R.layout.web_menu_popup, null));

 	                if (type==WebView.HitTestResult.IMAGE_TYPE){
 	                	inflateView.findViewById(R.id.copyurl).setVisibility(View.GONE);
 	                }

 	                inflateView.setTag(result.getExtra());
 	                MainActivity.dialog = new Dialog(activity);
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
			String url = ((LinearLayout) v.getParent()).getTag().toString();

	        Tools.DownloadAsyncTask download = new Tools.DownloadAsyncTask(url,activity);
	        download.execute(url);
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
			String url2 = ((LinearLayout) v.getParent()).getTag().toString();

			// Gets a handle to the Clipboard Manager
		    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		    ClipData clip = ClipData.newPlainText("Copied URL", url2);
		    clipboard.setPrimaryClip(clip);
		    break;


		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			if (!contentView.isDrawerOpen(browserListView))
				contentView.openDrawer(browserListView);
			else
				contentView.closeDrawer(browserListView);
			return true;
        }
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			//Finder is active, close it then
			 if (bar.findViewById(R.id.finder)!=null){
			    SetupLayouts.dismissFindBar(activity);
			 	return true;
			 }else{

				CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);

				if (WV!=null){
					if(WV.canGoBack())
		            {
		            	if (!contentView.isDrawerOpen(browserListView))
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

    protected void clearTraces(){
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

	Handler messageHandler = new Handler() {

        public void handleMessage(Message msg) {
        	if (msg.what == 1) {//toast
                String message = (String)msg.obj;
                Toast.makeText(activity, message , Toast.LENGTH_LONG).show();
            }
        }
 	};
 
 @Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
//	  if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus){
//		 
//
//		  CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
//		  if (WV!=null && WV.isVideoPlaying()){
//			  Log.d("LB", "do nothing");
//		  }else{
//			  Properties.ActionbarSize= Tools.getActionBarSize();
//			  StatusMargine = Tools.getStatusMargine();
//			  Log.d("LB", "SM"+StatusMargine+"  "+"ABS"+Properties.ActionbarSize);
//			  browserListView.setPadding(0, 0, 0, NavMargine+StatusMargine);
//			  browserListView.setY(StatusMargine);			
//			  MainActivity.webLayout.setPadding(0, MainActivity.StatusMargine, 0, 0);
//		  }
//		}
	}



    public void restoreInstance(){
        Bundle mainBundle = new BundleManager(activity).restoreFromPreferences();
        int numSavedTabs = 0;
        if (mainBundle!=null)
			numSavedTabs = mainBundle.getInt("numtabs",0);

        if (numSavedTabs>0){
            Log.d("LB","RESTORING STATE");

            int tabNumber = mainBundle.getInt("tabnumber",0);
            for (int I=0;I<numSavedTabs;I++){
				CustomWebView wv = new CustomWebView(MainActivity.this,null,"na");
				wv.restoreState(mainBundle.getBundle("WV"+I));
				webWindows.add(wv);
                //webWindows.add(new CustomWebView(MainActivity.this,null,urls[I]));
                browserListViewAdapter.notifyDataSetChanged();
            }
            ((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(tabNumber));
        }else{//If no InstanceState is found, just add a single page
            if (getIntent().getAction()!=Intent.ACTION_WEB_SEARCH && getIntent().getAction()!=Intent.ACTION_VIEW){//if page was requested from a different app, do not load home page
                webWindows.add(new CustomWebView(MainActivity.this,null,null));
                ((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(0));
                browserListViewAdapter.notifyDataSetChanged();
            }
        }
    }
 
	 @Override
	 public void onPause(){
	 	//BookmarksActivity.bookmarksMgr.saveBookmarksManager();
	 	super.onPause();
	 }
 
	@Override
 	public void onStop(){
	    super.onStop();
        saveState();
        if (isFinishing())
            clearAllTabsForExit();
	}
	
	@SuppressLint("NewApi")
	@Override   
	 protected void onActivityResult(int requestCode, int resultCode,  
	                                    Intent intent) {

		switch (requestCode) {
			case ActivityIds.REQUEST_OPEN_SETTINGS:

				if (intent!=null && intent.getExtras()!=null && intent.getExtras().getBoolean("restart",false)){
					finish();
					startActivity(new Intent(MainActivity.this, MainActivity.class));
				}else{
					//update settings and appearence
					Properties.update_preferences(MainActivity.this);
					SetupLayouts.setUpActionBar(MainActivity.this);
					SetupLayouts.colorizeSidebar(activity);
					SetupLayouts.setupWindow(activity);
					if (webWindows.size()>0)
						webWindows.get(getTabNumber()).setUrlBarText(webWindows.get(getTabNumber()).getUrl());
					browserListViewAdapter.notifyDataSetChanged();
					actionBarControls.show();
					Tools.setUpSystemPersistence(this);
				}
				break;
			case ActivityIds.REQUEST_PICK_BOOKMARK:
				if (intent!=null) {
					String url = intent.getStringExtra("url");
					if (url != null) {
						if (intent.getBooleanExtra("newtab", true))
							openURLInNewTab(url);
						else {
							CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
							if (WV != null) {
								WV.stopLoading();
								WV.loadUrl(url);
							}
						}
					}
				}
				break;
			case VideoEnabledWebChromeClient.FILECHOOSER_RESULTCODE:
				if (VideoEnabledWebChromeClient.mUploadMessage != null) {
					Uri result = intent == null || resultCode != RESULT_OK ? null
							: intent.getData();
					VideoEnabledWebChromeClient.mUploadMessage.onReceiveValue(result);
					VideoEnabledWebChromeClient.mUploadMessage = null;
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					if (VideoEnabledWebChromeClient.mUploadMessageLol != null) {
						Uri[] uris = VideoEnabledWebChromeClient.FileChooserParams.parseResult(resultCode, intent);
						VideoEnabledWebChromeClient.mUploadMessageLol.onReceiveValue(uris);
						VideoEnabledWebChromeClient.mUploadMessageLol = null;
					} else {
						Uri result = intent == null || resultCode != RESULT_OK ? null
								: intent.getData();
						Uri[] uriss = new Uri[1];
						uriss[0] = result;
						VideoEnabledWebChromeClient.mUploadMessageLol.onReceiveValue(uriss);
						VideoEnabledWebChromeClient.mUploadMessageLol = null;
					}
				break;

			}
		}
	  }
 
 	void saveState(){
        Log.d("LB","saving state now");
         Bundle mainBundle = new Bundle();
         if (Properties.webpageProp.closetabsonexit && isFinishing()){
             new BundleManager(activity).saveToPreferences(new Bundle());
         } else{
			 CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
             int tabNumber = getTabNumber();
             mainBundle.putInt("numtabs",webWindows.size());

			 if (tabNumber==-1)
				 tabNumber = 0;
			 mainBundle.putInt("tabnumber", tabNumber);

			 if (WV!=null)
				 for (int I=0;I<webWindows.size();I++){
					 Bundle bundle = new Bundle();
					 webWindows.get(I).saveState(bundle);
					 mainBundle.putBundle("WV"+I,bundle);
				 }

			 new BundleManager(activity).saveToPreferences(mainBundle);
         }
    }
 
 	public int getTabNumber(){
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
