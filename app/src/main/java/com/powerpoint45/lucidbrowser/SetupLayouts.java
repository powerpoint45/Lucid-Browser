package com.powerpoint45.lucidbrowser;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import views.CustomWebView;

@SuppressLint("Registered")
public class SetupLayouts extends MainActivity {
	static int actionBarNum;
	static final int ACTIONBAR_BROWSER   = 2;
	static final int ACTIONBAR_FIND      = 4;

	public static void setuplayouts(final MainActivity activity) {
		activity.actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		activity.barHolder.setClickable(true);
		activity.barHolder.setFocusable(true);
		activity.barHolder.setFocusableInTouchMode(true);
		setupWindow(activity);

		setUpActionBar(ACTIONBAR_BROWSER, activity);
		activity.actionBar.setCustomView(activity.barHolder
				,new android.support.v7.app.ActionBar.LayoutParams(android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT
				,android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT));

		setBarColors(activity);

		AppCompatAutoCompleteTextView browserEditText = activity.browserBar.findViewById(R.id.browser_searchbar);

		if (!Properties.webpageProp.disablesuggestions)
			browserEditText.setAdapter(new BrowserBarAdapter(activity));

		browserEditText.setFocusable(true);
		browserEditText.setFocusableInTouchMode(true);
		browserEditText.setScrollContainer(true);
		browserEditText.setDropDownAnchor(R.id.toolbar);
		browserEditText.setDropDownWidth(LayoutParams.MATCH_PARENT);
		browserEditText.setThreshold(0);
		browserEditText.setHint(activity.getResources().getString(R.string.urlbardefault));
		browserEditText.setHintTextColor(Properties.appProp.primaryIntColor);


		browserEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					activity.webLayout.postDelayed(new Runnable() {
						@Override
						public void run() {
							imm.hideSoftInputFromWindow(activity.browserBar.findViewById(R.id.browser_searchbar).getWindowToken(), 0);
							activity.browserSearch();
							Log.d("LL", "GO");
						}
					}, 300);
				}
				return true;   // Consume the event
			}
		});

		//detect when keyboard press enter or go in browser
		browserEditText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					Log.d("LL", "key press");
					activity.webLayout.postDelayed(new Runnable() {
						@Override
						public void run() {
							imm.hideSoftInputFromWindow(activity.browserBar.findViewById(R.id.browser_searchbar).getWindowToken(), 0);
							activity.browserSearch();
							Log.d("LL", "GO");
						}
					}, 300);
					return true;
				}
				return false;
			}
		});


		//detect when clicking a browser suggested search
		browserEditText.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
									int arg2, long arg3) {
				activity.webLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						Log.d("LL", "key click");
						final EditText ET = activity.browserBar.findViewById(R.id.browser_searchbar);
						imm.hideSoftInputFromWindow(ET.getWindowToken(), 0);
						activity.browserSearch();
						ET.clearFocus();
					}
				}, 300);
			}
		});


		colorizeSidebar(activity);
		activity.browserListView.setAdapter(activity.browserListViewAdapter);
		activity.browserListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,long arg3) {
				
				if (activity.barHolder.findViewById(R.id.finder)!=null)
					SetupLayouts.dismissFindBar(activity);
				
				MainActivity.imm.hideSoftInputFromInputMethod(activity.barHolder.findViewById(R.id.browser_searchbar).getWindowToken(), 0);
				
				ImageButton BookmarkButton = activity.barHolder.findViewById(R.id.browser_bookmark);
				ImageButton refreshButton = activity.barHolder.findViewById(R.id.browser_refresh);
				
				if (activity.barHolder.findViewById(R.id.browser_searchbar) !=null)
					activity.barHolder.findViewById(R.id.browser_searchbar).clearFocus();
				
				if (pos==activity.webWindows.size()){
					activity.drawerLayout.closeDrawer(activity.browserListView);
					activity.openNewTab();
					if (activity.barHolder.findViewById(R.id.browser_searchbar) !=null)
						((EditText) activity.barHolder.findViewById(R.id.browser_searchbar)).setText("");
					
				}
				else{
					activity.drawerLayout.closeDrawer(activity.browserListView);
					((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).removeAllViews();
					((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).addView(activity.webWindows.get(pos));
					if (activity.webLayout.findViewById(R.id.webpgbar)!=null){
						if (activity.webWindows.get(pos).getProgress()<100){
							refreshButton.setImageResource(R.drawable.btn_toolbar_stop_loading_normal);
							activity.webLayout.findViewById(R.id.webpgbar).setVisibility(View.VISIBLE);
						}
						else{
							refreshButton.setImageResource(R.drawable.btn_toolbar_reload_normal);
							activity.webLayout.findViewById(R.id.webpgbar).setVisibility(View.INVISIBLE);
						}
					}
					

		    		String bookmarkName = null;
		    		
		    		if (activity.webWindows.get(pos)!=null)
						if (activity.webWindows.get(pos).getUrl()!=null)
							bookmarkName = BookmarksActivity.bookmarksMgr.root.containsBookmarkDeep(activity.webWindows.get(pos).getUrl());
					
		    		if (BookmarkButton!=null){
		    			if (bookmarkName!=null)
		    				BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
		    			else
		    				BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
		    		}
					
					
					if (activity.webWindows.get(pos).getUrl()!=null){
						if (activity.webWindows.get(pos).getUrl().startsWith("file:///android_asset/")){
							((EditText) activity.barHolder.findViewById(R.id.browser_searchbar)).setText("");
							((EditText) activity.barHolder.findViewById(R.id.browser_searchbar)).setHint(R.string.urlbardefault);
						}
						else
							((EditText) activity.barHolder.findViewById(R.id.browser_searchbar)).setText(activity.webWindows.get(pos).getUrl().replace("http://", "").replace("https://", ""));
					}
					else
						((EditText) activity.barHolder.findViewById(R.id.browser_searchbar)).setText("");
				}
				activity.browserListViewAdapter.notifyDataSetChanged();
			}
		   });

		activity.drawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int state) {
				// TODO Auto-generated method stub

				final Handler handler = new Handler();
				handler.post(new Runnable() {
					@Override
					public void run() {
						activity.closeVideoViewIfOpen();
					}
				});
				
				if (state == DrawerLayout.STATE_IDLE && activity.drawerLayout.isDrawerOpen(activity.browserListView)){
					activity.actionBarControls.clearFocuses();
				}
			    
			}

			@Override
			public void onDrawerSlide(@NonNull View v, float arg1) {
			}

			@Override
			public void onDrawerOpened(@NonNull View arg0) {

			}

			@Override
			public void onDrawerClosed(@NonNull View arg0) {

			}
		});

	}
	
	
	
	static public int addTransparencyToColor(int alpha, int color) {
		int[] colorARGB = new int[4];

		// Cap the Alpha value at 255. (Happens at around 75% action barHolder
		// opacity)
		if (alpha > 255) {
			colorARGB[0] = 255;
		} else {
			colorARGB[0] = alpha;
		}
		colorARGB[1] = Color.red(color);
		colorARGB[2] = Color.green(color);
		colorARGB[3] = Color.blue(color);

		return Color.argb(colorARGB[0], colorARGB[1], colorARGB[2],
				colorARGB[3]);

	}

	@SuppressLint({"InflateParams", "PrivateResource"})
	static void setUpActionBar(int actionBarType, MainActivity activity) {

		//if activity is restarted actionBarNum could still be set but bar layout is empty
		boolean forceLayout = activity.barHolder.getChildCount()==0;

		if (forceLayout || actionBarType != actionBarNum) {
			switch (actionBarType) {
				case ACTIONBAR_BROWSER:
					activity.barHolder = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.browser_bar, null);
					activity.barHolder.removeAllViews();
					activity.barHolder.addView(activity.browserBar,
							new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

					activity.actionBar.setDisplayHomeAsUpEnabled(false);
					activity.actionBar.setCustomView(activity.barHolder);
					break;
				case ACTIONBAR_FIND:
					SetupLayouts.setUpFindBar(activity);
					activity.actionBar.setDisplayHomeAsUpEnabled(false);
					activity.actionBar.setHomeAsUpIndicator(R.drawable.abc_ic_clear_material);
					activity.setUpFindBarListeners();
					TextView findText = activity.actionBar.getCustomView().findViewById(R.id.find_searchbar);
					findText.requestFocus();
					MainActivity.imm.showSoftInput(findText, InputMethodManager.SHOW_IMPLICIT);
					break;

			}

			actionBarNum = actionBarType;
		}
	}

	static void setBarColors(MainActivity activity){
		activity.actionBarControls.setColor(Properties.appProp.actionBarColor);
		activity.barHolder.setBackgroundColor(Color.TRANSPARENT);
		activity.barHolder.requestLayout();

		if (Properties.webpageProp.showBackdrop){
			((ImageView)activity.browserBar.findViewById(R.id.backdrop)).setColorFilter(Color.argb(255, Color.red(Properties.appProp.urlBarColor), Color.green(Properties.appProp.urlBarColor), Color.blue(Properties.appProp.urlBarColor)),Mode.SRC_ATOP);
			activity.browserBar.findViewById(R.id.backdrop).setAlpha(Color.alpha(Properties.appProp.urlBarColor)/255f);
		}else{
			activity.browserBar.findViewById(R.id.backdrop).setAlpha(0f);
		}


	}
	
	static public void setUpFindBar(MainActivity activity) {
		activity.barHolder.removeAllViews();

		@SuppressLint("InflateParams") View finderBar = inflater.inflate(
				R.layout.browser_bar_find_mode, null);

		ImageView finderBackdrop = finderBar.findViewById(R.id.backdrop);

		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, Properties.numtodp(3,activity));
		relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		// URL barHolder backdrop color
		// ----------------------------------------------
		if (Properties.webpageProp.showBackdrop) {
			// ShowBackdrop is active -> Set chosen backdrop color (with
			// opacity)

			// Apply color filter on backdrop with a little more opacity to make
			// it always visible
			finderBackdrop.setColorFilter(Properties.appProp.urlBarColor, Mode.SRC);
			finderBar.findViewById(R.id.find_searchbar).getBackground().setAlpha(0);
		} else {
			// ShowBackdrop is inactive -> make backdrop invisible but show underlining
			finderBackdrop.setColorFilter(Color.TRANSPARENT, Mode.CLEAR);
			finderBar.findViewById(R.id.find_searchbar).getBackground()
			.setColorFilter(Properties.appProp.primaryIntColor, Mode.SRC_ATOP);
			finderBar.findViewById(R.id.find_searchbar).getBackground().setAlpha(255);


		}

		// Paint the buttons and text with the user selected color
		((ImageButton) finderBar.findViewById(R.id.find_back))
				.setColorFilter(Properties.appProp.primaryIntColor,
						Mode.MULTIPLY);
		((ImageButton) finderBar.findViewById(R.id.find_forward))
				.setColorFilter(Properties.appProp.primaryIntColor,
						Mode.MULTIPLY);
		((ImageButton) finderBar.findViewById(R.id.find_exit))
				.setColorFilter(Properties.appProp.primaryIntColor,
						Mode.MULTIPLY);
		((EditText) finderBar.findViewById(R.id.find_searchbar))
				.setTextColor(Properties.appProp.primaryIntColor);

		activity.barHolder.addView(finderBar, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	static public void dismissFindBar(MainActivity activity){
		activity.actionBarControls.lock(false);
		if (activity.barHolder.findViewById(R.id.find_searchbar)!=null)
			MainActivity.imm.hideSoftInputFromWindow(activity.barHolder.findViewById(R.id.find_searchbar).getWindowToken(),0);
		
		setUpActionBar(ACTIONBAR_BROWSER,activity);

		CustomWebView WV = activity.webLayout.findViewById(R.id.browser_page);
		WV.clearMatches();

		if (WV.getUrl()!=null && WV.getUrl().startsWith("file:///android_asset/")){
			((EditText) activity.barHolder.findViewById(R.id.browser_searchbar)).setText("");
			((EditText) activity.barHolder.findViewById(R.id.browser_searchbar)).setHint(R.string.urlbardefault);
			//((TextView) barHolder.findViewById(R.id.browser_searchbar)).setText(activity.getResources().getString(R.string.urlbardefault));
		} else if (WV.getUrl()!=null){
			((EditText) activity.barHolder.findViewById(R.id.browser_searchbar)).setText(WV.getUrl().replace("http://", "").replace("https://", ""));
		} else {
			((EditText) activity.barHolder.findViewById(R.id.browser_searchbar)).setText("");
		}
	}
	

	public static void colorizeSidebar(MainActivity activity){
		@SuppressLint("InflateParams") LinearLayout LL = (LinearLayout) inflater.inflate(
				R.layout.web_sidebar_footer, null);

		if (Properties.sidebarProp.theme.compareTo("w") == 0) {

			((TextView) LL.findViewById(R.id.browser_open_bookmarks))
					.setTextColor(Color.BLACK);
			((TextView) LL.findViewById(R.id.browser_home))
					.setTextColor(Color.BLACK);
			((TextView) LL.findViewById(R.id.browser_share))
					.setTextColor(Color.BLACK);
			((TextView) LL.findViewById(R.id.browser_set_home))
					.setTextColor(Color.BLACK);
			((TextView) LL.findViewById(R.id.browser_find_on_page))
					.setTextColor(Color.BLACK);
			((TextView) LL.findViewById(R.id.browser_settings))
					.setTextColor(Color.BLACK);
			((TextView) LL.findViewById(R.id.browser_toggle_desktop))
					.setTextColor(Color.BLACK);
			((TextView) LL.findViewById(R.id.browser_exit))
					.setTextColor(Color.BLACK);
		} else if (Properties.sidebarProp.theme.compareTo("c") == 0) {
			int sidebarTextColor = Properties.sidebarProp.sideBarTextColor;

			((TextView) LL.findViewById(R.id.browser_open_bookmarks))
					.setTextColor(sidebarTextColor);
			((TextView) LL.findViewById(R.id.browser_home))
					.setTextColor(sidebarTextColor);
			((TextView) LL.findViewById(R.id.browser_share))
					.setTextColor(sidebarTextColor);
			((TextView) LL.findViewById(R.id.browser_set_home))
					.setTextColor(sidebarTextColor);
			((TextView) LL.findViewById(R.id.browser_find_on_page))
					.setTextColor(sidebarTextColor);
			((TextView) LL.findViewById(R.id.browser_settings))
					.setTextColor(sidebarTextColor);
			((TextView) LL.findViewById(R.id.browser_toggle_desktop))
					.setTextColor(sidebarTextColor);
			((TextView) LL.findViewById(R.id.browser_exit))
					.setTextColor(sidebarTextColor);

		}

		for (int i = 0; i<activity.browserListView.getChildCount(); i++){
			if (activity.browserListView.getChildAt(i) instanceof  LinearLayout)
				activity.browserListView.removeFooterView(activity.browserListView.getChildAt(i));
		}
		activity.browserListView.addFooterView(LL);

		if (Properties.sidebarProp.theme.compareTo("b") == 0) {
			activity.browserListView.setBackgroundColor(Color.argb(
					254, 17, 17, 17));
		} else if (Properties.sidebarProp.theme.compareTo("w") == 0) {
			activity.browserListView.setBackgroundColor(Color.argb(
					254, 255, 255, 255));
		} else {
			activity.browserListView.setBackgroundColor(Properties.sidebarProp.sideBarColor);
		}


	}
	
	
	
	@SuppressLint({ "InlinedApi", "NewApi" })
	public static void setupWindow(MainActivity activity){
		//enable fullscreen
		if (Properties.appProp.fullscreen)
			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
					
		int id =activity.getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
		
		
		if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus)
			if (id == 0) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
					activity.drawerLayout.setSystemUiVisibility(4096);
			}
		
		if (activity.tintManager!=null){
			activity.tintManager.setStatusBarTintEnabled(false);
			activity.tintManager.setNavigationBarTintEnabled(false);
			activity.tintManager = null;
		}

		activity.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		activity.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		
		if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus){
			if (id != 0) {
				if (Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
			        if (Properties.appProp.transparentNav)
						activity.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			        
			        if (Properties.appProp.TransparentStatus)
						activity.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				}
		        
		        if (Properties.appProp.TransparentStatus){
					activity.tintManager = new SystemBarTintManager(activity);
					activity.tintManager.setStatusBarTintEnabled(true);
					activity.tintManager.setStatusBarTintColor(Properties.appProp.actionBarColor);
		        	if (Properties.appProp.fullscreen)
						activity.tintManager.setStatusBarAlpha(0.0f);
		        }
		        
		        if (Properties.appProp.transparentNav){
					if (activity.tintManager==null)
						activity.tintManager = new SystemBarTintManager(activity);
					activity.tintManager.setNavigationBarTintEnabled(true);
					activity.tintManager.setNavigationBarAlpha(0f);
				}else{
					if (activity.tintManager!= null){
						activity.tintManager.setNavigationBarAlpha(0f);
						activity.tintManager.setNavigationBarTintEnabled(false);
					}
				}
		    }
		}

		activity.drawerLayout.setScrimColor(Color.TRANSPARENT);
		activity.drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		activity.contentFrame.setFitsSystemWindows(true);
		activity.browserListView.setY(Tools.getStatusMargine(activity));
		activity.webLayout.setY(Properties.ActionbarSize);
		

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			if (Properties.appProp.transparentNav)
				activity.getWindow().setNavigationBarColor(Properties.appProp.actionBarColor);
			if (Properties.appProp.TransparentStatus)
				activity.getWindow().setStatusBarColor(Properties.appProp.actionBarColor);
			else
				activity.getWindow().setStatusBarColor(Color.BLACK);
	    }
		

	}




    /**
     * Restore tabs that were saved when browser was closed
     */
	static void setupWebWindows(MainActivity activity){
		if (activity.webLayout!=null){

			if (activity.webWindows!=null)
				activity.webWindows.clear();

			((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).removeAllViews();

			class LoadRunner implements Runnable{
				MainActivity activity;

				private LoadRunner(MainActivity activity){
					this.activity = activity;
				}

				@Override
				public void run() {
					Bundle mainBundle = new BundleManager(activity).restoreFromPreferences();

					class UIRunner implements Runnable{
						private Bundle mainBundle;
						private UIRunner(Bundle mainBundle){
							this.mainBundle = mainBundle;
						}

						@Override
						public void run() {
							int numSavedTabs = -1;
							if (mainBundle!=null)
								numSavedTabs = mainBundle.getInt("numtabs",-1);

							Intent intent = activity.getIntent();

							//detect if app was opened from a different app to open a site
							boolean urlOpenRequested =  (intent.getAction()!=null
									&& (intent.getAction().equals(Intent.ACTION_WEB_SEARCH) || intent.getAction().equals(Intent.ACTION_VIEW))
									&& intent.getDataString()!=null);

							if (numSavedTabs>0){
								Log.d("LB","RESTORING STATE");

								int tabNumber = mainBundle.getInt("tabnumber",0);
								for (int I=0;I<numSavedTabs;I++){
									CustomWebView wv = new CustomWebView(activity, "na");
									wv.restoreState(mainBundle.getBundle("WV"+I));
									if (wv.getUrl()!=null && wv.getUrl().startsWith("file:///android_asset/")
											&& wv.getUrl().contains("home.html") && !wv.getUrl().equals(Properties.webpageProp.assetHomePage)) {
										wv = new CustomWebView(activity, null);
									}
									activity.webWindows.add(wv);
									activity.browserListViewAdapter.notifyDataSetChanged();
								}
								if (!urlOpenRequested)
									((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).addView(activity.webWindows.get(tabNumber));
							}else if (numSavedTabs!=0){//If no InstanceState is found, just add a single page
								if (activity.getIntent().getAction()!=null && !activity.getIntent().getAction().equals(Intent.ACTION_WEB_SEARCH)
										&& !activity.getIntent().getAction().equals(Intent.ACTION_VIEW)){//if page was requested from a different app, do not load home page
									activity.webWindows.add(new CustomWebView(activity, null));
									if (!urlOpenRequested)
										((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).addView(activity.webWindows.get(0));
									activity.browserListViewAdapter.notifyDataSetChanged();
								}
							}else{
								if (!urlOpenRequested)
									activity.displayNoTabsView();
							}

							if (urlOpenRequested){
								activity.webWindows.add(new CustomWebView(activity, intent.getDataString()));
								((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).removeAllViews();
								((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder))
										.addView(activity.webWindows.get(activity.webWindows.size()-1));
								((EditText) activity.barHolder.findViewById(R.id.browser_searchbar)).setText(intent.getDataString());
								activity.browserListViewAdapter.notifyDataSetChanged();
								Log.d("LB", "onCreate ACTION_VIEW");
							}
						}
					}
					activity.runOnUiThread(new UIRunner(mainBundle));
				}
			}

			new Thread(new LoadRunner(activity)).start();
		}
	}
	
	
	
	
	
}