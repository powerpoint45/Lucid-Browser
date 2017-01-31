package com.powerpoint45.lucidbrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import bookmarkModel.Bookmark;
import bookmarkModel.BookmarkFolder;
import bookmarkModel.BookmarksManager;
import preferences.ColorPickerPreference;

public class SettingsV2 extends AppCompatPreferenceActivity {
	SharedPreferences globalPref;
	ColorPickerPreference sideColor;
	ColorPickerPreference sideTextColor;
	Boolean firstStart = true;
	BookmarksManager manager;
	boolean killBrowser;
	SharedPreferences.OnSharedPreferenceChangeListener changeListener;

	public static class HelperMethods {
		static void DeleteRecursive(File fileOrDirectory) {
			if (fileOrDirectory.exists()) {
				if (fileOrDirectory.isDirectory())
					for (File child : fileOrDirectory.listFiles())
						DeleteRecursive(child);
				fileOrDirectory.delete();
			}
		}

		static void clearBrowsingTrace(String trace, Activity activity) {
			ApplicationInfo appInfo = activity.getApplicationInfo();
			if (trace == "cache") {
				new WebView(activity).clearCache(true);
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/Cache/"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/GPUCache/"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/Service Worker/CacheStorage"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/Service Worker/ScriptCache"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/Local Storage/"));
				DeleteRecursive(new File(appInfo.dataDir+"/cache/"));

			} else if (trace == "cookies") {
				DeleteRecursive(new File(appInfo.dataDir+"/databases/webviewCookiesChromium.db"));
				DeleteRecursive(new File(appInfo.dataDir+"/databases/webviewCookiesChromiumPrivate.db"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/Cookies"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/Cookies-journal"));

			} else if (trace == "history") {
				//DeleteRecursive(new File(appInfo.dataDir+"/databases/webview.db"));
				DeleteRecursive(new File(appInfo.dataDir+"/databases/webview.db-shm"));
				DeleteRecursive(new File(appInfo.dataDir+"/databases/webview.db-wal"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/databases"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/IndexedDB"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/Web Data"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/Service Worker/Database"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/Web Data-journal"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/QuotaManager"));
				DeleteRecursive(new File(appInfo.dataDir+"/app_webview/QuotaManager-journal"));
			} else if (trace == "all") {
				clearBrowsingTrace("cache", activity);
				clearBrowsingTrace("cookies", activity);
				clearBrowsingTrace("history", activity);

			} else {
				System.err
						.println("clearBrowsingTrace(String trace) did nothing. Wrong parameter was given");
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		globalPref = PreferenceManager
				.getDefaultSharedPreferences(SettingsV2.this);

		boolean useDark = globalPref.getBoolean("holodark", false);

		if (!useDark){
			setTheme(R.style.NewAppThemeLight);
		}

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_v2);

		if (!globalPref.getBoolean("disableads", false))
			new AdPreference(globalPref, this).setUpAd();




		//START CHANGE LISTENER-------------------------------------------------------------------------------------------
		changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
				if (!killBrowser) {
					if (key.equals("fullscreen")) {
						killBrowser = true;
					}else if (key.equals("swapLayout")) {
						killBrowser = true;
					}else if (key.equals("enableimages")) {
						killBrowser = true;
					}else if (key.equals("enablecookies")) {
						killBrowser = true;
					}else if (key.equals("usedesktopview")) {
						killBrowser = true;
					}else if (key.equals("webfontsize")) {
						killBrowser = true;
					}else if (key.equals("systempersistent")){
						killBrowser = true;
					}else if (key.equals("systempersistent")){
						killBrowser = true;
					}else if (key.equals("transparentnav")){
						killBrowser = true;
					}else if ((key.equals("reset")))
						killBrowser = true;

				}
			}
		};

		globalPref.registerOnSharedPreferenceChangeListener(changeListener);















