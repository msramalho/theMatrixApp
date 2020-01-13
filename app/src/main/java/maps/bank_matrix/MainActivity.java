package maps.bank_matrix;

import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import database.Matrix;
import database.MatrixDatabase;

public class MainActivity extends AppCompatActivity {
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private SQLiteDatabase db;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        executor = ContextCompat.getMainExecutor(this);
        MatrixDatabase mDbHelper = new MatrixDatabase(this);
        db = mDbHelper.getReadableDatabase();

        if (Matrix.getAll(db).size() > 0) {
            // has data, need to check for old method of login
            preferences = new Preferences(this);
            if (preferences.isPasswordDefined()) {
                // user is still using old authentication method
                promptLegacyPassword();
            }
        }

        biometricPrompt = new BiometricPrompt(MainActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                        advanceToMatrix();
                    }

                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Matrix Biometric login")
                .setSubtitle("Log in using your biometric login or default phone credentials")
                .setDeviceCredentialAllowed(true)
                .build();

        // Login either with app open or click shield
        ImageView fingerprintIcon = findViewById(R.id.fingerprintIcon);
        fingerprintIcon.setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
        biometricPrompt.authenticate(promptInfo);
    }

    public void advanceToMatrix() {
        Intent intentMatrix = new Intent(this, MatrixList.class);
        startActivity(intentMatrix);
    }


    private void promptLegacyPassword() {
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("Enter your password so we can update your authentication mechanism")
                .setTitle("Update Authentication")
                .setNegativeButton("Abort", null)
                .setPositiveButton("Go", (dialog, which) -> {
                    Dialog dialogObj = (Dialog) dialog;
                    EditText oldEt = dialogObj.findViewById(R.id.oldP);

                    Preferences p = new Preferences(this);
                    String insertedPass = oldEt.getText().toString();
                    if (p.validatePassword(insertedPass)) {
                        getNewAuthAfterLegacy(insertedPass);
                    } else {
                        promptLegacyPassword();
                    }
                })
                .setView(inflater.inflate(R.layout.read_legacy_password, null));
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void getNewAuthAfterLegacy(String oldpass) {
        Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
        ArrayList<Matrix> matrices = Matrix.getAll(db);
        for (Matrix matrix : matrices) {
            try {
                matrix.decryptEncryptNewAuth(oldpass);//re-encrypt the matrix
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | NoSuchProviderException | InvalidAlgorithmParameterException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                System.exit(1);
            }
        }

        for (Matrix matrix : matrices)
            matrix.updateMatrix(db);//update it in the database

        preferences.deletePassword();
        Toast.makeText(getApplicationContext(), "Success in re-encrypting your data. Please restart.", Toast.LENGTH_SHORT).show();
    }
}

