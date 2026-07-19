package database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class HistoryItemDao_Impl implements HistoryItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HistoryItem> __insertionAdapterOfHistoryItem;

  private final EntityDeletionOrUpdateAdapter<HistoryItem> __deletionAdapterOfHistoryItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public HistoryItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHistoryItem = new EntityInsertionAdapter<HistoryItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `HistoryItem` (`uid`,`title`,`url`,`time`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final HistoryItem entity) {
        statement.bindLong(1, entity.uid);
        if (entity.title == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.title);
        }
        if (entity.url == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.url);
        }
        if (entity.time == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.time);
        }
      }
    };
    this.__deletionAdapterOfHistoryItem = new EntityDeletionOrUpdateAdapter<HistoryItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `HistoryItem` WHERE `uid` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final HistoryItem entity) {
        statement.bindLong(1, entity.uid);
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM historyitem";
        return _query;
      }
    };
  }

  @Override
  public void insertAll(final HistoryItem... historyItems) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfHistoryItem.insert(historyItems);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final HistoryItem historyItem) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfHistoryItem.handle(historyItem);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public List<HistoryItem> getAll() {
    final String _sql = "SELECT * FROM historyitem";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUid = CursorUtil.getColumnIndexOrThrow(_cursor, "uid");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
      final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
      final List<HistoryItem> _result = new ArrayList<HistoryItem>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final HistoryItem _item;
        _item = new HistoryItem();
        _item.uid = _cursor.getInt(_cursorIndexOfUid);
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _item.title = null;
        } else {
          _item.title = _cursor.getString(_cursorIndexOfTitle);
        }
        if (_cursor.isNull(_cursorIndexOfUrl)) {
          _item.url = null;
        } else {
          _item.url = _cursor.getString(_cursorIndexOfUrl);
        }
        if (_cursor.isNull(_cursorIndexOfTime)) {
          _item.time = null;
        } else {
          _item.time = _cursor.getString(_cursorIndexOfTime);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<HistoryItem> loadAllByIds(final int[] userIds) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM historyitem WHERE uid IN (");
    final int _inputSize = userIds == null ? 1 : userIds.length;
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    if (userIds == null) {
      _statement.bindNull(_argIndex);
    } else {
      for (int _item : userIds) {
        _statement.bindLong(_argIndex, _item);
        _argIndex++;
      }
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUid = CursorUtil.getColumnIndexOrThrow(_cursor, "uid");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
      final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
      final List<HistoryItem> _result = new ArrayList<HistoryItem>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final HistoryItem _item_1;
        _item_1 = new HistoryItem();
        _item_1.uid = _cursor.getInt(_cursorIndexOfUid);
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _item_1.title = null;
        } else {
          _item_1.title = _cursor.getString(_cursorIndexOfTitle);
        }
        if (_cursor.isNull(_cursorIndexOfUrl)) {
          _item_1.url = null;
        } else {
          _item_1.url = _cursor.getString(_cursorIndexOfUrl);
        }
        if (_cursor.isNull(_cursorIndexOfTime)) {
          _item_1.time = null;
        } else {
          _item_1.time = _cursor.getString(_cursorIndexOfTime);
        }
        _result.add(_item_1);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public HistoryItem find(final String title, final String url) {
    final String _sql = "SELECT * FROM historyitem WHERE title LIKE ? AND url LIKE ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (title == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, title);
    }
    _argIndex = 2;
    if (url == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, url);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUid = CursorUtil.getColumnIndexOrThrow(_cursor, "uid");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
      final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
      final HistoryItem _result;
      if (_cursor.moveToFirst()) {
        _result = new HistoryItem();
        _result.uid = _cursor.getInt(_cursorIndexOfUid);
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _result.title = null;
        } else {
          _result.title = _cursor.getString(_cursorIndexOfTitle);
        }
        if (_cursor.isNull(_cursorIndexOfUrl)) {
          _result.url = null;
        } else {
          _result.url = _cursor.getString(_cursorIndexOfUrl);
        }
        if (_cursor.isNull(_cursorIndexOfTime)) {
          _result.time = null;
        } else {
          _result.time = _cursor.getString(_cursorIndexOfTime);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public HistoryItem findByURL(final String url) {
    final String _sql = "SELECT * FROM historyitem WHERE url LIKE ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (url == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, url);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUid = CursorUtil.getColumnIndexOrThrow(_cursor, "uid");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
      final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
      final HistoryItem _result;
      if (_cursor.moveToFirst()) {
        _result = new HistoryItem();
        _result.uid = _cursor.getInt(_cursorIndexOfUid);
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _result.title = null;
        } else {
          _result.title = _cursor.getString(_cursorIndexOfTitle);
        }
        if (_cursor.isNull(_cursorIndexOfUrl)) {
          _result.url = null;
        } else {
          _result.url = _cursor.getString(_cursorIndexOfUrl);
        }
        if (_cursor.isNull(_cursorIndexOfTime)) {
          _result.time = null;
        } else {
          _result.time = _cursor.getString(_cursorIndexOfTime);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
