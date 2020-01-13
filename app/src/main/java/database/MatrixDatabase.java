package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import database.MatrixReader.MatrixEntry;

public class MatrixDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MatrixDB.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MatrixEntry.TABLE_NAME + " (" +
                    MatrixEntry._ID + " INTEGER PRIMARY KEY autoincrement," +
                    MatrixEntry.COLUMN_NAME + " TEXT," +
                    MatrixEntry.COLUMN_VALUE + " TEXT," +
                    MatrixEntry.COLUMN_LINES + " INT," +
                    MatrixEntry.COLUMN_COLUMNS + " INT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MatrixEntry.TABLE_NAME;

    public MatrixDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

}
