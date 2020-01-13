package maps.bank_matrix;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

class Preferences {
    private SharedPreferences prefs;
    private String KEY_PASS_PREFERENCE = "passwordInMyPreferencesMatrixed";
    Preferences(Context c) {
        String PREFS_NAME = "matrixPreferences";
        prefs = c.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.apply();
    }

    private String getString() {
        return prefs.getString(KEY_PASS_PREFERENCE, "");
    }

    //password
    boolean isPasswordDefined() {
        return getString().length() > 0;
    }

    boolean validatePassword(String userInput) {//attempts constant time, but compilers...
        Sha1 attemptHash = new Sha1(userInput);
        String attempt;
        try {
            attempt = attemptHash.hash();
        } catch (NoSuchAlgorithmException e) {
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

    void deletePassword() {
        prefs.edit().remove(KEY_PASS_PREFERENCE).apply();
    }
}
