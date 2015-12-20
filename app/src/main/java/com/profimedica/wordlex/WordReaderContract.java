package com.profimedica.wordlex;

import android.provider.BaseColumns;

/**
 * Created by Cumpanasu on 11/23/2015.
 */
public final class WordReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public WordReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class WordEntry implements BaseColumns {
        public static final String TABLE_NAME = "`word`";
        public static final String COLUMN_NAME_ENTRY_ID = "`id`";
        public static final String COLUMN_NAME_NATIVE = "`native`";
        public static final String COLUMN_NAME_FOREIGN = "`foreign`";
        public static final String COLUMN_NAME_GOOD = "`good`";
        public static final String COLUMN_NAME_BAD = "`bad`";
        public static final String COLUMN_NAME_FGOOD = "`fgood`";
        public static final String COLUMN_NAME_FBAD = "`fbad`";
        public static final String COLUMN_NAME_SPENT = "`spent`";
        public static final String COLUMN_NAME_FSPENT = "`fspent`";
        public static final String COLUMN_NAME_DICTIONARY = "`dictionary`";
    }
}