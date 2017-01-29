package com.powerpoint45.lucidbrowser;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.URL;
import java.util.List;

import bookmarkModel.BookmarkFolder;

public class BookmarksFolderListAdapter extends BaseAdapter {
	List<BookmarkFolder> bookmarkFolders = BookmarksActivity.bookmarksMgr.displayedFolder.getContainedFolders();
	URL url;
	
	BookmarkFolder ignore;

	public void setIgnore(BookmarkFolder folder){
		ignore = folder;
	}
	
	public BookmarkFolder getIgnore(){
		return ignore;
	}
	
	public void setFolderList(List<BookmarkFolder> newFolderList){
		this.bookmarkFolders = newFolderList;
	}
	@Override
	public int getCount() {
		return bookmarkFolders.size();
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
		BookmarkFolder folder = bookmarkFolders.get(arg0);
		
		if (ignore!=null && ignore.getInternalName().equals(folder.getInternalName())){
			return ((LinearLayout) MainActivity.inflater.inflate(R.layout.null_item, null));
		}

		RelativeLayout RL = (RelativeLayout) MainActivity.inflater.inflate(
				R.layout.bookmarkfolder_item, null);

		if (Properties.appProp.holoDark) {
			((TextView)(RL.findViewById(R.id.bookmark_title))).setTextColor(Color.WHITE);
			((ImageView)(RL.findViewById(R.id.bookmark_icon))).setColorFilter(Color.WHITE, Mode.SRC_ATOP);
		} else {
		// Use sight theme
		}


		((TextView) RL.findViewById(R.id.bookmark_title)).setText(folder
				.getDisplayName());
		RL.setTag(folder.getInternalName());

		((ImageView) RL.findViewById(R.id.bookmark_icon))
				.setImageResource(R.drawable.ic_action_collection);

		return RL;
	}

}
