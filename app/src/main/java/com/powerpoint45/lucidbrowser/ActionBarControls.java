package com.powerpoint45.lucidbrowser;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.ActionBar;

import com.powerpoint45.lucidbrowser.R;

public class ActionBarControls {
	ActionBar actionBar;
	public boolean hidden = false;
	boolean locked;
	MainActivity activity;

	public ActionBarControls(ActionBar ab, MainActivity activity){
		actionBar = ab;
		this.activity = activity;
		activity.mainLayout.postDelayed(new Runnable() {
			@Override
			public void run() {
				show();
			}
		}, 10);
	}

	public void setColor(int c){
		ColorDrawable colorDrawable = new ColorDrawable(c);
		actionBar.setBackgroundDrawable(colorDrawable);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			View root = activity.findViewById(android.R.id.content);
			root.setBackgroundColor(c);
		}
	}

	public void lock(boolean lock){
		locked = lock;
	}

	public void hide(){
		if (activity.findViewById(R.id.swipe_refresh).isEnabled() != false)
			activity.findViewById(R.id.swipe_refresh).setEnabled(false);
		Log.d("LL", "HIDE");
		hidden = true;
		activity.browserListView.setY(0);
		activity.browserListView.setPadding(0, 0, 0, Tools.getStatusSize(activity));

		ValueAnimator animator = ValueAnimator.ofInt(
				(int)(activity.toolbar.getY() - Tools.getStatusSize(activity)),
				-activity.toolbar.getHeight()
		);
		animator.setDuration(200);

		final int margine = Tools.getStatusMargine(activity);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = ((Integer)(animation.getAnimatedValue()));

				activity.webLayout.setY(value + margine);
				activity.toolbar.setY(value + Tools.getStatusSize(activity));

				if (activity.tabsScrollView != null)
					activity.tabsScrollView.setY(-value);
			}
		});
		animator.start();

		clearFocuses();
	}

	public void clearFocuses(){
		if (activity.barHolder.findViewById(R.id.finder) != null)
			SetupLayouts.dismissFindBar(activity);

		if (activity.barHolder.findViewById(R.id.browser_searchbar) != null){
			MainActivity.imm.hideSoftInputFromWindow(
					activity.barHolder.findViewById(R.id.browser_searchbar).getWindowToken(), 0
			);
			activity.barHolder.findViewById(R.id.browser_searchbar).clearFocus();
		}
	}

	public void move(float f){
		if (!locked){
			Float curToolbarY = activity.toolbar.getY() - Tools.getStatusSize(activity);
			Float newToolbarY = curToolbarY + f;

			int margine = Tools.getStatusMargine(activity);

			if (newToolbarY < -activity.toolbar.getHeight())
				newToolbarY = (float)(-activity.toolbar.getHeight());

			if (newToolbarY > 0)
				newToolbarY = 0f;

			if (activity.webLayout.getY() != newToolbarY + margine){
				activity.webLayout.setY((int)(newToolbarY + margine));
				activity.toolbar.setY(newToolbarY + Tools.getStatusSize(activity));

				if (activity.tabsScrollView != null)
					activity.tabsScrollView.setY(-newToolbarY);
			}
		}
	}

	public void show() {
		Log.d("LL", "SHOW ACTIONBAR");
		hidden = false;

		int statusHeight = Tools.getStatusSize(activity);
		int margine = Tools.getStatusMargine(activity); // consistent with hide/move

		activity.browserListView.setY(margine);
		activity.browserListView.setPadding(0, 0, 0, margine);

		// Animate from current webLayout Y to final margine
		ValueAnimator animator = ValueAnimator.ofInt(
				(int) activity.webLayout.getY(),
				margine
		);
		animator.setDuration(200);

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = (Integer) animation.getAnimatedValue();

				// Set positions during animation
				activity.webLayout.setY(value);
				activity.toolbar.setY(statusHeight); // always fixed just below status bar
			}
		});
		animator.start();

		if (activity.tabsScrollView != null)
			activity.tabsScrollView.setY(0);
	}




	public void showOrHide(){
		if (!locked){
			Float curY = activity.webLayout.getY();

			if (curY < (activity.toolbar.getHeight() + Tools.getStatusSize(activity)) / 2){
				hide();
			} else {
				show();
			}
		}
	}

	public void actionCanceled(){
		if (hidden)
			hide();
		else
			show();
	}
}
