package com.powerpoint45.lucidbrowser;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;

class Showcaser {
	MainActivity activity;
	private ShowcaseView showcase;
	private OnClickListener showcaseClick;
	private OnShowcaseEventListener showcaseEventListener;
	private int step;

    final static int STEP_BROWSER_SIDEBAR = 8;
    final static int STEP_BROWSER_CLOSE_SIDEBAR = 9;

	Showcaser(MainActivity act, int step){
		activity = act;

		showcaseClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.contentView.closeDrawer(activity.browserListView);
				uncolorBars();
				showcase.hide();
                MainActivity.mGlobalPrefs.edit().putBoolean("showcased",true).commit();
                Log.d("LB","DONESHOWCASE");
			}
		};

		showcaseEventListener = new OnShowcaseEventListener() {
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				//uncolorBars();

				next();
				Log.d("LL","hided");
			}

			@Override
			public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
			}

			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
				showcaseView.postDelayed(new Runnable() {
					@Override
					public void run() {
						colorBars();
					}
				},10);

			}

			@Override
			public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

			}
		};


		if (step == STEP_BROWSER_SIDEBAR)
			startShowcase();
		else{
			this.step = step-1;
			next();
		}
	}

	void showcase(int step){
		if (step == STEP_BROWSER_SIDEBAR)
			startShowcase();
		else{
			this.step = step-1;
			next();
		}
	}

	@SuppressWarnings("deprecation")
	private void next(){

//        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        // This aligns button to the bottom right side of screen and extra padding
//        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);int margin = ((Number) (activity.getResources().getDisplayMetrics().density * 16)).intValue();
//        lps.setMargins(margin, margin, margin+Properties.numtodp(30), margin+Properties.numtodp(50));



		step++;
		switch (step){
			case STEP_BROWSER_CLOSE_SIDEBAR:
				activity.contentView.closeDrawer(activity.browserListView);
				break;
			default:
				break;
		}
	}

	private void startShowcase(){
		Log.d("LL","startShowcase");
		activity.contentView.openDrawer(activity.browserListView);
		showcase = new ShowcaseView.Builder(activity)
				.setContentTitle(activity.getResources().getString(R.string.step_browser_sidebar_title))
				.setContentText(activity.getResources().getString(R.string.step_browser_sidebar_summary))
				.setShowcaseEventListener(showcaseEventListener)
				.setOnClickListener(showcaseClick)
				.setStyle(R.style.CustomShowcaseTheme2)
				.build();
		showcase.setButtonPosition(getButtonLP());
		showcase.setOnClickListener(showcaseClick);
	}

	private RelativeLayout.LayoutParams getButtonLP(){
		RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		// This aligns button to the bottom right side of screen and extra padding
		lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		int margin = Properties.numtodp(20, activity);
		lps.setMargins(margin, margin, margin, margin+ Properties.numtodp(20, activity));
		return lps;
	}

	boolean isShowcasing(){
		return showcase.isShowing();
	}

	private void colorBars(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.sv_backgroundColor));
			activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.sv_backgroundColor));
		}
	}

	private void uncolorBars(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if (activity.tintManager!=null)
				activity.tintManager.setStatusBarTintColor(Properties.appProp.actionBarColor);
			activity.getWindow().setNavigationBarColor(Color.BLACK);
            activity.getWindow().setStatusBarColor(Properties.appProp.actionBarColor);
		}
	}

}
