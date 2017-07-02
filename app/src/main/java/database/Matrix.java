package database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import database.MatrixReader.MatrixEntry;

public class Matrix {
    public long id;
    public String name;
    public String value;
    public int lines, columns;

    public Matrix() {
        this(0,"","",8,8);
    }

    public Matrix(long id, String name, String value, int lines, int columns) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.lines = lines;
        this.columns = columns;
    }


    public void insertMatrix(SQLiteDatabase db){
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(MatrixEntry.COLUMN_NAME, name);
        values.put(MatrixEntry.COLUMN_VALUE, value);
        values.put(MatrixEntry.COLUMN_LINES, lines);
        values.put(MatrixEntry.COLUMN_COLUMNS, columns);

        // Insert the new row, returning the primary key value of the new row
        id = db.insert(MatrixEntry.TABLE_NAME, null, values);
    }

    public static ArrayList<Matrix> getAll(SQLiteDatabase db){
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
        while(cursor.moveToNext()) {
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

    public static Matrix getMatrixById(SQLiteDatabase db, long idToLoad){
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
        if(cursor.moveToNext()) {
            return new Matrix(
                    cursor.getLong(cursor.getColumnIndexOrThrow(MatrixEntry._ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_VALUE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_LINES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MatrixEntry.COLUMN_COLUMNS))
            );
        }
        return null;
    }

    public void deleteMatrix(SQLiteDatabase db){
        // Define 'where' part of query.
        String selection = MatrixEntry._ID + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(id)};
        // Issue SQL statement.
        db.delete(MatrixEntry.TABLE_NAME, selection, selectionArgs);
    }

    public int updateMatrix(SQLiteDatabase db){
        // New value for one column
        ContentValues values = new ContentValues();
        values.put(MatrixEntry.COLUMN_NAME, name);
        values.put(MatrixEntry.COLUMN_VALUE, value);
        values.put(MatrixEntry.COLUMN_LINES, lines);
        values.put(MatrixEntry.COLUMN_COLUMNS, columns);

        // Which row to update, based on the title
        String selection = MatrixEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        int count = db.update(
                MatrixEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        return count;
    }

    public String getDimensionString() {
        return lines + " x " + columns;
    }

    public String[][] getMatrix(String passkey){
        String[][] result = new String[8][8];
        String myMatrix = decrypt(passkey, value);
        if(myMatrix.length() != 192)
            return result;
        int k = 0;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                result[i][j] = myMatrix.substring(k,k+3);
                k+=3;
            }
        }
        return result;
    }



    //Encryption functions

    public boolean validMatrix(){
        return value.length()>0;
    }

    public static String decrypt(String passkey, String matrixString){
        try {
            return decryptMsg(passkey, matrixString);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            System.out.println("ERROR DECRYPTING: " + e.getMessage());
        }
        return "";
    }

    public static String encrypt(String passkey, String matrixString){
        try {
            return encryptMsg(passkey, matrixString);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            System.out.println("ERROR ENCRYPTING: " + e.getMessage());
        }
        return "";
    }


    private static String encryptMsg(String passkey, String message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        //byte[] message64 = Base64.decode(message,Base64.DEFAULT);
        //SecretKeySpec secret = new SecretKeySpec(secret64, "AES");
        Cipher cipher  = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretFromKey(passkey));
        byte[] encVal = cipher.doFinal(message.getBytes());
        return Base64.encodeToString(encVal, Base64.DEFAULT);
    }

    private static String decryptMsg(String passkey, String cipherTextString) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        byte[] cipherText = Base64.decode(cipherTextString, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, getSecretFromKey(passkey));
        return new String(cipher.doFinal(cipherText), "UTF-8");
    }


    private static SecretKeySpec getSecretFromKey(String passkey) {
        byte[] key;
        MessageDigest sha;
        try {
            key = (passkey).getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
        } catch (UnsupportedEncodingException  | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        return new SecretKeySpec(key, "AES");
    }

    public int getTotalChars() {
        return 3 * lines * columns;
    }
}
