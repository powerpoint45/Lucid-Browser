package com.powerpoint45.lucidbrowser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Vector;

import bookmarkModel.Bookmark;
import bookmarkModel.BookmarksManager;
import views.CustomToolbar;
import views.CustomWebView;
import views.WebLayoutView;

public class MainActivity extends BrowserHandler {
	public MainActivity activity;
	public Context context;
	public static SharedPreferences prefs;
	public static SharedPreferences globalPrefs;
	static LayoutInflater inflater;
	public static InputMethodManager imm;

	public RelativeLayout barHolder;
	public RelativeLayout browserBar;
	public ActionBar actionBar;
	public ActionBarControls actionBarControls;
	public Toolbar toolbar;
	FrameLayout contentFrame;
	public DrawerLayout drawerLayout;
	
	public WebLayoutView webLayout;
	public ListView browserListView;
	public BrowserImageAdapter browserListViewAdapter;
	public Vector <CustomWebView> webWindows;
	public SystemBarTintManager tintManager;
	
	static Dialog dialog;


	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity     = this;
		context = getApplicationContext();

		prefs = getSharedPreferences("pref",0);
		globalPrefs = PreferenceManager.getDefaultSharedPreferences(context);

		inflater     = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		imm          = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		
		barHolder = new RelativeLayout(this);
		browserBar = (RelativeLayout)inflater.inflate(R.layout.browser_bar,null);
		
		webLayout                 = (WebLayoutView) inflater.inflate(R.layout.page_web, null);
		browserListViewAdapter    = new BrowserImageAdapter(this);
		webWindows                = new Vector<>();

		BookmarksManager.initBookmarksActivity(activity);
		Properties.update_preferences(activity);


		if (Properties.sidebarProp.swapLayout)
			drawerLayout = (DrawerLayout) inflater.inflate(R.layout.main_swapped, null);
		else
			drawerLayout = (DrawerLayout) inflater.inflate(R.layout.main, null);
		
		contentFrame = drawerLayout.findViewById(R.id.content_frame);
		browserListView = drawerLayout.findViewById(R.id.right_drawer);
		toolbar = drawerLayout.findViewById(R.id.toolbar);
		
		contentFrame.addView(webLayout,0);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar(); 
		actionBarControls = new ActionBarControls(actionBar,activity);
		setContentView(drawerLayout);
		
		
		if (Properties.appProp.fullscreen)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		tintManager = new SystemBarTintManager(activity);

		SetupLayouts.setuplayouts(activity);
        SetupLayouts.setupWebWindows(activity);

