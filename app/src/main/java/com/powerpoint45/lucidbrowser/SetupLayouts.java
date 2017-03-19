package com.powerpoint45.lucidbrowser;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Vector;

import views.CustomWebView;

public class SetupLayouts extends MainActivity {
	static int actionBarNum;
	public static PopupWindow popup;

	@SuppressLint("NewApi")
	public static void setuplayouts(final MainActivity activity) {
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		activity.bar.setClickable(true);
		activity.bar.setFocusable(true);
		activity.bar.setFocusableInTouchMode(true);
		setupWindow(activity);
		activity.webLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				//MainActivity.actionBarControls.show();
				activity.webLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		
		//bar.setBackgroundColor(Color.TRANSPARENT);
		//contentFrame.setFitsSystemWindows(true);
		setUpActionBar(activity);
		actionBar.setCustomView(activity.bar
				,new android.support.v7.app.ActionBar.LayoutParams(android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT
				,android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT));
		//actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));



		colorizeSidebar(activity);
		activity.browserListView.setAdapter(MainActivity.browserListViewAdapter);



		activity.browserListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,long arg3) {
				
				if (activity.bar.findViewById(R.id.finder)!=null)
					SetupLayouts.dismissFindBar(activity);
				
				MainActivity.imm.hideSoftInputFromInputMethod(((EditText) activity.bar.findViewById(R.id.browser_searchbar)).getWindowToken(), 0);
				
				ImageButton BookmarkButton = (ImageButton) activity.bar.findViewById(R.id.browser_bookmark);
				ImageButton refreshButton = (ImageButton) activity.bar.findViewById(R.id.browser_refresh);
				
				if (((EditText) activity.bar.findViewById(R.id.browser_searchbar))!=null)
					((EditText) activity.bar.findViewById(R.id.browser_searchbar)).clearFocus();
				
				if (pos==webWindows.size()){
					activity.contentView.closeDrawer(activity.browserListView);
					webWindows.add(new CustomWebView(activity,null,null));
					if (activity.webLayout!=null)
						if (((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder))!=null){
							((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).removeAllViews();
							((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(pos));
						}
					if (((EditText) activity.bar.findViewById(R.id.browser_searchbar))!=null)
						((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setText("");
					
				}
				else{
					activity.contentView.closeDrawer(activity.browserListView);
					((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).removeAllViews();
					((ViewGroup) activity.webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(pos));
					if (activity.webLayout.findViewById(R.id.webpgbar)!=null){
						if (webWindows.get(pos).getProgress()<100){
							refreshButton.setImageResource(R.drawable.btn_toolbar_stop_loading_normal);
							activity.webLayout.findViewById(R.id.webpgbar).setVisibility(View.VISIBLE);
						}
						else{
							refreshButton.setImageResource(R.drawable.btn_toolbar_reload_normal);
							activity.webLayout.findViewById(R.id.webpgbar).setVisibility(View.INVISIBLE);
						}
					}
					

		    		String bookmarkName = null;
		    		
		    		if (webWindows.get(pos)!=null)
						if (webWindows.get(pos).getUrl()!=null)
							bookmarkName = BookmarksActivity.bookmarksMgr.root.containsBookmarkDeep(webWindows.get(pos).getUrl());
					
		    		if (BookmarkButton!=null){
		    			if (bookmarkName!=null)
		    				BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
		    			else
		    				BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
		    		}
					
					
					if (webWindows.get(pos).getUrl()!=null){
						if (webWindows.get(pos).getUrl().startsWith("file:///android_asset/")){
							((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setText("");
							((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setHint(R.string.urlbardefault);
						}
						else
							((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setText(webWindows.get(pos).getUrl().replace("http://", "").replace("https://", ""));
					}
					else
						((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setText("");
				}
				MainActivity.browserListViewAdapter.notifyDataSetChanged();
			}
		   });
		
		
		//Padding and adjustments start-----
		//enabling transparent statusbar or navbar messes with padding so this will fix it
		if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus){
			activity.webLayout.setPadding(0, MainActivity.NavMargine, 0, 0);
		}
		//Padding and adjustments end-----

		activity.contentView.setDrawerListener(new DrawerListener() {

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
				
				if (state == DrawerLayout.STATE_IDLE && activity.contentView.isDrawerOpen(activity.browserListView)){
					MainActivity.actionBarControls.clearFocuses();
				}
			    
			}

			@Override
			public void onDrawerSlide(View v, float arg1) {
			}

			@Override
			public void onDrawerOpened(View arg0) {

			}

			@Override
			public void onDrawerClosed(View arg0) {

			}
		});

	}
	
	
	
	static public int addTransparencyToColor(int alpha, int color) {
		int[] colorARGB = new int[4];

		// Cap the Alpha value at 255. (Happens at around 75% action bar
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

	static public void setUpActionBar(final MainActivity activity) {
		activity.bar.removeAllViews();

		View browserBar = (RelativeLayout) inflater.inflate(
				R.layout.browser_bar, null);

		ImageView urlBarBackdrop = (ImageView) browserBar
				.findViewById(R.id.backdrop);

		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, Properties.numtodp(3, activity));
		relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		// URL bar backdrop color
		// ----------------------------------------------
		if (Properties.webpageProp.showBackdrop) {
			// ShowBackdrop is active -> Set chosen backdrop color (with
			// opacity)
			
			// Apply color filter on backdrop with a little more opacity to make
			// it always visible
			urlBarBackdrop.setColorFilter(Properties.appProp.urlBarColor, Mode.SRC);
		} else {
			// ShowBackdrop is inactive -> make backdrop invisible
			urlBarBackdrop.setColorFilter(Color.TRANSPARENT, Mode.CLEAR);
		}


		((EditText) browserBar.findViewById(R.id.browser_searchbar))
		.setHintTextColor(Properties.appProp.primaryIntColor);

		final AutoCompleteTextView ET = ((AutoCompleteTextView) browserBar
				.findViewById(R.id.browser_searchbar));
		
		// Suggestions
		if (!Properties.webpageProp.disablesuggestions){
			suggestionsAdapter        = new BrowserBarAdapter(activity, 0);

			ET.setAdapter(suggestionsAdapter);
			ET.setScrollContainer(true);

			ET.setDropDownAnchor(R.id.address_bar);
			ET.setDropDownWidth(LayoutParams.MATCH_PARENT);

			ET.setThreshold(0);
		}
		
		ET.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				boolean noCopyOption = false;
				boolean noPasteOption = false;

				if (((EditText) activity.bar.findViewById(R.id.browser_searchbar))!=null){
					((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setFocusable(false);
		    		((EditText) activity.bar.findViewById(R.id.browser_searchbar)).selectAll();
		    		if (((EditText) activity.bar.findViewById(R.id.browser_searchbar)).getText().toString().compareTo("")==0)
		    			noCopyOption = true;
				}


				System.out.println("LONG PRESSED");
				popup = new PopupWindow(inflater.inflate(R.layout.copy_url_popup, null), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);

				ClipboardManager clipboard = (ClipboardManager)
						activity.activity.getSystemService(Context.CLIPBOARD_SERVICE);

				if (!clipboard.hasPrimaryClip())
					noPasteOption = true;

				if (noPasteOption)
					popup.getContentView().findViewById(R.id.pastebutton).setVisibility(View.GONE);

				if (noCopyOption)
					popup.getContentView().findViewById(R.id.copyurlbutton).setVisibility(View.GONE);

				popup.setFocusable(true);
				popup.setBackgroundDrawable(new ColorDrawable());
				int[] loc = new int[2];
				v.getLocationOnScreen(loc);

				if (noCopyOption && noPasteOption)
					((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setFocusableInTouchMode(true);
				else
					popup.showAtLocation(activity.bar, Gravity.NO_GRAVITY, loc[0], loc[1]+v.getHeight());

				OnDismissListener dismissListener = new OnDismissListener() {

					@Override
					public void onDismiss() {
						// TODO Auto-generated method stub
						if (((EditText) activity.bar.findViewById(R.id.browser_searchbar))!=null){
							((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setFocusableInTouchMode(true);
						}
					}
				};

				popup.setOnDismissListener(dismissListener);

				return false;
			}
		});
		
		ET.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					System.out.println("key press");

					activity.webLayout.postDelayed(new Runnable() {
						@Override
						public void run() {
							imm.hideSoftInputFromWindow(ET.getWindowToken(), 0);
							activity.browserSearch();
							ET.clearFocus();
						}
					}, 300);

					return true;
				}
				return false;
			}
		});
		
		ET.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				activity.webLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						final EditText ET = ((AutoCompleteTextView) activity.bar.findViewById(R.id.browser_searchbar));
						imm.hideSoftInputFromWindow(ET.getWindowToken(), 0);
						activity.browserSearch();
						ET.clearFocus();
					}
				}, 300);
			}
		});
		
//		ET.setOnFocusChangeListener(new OnFocusChangeListener() {
//			
//			@Override
//			public void onFocusChange(View urlBar, boolean hasFocus) {
//				// TODO Auto-generated method stub
//				if (((EditText) urlBar).getText().toString().equals(MainActivity.activity.getResources().getString(R.string.urlbardefault)))
//					((EditText) urlBar).setText("");
//			}
//		});

		activity.bar.addView(browserBar);
		Tools.setActionBarColor(Properties.appProp.actionBarColor);
	}
	
	static public void setUpFindBar(MainActivity activity) {
		activity.bar.removeAllViews();

		View finderBar = (RelativeLayout) inflater.inflate(
				R.layout.browser_bar_find_mode, null);

		ImageView finderBackdrop = (ImageView) finderBar
				.findViewById(R.id.backdrop);

		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, Properties.numtodp(3,activity));
		relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		// URL bar backdrop color
		// ----------------------------------------------
		if (Properties.webpageProp.showBackdrop) {
			// ShowBackdrop is active -> Set chosen backdrop color (with
			// opacity)

			// Apply color filter on backdrop with a little more opacity to make
			// it always visible
			finderBackdrop.setColorFilter(Properties.appProp.urlBarColor, Mode.SRC);
			((EditText) finderBar.findViewById(R.id.find_searchbar)).getBackground().setAlpha(0);
		} else {
			// ShowBackdrop is inactive -> make backdrop invisible but show underlining
			finderBackdrop.setColorFilter(Color.TRANSPARENT, Mode.CLEAR);
			((EditText) finderBar.findViewById(R.id.find_searchbar)).getBackground()
			.setColorFilter(Properties.appProp.primaryIntColor, Mode.SRC_ATOP);
			((EditText) finderBar.findViewById(R.id.find_searchbar)).getBackground().setAlpha(255);


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

		activity.bar.addView(finderBar);
	}

	static public void dismissFindBar(MainActivity activity){
		actionBarControls.lock(false);
		if (activity.bar.findViewById(R.id.find_searchbar)!=null)
			MainActivity.imm.hideSoftInputFromWindow(activity.bar.findViewById(R.id.find_searchbar).getWindowToken(),0);
		
		setUpActionBar(activity);

		CustomWebView WV = (CustomWebView) activity.webLayout.findViewById(R.id.browser_page);
		WV.clearMatches();

		if (WV.getUrl()!=null && WV.getUrl().startsWith("file:///android_asset/")){
			((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setText("");
			((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setHint(R.string.urlbardefault);
			//((TextView) bar.findViewById(R.id.browser_searchbar)).setText(activity.getResources().getString(R.string.urlbardefault));					
		} else if (WV.getUrl()!=null){
			((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setText(WV.getUrl().replace("http://", "").replace("https://", ""));
		} else {
			((EditText) activity.bar.findViewById(R.id.browser_searchbar)).setText("");
		}
	}
	

	public static void colorizeSidebar(MainActivity activity){
		LinearLayout LL = (LinearLayout) inflater.inflate(
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
					activity.contentView.setSystemUiVisibility(4096);
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

		activity.contentView.setScrimColor(Color.TRANSPARENT);
		activity.contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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
	
	
	
	
	
}