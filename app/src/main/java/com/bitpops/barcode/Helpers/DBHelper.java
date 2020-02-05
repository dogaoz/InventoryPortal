package com.bitpops.barcode.Helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bitpops.barcode.Helpers.LastActions;

public class DBHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "sample_database";

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(LastActions.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LastActions.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }

}
