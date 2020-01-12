package maps.bank_matrix;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import database.Matrix;
import database.MatrixDatabase;

class Preferences {
    private Context myContext;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    Preferences(Context c) {
        myContext = c;
        String PREFS_NAME = "matrixPreferences";
        prefs = myContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.apply();
    }

    private String getString() {
        return prefs.getString("passwordInMyPreferencesMatrixed", "");
    }

    //password
    boolean isPasswordDefined() {
        return getString().length() > 0;
    }

    boolean changePassword(String oldP, String newP) {
        if (newP.length() > 0 && validatePassword(oldP)) {
            Sha1 sha1 = new Sha1(newP);
            try {
                //first update all the matrices to be encrypted using the new password
                MatrixDatabase mDbHelper = new MatrixDatabase(myContext);
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                ArrayList<Matrix> matrices = Matrix.getAll(db);

                for (Matrix m : matrices) {
                    m.decryptEncrypt(oldP, newP);//re-encrypt the matrix
                    m.updateMatrix(db);//update it in the database
                }
                //then save the new password
                String PASSWORD_NAME = "passwordInMyPreferencesMatrixed";
                editor.putString(PASSWORD_NAME, sha1.hash());
                editor.commit();
                return true;
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    boolean validatePassword(String userInput) {//attempts constant time, but compilers...
        Sha1 attemptHash = new Sha1(userInput);
        String attempt;
        try {
            attempt = attemptHash.hash();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        String real = getString();

        if (real.equals(""))//never set before
            return true;

        if (attempt.length() != real.length())
            return false;

        int delta = 0;
        for (int i = 0; i < attempt.length(); i++) {
            delta += attempt.charAt(i) ^ real.charAt(i);
        }

        return delta == 0;
    }

}