        if (!globalPrefs.getBoolean("showcased",false))
            new Showcaser(activity, Showcaser.STEP_BROWSER_SIDEBAR);
	}

	/**
	 * start searching in current tab based on url barHolder input
	 */
	public void browserSearch(){

		if (webWindows.size() == 0) {
			webWindows.add(new CustomWebView(activity, null));
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(0));
		}

		CustomWebView WV = webLayout.findViewById(R.id.browser_page);

		WV.stopLoading();
		if (SetupLayouts.actionBarNum == SetupLayouts.ACTIONBAR_BROWSER)
			barHolder.findViewById(R.id.browser_searchbar).clearFocus();
		String q = ((EditText) barHolder.findViewById(R.id.browser_searchbar)).getText().toString();
		WV.loadUrl(Tools.fixURL(q));
	}

	/**
	 * onClick method for most all buttons relating to the web browser
	 * @param v provided by onClick
	 */
	@SuppressLint("InflateParams")
	public void browserActionClicked(View v) {

		if (v.getId() != R.id.browser_bookmark) {
			Handler handler = new Handler();
			Runnable r = new Runnable() {
				public void run() {
					drawerLayout.closeDrawers();
				}
			};
			handler.postDelayed(r, 500);
		}

		dismissDialog();


		if (webWindows.size() == 0 && v.getId() != R.id.browser_open_bookmarks) {
			webWindows.add(new CustomWebView(activity, null));
			if (webLayout!=null && webLayout.findViewById(R.id.webviewholder)!=null) {
				((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
				((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(0));
			}
		}

		assert webLayout != null;
		CustomWebView WV = webLayout.findViewById(R.id.browser_page);

		switch (v.getId()) {
			case R.id.browser_home:
				WV.loadUrl(prefs.getString("setbrowserhome", Properties.webpageProp.assetHomePage));
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
				if (WV.getProgress() != 100)
					WV.stopLoading();
				else
					WV.reload();
				break;
			case R.id.browser_find_on_page:
				SetupLayouts.setUpActionBar(SetupLayouts.ACTIONBAR_FIND, activity);
				break;
			case R.id.browser_bookmark:
				ImageButton BI = barHolder.findViewById(R.id.browser_bookmark);

				// Find out if already a bookmark
				String url = WV.getUrl();
				if (url != null) {
					String bookmarkName = BookmarksActivity.bookmarksMgr.root.containsBookmarkDeep(WV.getUrl());

					if (bookmarkName != null) {
						BookmarksActivity.bookmarksMgr.root.removeBookmarkDeep(bookmarkName);
						BI.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
					} else {
						Bookmark newBookmark = new Bookmark(url, WV.getTitle());

						try {
							Bitmap b = WV.getFavicon();
							if (b.getRowBytes() > 1) {
								new File(getApplicationInfo().dataDir + "/icons/").mkdirs();
								URL wvURL = new URL(url);
								String pathToFavicon = getApplicationInfo().dataDir + "/icons/" + wvURL.getHost();
								FileOutputStream out = new FileOutputStream(pathToFavicon);
								WV.getFavicon().compress(Bitmap.CompressFormat.PNG, 100, out);
								out.flush();
								out.close();
								newBookmark.setPathToFavicon(pathToFavicon);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						BookmarksActivity.bookmarksMgr.root.addBookmark(newBookmark);
						BI.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
					}

				}
				BookmarksActivity.bookmarksMgr.saveBookmarksManager(MainActivity.this);
				CustomToolbar.colorizeToolbar(toolbar, Properties.appProp.primaryIntColor);
				break;
			case R.id.browser_open_bookmarks:
				Intent i = new Intent(activity, BookmarksActivity.class);
				Tools.launchIntentForResult(i,v,activity,ActivityIds.REQUEST_PICK_BOOKMARK);
				break;
			case R.id.browser_set_home:
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				adb.setTitle(R.string.set_home);
				adb.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String url = ((EditText) MainActivity.dialog.findViewById(R.id.homepage_url)).getText().toString();
						if (url.equals(""))
							prefs.edit().remove("setbrowserhome").apply();
						else
							prefs.edit().putString("setbrowserhome", Tools.fixURL(url)).apply();
					}
				});
				adb.setNegativeButton(android.R.string.cancel, null);
				adb.setView(getLayoutInflater().inflate(R.layout.homepage_enter, null));
				dialog = adb.create();
				dialog.show();

				if (!Properties.appProp.darkTheme)
					((ImageView) dialog.findViewById(R.id.reset_homepage)).setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
				else
					((ImageView) dialog.findViewById(R.id.reset_homepage)).setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
				if (!WV.getUrl().equals(Properties.webpageProp.assetHomePage))
					((EditText) dialog.findViewById(R.id.homepage_url)).setText(WV.getUrl());
				break;
			case R.id.reset_homepage:
				((EditText) dialog.findViewById(R.id.homepage_url)).setText("");
				break;
			case R.id.browser_toggle_desktop:
				globalPrefs.edit().putBoolean("usedesktopview", !Properties.webpageProp.useDesktopView).apply();
				Properties.webpageProp.useDesktopView = !Properties.webpageProp.useDesktopView;

				for (int I = 0; I < webWindows.size(); I++) {
					webWindows.get(I).setDesktopMode(Properties.webpageProp.useDesktopView);
					webWindows.get(I).reload();
				}
				break;
			case R.id.browser_settings:
				Intent settingsIntent = new Intent(activity, SettingsV2.class);
				Tools.launchIntentForResult(settingsIntent,v,activity,ActivityIds.REQUEST_OPEN_SETTINGS);
				break;

			case R.id.find_exit:
				SetupLayouts.dismissFindBar(activity);
				break;
			case R.id.browser_exit:
				doExiting();
				break;
		}
	}



	/**
	 * Set up listeners for searching current webpage
	 */
	public void setUpFindBarListeners() {
		final CustomWebView WV = webLayout.findViewById(R.id.browser_page);

		// Setup Button Listeners
		actionBar.getCustomView().findViewById(R.id.find_back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WV.findNext(false);
			}
		});

		actionBar.getCustomView().findViewById(R.id.find_forward).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WV.findNext(true);
			}
		});

		((EditText) actionBar.getCustomView().findViewById(R.id.find_searchbar)).addTextChangedListener(new TextWatcher() {

			@SuppressWarnings("deprecation")
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				WV.findAll(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void afterTextChanged(Editable s) { }
		});

		actionBar.getCustomView().findViewById(R.id.find_searchbar).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				drawerLayout.closeDrawers();
			}
		});
	}


	/**
	 * simply finish activity or clear browser traces and finish (if enabled in settings)
	 */
	private void exitBrowser(){
		if (Properties.webpageProp.clearonexit)
			clearTraces();

		finish();
	}


	/**
	 * should be called before finishing activity. Displays an exit confirmation (if enabled in settings)
	 */
	private void doExiting() {
		if (Properties.webpageProp.exitconfirmation){
			exitBrowserWithConfirmation();
		} else {
			exitBrowser();
		}
	}

	/**
	 * displays an exit confirmation before calling exitBrowser
	 */
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


	/**
	 * Closes tab defined by View
	 * @param v view clicked. Must have a tag with the tab position
	 */
	public void closeTab(View v) {
		int pos = (Integer) v.getTag();
		ProgressBar PB = webLayout.findViewById(R.id.webpgbar);
		ImageButton BookmarkButton = browserBar.findViewById(R.id.browser_bookmark);
		ImageButton refreshButton = browserBar.findViewById(R.id.browser_refresh);
		webWindows.get(pos).loadUrl("about:blank");
		unregisterForContextMenu(webWindows.get(pos));
		webWindows.get(pos).destroy();

		browserBar.findViewById(R.id.browser_searchbar).clearFocus();

		Log.d("lucid", "closing child count" + pos + "," + getTabNumber() );
		if (pos == getTabNumber()) {
			if (pos <= (webWindows.size() - 1)) {
				if (webLayout.findViewById(R.id.browser_page) == webWindows.get(pos)) {
					if (webWindows.size() - 1 > 0) {
						int newTab = 0;
						if (pos == 0)
							newTab = 1;
						if (pos > 0)
							newTab = pos - 1;

						((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
						((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(newTab));
						if (browserBar.findViewById(R.id.browser_searchbar) != null && webWindows.get(newTab).getUrl() != null) {
							webWindows.get(newTab).setUrlBarText(webWindows.get(newTab).getUrl());
						}
						if (webWindows.get(newTab).getProgress() < 100) {
							PB.setVisibility(View.VISIBLE);
							if (refreshButton != null)
								refreshButton.setImageResource(R.drawable.btn_toolbar_stop_loading_normal);
						} else {
							PB.setVisibility(View.INVISIBLE);
							if (refreshButton != null)
								refreshButton.setImageResource(R.drawable.btn_toolbar_reload_normal);
						}

						// Find out if already a bookmark
						String bookmarkName = null;
						if (webWindows.get(newTab) != null && webWindows.get(newTab).getUrl() != null) {
							bookmarkName = BookmarksActivity.bookmarksMgr.root.containsBookmarkDeep(webWindows.get(newTab).getUrl());
						}

						if (bookmarkName != null) {
							BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
						} else {
							BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
						}
					} else {
                        displayNoTabsView();
						PB.setVisibility(View.INVISIBLE);
						BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
						if (refreshButton != null)
							refreshButton.setImageResource(R.drawable.btn_toolbar_reload_normal);
					}
				}
			}
		}
		webWindows.set(pos,null);
		webWindows.remove(pos);
		browserListViewAdapter.notifyDataSetChanged();
	}

    /**
     * remove any visible webview and place a web icon in its place to show no tabs are open
     */
    public void displayNoTabsView() {
        ((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
        if (browserBar.findViewById(R.id.browser_searchbar) != null)
            ((TextView) browserBar.findViewById(R.id.browser_searchbar)).setText("");
        ImageView IV = new ImageView(activity);
        IV.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        IV.setScaleType(ImageView.ScaleType.CENTER);
        IV.setImageResource(R.drawable.web_logo_material);
        ((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(IV);
    }

    /**
     * adds a new WebView and displays it in webLayout
     */
    public void openNewTab(){
        activity.webWindows.add(new CustomWebView(activity, null));
        if (activity.webLayout!=null)
            if (activity.webLayout.findViewById(R.id.webviewholder) !=null){
                ((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).removeAllViews();
                ((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).addView(activity.webWindows.get(activity.webWindows.size()-1));
            }
    }

    /**
	 * When an app chooses to open a link with this browser (and the browser is already open), onNewIntent is called
	 * @param intent provided by different app/activity
	 */
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("LB", "onNewIntent");
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) !=
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
        	drawerLayout.closeDrawers();
        }
        
        if (intent.getAction()!=null && (intent.getAction().equals(Intent.ACTION_WEB_SEARCH) ||intent.getAction().equals(Intent.ACTION_VIEW))){
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

	/**
	 * @param url to open in new tab
	 */
	public void openURLInNewTab(String url) {
		if (url != null && webWindows != null && webLayout != null) {
			webWindows.add(new CustomWebView(activity, url));
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
			((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(webWindows.size() - 1));
			if (barHolder.findViewById(R.id.browser_searchbar) != null)
				((EditText) barHolder.findViewById(R.id.browser_searchbar)).setText(url);
			browserListViewAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Hides Fullscreen video if it is playing in fullscreen
	 */
	public void closeVideoViewIfOpen(){
		try{
			CustomWebView WV = getFocussedWebView();
			if (WV!=null)
				if (WV.isVideoPlaying())
					WV.getChromeClient().onHideCustomView();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * @return true if a video is playing in fullscreen
	 */
	public boolean isVideoViewOpen(){
		try {
			CustomWebView WV = getFocussedWebView();
			if (WV != null) {
				return WV.getChromeClient().isVideoFullscreen();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


	/**
	 * @return Webview that is curently being displayed
	 */
	public CustomWebView getFocussedWebView(){
		if (drawerLayout!=null){
			return drawerLayout.findViewById(R.id.web_holder).findViewById(R.id.browser_page);
		}
		return null;
	}

	/**
	 * When Webview needs to make a menu (whwn you long press on an item) onCreateContextMenu is called
	 * @param menu provided
	 * @param v view showing menu
	 * @param menuInfo provided
	 */
	@SuppressLint({"InflateParams", "HandlerLeak"})
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

	/**
	 * Deals with menu buttons after long pressing on a WebView item
	 * such as a link or image
	 * @param v handled by onClick
	 */
	public void webviewActionClicked(View v) {
		switch (v.getId()) {
			case R.id.saveimage:
				dismissDialog();
				String url1 = ((LinearLayout) v.getParent()).getTag().toString();

				Tools.DownloadAsyncTask download = new Tools.DownloadAsyncTask(url1, activity);
				download.execute(url1);
				break;
			case R.id.openinnewtab:
				dismissDialog();
				webWindows.add(new CustomWebView(activity, ((LinearLayout) v.getParent()).getTag().toString()));
				((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
				((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(webWindows.size() - 1));
				((EditText) barHolder.findViewById(R.id.browser_searchbar)).setText("...");
				browserListViewAdapter.notifyDataSetChanged();
				break;
			case R.id.copyurl:
				dismissDialog();
				String url = ((LinearLayout) v.getParent()).getTag().toString();
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("URL", url);
				assert clipboard != null;
				clipboard.setPrimaryClip(clip);
		}
	}

	/**
	 * Used to deal with back button presses
	 * @param keyCode provided by key event
	 * @param event provided by key event
	 * @return true if managed manually
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			if (!drawerLayout.isDrawerOpen(browserListView))
				drawerLayout.openDrawer(browserListView);
			else
				drawerLayout.closeDrawer(browserListView);
			return true;
        }
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			//Finder is active, close it then
			 if (barHolder.findViewById(R.id.finder)!=null){
			    SetupLayouts.dismissFindBar(activity);
			 	return true;
			 }else{

				CustomWebView WV = webLayout.findViewById(R.id.browser_page);

				if (WV!=null){
					if(WV.canGoBack())
		            {
		            	if (!drawerLayout.isDrawerOpen(browserListView))
		            		WV.goBack();
		                return true;
		            }
				}
				if ((WV!=null && !WV.canGoBack()) || webWindows.size()==0){
					doExiting();

				}
			}
				return true;
        }
	    return false;
	}

	@Override
	public void onUserLeaveHint(){
		// TODO Check: Should tabs be closed too?
		if (Properties.webpageProp.clearonexit){
			clearTraces();
			
		}
		
	}

	/**
	 * closes any open popup windows etc
	 */
    static void dismissDialog(){
   	 if (dialog!=null){
   		 dialog.dismiss();
   		 dialog=null;
   	 }
    }

	/**
	 * Clears browser cache etc
	 */
    protected void clearTraces(){
    	CustomWebView WV = webLayout.findViewById(R.id.browser_page);
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



//    public void restoreInstance(){
//        Bundle mainBundle = new BundleManager(activity).restoreFromPreferences();
//        int numSavedTabs = 0;
//        if (mainBundle!=null)
//			numSavedTabs = mainBundle.getInt("numtabs",0);
//
//        if (numSavedTabs>0){
//            Log.d("LB","RESTORING STATE");
//
//            int tabNumber = mainBundle.getInt("tabnumber",0);
//            for (int I=0;I<numSavedTabs;I++){
//				CustomWebView wv = new CustomWebView(MainActivity.this, "na");
//				wv.restoreState(mainBundle.getBundle("WV"+I));
//				webWindows.add(wv);
//                browserListViewAdapter.notifyDataSetChanged();
//            }
//            ((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(tabNumber));
//        }else{//If no InstanceState is found, just add a single page
//            if (getIntent().getAction()!=null &&
//					!getIntent().getAction().equals(Intent.ACTION_WEB_SEARCH) &&
//					!getIntent().getAction().equals(Intent.ACTION_VIEW)){//if page was requested from a different app, do not load home page
//                webWindows.add(new CustomWebView(MainActivity.this, null));
//                ((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(0));
//                browserListViewAdapter.notifyDataSetChanged();
//            }
//        }
//    }
 
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
					SetupLayouts.setUpActionBar(SetupLayouts.ACTIONBAR_BROWSER,MainActivity.this);
					SetupLayouts.colorizeSidebar(activity);
					SetupLayouts.setupWindow(activity);
					SetupLayouts.setBarColors(activity);
					if (webWindows.size()>0)
						webWindows.get(getTabNumber()).setUrlBarText(webWindows.get(getTabNumber()).getUrl());
					browserListViewAdapter.notifyDataSetChanged();
					actionBarControls.show();
				}

				if (intent!=null && intent.getBooleanExtra("initBrowser",false))
					SetupLayouts.setupWebWindows(activity);

				break;
			case ActivityIds.REQUEST_PICK_BOOKMARK:
				if (intent!=null) {
					String url = intent.getStringExtra("url");
					if (url != null) {
						if (intent.getBooleanExtra("newtab",true))
							openURLInNewTab(url);
						else {
							CustomWebView WV = webLayout.findViewById(R.id.browser_page);
							if (WV!=null) {
								WV.stopLoading();
								WV.loadUrl(url);
							}else
								openURLInNewTab(url);
						}
					}
					break;
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

	/**
	 * Saves tabs that are opened to be reopened when app is opened again
	 */
 	void saveState(){
        Log.d("LB","saving state now");
         Bundle mainBundle = new Bundle();
         if (Properties.webpageProp.closetabsonexit && isFinishing()){
             new BundleManager(activity).saveToPreferences(new Bundle());
         } else{
			 CustomWebView WV = webLayout.findViewById(R.id.browser_page);
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

	/**
	 * @return active browser tab.
	 * Returns -1 if no active tab found
	 * Returns 0 if webLayout is null
	 */
	public int getTabNumber() {
		int tabNumber = -1;
		if (webLayout != null) {
			CustomWebView WV = webLayout.findViewById(R.id.browser_page);
			if (WV != null)
				for (int I = 0; I < webWindows.size(); I++) {
					if (webWindows.get(I) == WV)
						tabNumber = I;
				}
		} else
			return 0;
		return tabNumber;
	}

	/**
	 * Gets webviews ready for app close. Helps to clear out memory
	 */
 	void clearAllTabsForExit(){
         for (int i =0; i<webWindows.size();i++){
             webWindows.get(i).loadUrl("about:blank");
         }
    }
}
