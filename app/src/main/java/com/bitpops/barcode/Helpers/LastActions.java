package com.bitpops.barcode.Helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.bitpops.barcode.Model.Transfer;

import java.util.ArrayList;

import static com.bitpops.barcode.Helpers.PosPrinter.ct;

public class LastActions implements BaseColumns {

    public static String TABLE_NAME = "LastAction";
    public static final String _ID = "id";
    public static final String COLUMN_TRANSFER_NO = "transfer_no";
    public static final String COLUMN_FROM = "from";
    public static final String COLUMN_TO = "to";
    public static final String COLUMN_DATE = "date";

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TRANSFER_NO + " INTEGER, " +
            COLUMN_FROM + " TEXT, " +
            COLUMN_TO + " TEXT, " +
            COLUMN_DATE + " INTEGER" + ")";

    private void addNewActionToDB(Context ct) {
        SQLiteDatabase db = new DBHelper(ct).getWritableDatabase();
        try
        {
            db.execSQL("INSERT INTO "+ TABLE_NAME +" VALUES (null, ?)",
                    new Object[] {  "value" });
        }
        catch (Exception e)
        {
            Log.e("ERROR", e.toString());
        }

       // int last_insert_id = db.rawQuery("SELECT last_insert_rowid();");

        //Toast.makeText(this, "The new Row Id is " + newRowId, Toast.LENGTH_LONG).show();
    }

    public ArrayList<Transfer> getOrderDetails(String company, String orderNumber) throws SQLException {


        ArrayList<Transfer> transferList = new ArrayList<Transfer>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db  = new DBHelper(ct).getReadableDatabase();
        Cursor mCursor = db.rawQuery(selectQuery, null);


        if (mCursor.moveToFirst()) {
            do {
                Transfer tr = new Transfer(mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_FROM)),
                                           mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_TO)),
                                         mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_TO)),
                                         mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_TO)));
                transferList.add(tr);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        return transferList;


    }
}
