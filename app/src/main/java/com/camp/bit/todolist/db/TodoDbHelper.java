package com.camp.bit.todolist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public class TodoDbHelper extends SQLiteOpenHelper {

    // TODO 定义数据库名、版本；创建数据库

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TodoContract.TodoEntry.TABLE_NAME + " (" +
                    TodoContract.TodoEntry._ID + " INTEGER PRIMARY KEY," +
                    TodoContract.TodoEntry.COLUMN_DATE + " INTEGER," +
                    TodoContract.TodoEntry.COLUMN_STATE + " INTEGER," +
                    TodoContract.TodoEntry.COLUMN_CONTENT + " TEXT," +
                    TodoContract.TodoEntry.COLUMN_PRIORITY + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TodoContract.TodoEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "todo.db";

    public TodoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String upgrade = "";
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {
                case 1:
                    upgrade = "ALTER TABLE " + TodoContract.TodoEntry.TABLE_NAME +
                            " ADD " + TodoContract.TodoEntry.COLUMN_PRIORITY + " INTEGER NOT null DEFAULT 0";
                    db.execSQL(upgrade);
                    break;
                default:
                    break;
            }
        }
    }

}
