package com.powerpoint45.lucidbrowser;

import java.util.List;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import bookmarkModel.Bookmark;

public class BookmarksListAdapter extends BaseAdapter {

	List<Bookmark> bookmarks = BookmarksActivity.bookmarksMgr.displayedFolder.getContainedBookmarks();
	
	
	
	public void setBookmarkList(List<Bookmark> newBookmarkList){
		this.bookmarks = newBookmarkList;
	}
	
	@Override
	public int getCount() {
		return bookmarks.size();
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

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		RelativeLayout RL = (RelativeLayout) MainActivity.inflater.inflate(
				R.layout.bookmark_item, null);

		if (Properties.appProp.holoDark) {
			((TextView)(RL.findViewById(R.id.bookmark_title))).setTextColor(Color.WHITE);
			((TextView)(RL.findViewById(R.id.bookmark_url_title))).setTextColor(Color.WHITE);
		} else {
		// Use sight theme
		}
		
		Bookmark bookmark = bookmarks.get(arg0);
		String bookmarkTitle = bookmark.getDisplayName();
		String bookmarkURL = bookmark.getUrl();
		
		boolean hasFavicon = checkBookmarkForFavicon(bookmark);
		
		((TextView) RL.findViewById(R.id.bookmark_title)).setText(bookmarkTitle);
		
		if (bookmarkURL.compareTo(MainActivity.assetHomePage)==0){
			((TextView) RL.findViewById(R.id.bookmark_url_title)).setText("about:home");
			
			if (!Properties.appProp.holoDark)
				((ImageView) RL.findViewById(R.id.bookmark_icon)).setColorFilter(Color.BLACK);
			((ImageView) RL.findViewById(R.id.bookmark_icon)).setImageResource(R.drawable.ic_collections_view_as_list);
			
		}
		else{
			((TextView) RL.findViewById(R.id.bookmark_url_title)).setText(bookmarkURL);
		
			//Try to set Favicon
			if (hasFavicon){
				try {
					((ImageView) RL.findViewById(R.id.bookmark_icon)).setColorFilter(null);
					((ImageView) RL.findViewById(R.id.bookmark_icon)).setImageBitmap(BitmapFactory.decodeFile(bookmark.getPathToFavicon()));
				} catch (Exception e) {}
			}else{
				if (!Properties.appProp.holoDark)
					((ImageView) RL.findViewById(R.id.bookmark_icon)).setColorFilter(Color.BLACK);
				((ImageView) RL.findViewById(R.id.bookmark_icon)).setImageResource(R.drawable.ic_collections_view_as_list);
			}
		
		}
		
		RL.setTag(bookmark);
		return RL;
	}
	
	public boolean checkBookmarkForFavicon(Bookmark b){
		if (b.getPathToFavicon()==null){
			Log.d("LB", "the bookmark "+ b.getDisplayName() + " does not have a favicon");
			return false;
		}else{
			return true;
		}
		
	}
	

}
