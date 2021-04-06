package com.maple.rubbishseparator.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    final static String databaseName = "MapleRubbish";
    static final int DB_VERSION = 1;//数据库版本

    //数据库用户表的名称及相关参数
    final static String TABLE_NAME_HISTORY = "user_search_history";


    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //初始化数据表，可以再这里面对多个表进行处理
        String sql = "create table " + TABLE_NAME_HISTORY + "(content text)";
        db.execSQL(sql);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
