package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import database.MatrixReader.MatrixEntry;
import maps.bank_matrix.R;

public class Matrix {
    public long id;
    public String name;
    public String value;
    public int lines, columns;
    public static String KEY_NAME = "KeyNameAuthMatrixed";
    public static int VALIDITY_DURATION = 3600;

    // initialization can be random due to low probability of collision in bank matrices
    // https://stackoverflow.com/questions/8041451/good-aes-initialization-vector-practice
    private static byte[] iv = {7, 2, 5, 42, 6, 2, 5, 6, 9, 3, 9, 1, 8, 9, 2, 7};
    private static IvParameterSpec ivspec = new IvParameterSpec(iv);

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

    public String[][] getMatrix() throws NoSuchPaddingException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        String[][] result = new String[lines][columns];
        String myMatrix = decryptString(value);
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

    public void decryptEncryptNewAuth(String oldPasskey) throws NoSuchPaddingException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        String temp = value;
        temp = Matrix.decrypt(oldPasskey, temp);
        temp = Matrix.encryptString(temp);
        value = temp;
    }


    public boolean validMatrix() {
        return value.length() > 0;
    }

    private static String decrypt(String passkey, String matrixString) {
        try {
            return decryptMsg(passkey, matrixString);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            e.printStackTrace();
            System.out.println("ERROR DECRYPTING: " + e.getMessage());
        }
        return "";
    }


    private static String decryptMsg(String passkey, String cipherTextString) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] cipherText = Base64.decode(cipherTextString, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, getSecretFromKey(passkey));
        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }


    private static SecretKeySpec getSecretFromKey(String passkey) {
        byte[] key;
        MessageDigest sha;
        try {
            key = (passkey).getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
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

    public static void generateSecretKey(KeyGenParameterSpec keyGenParameterSpec) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator;
        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    private static SecretKey getSecretKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        KeyStore keyStore;
        keyStore = KeyStore.getInstance("AndroidKeyStore");

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null);
        return ((SecretKey) keyStore.getKey(KEY_NAME, null));
    }

    private static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }


    public static String encryptString(String toEncrypt) throws NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        Cipher cipher = Matrix.getCipher();
        SecretKey secretKey = Matrix.getSecretKey();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, Matrix.ivspec);
            return Arrays.toString(cipher.doFinal(toEncrypt.getBytes(Charset.defaultCharset())));
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            Log.e("matrix", "Key is invalid in encryption." + e.getMessage());
            System.exit(1);
        }
        return "";
    }

    private String decryptString(String toDecrypt) throws NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        // Exceptions are unhandled for getCipher() and getSecretKey().
        Cipher cipher = Matrix.getCipher();
        SecretKey secretKey = Matrix.getSecretKey();

        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, Matrix.ivspec);
            return Arrays.toString(cipher.doFinal(toDecrypt.getBytes(Charset.defaultCharset())));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
            Log.e("matrix", "Key is invalid: " + e.getMessage());
            System.exit(1);
        }
        return "";
    }

}
