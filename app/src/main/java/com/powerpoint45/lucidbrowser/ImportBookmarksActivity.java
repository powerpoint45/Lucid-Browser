package com.powerpoint45.lucidbrowser;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.Browser;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import bookmarkModel.Bookmark;
import bookmarkModel.BookmarksManager;

/**
 * Created by michael on 17/11/16.
 */

public class ImportBookmarksActivity extends AppCompatActivity {

    private class BookmarkItem{
        String title;
        String url;
        Bitmap favicon;
        boolean selected;
    }

    ArrayList<BookmarkItem> bookmarkItems;
    LayoutInflater inflater;
    SharedPreferences globalPref;

    BookmarksManager manager;
    boolean useDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        globalPref = PreferenceManager
                .getDefaultSharedPreferences(ImportBookmarksActivity.this);

        useDark = globalPref.getBoolean("holodark", false);

        if (!useDark){
            setTheme(R.style.NewAppThemeLight);
        }

        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        bookmarkItems = new ArrayList<>();
        setContentView(R.layout.import_bookmarks_activity);

        ListView bookmarksLV = (ListView)findViewById(R.id.import_bookmarks_lv);
        bookmarksLV.setAdapter(bookmarksAdapter);

        bookmarksLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bookmarkItems.get(position).selected=! bookmarkItems.get(position).selected;
                bookmarksAdapter.notifyDataSetChanged();
            }
        });

        manager = BookmarksManager.loadBookmarksManager(ImportBookmarksActivity.this);
        if (manager == null){
            Log.d("LB","BookmarksActivity.bookmarksMgr is null. Making new one");
            manager = new BookmarksManager();
        }


        Uri[] uris = new Uri[]
                {
                        Uri.parse("content://com.android.chrome.browser/bookmarks"),
                        BOOKMARKS_URI
                };

        String[] proj = new String[]
                {
                        android.provider.BaseColumns._ID,
                        BookmarkColumns.URL,
                        BookmarkColumns.TITLE,
                        BookmarkColumns.FAVICON
                };

        for (int uriIndex = 0; uriIndex<2; uriIndex++) {
            Cursor results;
            results = managedQuery(uris[uriIndex], proj,
                    BookmarkColumns.BOOKMARK, null, null);
            if (results != null) {
                int urlColumn = results.getColumnIndex(BookmarkColumns.URL);
                int titleColumn = results.getColumnIndex(BookmarkColumns.TITLE);
                int faviconColumn = results.getColumnIndex(BookmarkColumns.FAVICON);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                if (results.moveToFirst()) {
                    do {
                        if (!isDuplicate(results.getString(urlColumn))) {
                            if (results.getString(urlColumn) != null && manager.root.containsBookmarkDeep(results.getString(urlColumn)) == null) {
                                byte[] blob = results.getBlob(faviconColumn);
                                BookmarkItem item = new BookmarkItem();
                                item.selected = true;
                                item.title = results.getString(titleColumn);
                                item.url = results.getString(urlColumn);
                                if (blob != null)
                                    item.favicon = BitmapFactory.decodeByteArray(blob, 0, blob.length, options);

                                bookmarkItems.add(item);
                                bookmarksAdapter.notifyDataSetChanged();
                            }
                        }
                    } while (results.moveToNext());
                }
            }
        }

        if (bookmarkItems.size()==0){
            TextView noBooksTV = new TextView(this);
            noBooksTV.setText(R.string.no_bookmarks);
            noBooksTV.setPadding(Properties.numtodp(10, this), Properties.numtodp(10, this)
                    , Properties.numtodp(10, this), Properties.numtodp(10, this));
            setContentView(noBooksTV);
        }

    }

    BaseAdapter bookmarksAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            if (bookmarkItems!=null)
                return bookmarkItems.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null)
                convertView = inflater.inflate(R.layout.bookmark_item_with_checkbox,null);

            TextView urlText = (TextView) convertView.findViewById(R.id.bookmark_url_title);
            TextView title = (TextView) convertView.findViewById(R.id.bookmark_title);
            ImageView icon = (ImageView) convertView.findViewById(R.id.bookmark_icon);
            CheckBox selector = (CheckBox) convertView.findViewById(R.id.bookmark_check);

            if (useDark) {
                title.setTextColor(Color.WHITE);
                urlText.setTextColor(Color.WHITE);
            }else {
                title.setTextColor(Color.BLACK);
                urlText.setTextColor(Color.BLACK);
            }

            title.setText(bookmarkItems.get(position).title);
            urlText.setText(bookmarkItems.get(position).url);

            selector.setChecked(bookmarkItems.get(position).selected);

            if (bookmarkItems.get(position).favicon!=null)
                icon.setImageBitmap(bookmarkItems.get(position).favicon);
            else
                icon.setImageResource(R.drawable.ic_browser);

            return convertView;
        }
    };

    public void importClicked(View v){
        if (manager!=null) {
            for (int bookmarkIndex = 0; bookmarkIndex < bookmarkItems.size(); bookmarkIndex++) {
                if (bookmarkItems.get(bookmarkIndex).selected) {
                    Bookmark bookmarkToAdd = new Bookmark(bookmarkItems.get(bookmarkIndex).url,
                            bookmarkItems.get(bookmarkIndex).title);
                    bookmarkToAdd.setFavIcon(ImportBookmarksActivity.this, bookmarkItems.get(bookmarkIndex).favicon);
                    manager.root.addBookmark(bookmarkToAdd);
                }
            }
            manager.saveBookmarksManager(ImportBookmarksActivity.this);
            finish();
        }
    }

    private boolean isDuplicate(String url){
        for (int i =0; i<bookmarkItems.size(); i++){
            if (bookmarkItems.get(i).url.contains(url))
                return true;
        }

        return false;
    }

    public static final Uri BOOKMARKS_URI =
            Uri.parse("content://browser/bookmarks");

    private static class BookmarkColumns implements BaseColumns {
        public static final String URL = "url";
        public static final String VISITS = "visits";
        public static final String DATE = "date";
        public static final String BOOKMARK = "bookmark";
        public static final String TITLE = "title";
        public static final String CREATED = "created";
        public static final String FAVICON = "favicon";

        public static final String THUMBNAIL = "thumbnail";

        public static final String TOUCH_ICON = "touch_icon";

        public static final String USER_ENTERED = "user_entered";
    }

}
