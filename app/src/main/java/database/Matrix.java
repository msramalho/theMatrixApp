package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import database.MatrixReader.MatrixEntry;
import maps.bank_matrix.Cryptography;
import maps.bank_matrix.R;

public class Matrix {
    public long id;
    public String name;
    public String value;
    public int lines, columns;
    public static String KEY_NAME = "KeyNameAuthMatrixed";


    public Matrix() {
        this(0, "", "", 8, 8);
    }

    private Matrix(long id, String name, String value, int lines, int columns) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.lines = lines;
        this.columns = columns;
    }


    public void insertMatrix(SQLiteDatabase db) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(MatrixEntry.COLUMN_NAME, name);
        values.put(MatrixEntry.COLUMN_VALUE, value);
        values.put(MatrixEntry.COLUMN_LINES, lines);
        values.put(MatrixEntry.COLUMN_COLUMNS, columns);

        // Insert the new row, returning the primary key value of the new row
        id = db.insert(MatrixEntry.TABLE_NAME, null, values);
    }

    public static ArrayList<Matrix> getAll(SQLiteDatabase db) {
        String[] projection = {
                MatrixEntry._ID,
                MatrixEntry.COLUMN_NAME,
                MatrixEntry.COLUMN_VALUE,
                MatrixEntry.COLUMN_LINES,
                MatrixEntry.COLUMN_COLUMNS
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = MatrixEntry.COLUMN_NAME + " ASC";


        Cursor cursor = db.query(
                MatrixEntry.TABLE_NAME,                     // The table to query
                projection,                                 // The columns to return
                null,                                       // The columns for the WHERE clause
                null,                                       // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                sortOrder                                   // The sort order
        );


        ArrayList<Matrix> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Matrix m = new Matrix(
                    cursor.getLong(cursor.getColumnIndexOrThrow(MatrixEntry._ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_VALUE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_LINES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_COLUMNS))
            );
            result.add(m);
        }
        cursor.close();

        return result;
    }

    public static Matrix getMatrixById(SQLiteDatabase db, long idToLoad) {
        String[] projection = {
                MatrixEntry._ID,
                MatrixEntry.COLUMN_NAME,
                MatrixEntry.COLUMN_VALUE,
                MatrixEntry.COLUMN_LINES,
                MatrixEntry.COLUMN_COLUMNS
        };

        // Filter results WHERE "id" = '1234'
        String selection = MatrixEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(idToLoad)};

        Cursor cursor = db.query(
                MatrixEntry.TABLE_NAME,                     // The table to query
                projection,                                 // The columns to return
                selection,                                  // The columns for the WHERE clause
                selectionArgs,                              // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                null                                        // The sort order
        );
        if (cursor.moveToNext()) {
            return new Matrix(
                    cursor.getLong(cursor.getColumnIndexOrThrow(MatrixEntry._ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_VALUE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_LINES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_COLUMNS))
            );
        }
        cursor.close();
        return null;
    }

    public void deleteMatrix(SQLiteDatabase db) {
        // Define 'where' part of query.
        String selection = MatrixEntry._ID + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(id)};
        // Issue SQL statement.
        db.delete(MatrixEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void updateMatrix(SQLiteDatabase db) {
        // New value for one column
        ContentValues values = new ContentValues();
        values.put(MatrixEntry.COLUMN_NAME, name);
        values.put(MatrixEntry.COLUMN_VALUE, value);
        values.put(MatrixEntry.COLUMN_LINES, lines);
        values.put(MatrixEntry.COLUMN_COLUMNS, columns);

        // Which row to update, based on the title
        String selection = MatrixEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.update(
                MatrixEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public String getDimensionString() {
        return lines + " x " + columns;
    }

    public String[][] getMatrix() throws NoSuchPaddingException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        Cryptography cryptography = new Cryptography(KEY_NAME);
        String[][] result = new String[lines][columns];
        String myMatrix = cryptography.decrypt(value);
        if (myMatrix.length() != lines * columns * 3)
            return result;
        int k = 0;
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = myMatrix.substring(k, k + 3);
                k += 3;
            }
        }
        return result;
    }

    public String getName(Context context) {
        return name.length() == 0 ? context.getString(R.string.matrix_unnamed) : name;
    }


    //Encryption functions

    public boolean validMatrix() {
        return value.length() > 0;
    }

    public int getTotalChars() {
        return 3 * lines * columns;
    }
}
