package com.jonahbauer.qed.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.jonahbauer.qed.qeddb.person.Person;

import java.util.ArrayList;
import java.util.List;

import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_ACTIVE;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_BIRTHDAY;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_EMAIL;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_FIRST_NAME;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_ID;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_LAST_NAME;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_MEMBER;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_MEMBER_SINCE;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.TABLE_NAME;

@Deprecated
public class PersonsDatabase {
    private PersonsDatabaseHelper personsDatabaseHelper;
    private int insertsRunning = 0;
    private PersonsDatabaseReceiver receiver;
    private List<PersonsDatabaseAsync> asyncTasks;

    public void init(Context context, PersonsDatabaseReceiver receiver) {
        personsDatabaseHelper = new PersonsDatabaseHelper(context);
        asyncTasks = new ArrayList<>();
        this.receiver = receiver;
    }

    public void close() {
        if (personsDatabaseHelper != null) personsDatabaseHelper.close();
        asyncTasks.forEach(async -> {
            if (!async.isCancelled()) async.cancel(true);
        });
    }

    @SuppressWarnings("UnusedReturnValue")
    public long insert(Person person) {
        insertsRunning ++;
        ContentValues value = new ContentValues();
        value.put(COLUMN_NAME_ID, person.id);
        value.put(COLUMN_NAME_FIRST_NAME, person.firstName);
        value.put(COLUMN_NAME_LAST_NAME, person.lastName);
        value.put(COLUMN_NAME_BIRTHDAY, person.birthday);
        value.put(COLUMN_NAME_EMAIL, person.email);
        value.put(COLUMN_NAME_MEMBER, person.member);
        value.put(COLUMN_NAME_ACTIVE, person.active);
        value.put(COLUMN_NAME_MEMBER_SINCE, person.memberSince);

        long row = -1;

        SQLiteDatabase chatLogWritable = personsDatabaseHelper.getWritableDatabase();

        try {
            row = chatLogWritable.insertOrThrow(TABLE_NAME, null, value);
        } catch (SQLiteConstraintException ignored) {}

        insertsRunning --;
        if (insertsRunning == 0) chatLogWritable.close();
        return row;
    }

    public void insertAll(List<Person> persons) {
        PersonsDatabaseAsync async = new PersonsDatabaseAsync();
        asyncTasks.add(async);
        async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, PersonsDatabaseAsync.Mode.INSERTALL, personsDatabaseHelper, persons, receiver);
    }

    public void query(String sql, String[] selectionArgs) {
        PersonsDatabaseAsync async = new PersonsDatabaseAsync();
        asyncTasks.add(async);
        async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, PersonsDatabaseAsync.Mode.QUERY, personsDatabaseHelper, receiver, sql, selectionArgs);
    }
}

