package com.powerpoint45.lucidbrowser;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BrowserImageAdapter extends BaseAdapter{

	
	public BrowserImageAdapter(Context c) {
		
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
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		
		
		if (convertView == null){
			
        	convertView = MainActivity.inflater.inflate(R.layout.browser_item, null);
        	
        	viewHolder = new ViewHolder();
            viewHolder.tabStatus = (TextView)convertView.findViewById(R.id.tab_text);
            viewHolder.tabIcon = (ImageView)convertView.findViewById(R.id.tab_icon);
            viewHolder.closeButton = (ImageView)convertView.findViewById(R.id.close_tb_button);
            
            if (Properties.sidebarProp.theme.compareTo("w")==0){
            	viewHolder.tabIcon.setImageResource(R.drawable.ic_new_window_holo_light);
				viewHolder.tabStatus.setTextColor(Color.BLACK);
				viewHolder.closeButton.setColorFilter(Color.BLACK, Mode.MULTIPLY);
			} 
            else if (Properties.sidebarProp.theme.compareTo("b")==0){
				viewHolder.tabIcon.setImageResource(R.drawable.ic_new_window_holo_dark);
				viewHolder.closeButton.setColorFilter(Color.WHITE, Mode.MULTIPLY);
            }
            else if (Properties.sidebarProp.theme.compareTo("c")==0){
				int sidetextcolor = Properties.sidebarProp.sideBarTextColor;
				viewHolder.tabStatus.setTextColor(sidetextcolor);
				viewHolder.closeButton.setColorFilter(sidetextcolor, Mode.MULTIPLY);
				Drawable newTab = viewHolder.tabIcon.getResources().getDrawable(R.drawable.ic_new_window_holo_dark);
				viewHolder.tabIcon.setImageDrawable(newTab);
				viewHolder.tabIcon.setColorFilter(sidetextcolor, Mode.MULTIPLY);
			}
            
            convertView.setTag(viewHolder);
        }
		else
            viewHolder = (ViewHolder) convertView.getTag();
		viewHolder.closeButton.setTag(pos);
		if (pos==MainActivity.webWindows.size()){
			viewHolder.tabIcon.setVisibility(View.VISIBLE);
			viewHolder.tabStatus.setText("");
			viewHolder.closeButton.setVisibility(View.GONE);
		}
		else{
			viewHolder.tabIcon.setVisibility(View.GONE);
			viewHolder.closeButton.setVisibility(View.VISIBLE);
			if (MainActivity.webWindows.get(pos).getUrl()!=null && MainActivity.webWindows.get(pos).getUrl().compareTo(MainActivity.assetHomePage)==0)
				viewHolder.tabStatus.setText(MainActivity.activity.getResources().getString(R.string.home));
			else
				viewHolder.tabStatus.setText(MainActivity.webWindows.get(pos).getTitle());
			
			if (viewHolder.tabStatus.getText() == null || viewHolder.tabStatus.getText().toString().equals("")){
				viewHolder.tabStatus.setText("...");
			}
			
			viewHolder.closeButton.setVisibility(View.VISIBLE);
			
			if (pos == MainActivity.getTabNumber())
				viewHolder.tabStatus.setTypeface(null, Typeface.BOLD);
			else
				viewHolder.tabStatus.setTypeface(null, Typeface.NORMAL);
		}
		
		//convertView.findViewById(R.id.browser_div).setAlpha(.8f);
		
		return convertView;
	}
	
	

}
