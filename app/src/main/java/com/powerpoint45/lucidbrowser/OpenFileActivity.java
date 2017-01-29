package com.powerpoint45.lucidbrowser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

/**
 * Created by michael on 20/01/17.
 */

public class OpenFileActivity extends AppCompatActivity {

    SharedPreferences globalPref;
    boolean useDark;
    LayoutInflater inflater;
    BaseAdapter adapter;
    File[] files;
    File folder;
    Typeface tf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        globalPref = PreferenceManager
                .getDefaultSharedPreferences(this);

        useDark = globalPref.getBoolean("holodark", false);

        if (!useDark){
            setTheme(R.style.NewAppThemeLight);
        }

        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        final ListView listView = new ListView(this);
        folder = new File(Environment.getExternalStorageDirectory().getPath());
        files = folder.listFiles();
        adapter = new BaseAdapter() {

            @Override
            public int getCount() {
                if (files==null)
                    return 0;
                files = folder.listFiles();
                setTitle(folder.getPath());
                Arrays.sort(files);
                return files.length;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            class Views{
                ImageView imageView;
                TextView textView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Views views;
                if (convertView == null){
                    convertView = inflater.inflate(R.layout.file_item,null);
                    views = new Views();
                    views.imageView = (ImageView) convertView.findViewById(R.id.favorites_icon);
                    views.textView = (TextView) convertView.findViewById(R.id.favorites_text);
                    convertView.setTag(views);
                }else
                    views = (Views)convertView.getTag();


                views.textView.setText(files[position].getName());
                if (tf == null)
                    tf = views.textView.getTypeface();

                if (!views.textView.getTypeface().equals(tf))
                    views.textView.setTypeface(tf);

                if (files[position].isDirectory()){
                    if (files[position].getName().toString().equals("LucidBrowser"))
                        views.textView.setTypeface(null,Typeface.BOLD);
                    views.imageView.setImageResource(R.drawable.ic_action_collection);
                }else{
                    views.imageView.setImageResource(R.drawable.ic_insert_drive_file);
                }

                return convertView;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (files[position].canRead()) {
                    if (files[position].isDirectory()) {
                        folder = files[position];
                        files = files[position].listFiles();
                        adapter.notifyDataSetChanged();
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("file", files[position].getPath());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });

        setContentView(listView);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //do your own thing here
                finish();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        if (folder.getPath().equals(new File(Environment.getExternalStorageDirectory().getPath()))
                || folder.getPath().equals("/")) {
            Log.d("LL","finishing");
            finish();
        }else if (folder.getParentFile().canRead()){
            Log.d("LL","notify");
            folder = folder.getParentFile();
            files = folder.listFiles();
            adapter.notifyDataSetChanged();
        }else
            finish();
    }


}
