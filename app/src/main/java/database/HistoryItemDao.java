package database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryItemDao {
    @Query("SELECT * FROM historyitem")
    List<HistoryItem> getAll();

    @Query("SELECT * FROM historyitem WHERE uid IN (:userIds)")
    List<HistoryItem> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM historyitem WHERE title LIKE :title AND " +
           "url LIKE :url LIMIT 1")
    HistoryItem find(String title, String url);

    @Query("SELECT * FROM historyitem WHERE url LIKE :url LIMIT 1")
    HistoryItem findByURL(String url);

    @Insert
    void insertAll(HistoryItem... historyItems);

    @Delete
    void delete(HistoryItem historyItem);

    @Query("DELETE FROM historyitem")
    void deleteAll();
}