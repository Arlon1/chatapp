package com.jonahbauer.qed.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.jonahbauer.qed.qeddb.person.Person;

import java.util.ArrayList;
import java.util.List;

import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_ACTIVE;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_BIRTHDAY;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_FIRST_NAME;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_ID;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_LAST_NAME;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_MEMBER;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.COLUMN_NAME_MEMBER_SINCE;
import static com.jonahbauer.qed.database.PersonsDatabaseContract.PersonsEntry.TABLE_NAME;

@Deprecated
public class PersonsDatabaseAsync extends AsyncTask<Object, Integer, Boolean> {
    private static int insertsRunning = 0;
    private PersonsDatabaseHelper databaseHelper;
    private Mode mode;
    private PersonsDatabaseReceiver receiver;

    @Override
    protected Boolean doInBackground(Object... objects) {
        if (objects.length < 2) return false;
        else {
            if (objects[0] instanceof Mode) mode = (Mode) objects[0];
            else return false;

            if (objects[1] instanceof PersonsDatabaseHelper) databaseHelper = (PersonsDatabaseHelper) objects[1];
            else return false;
        }

        switch (mode) {
            case QUERY:
                if (objects.length < 4) return false;
                else {
                    String query;
                    String[] args;

                    if (objects[2] instanceof PersonsDatabaseReceiver) receiver = (PersonsDatabaseReceiver) objects[2];
                    else return false;

                    if (objects[3] instanceof String) query = (String) objects[3];
                    else return false;

                    if (objects.length >= 5) {
                        if (objects[4] != null) {
                            if (objects[4] instanceof String[]) args = (String[]) objects[4];
                            else args = null;
                        } else args = null;
                    } else args = null;

                    query(receiver, query, args);
                    return true;
                }
            case INSERTALL:
                if (objects.length < 3) return false;
                else {
                    List persons;

                    if (objects[2] instanceof List) persons = (List) objects[2];
                    else return false;

                    if (objects.length >= 4) if (objects[3] instanceof PersonsDatabaseReceiver) receiver = (PersonsDatabaseReceiver) objects[3];

                    //noinspection unchecked
                    insertAll((List<Person>) persons);
                    return true;
                }
        }
        return true;
    }

    private void query(PersonsDatabaseReceiver receiver, String query, String[] args) {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        Cursor cursor = database.rawQuery(query, args);

        List<Person> persons = new ArrayList<>();

        while (cursor.moveToNext()) {
//            persons.add(new Person(
//                    cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)),
//                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME_FIRST_NAME)),
//                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LAST_NAME)),
//                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ADDRESS)),
//                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME_BIRTHDAY)),
//                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME_EMAIL)),
//                    cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_MEMBER)) == 1,
//                    cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ACTIVE)) == 1,
//                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME_MEMBER_SINCE))
//            ));
        }

        cursor.close();
        database.close();

        receiver.onReceiveResult(persons);
    }

    private void insertAll(List<Person> persons) {
        insertsRunning ++;
        SQLiteDatabase personsWritable = databaseHelper.getWritableDatabase();

        List<Person> donePersons = new ArrayList<>();

        persons.stream().parallel().forEach(person -> {
            ContentValues value = new ContentValues();
            value.put(COLUMN_NAME_ID, person.id);
            value.put(COLUMN_NAME_FIRST_NAME, person.firstName);
            value.put(COLUMN_NAME_LAST_NAME, person.lastName);
            value.put(COLUMN_NAME_BIRTHDAY, person.birthday);
            value.put(COLUMN_NAME_MEMBER, person.member);
            value.put(COLUMN_NAME_ACTIVE, person.active);
            value.put(COLUMN_NAME_MEMBER_SINCE, person.memberSince);

            try {
                personsWritable.insertOrThrow(TABLE_NAME, null, value);
            } catch (SQLiteConstraintException ignored) {}

            donePersons.add(person);

            publishProgress(donePersons.size(), persons.size());
        });

        insertsRunning --;
        if (insertsRunning == 0) personsWritable.close();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (!success && (mode == Mode.QUERY) && (receiver != null)) receiver.onDatabaseError();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mode == Mode.INSERTALL && (receiver != null)) receiver.onInsertAllUpdate(values[0], values[1]);
    }

    enum Mode {
        QUERY, INSERTALL
    }
}
