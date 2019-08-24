package com.jonahbauer.qed.database;

import com.jonahbauer.qed.qeddb.person.Person;

import java.util.List;

@Deprecated
public interface PersonsDatabaseReceiver {
    void onReceiveResult(List<Person> persons);
    void onDatabaseError();

    void onInsertAllUpdate(int done, int total);
}
