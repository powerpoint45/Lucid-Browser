package com.powerpoint45.lucidbrowser;

import java.util.Vector;

import android.app.ActionBar;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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

public class SetupLayouts extends MainActivity {
	static int actionBarNum;
	public static PopupWindow popup;

	public static void setuplayouts() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				Properties.ActionbarSize, Properties.ActionbarSize);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		bar.setClickable(true);
		bar.setFocusable(true);
		bar.setFocusableInTouchMode(true);
		bar.setBackgroundColor(Properties.appProp.actionBarColor);
		setUpActionBar();
		actionBar.setCustomView(bar);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
		browserListView.addFooterView(LL);
		MainActivity.browserListView
				.setAdapter(MainActivity.browserListViewAdapter);

		if (Properties.sidebarProp.theme.compareTo("b") == 0) {
			browserListView.setBackgroundColor(Color.argb(
					254, 17, 17, 17));
		} else if (Properties.sidebarProp.theme.compareTo("w") == 0) {
			browserListView.setBackgroundColor(Color.argb(
					254, 255, 255, 255));
			activity.setContentView(R.layout.browser_item);

			
		} else {
           browserListView.setBackgroundColor(Properties.sidebarProp.sideBarColor);
		}

		browserListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,long arg3) {
				
				ImageButton BookmarkButton = (ImageButton) MainActivity.bar.findViewById(R.id.browser_bookmark);
				ImageButton refreshButton = (ImageButton) MainActivity.bar.findViewById(R.id.browser_refresh);
				
				if (((EditText) bar.findViewById(R.id.browser_searchbar))!=null)
					((EditText) bar.findViewById(R.id.browser_searchbar)).clearFocus();
				
				if (pos==webWindows.size()){
					mainView.closeDrawer(browserListView);
					webWindows.add(new CustomWebView(MainActivity.activity,null,null));
					if (webLayout!=null)
						if (((ViewGroup) webLayout.findViewById(R.id.webviewholder))!=null){
							((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
							((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(pos));
						}
					if (((EditText) bar.findViewById(R.id.browser_searchbar))!=null)
						((EditText) bar.findViewById(R.id.browser_searchbar)).setText("");
					
				}
				else{
					mainView.closeDrawer(browserListView);
					((ViewGroup) webLayout.findViewById(R.id.webviewholder)).removeAllViews();
					((ViewGroup) webLayout.findViewById(R.id.webviewholder)).addView(webWindows.get(pos));
					if (MainActivity.webLayout.findViewById(R.id.webpgbar)!=null){
						if (webWindows.get(pos).getProgress()<100){
							refreshButton.setImageResource(R.drawable.btn_toolbar_stop_loading_normal);
							MainActivity.webLayout.findViewById(R.id.webpgbar).setVisibility(View.VISIBLE);
						}
						else{
							refreshButton.setImageResource(R.drawable.btn_toolbar_reload_normal);
							MainActivity.webLayout.findViewById(R.id.webpgbar).setVisibility(View.INVISIBLE);
						}
					}
					
					int numBooks=MainActivity.mPrefs.getInt("numbookmarkedpages", 0);
					boolean isBook = false;
					for (int i=0;i<numBooks;i++){
						if (webWindows.get(pos)!=null)
							if (webWindows.get(pos).getUrl()!=null)
			    				if (MainActivity.mPrefs.getString("bookmark"+i, "").compareTo(webWindows.get(pos).getUrl())==0){
			    					BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_selected_normal);
			    					isBook=true;
			    					break;
			    				}
					}
					if (!isBook){
						BookmarkButton.setImageResource(R.drawable.btn_omnibox_bookmark_normal);
					}
					
					if (webWindows.get(pos).getUrl()!=null)
						((EditText) bar.findViewById(R.id.browser_searchbar)).setText(webWindows.get(pos).getUrl().replace("http://", "").replace("https://", ""));
					else
						((EditText) bar.findViewById(R.id.browser_searchbar)).setText("");
				}
				MainActivity.browserListViewAdapter.notifyDataSetChanged();
			}
		   });
		
		
		//Padding and adjustments start-----
		//enabling transparent statusbar or navbar messes with padding so this will fix it
		if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus){
			browserListView.setPadding(0, 0, 0, NavMargine+StatusMargine);
			browserListView.setY(StatusMargine);			
		    MainActivity.webLayout.setPadding(0, MainActivity.StatusMargine, 0, 0);
		}
		//Padding and adjustments end-----
		
		mainView.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int state) {
				// TODO Auto-generated method stub

				final Handler handler = new Handler();
				handler.post(new Runnable() {
					@Override
					public void run() {
						closeVideoViewIfOpen();
					}
				});
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

	static public void setUpActionBar() {
		bar.removeAllViews();

		View browserBar = (RelativeLayout) inflater.inflate(
				R.layout.browser_bar, null);

		ImageView urlBarBackdrop = (ImageView) browserBar
				.findViewById(R.id.backdrop);

		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, Properties.numtodp(3));
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

		// Paint the buttons and text with the user selected color
		((ImageButton) browserBar.findViewById(R.id.browser_back))
				.setColorFilter(Properties.appProp.primaryIntColor,
						Mode.MULTIPLY);
		((ImageButton) browserBar.findViewById(R.id.browser_forward))
				.setColorFilter(Properties.appProp.primaryIntColor,
						Mode.MULTIPLY);
		((ImageButton) browserBar.findViewById(R.id.browser_refresh))
				.setColorFilter(Properties.appProp.primaryIntColor,
						Mode.MULTIPLY);
		((ImageButton) browserBar.findViewById(R.id.browser_bookmark))
				.setColorFilter(Properties.appProp.primaryIntColor,
						Mode.MULTIPLY);
		((EditText) browserBar.findViewById(R.id.browser_searchbar))
				.setTextColor(Properties.appProp.primaryIntColor);

		final AutoCompleteTextView ET = ((AutoCompleteTextView) browserBar
				.findViewById(R.id.browser_searchbar));
		
		responses    = new Vector<String>(0);
		suggestionsAdapter        = new BrowserBarAdapter(activity, 0, responses);
		
		ET.setAdapter(suggestionsAdapter);
		ET.setScrollContainer(true);
		
		ET.setDropDownAnchor(R.id.address_bar);
    	ET.setDropDownWidth(LayoutParams.MATCH_PARENT);

    	ET.setThreshold(0);
		ET.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				boolean noCopyOption = false;
				boolean noPasteOption = false;
				
				if (((EditText) bar.findViewById(R.id.browser_searchbar))!=null){
					((EditText) bar.findViewById(R.id.browser_searchbar)).setFocusable(false);
		    		((EditText) bar.findViewById(R.id.browser_searchbar)).selectAll();
		    		if (((EditText) bar.findViewById(R.id.browser_searchbar)).getText().toString().compareTo("")==0)
		    			noCopyOption = true;
				}
				
				
				System.out.println("LONG PRESSED");
				popup = new PopupWindow(inflater.inflate(R.layout.copy_url_popup, null), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
				
				ClipboardManager clipboard = (ClipboardManager)
			 	        MainActivity.activity.getSystemService(Context.CLIPBOARD_SERVICE);
				
				if (!clipboard.hasPrimaryClip())
					noPasteOption = true;
				
				if (noPasteOption)
					popup.getContentView().findViewById(R.id.pastebutton).setVisibility(View.GONE);
				
				if (noCopyOption)
					popup.getContentView().findViewById(R.id.copyurlbutton).setVisibility(View.GONE);
				
				popup.setFocusable(true);
				popup.setBackgroundDrawable(new ColorDrawable());
				popup.setAnimationStyle(R.style.AnimationPopup);
				int[] loc = new int[2];
				v.getLocationOnScreen(loc);
				
				if (noCopyOption && noPasteOption)
					System.out.println("DO NOTHING");
				else
					popup.showAtLocation(MainActivity.bar, Gravity.NO_GRAVITY, loc[0], loc[1]+v.getHeight());
				
				OnDismissListener dismissListener = new OnDismissListener() {
					
					@Override
					public void onDismiss() {
						// TODO Auto-generated method stub
						if (((EditText) bar.findViewById(R.id.browser_searchbar))!=null){
							((EditText) bar.findViewById(R.id.browser_searchbar)).setFocusableInTouchMode(true);
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
					new AsyncTask<Void, Void, Void>() {
						@Override
						protected void onPostExecute(Void result) {
							super.onPostExecute(result);
							imm.hideSoftInputFromWindow(ET.getWindowToken(), 0);
							browserSearch();
							ET.clearFocus();
						}

						@Override
						protected Void doInBackground(Void... params) {
							return null;
						}
					}.execute();
					return true;
				}
				return false;
			}
		});
		
		ET.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected void onPostExecute(Void result) {
						super.onPostExecute(result);
						final EditText ET = ((AutoCompleteTextView) bar.findViewById(R.id.browser_searchbar));
						imm.hideSoftInputFromWindow(ET.getWindowToken(), 0);
						browserSearch();
						ET.clearFocus();
					}

					@Override
					protected Void doInBackground(Void... params) {
						return null;
					}
				}.execute();
			}
		});

		bar.addView(browserBar);
	}
	
	static public void setUpFindBar() {
		bar.removeAllViews();

		View finderBar = (RelativeLayout) inflater.inflate(
				R.layout.browser_bar_find_mode, null);

		ImageView finderBackdrop = (ImageView) finderBar
				.findViewById(R.id.backdrop);

		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, Properties.numtodp(3));
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

		bar.addView(finderBar);
	}

	static public void dismissFindBar(){
		setUpActionBar();

		CustomWebView WV = (CustomWebView) webLayout.findViewById(R.id.browser_page);
		WV.clearMatches();

		if (WV.getUrl()!=null && WV.getUrl().startsWith("file:///android_asset/")){
			((TextView) bar.findViewById(R.id.browser_searchbar)).setText(activity.getResources().getString(R.string.urlbardefault));					
		} else if (WV.getUrl()!=null){
			((EditText) bar.findViewById(R.id.browser_searchbar)).setText(WV.getUrl().replace("http://", "").replace("https://", ""));					
		} else {
			((EditText) bar.findViewById(R.id.browser_searchbar)).setText("");
		}
	}
}
