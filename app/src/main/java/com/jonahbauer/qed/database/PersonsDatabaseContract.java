package com.jonahbauer.qed.database;

import android.provider.BaseColumns;

@Deprecated
public final class PersonsDatabaseContract {
    private PersonsDatabaseContract() {}

    public static class PersonsEntry implements BaseColumns {
        public static final String TABLE_NAME = "persons";
        public static final String COLUMN_NAME_ID = "userId";
        public static final String COLUMN_NAME_FIRST_NAME = "firstName";
        public static final String COLUMN_NAME_LAST_NAME = "lastName";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_BIRTHDAY = "birthday";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_MEMBER = "member";
        public static final String COLUMN_NAME_ACTIVE = "active";
        public static final String COLUMN_NAME_MEMBER_SINCE = "memberSince";
    }
}
