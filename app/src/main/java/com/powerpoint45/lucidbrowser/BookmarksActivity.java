package com.powerpoint45.lucidbrowser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import bookmarkModel.Bookmark;
import bookmarkModel.BookmarkFolder;
import bookmarkModel.BookmarksManager;

public class BookmarksActivity extends Activity{

	public static BookmarksManager bookmarksMgr;
	
	ListView bookmarksListView;
	ListView bookmarksFolderListView;
	static BookmarksListAdapter bookmarksListAdapter;
	static BookmarksFolderListAdapter bookmarksFolderListAdapter;
	
	public static Context activity;
	RelativeLayout bookmarkActivityLayout;
	Dialog dialog;
	Dialog editDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// Start with displaying bookmarks root
		bookmarksMgr.displayedFolder = bookmarksMgr.root;
		
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		bookmarkActivityLayout = (RelativeLayout) inflater.inflate(R.layout.bookmarks, null);
		activity = this;
		
		// Set Theme
		if (Properties.appProp.holoDark){
			setTheme(R.style.CustomDarkTheme);
			((TextView)(bookmarkActivityLayout.findViewById(R.id.bookmark_title))).setTextColor(Color.WHITE);
			((TextView)(bookmarkActivityLayout.findViewById(R.id.current_location))).setBackgroundColor(Color.DKGRAY);
			((TextView)(bookmarkActivityLayout.findViewById(R.id.current_location))).setTextColor(Color.GRAY);
			((ImageView)(bookmarkActivityLayout.findViewById(R.id.bookmark_icon))).setColorFilter(Color.WHITE, Mode.SRC_ATOP);
		} else {
			// uses light theme
		}
		
		setContentView(bookmarkActivityLayout);

		bookmarksListAdapter = new BookmarksListAdapter();
		bookmarksFolderListAdapter = new BookmarksFolderListAdapter();

		// Take care of Listeners
		bookmarksListView = (ListView) findViewById(R.id.bookmarks_list);
		bookmarksFolderListView = (ListView) findViewById(R.id.bookmarksfolder_list);
		
		bookmarksListView.setAdapter(bookmarksListAdapter);
		bookmarksFolderListView.setAdapter(bookmarksFolderListAdapter);
		
		OnItemClickListener clickItemListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				
				String url = ((Bookmark) arg1.getTag()).getUrl();
				WebView WV = (WebView) ((ViewGroup) MainActivity.webLayout.findViewById(R.id.webviewholder)).findViewById(R.id.browser_page);
				
