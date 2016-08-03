package com.powerpoint45.lucidbrowser;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import bookmarkModel.Bookmark;
import bookmarkModel.BookmarkFolder;

public class BookmarkMoveDialog extends Dialog {

	BookmarkFolder folderToMove;
	Bookmark bookmarkToMove;
	
	BookmarkFolder displayFolder = BookmarksActivity.bookmarksMgr.root;
	BookmarksFolderListAdapter folderListAdapter;
	RelativeLayout dialogLayout;

	public void setFolderToMove(BookmarkFolder toMove){
		this.folderToMove = toMove;
		this.folderListAdapter.setIgnore(toMove);
	}
	
	public void setBookmarkToMove(Bookmark toMove){
		this.bookmarkToMove = toMove;
	}
	
	public BookmarkMoveDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	public BookmarkMoveDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public BookmarkMoveDialog(Context context) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) MainActivity.activity
				.getSystemService(MainActivity.activity.LAYOUT_INFLATER_SERVICE);
		dialogLayout = (RelativeLayout) inflater.inflate(
				R.layout.bookmarks_move_dialog, null);

		// Set Theme
		if (Properties.appProp.holoDark) {
			((TextView) (dialogLayout.findViewById(R.id.bookmark_title)))
					.setTextColor(Color.WHITE);
			((TextView) (dialogLayout.findViewById(R.id.current_location)))
					.setBackgroundColor(Color.DKGRAY);
			((TextView) (dialogLayout.findViewById(R.id.current_location)))
					.setTextColor(Color.GRAY);
			((ImageView) (dialogLayout.findViewById(R.id.bookmark_icon)))
					.setColorFilter(Color.WHITE, Mode.SRC_ATOP);
		} else {
			// uses light theme
		}

		setContentView(dialogLayout);

		this.setTitle(MainActivity.activity.getResources().getString(
				R.string.bookmark_move)
				+ "...");

		ListView bookmarksFolderListView = (ListView) findViewById(R.id.bookmarksfolder_list);
		this.folderListAdapter = new BookmarksFolderListAdapter();
		folderListAdapter.setFolderList(this.displayFolder
				.getContainedFolders());
		bookmarksFolderListView.setAdapter(folderListAdapter);

		OnItemClickListener clickFolderListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {

				displayFolder = displayFolder.getFolder(pos);
				folderListAdapter.setFolderList(displayFolder
						.getContainedFolders());
				folderListAdapter.notifyDataSetChanged();
				((TextView) (dialogLayout.findViewById(R.id.current_location)))
						.setText(displayFolder.getDisplayName());
				((RelativeLayout) dialogLayout.findViewById(R.id.folder_back))
						.setVisibility(View.VISIBLE);
				// finish();
			}
		};

		bookmarksFolderListView.setOnItemClickListener(clickFolderListener);
		
		((RelativeLayout)(dialogLayout.findViewById(R.id.folder_back))).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!displayFolder.isRoot) {
					displayFolder = displayFolder.parentFolder;
					if (displayFolder.isRoot) {
						((RelativeLayout) dialogLayout.findViewById(R.id.folder_back))
								.setVisibility(View.GONE);
					}

					folderListAdapter
							.setFolderList(displayFolder.getContainedFolders());
					folderListAdapter.notifyDataSetChanged();
					((TextView) (dialogLayout.findViewById(R.id.current_location)))
							.setText(displayFolder.getDisplayName());
				}
			}
		});
		
		((Button) (dialogLayout.findViewById(R.id.btn_move_here)))
				.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (folderToMove != null) {
							folderToMove.parentFolder.removeFolder(folderToMove
									.getInternalName());
							displayFolder.addFolder(folderToMove);
							BookmarksActivity.refreshBookmarksView();
							dismiss();
						}
						
						if (bookmarkToMove != null) {
							System.out.println(bookmarkToMove.getInFolder().getDisplayName());
							bookmarkToMove.getInFolder().removeBookmark(bookmarkToMove
									.getInternalName());
							displayFolder.addBookmark(bookmarkToMove);
							BookmarksActivity.refreshBookmarksView();
							dismiss();
						}
					}
				});
	}
}
