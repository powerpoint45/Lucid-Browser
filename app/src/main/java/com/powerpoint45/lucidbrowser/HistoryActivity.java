package com.powerpoint45.lucidbrowser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.powerpoint45.lucidbrowser.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import database.AppDatabase;
import database.HistoryItem;

public class HistoryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<HistoryItem> historyList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Properties.appProp.darkTheme)
            setTheme(R.style.BookmarksThemeDark);
        else
            setTheme(R.style.BookmarksThemeLight);

        setContentView(R.layout.history_activity);
        recyclerView = findViewById(R.id.history_rv);

        Toolbar tb = findViewById(R.id.h_toolbar);
        setSupportActionBar(tb);
        tb.setTitleTextColor(Color.WHITE);
        tb.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        setActionBarTitle();

        new Thread(new Runnable() {
            @Override
            public void run() {
                historyList = AppDatabase.getDb(getApplicationContext()).historyItemDao().getAll();
                Collections.reverse(historyList);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
                        recyclerView.setAdapter(new CustomAdapter());
                    }
                });

            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history_actionbar, menu);

        // Find the menu items
        MenuItem pauseItem = menu.findItem(R.id.pause_history);
        MenuItem resumeItem = menu.findItem(R.id.resume_history);

        // Assume your logic here to determine whether to show pause or resume
        boolean isHistoryPaused = isHistoryPaused(); // Change this based on your logic

        if (!isHistoryPaused) {
            // Show the pause icon and hide the resume icon
            pauseItem.setVisible(true);
            resumeItem.setVisible(false);
        } else {
            // Show the resume icon and hide the pause icon
            pauseItem.setVisible(false);
            resumeItem.setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.pause_history:
                saveHistoryState(true);
                setActionBarTitle();
                invalidateOptionsMenu();
                return true;
            case R.id.resume_history:
                saveHistoryState(false);
                setActionBarTitle();
                invalidateOptionsMenu();
                return true;
            case R.id.clearhistory:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase.getDb(getApplicationContext()).historyItemDao().deleteAll();
                    }
                }).start();

                historyList.clear();
                recyclerView.getAdapter().notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }






    private boolean isHistoryPaused() {
        // Retrieve history state from SharedPreferences
        SharedPreferences sharedPreferences  =  PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("history_paused", false); // Default to false
    }

    private void saveHistoryState(boolean isPaused) {
        SharedPreferences sharedPreferences  =  PreferenceManager.getDefaultSharedPreferences(this);
        Properties.appProp.historyPaused = isPaused;
        // Save history state to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("history_paused", isPaused);
        editor.apply();
    }

    private void setActionBarTitle() {
        // Set ActionBar title based on history state
        if (isHistoryPaused()) {
            getSupportActionBar().setTitle(R.string.history_paused);
        } else {
            getSupportActionBar().setTitle(R.string.history_active);
        }
    }







    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder)
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView historyTitle;
            private final TextView historyUrl;

            public RelativeTimeTextView getHistoryTime() {
                return historyTime;
            }

            private final RelativeTimeTextView historyTime;


            private final ImageView icon;

            public ViewHolder(View view) {
                super(view);
                // Define click listener for the ViewHolder's View
                historyUrl = view.findViewById(R.id.history_url_title);
                historyTitle = view.findViewById(R.id.history_title);
                historyTime = view.findViewById(R.id.history_time);
                icon = view.findViewById(R.id.history_icon);
            }

            public TextView getHistoryTitle() {
                return historyTitle;
            }
            public TextView getHistoryUrl() {
                return historyUrl;
            }
            public ImageView getIcon() {
                return icon;
            }
        }


        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.history_item, viewGroup, false);

            return new ViewHolder(view);
        }


        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.getHistoryTitle().setText(historyList.get(position).title);
            viewHolder.getHistoryUrl().setText(historyList.get(position).url);
            viewHolder.getHistoryTime().setReferenceTime(Tools.getDateLong(historyList.get(position).time));
            viewHolder.itemView.setTag(historyList.get(position));
            viewHolder.itemView.setOnClickListener(onClickListener);
            viewHolder.itemView.setOnLongClickListener(onLongClickListener);

            if (Properties.appProp.darkTheme) {
                viewHolder.getHistoryUrl().setTextColor(Color.WHITE);
                viewHolder.getHistoryTitle().setTextColor(Color.WHITE);
                viewHolder.getHistoryTime().setTextColor(Color.WHITE);
            }


            try {
                URL curURL = new URL(historyList.get(position).url);
                Glide.with(HistoryActivity.this).asDrawable().load(curURL.getProtocol() + "://" + curURL.getHost() + "/favicon.ico").into(viewHolder.icon);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }




//            if (!Properties.appProp.darkTheme)
//                viewHolder.getIcon().setColorFilter(Color.BLACK);

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return historyList.size();
        }

        Dialog longPressDialog;

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryItem item = (HistoryItem) v.getTag();
                Intent result = new Intent();
                result.putExtra("url",item.url);
                result.putExtra("newtab",false);
                setResult(RESULT_OK,result);
                finish();
            }
        };

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                HistoryItem item = (HistoryItem) v.getTag();
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);

                builder.setTitle(item.title);

                ListView modeList = new ListView(HistoryActivity.this);
                String[] stringArray = new String[] {  getResources().getString(R.string.openinnewtab), getResources().getString(R.string.remove)};
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(HistoryActivity.this, android.R.layout.simple_list_item_1,android.R.id.text1, stringArray);
                modeList.setAdapter(modeAdapter);
                modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @SuppressWarnings("deprecation")
                    @SuppressLint({"InflateParams", "SetTextI18n"})
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int dialogPos, long arg3) {
                        switch (dialogPos){
                            case 0:   //new tab
                                Intent intent = new Intent();
                                intent.putExtra("url",item.url);
                                setResult(RESULT_OK,intent);
                                finish();
                                break;
                            case 1:   //remove
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AppDatabase.getDb(getApplicationContext()).historyItemDao().delete(item);
                                    }
                                }).start();

                                int index = historyList.indexOf(item);
                                historyList.remove(item);
                                recyclerView.getAdapter().notifyItemRemoved(index);

                                break;
                        }

                        longPressDialog.dismiss();
                    }
                });

                builder.setView(modeList);
                longPressDialog = builder.create();
                longPressDialog.show();
                System.out.println("LONG PRESSED");
                return true;
            }
        };
    }

}
