package com.profimedica.wordlex;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Cumpanasu on 11/23/2015.
 */
    public class WordReaderDbHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + WordReaderContract.WordEntry.TABLE_NAME + " (" +
        WordReaderContract.WordEntry.COLUMN_NAME_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," +
        WordReaderContract.WordEntry.COLUMN_NAME_NATIVE + TEXT_TYPE + COMMA_SEP +
        WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN + TEXT_TYPE + COMMA_SEP +
                WordReaderContract.WordEntry.COLUMN_NAME_GOOD + INT_TYPE + COMMA_SEP +
                WordReaderContract.WordEntry.COLUMN_NAME_BAD + INT_TYPE + COMMA_SEP +
                WordReaderContract.WordEntry.COLUMN_NAME_FGOOD + INT_TYPE + COMMA_SEP +
                WordReaderContract.WordEntry.COLUMN_NAME_FBAD + INT_TYPE + COMMA_SEP +
                WordReaderContract.WordEntry.COLUMN_NAME_SPENT + INT_TYPE + COMMA_SEP +
                WordReaderContract.WordEntry.COLUMN_NAME_FSPENT + INT_TYPE + COMMA_SEP +
                WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY + TEXT_TYPE +
        " )";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + WordReaderContract.WordEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "lex.db";

    public WordReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}