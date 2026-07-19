package database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.powerpoint45.lucidbrowser.Tools;

@Database(entities = {HistoryItem.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HistoryItemDao historyItemDao();
    static AppDatabase db;

    public static AppDatabase getDb(Context appContext){
        if (db == null){
            db = Room.databaseBuilder(appContext, AppDatabase.class, "history-db").fallbackToDestructiveMigration().build();
        }

        return db;
    }

    public void addURL(String url, String title){
        //ignore adding history for following pages
        if (url.startsWith("file:///android_asset/") || url.startsWith("about:blank"))
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                HistoryItemDao historyItemDao = db.historyItemDao();
                HistoryItem historyItem = historyItemDao.findByURL(url);

                //Delete existing reference to url and update to bottom of list
                if (historyItem!=null){
                    historyItemDao.delete(historyItem);
                }

                historyItem = new HistoryItem();
                historyItem.url=url;
                historyItem.title = title;
                historyItem.time = Tools.getTime();
                historyItemDao.insertAll(historyItem);

            }
        }).start();
    }

}