package com.maple.rubbishseparator.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class DatabaseDAO {
    private static DatabaseDAO INSTANCE = null;
    private DBHelper mHelper;
    Context context;
    private static SQLiteDatabase mDB = null;

    private DatabaseDAO(Context context) {
        this.context = context;
        mHelper = new DBHelper(context, DBHelper.databaseName, null, DBHelper.DB_VERSION);
        mDB = mHelper.getWritableDatabase();
    }

    public static DatabaseDAO getInstance(Context context) {
        if (INSTANCE == null) {
            return new DatabaseDAO(context);
        }
        return INSTANCE;
    }

    //增添历史记录
    public void updateHistory(String history) {
        ContentValues content = new ContentValues();
        content.put("content", history);
        mDB.insert(DBHelper.TABLE_NAME_HISTORY, null, content);
    }

    //查找历史记录
    public ArrayList<String> searchHistory() {
        ArrayList<String> history = new ArrayList<String>();
        String sql;
        String[] info = new String[0];
        sql = "select * from user_search_history";
        Cursor cursor = mDB.rawQuery(sql, info);
        while (cursor.moveToNext()) {
            history.add(cursor.getString(cursor.getColumnIndex("content")));
        }
        cursor.close();
        return history;
    }


    //删除历史记录
    public void deleteHistory() {
        String[] delete_data = new String[0];
            mDB.delete(DBHelper.TABLE_NAME_HISTORY, "", delete_data);
    }

}
