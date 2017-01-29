package com.powerpoint45.lucidbrowser;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

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

	class ViewHolder{
		TextView title;
		TextView url;
		ImageView icon;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		ViewHolder holder;

		if (convertView==null) {
			convertView = (RelativeLayout) MainActivity.inflater.inflate(
					R.layout.bookmark_item, null);
			holder = new ViewHolder();
			holder.title = ((TextView)(convertView.findViewById(R.id.bookmark_title)));
			holder.url = ((TextView)(convertView.findViewById(R.id.bookmark_url_title)));
			holder.icon = ((ImageView) convertView.findViewById(R.id.bookmark_icon));
			holder.url.setTag(holder);
		}else {
			holder = (ViewHolder) ((TextView)(convertView.findViewById(R.id.bookmark_url_title))).getTag();
			//performance:
			//if icon exists then just return cached view but if not then do not because the icon may have been downloaded
			//and if url view is the same as the current url
			if (((Boolean)holder.icon.getTag())!=null && holder.url.getText()!=null&&
					((Boolean)holder.icon.getTag())==true && holder.url.getText().equals(bookmarks.get(arg0).getUrl())
					&& holder.title.getText().toString().equals(bookmarks.get(arg0).getDisplayName())) {
				return convertView;
			}
		}

		if (Properties.appProp.holoDark) {
			holder.title.setTextColor(Color.WHITE);
			holder.url.setTextColor(Color.WHITE);
		} else {
		// Use light theme
		}
		
		Bookmark bookmark = bookmarks.get(arg0);
		String bookmarkTitle = bookmark.getDisplayName();
		String bookmarkURL = bookmark.getUrl();
		
		boolean hasFavicon = checkBookmarkForFavicon(bookmark);
		
		holder.title.setText(bookmarkTitle);
		
		if (bookmarkURL.equals(Properties.webpageProp.assetHomePage)){
			holder.title.setText("about:home");
			
			if (!Properties.appProp.holoDark)
				holder.icon.setColorFilter(Color.BLACK);
			holder.icon.setImageResource(R.drawable.ic_collections_view_as_list);
			
		}
		else{
			((TextView) convertView.findViewById(R.id.bookmark_url_title)).setText(bookmarkURL);
		
			//Try to set Favicon
			if (hasFavicon){
				try {
					holder.icon.setColorFilter(null);
					holder.icon.setTag(true);
					holder.icon.setImageBitmap(bookmark.getIconBitmap());
				} catch (Exception e) {}
			}else{
				if (!Properties.appProp.holoDark)
					holder.icon.setColorFilter(Color.BLACK);
				holder.icon.setTag(false);
				holder.icon.setImageResource(R.drawable.ic_collections_view_as_list);
			}
		
		}

		convertView.setTag(bookmark);
		return convertView;
	}
	
	public boolean checkBookmarkForFavicon(Bookmark b){
		if (b.getPathToFavicon()==null){
			return false;
		}else{
			return true;
		}
		
	}
	

}
