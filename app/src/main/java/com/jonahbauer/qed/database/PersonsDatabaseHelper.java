package com.jonahbauer.qed.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry;

@Deprecated
class PersonsDatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "persons.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PersonsEntry.TABLE_NAME + " (" +
                    PersonsEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    PersonsEntry.COLUMN_NAME_FIRST_NAME + " TEXT," +
                    PersonsEntry.COLUMN_NAME_LAST_NAME + " TEXT," +
                    PersonsEntry.COLUMN_NAME_ADDRESS + " TEXT," +
                    PersonsEntry.COLUMN_NAME_BIRTHDAY + " DATETIME," +
                    PersonsEntry.COLUMN_NAME_EMAIL + " TEXT," +
                    PersonsEntry.COLUMN_NAME_MEMBER + " INTEGER," +
                    PersonsEntry.COLUMN_NAME_ACTIVE + " INTEGER," +
                    PersonsEntry.COLUMN_NAME_MEMBER_SINCE + " DATETIME)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PersonsEntry.TABLE_NAME;

    PersonsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}

