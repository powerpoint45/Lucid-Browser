package com.powerpoint45.lucidbrowser;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import bookmarkModel.Bookmark;
import bookmarkModel.BookmarkFolder;

class BookmarkMoveDialog extends Dialog {

	private BookmarkFolder folderToMove;
	private Bookmark bookmarkToMove;

	private BookmarkFolder displayFolder = BookmarksActivity.bookmarksMgr.root;
	private BookmarksFolderListAdapter folderListAdapter;
	private RelativeLayout dialogLayout;

	private BookmarksActivity bookmarksActivity;

	void setFolderToMove(BookmarkFolder toMove){
		this.folderToMove = toMove;
		this.folderListAdapter.setIgnore(toMove);
	}

	void setBookmarkToMove(Bookmark toMove){
		this.bookmarkToMove = toMove;
	}

	BookmarkMoveDialog(BookmarksActivity context) {
		super(context);
		bookmarksActivity = context;

		dialogLayout = (RelativeLayout) bookmarksActivity.getLayoutInflater().inflate(
				R.layout.bookmarks_move_dialog, null);

		// Set Theme
		if (Properties.appProp.darkTheme) {
			((TextView) (dialogLayout.findViewById(R.id.bookmark_title)))
					.setTextColor(Color.WHITE);
			dialogLayout.findViewById(R.id.current_location)
					.setBackgroundColor(Color.DKGRAY);
			((TextView) (dialogLayout.findViewById(R.id.current_location)))
					.setTextColor(Color.GRAY);
			((ImageView) (dialogLayout.findViewById(R.id.bookmark_icon)))
					.setColorFilter(Color.WHITE, Mode.SRC_ATOP);
		} else {
			// uses light theme
		}

		setContentView(dialogLayout);

		this.setTitle(context.getResources().getString(
				R.string.bookmark_move)
				+ "...");

		ListView bookmarksFolderListView = findViewById(R.id.bookmarksfolder_list);
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
				dialogLayout.findViewById(R.id.folder_back)
						.setVisibility(View.VISIBLE);
				// finish();
			}
		};

		bookmarksFolderListView.setOnItemClickListener(clickFolderListener);

		dialogLayout.findViewById(R.id.folder_back).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (!displayFolder.isRoot) {
					displayFolder = displayFolder.parentFolder;
					if (displayFolder.isRoot) {
						dialogLayout.findViewById(R.id.folder_back)
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

		dialogLayout.findViewById(R.id.btn_move_here)
				.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						if (folderToMove != null) {
							folderToMove.parentFolder.removeFolder(folderToMove
									.getInternalName());
							displayFolder.addFolder(folderToMove);
							bookmarksActivity.refreshBookmarksView();
							dismiss();
						}

						if (bookmarkToMove != null) {
							System.out.println("BOOK NAME:"+ bookmarkToMove.getInFolder().getDisplayName());
							bookmarkToMove.getInFolder().removeBookmark(bookmarkToMove
									.getInternalName());
							displayFolder.addBookmark(bookmarkToMove);
							bookmarksActivity.refreshBookmarksView();
							dismiss();
						}
					}
				});
	}
}
