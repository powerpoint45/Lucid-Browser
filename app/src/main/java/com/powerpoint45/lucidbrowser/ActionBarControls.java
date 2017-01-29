package com.powerpoint45.lucidbrowser;

import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.EditText;

public class ActionBarControls {
	ActionBar actionBar;
	public boolean hidden = false;
	boolean locked;
	MainActivity activity;
	
	public ActionBarControls(ActionBar ab, MainActivity activity){
		actionBar = ab;
		this.activity = activity;
	}
	
	public void setColor(int c){
		ColorDrawable colorDrawable = new ColorDrawable(c);
  		actionBar.setBackgroundDrawable(colorDrawable);
	}
	
	public void lock(boolean lock){
		locked = lock;
	}
	
	public void hide(){
		//if (hidden!=true){
		Log.d("LL", "HIDE");
		hidden = true;
		activity.browserListView.setY(Tools.getStatusSize(activity));
		activity.browserListView.setPadding(0, 0, 0, Tools.getStatusSize(activity));
		    
		ValueAnimator animator = ValueAnimator.ofInt((int)(activity.toolbar.getY()- Tools.getStatusSize(activity))
				, -Properties.ActionbarSize);
		animator.setDuration(200);

		final int margine = Tools.getStatusMargine(activity);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = ((Integer) (animation.getAnimatedValue()));

				activity.webLayout.setY((int) (value + margine));
				activity.toolbar.setY(value+ Tools.getStatusSize(activity));
			}
		});
		animator.start();

		clearFocuses();
		    
	}
	
	public void clearFocuses(){
		if (activity.bar.findViewById(R.id.finder)!=null)
		    SetupLayouts.dismissFindBar(activity);
	    
	    if (((EditText) activity.bar.findViewById(R.id.browser_searchbar))!=null){
	    	MainActivity.imm.hideSoftInputFromWindow(activity.bar.findViewById(R.id.browser_searchbar).getWindowToken(),0);
			activity.bar.findViewById(R.id.browser_searchbar).clearFocus();
	    }
	}
	
	
	
	public void move(float f){
		if (!locked){
			Float curToolbarY = activity.toolbar.getY()- Tools.getStatusSize(activity);
			Float newToolbarY = curToolbarY + f;
			
			int margine = Tools.getStatusMargine(activity);
			
			if (newToolbarY<-Properties.ActionbarSize)
				newToolbarY = (float) -Properties.ActionbarSize;
			
			if (newToolbarY>0)
				newToolbarY = 0f;
			
			if (activity.webLayout.getY()!=newToolbarY+margine){
				activity.webLayout.setY((int) (newToolbarY + margine));
				activity.toolbar.setY(newToolbarY+ Tools.getStatusSize(activity));
			}
		}
	}
	
	public void show(){
		//if (hidden!=false){
		Log.d("LL", "SHOW ACTIONBAR");
		hidden = false;
		int margine = Tools.getStatusMargine(activity);
		activity.browserListView.setY(margine);
		activity.browserListView.setPadding(0, 0, 0, margine);
			
		ValueAnimator animator = ValueAnimator.ofInt((int)activity.webLayout.getY(), Tools.getStatusMargine(activity));
		animator.setDuration(200);

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = ((Integer) (animation.getAnimatedValue()));

				if (value>=0){
					if (value<= Tools.getStatusMargine(activity))
						activity.webLayout.setY(value);
					else
						activity.webLayout.setY(Tools.getStatusMargine(activity));
				}

				activity.toolbar.setY(-Properties.ActionbarSize+value);
			}
		});
		animator.start();
	}
	
	public void showOrHide(){
		if (!locked){
			Float curY = activity.webLayout.getY();
			
			if (curY<(activity.toolbar.getHeight()+ Tools.getStatusSize(activity))/2){
				hide();
			}
			else
				show();
		}
	}
	
	public void actionCanceled(){
		if (hidden)
			hide();
		else
			show();
	}
	
	
}
