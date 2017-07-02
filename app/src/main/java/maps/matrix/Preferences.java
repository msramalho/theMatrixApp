package maps.matrix;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class Preferences {
    private final String PREFS_NAME = "matrixPreferences";
    private final String PASSWORD_NAME = "passwordInMyPreferencesMatrixed";
    //private final String MATRIX_NAME = "matrixInMyPreferencesMatrixed";//

    private Context myContext;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public Preferences(Context c) {
        myContext = c;
        prefs = myContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.commit();
    }

    private String getString(String key){
        return prefs.getString(key, "");
    }

//password
    public boolean isPasswordDefined(){
        return getString(PASSWORD_NAME).length()>0;
    }

    public boolean changePassword(String oldP, String newP){
        if(newP.length() > 0 && validatePassword(oldP)){
            Sha1 sha1 = new Sha1(newP);
            try {
                editor.putString(PASSWORD_NAME, sha1.hash());
                editor.commit();
                return true;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean validatePassword(String userInput) {//attempts constant time, but compilers...
        Sha1 attemptHash = new Sha1(userInput);
        String attempt = "";
        try {
            attempt = attemptHash.hash();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        String real = getString(PASSWORD_NAME);

        if (real == "")//never set before
            return true;

        if (attempt.length() != real.length()){
            System.out.println("Different length: " + attempt + " - " +  real);
            return false;
        }

        int delta = 0;
        for (int i = 0;i < attempt.length(); i++){
            delta += attempt.charAt(i) ^ real.charAt(i);
        }

        return delta == 0;
    }

//matrix
    /*public String getMatrix(){
        return getString(MATRIX_NAME);
    }
    public void updateMatrix(String newMatrix){
        editor.putString(MATRIX_NAME, newMatrix);
        editor.commit();
    }*/
}