				if (WV!=null && url!=null){
					
					if (url.contains(".") && !url.contains(" ")){
						if (url.startsWith("http://")||url.startsWith("https://"))
							WV.loadUrl(url);
						else if (url.startsWith("www."))
							WV.loadUrl("http://"+url);
						else if (url.startsWith("about:")||url.startsWith("file:"))
							WV.loadUrl(url);
						else
							WV.loadUrl("http://"+url);
					}
					else
						WV.loadUrl("http://www.google.com/search?q="+url.replace(" ", "+"));
					
				}
				finish();
			}
			
		};
		
		OnItemClickListener clickFolderListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				

				String internalFolderName = (String) arg1.getTag();
				bookmarksMgr.displayedFolder = bookmarksMgr.displayedFolder.getFolder(pos);
				refreshBookmarksView();
				
				((TextView) (bookmarkActivityLayout.findViewById(R.id.current_location)))
						.setText(bookmarksMgr.displayedFolder.getDisplayName());
				((RelativeLayout) bookmarkActivityLayout.findViewById(R.id.folder_back))
						.setVisibility(View.VISIBLE);
				// finish();
				
			}			
		};
		
		OnItemLongClickListener longClickItemListener = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					final int pos, long arg3) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(BookmarksActivity.this);
				
	        	builder.setTitle(bookmarksMgr.displayedFolder.getBookmark(pos).getDisplayName());
				
				ListView modeList = new ListView(BookmarksActivity.this);
	        	  String[] stringArray = new String[] {  getResources().getString(R.string.openinnewtab), 
							 getResources().getString(R.string.edit),
							 getResources().getString(R.string.remove),
							 getResources().getString(R.string.bookmark_move)};
	        	  ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(BookmarksActivity.this, android.R.layout.simple_list_item_1,android.R.id.text1, stringArray);
	        	  modeList.setAdapter(modeAdapter);
	        	  modeList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int dialogPos, long arg3) {
						
						Bookmark clickedBookmark = bookmarksMgr.displayedFolder.getBookmark(pos);
						switch (dialogPos){
						case 0:   //new tab
							MainActivity.openURLInNewTab(clickedBookmark.getUrl());
							finish();
							break;
						case 1:   //edit
							LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
							final LinearLayout editLayout = (LinearLayout) inflater.inflate(R.layout.edit_bookmark_dialog, null);
							
							// For Title Editing
							((EditText) editLayout.findViewById(R.id.edit_bookmark_title)).setText(clickedBookmark.getDisplayName());
							
							// For URL Editing
							if (clickedBookmark.getUrl().compareTo(MainActivity.assetHomePage)==0)
								((EditText) editLayout.findViewById(R.id.edit_bookmark_url)).setText("about:home");
							else
								((EditText) editLayout.findViewById(R.id.edit_bookmark_url)).setText(clickedBookmark.getUrl());
							
							AlertDialog.Builder builder = new AlertDialog.Builder(activity);
							builder.setTitle(R.string.edit_bookmark)
							.setView(editLayout)
							.setPositiveButton(android.R.string.ok, null)
							.setNegativeButton(android.R.string.cancel, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
						
							//Following is to prevent empty names
							final AlertDialog editDialog = builder.create();
							editDialog.setOnShowListener(new DialogInterface.OnShowListener() {

							    @Override
							    public void onShow(DialogInterface dialog) {

							        Button b = editDialog.getButton(AlertDialog.BUTTON_POSITIVE);
							        b.setOnClickListener(new View.OnClickListener() {

							            @Override
							            public void onClick(View view) {
							            	String url =((EditText) editLayout.findViewById(R.id.edit_bookmark_url)).getText().toString();
							            	String title =((EditText) editLayout.findViewById(R.id.edit_bookmark_title)).getText().toString();
											if (!url.matches("\\s*")&&!title.matches("\\s*")){
												
												if (url.compareTo("about:home")==0)
													url = MainActivity.assetHomePage;
												
												bookmarksMgr.displayedFolder.getBookmark(pos).setUrl(url);
												bookmarksMgr.displayedFolder.getBookmark(pos).setDisplayName(title);
												
												bookmarksListAdapter.notifyDataSetChanged();
												editDialog.dismiss();
											}
							            }
							        });
							    }
							});
							
							dialog.dismiss();
							
							
							editDialog.show();
							
							break;
						case 2:   //remove
							bookmarksMgr.displayedFolder.removeBookmark(pos);
							refreshBookmarksView();
							dialog.dismiss();
							break;
						case 3: // Move
							// Show dialog
							
							Point screenSize = new Point();
							screenSize.x=getWindow().getWindowManager().getDefaultDisplay().getWidth();
							screenSize.y=getWindow().getWindowManager().getDefaultDisplay().getHeight();
							
							Double width = ((double) screenSize.x)-(screenSize.x*0.2);
							Double height =((double) screenSize.y)-(screenSize.y*0.2); 
							
							BookmarkMoveDialog moveFolder = new BookmarkMoveDialog(activity);
							moveFolder.setBookmarkToMove(clickedBookmark);
							moveFolder.show();
							moveFolder.getWindow().setLayout(width.intValue(), height.intValue());

							dialog.dismiss();
							break;
						
						}
					}
	        	  });
	        	  
	        	  builder.setView(modeList);
	        	  dialog = builder.create();
	        	  dialog.show();	
	        	  System.out.println("LONG PRESSED");
				
				
				return true;
			}
		};
		
		OnItemLongClickListener longClickFolderListener = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					final int pos, long arg3) {
				
				final BookmarkFolder clickedFolder = bookmarksMgr.displayedFolder.getFolder(pos);
				AlertDialog.Builder builder = new AlertDialog.Builder(BookmarksActivity.this);
	        	builder.setTitle(clickedFolder.getDisplayName());
				
				ListView modeList = new ListView(BookmarksActivity.this);
	        	  String[] stringArray = new String[] {
							 getResources().getString(R.string.edit),
							 getResources().getString(R.string.remove),
							 getResources().getString(R.string.bookmark_move)};
	        	  ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(BookmarksActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
	        	  modeList.setAdapter(modeAdapter);
	        	  modeList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int dialogPos, long arg3) {
						
						
						switch (dialogPos){
						case 0:   //edit
							LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
							final LinearLayout editLayout = (LinearLayout) inflater.inflate(R.layout.bookmark_new_folder_dialog, null);
							((EditText)(editLayout.findViewById(R.id.new_folder_name))).setText(clickedFolder.getDisplayName());
							
							AlertDialog.Builder builder = new AlertDialog.Builder(activity);
							builder.setTitle(R.string.edit_bookmark_folder)
							.setView(editLayout)
							.setPositiveButton(android.R.string.ok, null)
							.setNegativeButton(android.R.string.cancel, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});

							//Following is to prevent empty names
							final AlertDialog editDialog = builder.create();
							editDialog.setOnShowListener(new DialogInterface.OnShowListener() {

							    @Override
							    public void onShow(DialogInterface dialog) {

							        Button b = editDialog.getButton(AlertDialog.BUTTON_POSITIVE);
							        b.setOnClickListener(new View.OnClickListener() {

							            @Override
							            public void onClick(View view) {
							            	String title =((EditText) editLayout.findViewById(R.id.new_folder_name)).getText().toString();
											if (!title.matches("\\s*")){
												bookmarksMgr.displayedFolder.getFolder(pos).setDisplayName(title);
												bookmarksFolderListAdapter.notifyDataSetChanged();
												editDialog.dismiss();
											}
							            }
							        });
							    }
							});
							
							dialog.dismiss();

							editDialog.show();
							
							break;
						case 1:   //remove
							// Confirmation dialog
							builder = new AlertDialog.Builder(activity);
						    builder.setMessage(R.string.remove_bookmark_folder_confirm)
						        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						        	public void onClick(DialogInterface dialog, int id) {
						                	   bookmarksMgr.displayedFolder.removeFolder(pos);
											   refreshBookmarksView();
						                   }
						               })
						        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						        	public void onClick(DialogInterface dialog, int id) {
						        		}
						        });
						    
						        // Create the AlertDialog
						        AlertDialog confirm = builder.create();
						        confirm.show();
							dialog.dismiss();
							break;
						case 2: // Move
							// Show dialog
							
							Point screenSize = new Point();
							screenSize.x=getWindow().getWindowManager().getDefaultDisplay().getWidth();
							screenSize.y=getWindow().getWindowManager().getDefaultDisplay().getHeight();
							
							Double width = ((double) screenSize.x)-(screenSize.x*0.2);
							Double height =((double) screenSize.y)-(screenSize.y*0.2); 
							
							BookmarkMoveDialog moveFolder = new BookmarkMoveDialog(activity);
							moveFolder.setFolderToMove(clickedFolder);
							moveFolder.show();
							moveFolder.getWindow().setLayout(width.intValue(), height.intValue());

							dialog.dismiss();
							break;
						
						}
					}
	        	  });
	        	  
	        	  builder.setView(modeList);
	        	  dialog = builder.create();
	        	  dialog.show();	
	        	  System.out.println("LONG PRESSED");
				
				
				return true;
			}
		};
		
		bookmarksListView.setOnItemLongClickListener(longClickItemListener);
		bookmarksListView.setOnItemClickListener(clickItemListener);
		bookmarksFolderListView.setOnItemClickListener(clickFolderListener);
		bookmarksFolderListView.setOnItemLongClickListener(longClickFolderListener);
		
		
		
		//Start Downloading missing Favicons
		List <Bookmark> currentBooks = BookmarksActivity.bookmarksMgr.root.getAllBookMarks();
		int numURLsToDownload = 0;
		File imageFile = null;
		URL curURL = null;
		
		Vector<Bookmark> urlsToDownload = new Vector<Bookmark>();
		
		for (int i = 0; i < currentBooks.size() ; i++){
			curURL = currentBooks.get(i).getURL();
			if (curURL!=null && curURL.getHost().compareTo("")!=0 && curURL.getPath().compareTo(MainActivity.assetHomePage)!=0){
				imageFile = new File(MainActivity.ctxt.getApplicationInfo().dataDir+"/icons/"+ curURL.getHost());
				if (!imageFile.exists() || currentBooks.get(i).getPathToFavicon()==null){
					numURLsToDownload++;
					urlsToDownload.add(currentBooks.get(i));
				}
			}
		}
		
		Log.d("LB", numURLsToDownload+" favicons should download");
		if (numURLsToDownload>0){
			Bookmark arrayURLsToDownload[] = new Bookmark[urlsToDownload.size()];
			
			for (int i = 0; i<urlsToDownload.size(); i++){
				arrayURLsToDownload[i] = urlsToDownload.get(i);
			}
			new DownloadFilesTask().execute(arrayURLsToDownload);
		}

	}
	
	private class DownloadFilesTask extends AsyncTask<Bookmark, Integer, Long> {
	    protected Long doInBackground(Bookmark... urls) {
	        int count = urls.length;
	        long totalSize = 0;
	        for (int i = 0; i < count; i++) {
	        	
	        	URL curURL = urls[i].getURL();
	        	URL urlToAdd = null;
				try {
					urlToAdd = new URL(curURL.getProtocol() + "://" + curURL.getHost() + "/favicon.ico");
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (urlToAdd!=null){
		        	downloadImage(urlToAdd, urls[i]);
		        	Log.d("LB", "DOWNLOADING INDEX "+i +"AND URL " +urlToAdd.getPath());
		        	publishProgress(0);
				}
	            if (isCancelled()) break;
	        }
	        return totalSize;
	    }

	    protected void onProgressUpdate(Integer... progress) {
	    	bookmarksListAdapter.notifyDataSetChanged();
	    }

	    protected void onPostExecute(Long result) {
	    	bookmarksListAdapter.notifyDataSetChanged();
	    }
	}
	
	public void downloadImage(URL url, Bookmark b){
		try{
			InputStream in = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1!=(n=in.read(buf)))
			{
			   out.write(buf, 0, n);
			}
			out.close();
			in.close();
			byte[] response = out.toByteArray();
			new File(MainActivity.ctxt.getApplicationInfo().dataDir+"/icons/").mkdirs();
			FileOutputStream fos = new FileOutputStream((MainActivity.ctxt.getApplicationInfo().dataDir+"/icons/"+ url.getHost()));
			fos.write(response);
			fos.close();
			b.setPathToFavicon(MainActivity.ctxt.getApplicationInfo().dataDir+"/icons/"+ url.getHost());
			bookmarksMgr.saveBookmarksManager();
		}catch(Exception e){};
		
		
	}
	
	@Override
	public void onPause(){
		bookmarksMgr.saveBookmarksManager();
		super.onPause();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.addbookmarkfolder: // Add folder button pressed
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				final LinearLayout editLayout = (LinearLayout) inflater.inflate(R.layout.bookmark_new_folder_dialog, null);
				
				// Name for new Folder
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle(R.string.add_bookmark_folder)
				.setView(editLayout)
				.setPositiveButton(android.R.string.ok,null)
				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

				//Following is to prevent empty names
				final AlertDialog editDialog = builder.create();
				editDialog.setOnShowListener(new DialogInterface.OnShowListener() {

				    @Override
				    public void onShow(DialogInterface dialog) {

				        Button b = editDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				        b.setOnClickListener(new View.OnClickListener() {

				            @Override
				            public void onClick(View view) {
								String folderName =((EditText) editLayout.findViewById(R.id.new_folder_name)).getText().toString();
								if (!folderName.matches("\\s*")){
									bookmarksMgr.displayedFolder.addFolder(new BookmarkFolder(folderName));
									bookmarksFolderListAdapter.notifyDataSetChanged();
									editDialog.dismiss();
								}
				            }
				        });
				    }
				});
				
				editDialog.show();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.bookmarks_actionbar, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	public static void refreshBookmarksView() {
		bookmarksFolderListAdapter.setFolderList(bookmarksMgr.displayedFolder
				.getContainedFolders());
		bookmarksFolderListAdapter.notifyDataSetChanged();
		bookmarksListAdapter.setBookmarkList(bookmarksMgr.displayedFolder
				.getContainedBookmarks());
		bookmarksListAdapter.notifyDataSetChanged();
	}
	
	public void goToParentFolder(View v){
		//TODO CLEAN UP LATER
		if (!bookmarksMgr.displayedFolder.isRoot) {
			bookmarksMgr.displayedFolder = bookmarksMgr.displayedFolder.parentFolder;
			if (bookmarksMgr.displayedFolder.isRoot) {
				((RelativeLayout) bookmarkActivityLayout.findViewById(R.id.folder_back))
						.setVisibility(View.GONE);
			}

			refreshBookmarksView();
			
			((TextView) (bookmarkActivityLayout.findViewById(R.id.current_location)))
					.setText(bookmarksMgr.displayedFolder.getDisplayName());
		}
	}
	
	public void setCurrentPositionHeaderText(String text){
		((TextView) bookmarkActivityLayout.findViewById(R.id.current_location)).setText(text);
	}
	
	@Override 
	public void onBackPressed() { 
		if (bookmarksMgr.displayedFolder.isRoot)
			finish();
		else
			goToParentFolder(null);
	} 

	
}