		((Preference) findPreference("holodark"))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {

						Intent intent = getIntent();
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						finish();
						startActivity(intent);
						return false;
					}
				});

		((Preference) findPreference("reset"))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences.Editor ed = globalPref.edit();
						ed.clear();
						ed.commit();
						Toast.makeText(getApplicationContext(),
								(getResources().getText(R.string.complete)),
								Toast.LENGTH_LONG).show();
						return false;
					}
				});

		((Preference) findPreference("clearbrowsercache"))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						ApplicationInfo appInfo = getApplicationInfo();
						HelperMethods.clearBrowsingTrace("cache", SettingsV2.this);
						Toast.makeText(getApplicationContext(),
								(getResources().getText(R.string.complete)),
								Toast.LENGTH_LONG).show();
						return false;
					}
				});

		((Preference) findPreference("clearbrowserhistory"))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						ApplicationInfo appInfo = getApplicationInfo();
						HelperMethods.clearBrowsingTrace("history", SettingsV2.this);
						Toast.makeText(getApplicationContext(),
								(getResources().getText(R.string.complete)),
								Toast.LENGTH_LONG).show();
						return false;
					}
				});

		((Preference) findPreference("clearbrowsercookies"))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						ApplicationInfo appInfo = getApplicationInfo();
						HelperMethods.clearBrowsingTrace("cookies", SettingsV2.this);
						Toast.makeText(getApplicationContext(),
								(getResources().getText(R.string.complete)),
								Toast.LENGTH_LONG).show();
						return false;
					}
				});

		/*
		 * Customizable side bar
		 *
		 * 1. Remove sidebar color settings from settings_v2.xml Save it
		 * globally, so that it can be added later
		 *
		 * 2. Set OnPreferenceChangeListener to hide sidebarcolor and
		 * sidebartextcolor when sidebartheme = b or sidebartheme = w Add
		 * sidebar color settings when c (custom) is selected)
		 */

		PreferenceScreen preferenceScreen = getPreferenceScreen();
		sideColor = (ColorPickerPreference) preferenceScreen
				.findPreference("sidebarcolor");
		sideTextColor = (ColorPickerPreference) preferenceScreen
				.findPreference("sidebartextcolor");

		if (firstStart) {
			String sidebarTheme = globalPref.getString("sidebartheme", "b");
			if (!sidebarTheme.equals("c")) {


				((PreferenceGroup) findPreference("sideappearance"))
						.removePreference(sideColor);
				((PreferenceGroup) findPreference("sideappearance"))
						.removePreference(sideTextColor);

				firstStart = false;
			}
			;
		}
		;

		((Preference) findPreference("sidebartheme"))
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
													  Object newValue) {

						String sidebarTheme = (String) newValue;
						if (!newValue.equals("c")) {
							try {
								PreferenceScreen preferenceScreen = getPreferenceScreen();

								((PreferenceGroup) findPreference("sideappearance"))
										.removePreference(sideColor);
								((PreferenceGroup) findPreference("sideappearance"))
										.removePreference(sideTextColor);

							} catch (Exception e) {
								System.out
										.println("Sidebar color preferences already removed");
							}
						} else {
							PreferenceScreen preferenceScreen = getPreferenceScreen();

							ColorPickerPreference testSideColor = (ColorPickerPreference) preferenceScreen
									.findPreference("sidebarcolor");

							if (testSideColor == null) {

								((PreferenceGroup) findPreference("sideappearance"))
										.addPreference(sideColor);
								((PreferenceGroup) findPreference("sideappearance"))
										.addPreference(sideTextColor);
							}

						}
						;
						return true;
					}
				});

		((Preference)findPreference("import_bookmark_external")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(SettingsV2.this,ImportBookmarksActivity.class));
				return true;
			}
		});

		((Preference)findPreference("delete_bookmarks")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				manager = new BookmarksManager();
				manager.saveBookmarksManager(SettingsV2.this);
				BookmarksActivity.bookmarksMgr = manager;
				Tools.toastString(R.string.complete,SettingsV2.this);
				return true;
			}
		});

		((Preference)findPreference("bookmark_export")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {

				final EditText inputText = new EditText(SettingsV2.this);
				DateFormat df = new SimpleDateFormat("ddMMyyyyhhmm");
				inputText.setHint(getResources().getString(R.string.bookmarks)+df.format(new Date()).toString()+".txt");

				new AlertDialog.Builder(SettingsV2.this)
						.setTitle(R.string.backup_title)
						.setView(inputText)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								manager = BookmarksManager.loadBookmarksManager(SettingsV2.this);

								if (manager!=null) {
									try {
										File folderLoc = new File(Environment.getExternalStorageDirectory().getPath()+"/LucidBrowser/");
										folderLoc.mkdirs();
										String fileNameToWrite = inputText.getHint().toString();
										if (!inputText.getText().toString().equals(""))
											fileNameToWrite = inputText.getText().toString();

										PrintWriter printWriter = new PrintWriter(folderLoc.getPath()+"/"+fileNameToWrite);

										//add bookmarks from root first
										for (int i =0; i<manager.root.getContainedBookmarks().size(); i++){
											JSONObject obj = new JSONObject();
											//export root first
											obj.put("order",i);
											obj.put("folder","");
											obj.put("title", manager.root.getContainedBookmarks().get(i).getDisplayName());
											obj.put("url", manager.root.getContainedBookmarks().get(i).getURL());
											printWriter.write(obj.toString()+"\n");
										}

										for (int i=0; i<manager.root.getContainedFolders().size(); i++){
											for (int j = 0; j<manager.root.getContainedFolders().get(i).getContainedBookmarks().size(); j++){
												JSONObject obj = new JSONObject();
												//export root first
												obj.put("order",j);
												obj.put("folder",manager.root.getContainedFolders().get(i).getDisplayName());
												obj.put("title", manager.root.getContainedFolders().get(i).getContainedBookmarks().get(j).getDisplayName());
												obj.put("url", manager.root.getContainedFolders().get(i).getContainedBookmarks().get(j).getURL());
												printWriter.write(obj.toString()+"\n");
											}
										}

										printWriter.close();
										Tools.toastString(R.string.complete, SettingsV2.this);
										Tools.toastString(folderLoc.getPath()+"/"+fileNameToWrite,SettingsV2.this);
									} catch (Exception e) {
										e.printStackTrace();
										Tools.toastString(R.string.failed, SettingsV2.this);
									}
								}
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.show();
				return false;
			}
		});

		((Preference)findPreference("bookmark_import")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivityForResult(new Intent(SettingsV2.this, OpenFileActivity.class), ActivityIds.REQUEST_PICK_FILE);
				return true;
			}
		});



		if (Build.VERSION.SDK_INT >= 23) {
			//importing global bookmarks is not allowed in marshamallow +
			try {
				PreferenceScreen screen = (PreferenceScreen) findPreference("browsersettings_tools");
				screen.removePreference((Preference) findPreference("import_bookmark_external"));
			}catch(Exception e){
				e.printStackTrace();
			}
		}


		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			// Check if translucent is available
			PreferenceCategory mCategory = (PreferenceCategory) findPreference("mainsettings_appearance");
			int id = getResources().getIdentifier("config_enableTranslucentDecor",
					"bool", "android");
			if (id == 0) {
				try {
					//PreferenceScreen mCategory = (PreferenceScreen) findPreference("mainsettings");
					mCategory
							.removePreference(((Preference) findPreference("transparentnav")));
					mCategory
							.removePreference(((Preference) findPreference("transparentstatus")));
				} catch (Exception e) {
				}
				;
			}
		}

	}



	public void importBookmarksFromFile(String file){
		BookmarksManager manager = BookmarksManager.loadBookmarksManager(this);
		if (manager == null){
			Log.d("LB","BookmarksActivity.bookmarksMgr is null. Making new one");
			manager = new BookmarksManager();
		}


		final int FILE_JSON = 1;
		final int FILE_HTML = 2;
		int fileType = 0;
		boolean failed = false;

		if(new File(file).getName().endsWith(".html"))
			fileType = FILE_HTML;

		try {
			String currentFolderName = null;
			Scanner s = new Scanner(new File(file));
			while (s.hasNext()) {
				String line = s.nextLine();
				if (fileType == 0) {
					if (line.startsWith("{") && line.contains('"' + "url" + '"')) {
						fileType = FILE_JSON;
					} else {
						failed = true;
						break;
					}
				}

				if (fileType == FILE_JSON) {
					JSONObject object = new JSONObject(line);
					String url = object.getString("url");
					String title = object.getString("title");
					String folder = null;
					if (object.has("folder"))
						folder = object.getString("folder");
					Log.d("LL", object.getString("url"));
					Bookmark bookmark = new Bookmark(url, title);
					importBookmark(bookmark, folder, manager);
				}else if (fileType == FILE_HTML){
					//Log.d("LL", line);
					if (line.contains("<DT>") && line.contains("<H3")){//has a bookmark folder name
						String part = line.substring(line.indexOf("<H3"));
						String part2 = part.substring(part.indexOf(">")+1);
						currentFolderName = part2.substring(0,part2.indexOf("</H3"));
						Log.d("LL",currentFolderName);
					}else if (line.contains("<DT>") && line.contains("<A HREF=")){
						String part1 = line.substring(line.indexOf("<A HREF="+'"')+9);
						String url = part1.substring(0, part1.indexOf('"'));
						String title = part1.substring(part1.indexOf(">")+1,part1.indexOf("<"));
						Log.d("LL", title);
						Log.d("LL", url);
						if (title!=null && url!=null){
							Bookmark bookmark = new Bookmark();
							bookmark.setDisplayName(title);
							bookmark.setUrl(url);
							importBookmark(bookmark,currentFolderName, manager);
						}

					}

				}
			}

			if (!failed && fileType!=0)
				Tools.toastString(R.string.complete, this);
			else
				Tools.toastString(R.string.failed,this);

			manager.saveBookmarksManager(this);
			BookmarksActivity.bookmarksMgr = manager;
		}catch (Exception e){
			e.printStackTrace();
			Tools.toastString(R.string.failed,this);
		}
	}

	public void importBookmark(Bookmark bookmarkToAdd, String folderName, BookmarksManager manager){
		BookmarkFolder folder = null;
		if (folderName!=null && !folderName.equals("")){
			List<BookmarkFolder> folders = manager.root.getContainedFolders();
			for (int i = 0; i<folders.size(); i++){
				if (folders.get(i).getDisplayName().equals(folderName)) {
					folder = folders.get(i);
					break;
				}
			}
			if (folder == null) {
				BookmarkFolder newFolder = new BookmarkFolder(folderName);
				manager.root.addFolder(newFolder);
				folder = newFolder;
			}
		}

		boolean bookmarkAlreadyExists = false;
		if (folder!=null){
			for (Bookmark b: folder.getAllBookMarks()){
				if (b.getUrl().equals(bookmarkToAdd.getUrl())){
					bookmarkAlreadyExists = true;
					break;
				}
			}

			if (!bookmarkAlreadyExists) {
				Log.d("LL", "Adding book to folder:" + bookmarkToAdd.getDisplayName());
				folder.addBookmark(bookmarkToAdd);
			}
		}else {
			for (Bookmark b: manager.root.getAllBookMarks()){
				if (b.getUrl().equals(bookmarkToAdd.getUrl())){
					bookmarkAlreadyExists = true;
					break;
				}
			}

			if (!bookmarkAlreadyExists)
				manager.root.addBookmark(bookmarkToAdd);
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case ActivityIds.REQUEST_PICK_FILE:
					String fileString = data.getStringExtra("file");
					importBookmarksFromFile(fileString);
					break;
			}
		}
	}


	@Override
	public void finish() {
		Intent i = new Intent();
		i.putExtra("restart",killBrowser);
		setResult(RESULT_OK,i);

		super.finish();
	}



}