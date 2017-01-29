package com.powerpoint45.lucidbrowser;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import views.CustomWebView;

public class BrowserImageAdapter extends BaseAdapter{

	MainActivity activity;
	public BrowserImageAdapter(MainActivity c) {
		activity = c;

    }
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return MainActivity.webWindows.size()+1;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	static class ViewHolder {
		  TextView tabStatus;
		  ImageView tabIcon;
		  ImageView  closeButton;
		}
	
	@Override
	public View getView(int pos, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder;


		if (convertView == null){
			
        	convertView = MainActivity.inflater.inflate(R.layout.browser_item, null);
        	
        	viewHolder = new ViewHolder();
            viewHolder.tabStatus = (TextView)convertView.findViewById(R.id.tab_text);
            viewHolder.tabIcon = (ImageView)convertView.findViewById(R.id.tab_icon);
            viewHolder.closeButton = (ImageView)convertView.findViewById(R.id.close_tab_button);
            convertView.setTag(viewHolder);
        }
		else
            viewHolder = (ViewHolder) convertView.getTag();

		if (Properties.sidebarProp.theme.compareTo("w")==0){
			viewHolder.tabIcon.setImageResource(R.drawable.ic_new_window_holo_light);
			viewHolder.tabStatus.setTextColor(Color.BLACK);
			viewHolder.closeButton.setColorFilter(Color.BLACK, Mode.MULTIPLY);
		}else if (Properties.sidebarProp.theme.compareTo("b")==0){
			viewHolder.tabIcon.setImageResource(R.drawable.ic_new_window_holo_dark);
			viewHolder.tabStatus.setTextColor(Color.WHITE);
			viewHolder.closeButton.setColorFilter(Color.WHITE, Mode.MULTIPLY);
		}else if (Properties.sidebarProp.theme.compareTo("c")==0){
			int sidetextcolor = Properties.sidebarProp.sideBarTextColor;
			viewHolder.tabStatus.setTextColor(sidetextcolor);
			viewHolder.closeButton.setColorFilter(sidetextcolor, Mode.MULTIPLY);
			Drawable newTab = viewHolder.tabIcon.getResources().getDrawable(R.drawable.ic_new_window_holo_dark);
			viewHolder.tabIcon.setImageDrawable(newTab);
			viewHolder.tabIcon.setColorFilter(sidetextcolor, Mode.MULTIPLY);
		}


		viewHolder.closeButton.setTag(pos);
		if (pos== MainActivity.webWindows.size()){
			viewHolder.tabIcon.setVisibility(View.VISIBLE);
			viewHolder.tabStatus.setText("");
			viewHolder.closeButton.setVisibility(View.GONE);
		}
		else{
			viewHolder.tabIcon.setVisibility(View.GONE);
			viewHolder.closeButton.setVisibility(View.VISIBLE);


			CustomWebView WV = MainActivity.webWindows.get(pos);
			if (WV.getUrl()!=null && WV.getUrl().startsWith("file:///android_asset/"))
				viewHolder.tabStatus.setText(activity.getResources().getString(R.string.home));
			else if ((WV.getTitle()!=null && (WV.getTitle().equals("ehome.html")
					|| WV.getTitle().equals("home.html")))
					&&(WV.getUrl()!=null && !WV.getUrl().startsWith("file:///android_asset/")))
				//title points to asset home but url is something else
				viewHolder.tabStatus.setText(WV.getUrl());
			else if (WV.getUrl()!=null)
				viewHolder.tabStatus.setText(WV.getTitle());
			else if (WV.getUrl()!=null && WV.getProgress()!=100)
				viewHolder.tabStatus.setText(WV.getUrl());
			else
				viewHolder.tabStatus.setText("...");

			if (viewHolder.tabStatus.getText().toString()==null || viewHolder.tabStatus.getText().toString().equals(""))
				viewHolder.tabStatus.setText("...");
			
			viewHolder.closeButton.setVisibility(View.VISIBLE);
			
			if (pos == activity.getTabNumber())
				viewHolder.tabStatus.setTypeface(null, Typeface.BOLD);
			else
				viewHolder.tabStatus.setTypeface(null, Typeface.NORMAL);
		}
		
		//convertView.findViewById(R.id.browser_div).setAlpha(.8f);
		
		return convertView;
	}
	
	

}
