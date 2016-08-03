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
	
	public ActionBarControls(ActionBar ab){
		actionBar = ab;
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
			MainActivity.browserListView.setY(Tools.getStatusSize());
			MainActivity.browserListView.setPadding(0, 0, 0, Tools.getStatusSize());
		    
			ValueAnimator animator = ValueAnimator.ofInt((int)(MainActivity.toolbar.getY()-Tools.getStatusSize()), -Properties.ActionbarSize);
		    animator.setDuration(200); 
		 
		    final int margine = Tools.getStatusMargine();
		    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
		        @Override 
		        public void onAnimationUpdate(ValueAnimator animation) {
		            int value = ((Integer) (animation.getAnimatedValue()));
		            
		            MainActivity.webLayout.setY((int) (value + margine));
		    		MainActivity.toolbar.setY(value+Tools.getStatusSize());
		        } 
		    });
		    animator.start();
		    
		    clearFocuses();
		    
	}
	
	public void clearFocuses(){
		if (MainActivity.bar.findViewById(R.id.finder)!=null)
		    SetupLayouts.dismissFindBar();
	    
	    if (((EditText) MainActivity.bar.findViewById(R.id.browser_searchbar))!=null){
	    	MainActivity.imm.hideSoftInputFromWindow(MainActivity.bar.findViewById(R.id.browser_searchbar).getWindowToken(),0);
	    	MainActivity.bar.findViewById(R.id.browser_searchbar).clearFocus();
	    }
	}
	
	
	
	public void move(float f){
		if (!locked){
			Float curToolbarY = MainActivity.toolbar.getY()-Tools.getStatusSize();
			Float newToolbarY = curToolbarY + f;
			
			int margine = Tools.getStatusMargine();
			
			if (newToolbarY<-Properties.ActionbarSize)
				newToolbarY = (float) -Properties.ActionbarSize;
			
			if (newToolbarY>0)
				newToolbarY = 0f;
			
			if (MainActivity.webLayout.getY()!=newToolbarY+margine){
				MainActivity.webLayout.setY((int) (newToolbarY + margine));
				MainActivity.toolbar.setY(newToolbarY+Tools.getStatusSize());
			}
		}
	}
	
	public void show(){
		//if (hidden!=false){
		Log.d("LL", "SHOW");
			hidden = false;
			int margine = Tools.getStatusMargine();
			MainActivity.browserListView.setY(margine);
			MainActivity.browserListView.setPadding(0, 0, 0, margine);
			
			ValueAnimator animator = ValueAnimator.ofInt((int)MainActivity.webLayout.getY(), Tools.getStatusMargine());
		    animator.setDuration(200); 
		 
		    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
		        @Override 
		        public void onAnimationUpdate(ValueAnimator animation) {
		            int value = ((Integer) (animation.getAnimatedValue()));
		            
		            if (value>=0){
			            if (value<=Tools.getStatusMargine())
			            	MainActivity.webLayout.setY(value);
			            else
			            	MainActivity.webLayout.setY(Tools.getStatusMargine());
		            }
		            
		            MainActivity.toolbar.setY(-Properties.ActionbarSize+value);
		        } 
		    });
		    animator.start();
	}
	
	public void showOrHide(){
		if (!locked){
			Float curY = MainActivity.webLayout.getY();
			
			if (curY<(MainActivity.toolbar.getHeight()+Tools.getStatusSize())/2){
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
